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

import arrow.core.Either
import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.objects.KyFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.*
import java.util.*

/**
 * Represents a single stack frame on the stack of stack frames.
 *
 * @param function: The function being ran. This may not be a *real* function, but we treat it as if it is.
 */
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

    /** The varname storage. */
    val realVarnames = arrayOfNulls<PyObject>(this.function.code.varnames.size)
    /** The name storage. */
    val realNames = arrayOfNulls<PyObject>(this.function.code.names.size)

    override fun getStackFrameInfo(): StackFrameInfo.UserFrameInfo {
        return StackFrameInfo.UserFrameInfo(this)
    }

    /**
     * Runs this stack frame, executing the function within.
     */
    override fun runFrame(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject> {
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
                return Either.Left(opcodeResult.orNull()!!)
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
            LoadPool.CONST -> Either.Right(this.function.code.consts[idx])
            LoadPool.FAST -> Either.Right(this.realVarnames[idx]!!)
            LoadPool.NAME -> {
                // sometimes a global...
                val realName = this.realNames[idx]
                val result = if (realName == null) {
                    val name = this.function.code.names[idx]
                    val global = this.function.getGlobal(name)

                    if (global.isRight()) {
                        this.realNames[idx] = (global as Either.Right).b
                    }

                    global
                } else {
                    Either.Right(realName)
                }
                result
            }
            else -> error("Unknown pool for LOAD_X instruction: $pool")  // interpreter error, not python error
        }

        return when (loadResult) {
            is Either.Left -> {
                Some(loadResult.a)
            }
            is Either.Right -> {
                val toPush = loadResult.b
                this.stack.push(toPush)
                this.bytecodePointer += 1
                none()
            }
        }

    }

    /**
     * STORE_(NAME|FAST).
     */
    fun store(pool: LoadPool, arg: Byte): Option<PyException> {
        val idx = arg.toInt()
        val toStoreIn = when (pool) {
            LoadPool.NAME -> this.realNames
            LoadPool.FAST -> this.realVarnames
            else -> error("Can't store items in pool $pool")
        }
        toStoreIn[idx] = this.stack.pop()
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

        val posArgs = PyTuple(toCallWith.reversed())
        val fn = this.stack.pop()
        if (fn !is PyCallable) {
            error("CALL_FUNCTION called on non-callable $fn!")
        }

        val childFrame = fn.getFrame(this)
        this.childFrame = childFrame
        val result = childFrame.runFrame(posArgs, PyDict.EMPTY)
        // errors should be passed down, and results should be put onto the stack
        if (result is Either.Left) {
            return Some(result.a)
        } else if (result is Either.Right) {
            // this cast must always succeed, because runFrame should never return anything other than these two
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
