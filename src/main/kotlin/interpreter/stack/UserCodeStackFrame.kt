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

import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.objects.KyFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.PyDict
import green.sailor.kython.interpreter.objects.python.PyInt
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.PyTuple
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
    override fun runFrame(args: PyTuple, kwargs: PyDict): InterpreterResult {
        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = this.function.getInstruction(this.bytecodePointer)
            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument

            // switch on opcode
            val opcodeResult: InterpreterResult = when (nextInstruction.opcode) {
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
                InstructionOpcode.RETURN_VALUE -> this.returnValue(param)

                InstructionOpcode.POP_TOP -> this.popTop(param)

                else -> error("Unimplemented opcode $opcode")
            }

            if (opcodeResult != InterpreterResultNoAction) {
                return opcodeResult
            }
        }
    }


    // scary instruction implementations
    // this is all below the main class because there's a LOT going on here

    fun returnValue(arg: Byte): InterpreterResult {
        val tos = stack.pop()
        return InterpreterResultReturn(tos)
    }

    /**
     * LOAD_(NAME|FAST).
     */
    fun load(pool: LoadPool, opval: Byte): InterpreterResult {
        // pool is the type we want to load
        val idx = opval.toInt()
        val toPush = when (pool) {
            LoadPool.CONST -> this.function.code.consts[idx]
            LoadPool.FAST -> this.realVarnames[idx]
            LoadPool.NAME -> {
                // sometimes a global...
                val realName = this.realNames[idx]
                val result = if (realName == null) {
                    val name = this.function.code.names[idx]
                    val global = this.function.getGlobal(name)
                    this.realNames[idx] = global
                    global
                } else {
                    realName
                }
                result
            }
            else -> error("Unknown pool for LOAD_X instruction: $pool")
        }

        this.stack.push(toPush)
        this.bytecodePointer += 1

        return InterpreterResultNoAction
    }

    /**
     * STORE_(NAME|FAST).
     */
    fun store(pool: LoadPool, arg: Byte): InterpreterResult {
        val idx = arg.toInt()
        val toStoreIn = when (pool) {
            LoadPool.NAME -> this.realNames
            LoadPool.FAST -> this.realVarnames
            else -> error("Can't store items in pool $pool")
        }
        toStoreIn[idx] = this.stack.pop()
        this.bytecodePointer += 1
        return InterpreterResultNoAction
    }

    /**
     * CALL_FUNCTION.
     */
    fun callFunction(opval: Byte): InterpreterResult {
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
            error("CALL_FUNCTION called on a non-callable!")
        }

        val childFrame = fn.getFrame(this)
        this.childFrame = childFrame
        val result = childFrame.runFrame(posArgs, PyDict.EMPTY)
        // errors should be passed down, and results should be put onto the stack
        if (result is InterpreterResultError) {
            return result
        } else {
            // this cast must always succeed, because runFrame should never return anything other than these two
            val unwrapped = (result as InterpreterResultReturn).result
            this.stack.push(unwrapped)
            this.childFrame = null
            // not needed, but just to speed up GC
            childFrame.parentFrame = null
        }


        this.bytecodePointer += 1
        return InterpreterResultNoAction
    }

    /**
     * POP_TOP.
     */
    fun popTop(arg: Byte): InterpreterResult {
        assert(arg.toInt() == 0) { "POP_TOP never has an argument" }
        this.stack.pop()
        this.bytecodePointer += 1
        return InterpreterResultNoAction
    }

    /**
     * BINARY_* (ADD, etc)
     */
    fun binaryOp(type: BinaryOp, arg: Byte): InterpreterResult {
        val result = when (type) {
            BinaryOp.ADD -> {
                // todo: __add__
                stack.push(PyInt((stack.pop() as PyInt).wrappedInt + (stack.pop() as PyInt).wrappedInt))
                InterpreterResultNoAction
            }
            else -> error("Unsupported binary op $type")
        }
        this.bytecodePointer += 1
        return result
    }
}
