/*
 * This file is part of kython.
 *
 * kython is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kython is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with kython.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package green.sailor.kython.interpreter.stack

import arrow.core.*
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.objects.Exceptions
import green.sailor.kython.interpreter.objects.KyFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.primitives.PyInt
import java.util.*

/**
 * Represents a single stack frame on the stack of stack frames.
 *
 * @param function: The function being ran. This may not be a *real* function, but we treat it as if it is.
 */
@Suppress("MemberVisibilityCanBePrivate")
class UserCodeStackFrame(
    val function: KyFunction
) : StackFrame() {
    companion object {
        /** Load pools for LOAD/STORE instructions. These represent where the instruction will operate on. */
        enum class LoadPool {
            CONST,
            FAST,
            NAME,
            ATTR,
        }

        enum class BinaryOp {
            ADD,
        }
    }

    /**
     * The bytecode pointer to the bytecode of the KyFunction.
     *
     * This points to the actual instruction index, not the raw code index.
     */
    var bytecodePointer: Int = 0

    /**
     * The inner stack for this stack frame.
     */
    val stack = ArrayDeque<PyObject>(this.function.code.stackSize)

    /** The local variables for this frame. */
    val locals = mutableMapOf<String, PyObject>()

    override fun getStackFrameInfo(): StackFrameInfo.UserFrameInfo {
        return StackFrameInfo.UserFrameInfo(this)
    }

    /**
     * Gets the source code line number currently being executed.
     */
    fun getLineNo(): Int {
        return this.function.code.getLineNumber(this.bytecodePointer)
    }

    /**
     * Runs this stack frame, executing the function within.
     */
    override fun runFrame(kwargs: Map<String, PyObject>): Either<PyException, PyObject> {
        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = this.function.getInstruction(this.bytecodePointer)
            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument

            // special case this, because it returns from runFrame
            if (nextInstruction.opcode == InstructionOpcode.RETURN_VALUE) {
                val result = this.returnValue(param)
                return Either.Right(result)
            }

            // switch on opcode
            val opcodeResult = when (nextInstruction.opcode) {
                // load ops
                InstructionOpcode.LOAD_FAST -> this.load(LoadPool.FAST, param)
                InstructionOpcode.LOAD_NAME -> this.load(LoadPool.NAME, param)
                InstructionOpcode.LOAD_CONST -> this.load(LoadPool.CONST, param)

                // store ops
                InstructionOpcode.STORE_NAME -> this.store(LoadPool.NAME, param)
                InstructionOpcode.STORE_FAST -> this.store(LoadPool.FAST, param)

                // binary ops
                InstructionOpcode.BINARY_ADD -> this.binaryOp(BinaryOp.ADD, param)

                InstructionOpcode.CALL_FUNCTION -> this.callFunction(param)

                InstructionOpcode.POP_TOP -> this.popTop(param)

                else -> error("Unimplemented opcode $opcode")
            }

            // TODO: Try handling
            if (opcodeResult.isDefined()) {
                // this will never be null, since we call isDefined.
                // we tag ourselves onto the traceback, too.
                val exc = opcodeResult.orNull()!!
                return Either.Left(exc)
            }
        }
    }


    // scary instruction implementations
    // this is all below the main class because there's a LOT going on here

    // i don't see how this can ever error...
    fun returnValue(arg: Byte): PyObject {
        return stack.pop()
    }

    /**
     * LOAD_(NAME|FAST).
     */
    fun load(pool: LoadPool, opval: Byte): Option<PyException> {
        // pool is the type we want to load
        val idx = opval.toInt()
        val loadResult = when (pool) {
            LoadPool.CONST -> Either.right(this.function.code.consts[idx])
            LoadPool.FAST -> {
                val name = this.function.code.varnames[idx]
                Either.right(this.locals[name]!!)
            }
            LoadPool.NAME -> {
                // sometimes a global...
                val name = this.function.code.names[idx]
                val realName = this.locals[name]
                val result = if (realName == null) {
                    val name = this.function.code.names[idx]
                    this.function.getGlobal(name)
                } else {
                    Either.Right(realName)
                }
                result
            }
            else -> error("Unknown pool for LOAD_X instruction: $pool")  // interpreter error, not python error
        }

        val option: Option<PyException> = loadResult.fold(
            { Some(it) },
            {
                this.stack.push(it)
                this.bytecodePointer += 1
                none()
            }
        )
        return option
    }

    /**
     * STORE_(NAME|FAST).
     */
    fun store(pool: LoadPool, arg: Byte): Option<PyException> {
        val idx = arg.toInt()
        val toGetName = when (pool) {
            LoadPool.NAME -> this.function.code.names
            LoadPool.FAST -> this.function.code.varnames
            else -> error("Can't store items in pool $pool")
        }
        val name = toGetName[idx]
        this.locals[name] = this.stack.pop()
        this.bytecodePointer += 1
        return none()
    }

    /**
     * CALL_FUNCTION.
     */
    fun callFunction(opval: Byte): Option<PyException> {
        // CALL_FUNCTION(argc)
        // pops (argc) arguments off the stack (right to left) then invokes a function.
        val args = opval.toInt()
        val toCallWith = mutableListOf<PyObject>()
        for (x in 0 until args) {
            toCallWith.add(this.stack.pop())
        }

        val fn = this.stack.pop()
        if (fn !is PyCallable) {
            return Some(Exceptions.TYPE_ERROR.makeWithMessage("'${fn.type.name}' is not callable"))
        }

        val childFrame = fn.getFrame(this)
        this.childFrame = childFrame
        val sig = fn.signature

        val argsToPass = sig.getFinalArgs(toCallWith)
        val result = argsToPass.flatMap { KythonInterpreter.runStackFrame(childFrame, it) }

        // errors should be passed down, and results should be put onto the stack
        if (result is Either.Left) {
            return Some(result.a)
        } else if (result is Either.Right) {
            val unwrapped = result.b
            this.stack.push(unwrapped)
            this.childFrame = null
            // not needed, but just to speed up GC
            childFrame.parentFrame = null
        }


        this.bytecodePointer += 1
        return none()
    }

    /**
     * POP_TOP.
     */
    fun popTop(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "POP_TOP never has an argument" }
        this.stack.pop()
        this.bytecodePointer += 1
        return none()
    }

    /**
     * BINARY_* (ADD, etc)
     */
    fun binaryOp(type: BinaryOp, arg: Byte): Option<PyException> {
        val result: Option<PyException> = when (type) {
            BinaryOp.ADD -> {
                // todo: __add__
                stack.push(PyInt((stack.pop() as PyInt).wrappedInt + (stack.pop() as PyInt).wrappedInt))
                none()
            }
            else -> error("Unsupported binary op $type")
        }
        this.bytecodePointer += 1
        return result
    }
}
