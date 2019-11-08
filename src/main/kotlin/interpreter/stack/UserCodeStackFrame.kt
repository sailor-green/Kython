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
        // no need for an enum
        const val LT_CONST = 0
        const val LT_FAST = 1
        const val LT_NAME = 2
        const val LT_ATTR = 3
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
    override fun runFrame(args: PyTuple, kwargs: PyDict) {
        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = this.function.getInstruction(this.bytecodePointer)
            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument

            // switch on opcode
            when (nextInstruction.opcode) {
                // easy ones
                InstructionOpcode.LOAD_FAST -> this.load(LT_FAST, param)
                InstructionOpcode.LOAD_NAME -> this.load(LT_NAME, param)
                InstructionOpcode.LOAD_CONST -> this.load(LT_CONST, param)

                InstructionOpcode.CALL_FUNCTION -> this.callFunction(param)

                else -> error("Unimplemented opcode $opcode")
            }
        }
    }


    // scary instruction implementations
    // this is all below the main class because there's a LOT going on here

    /**
     * LOAD_(NAME|FAST).
     */
    fun load(pool: Int, opval: Byte) {
        // pool is the type we want to load
        val idx = opval.toInt()
        val toPush = when (pool) {
            LT_CONST -> this.function.code.consts[idx]
            LT_FAST -> this.realVarnames[idx]
            LT_NAME -> {
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
    }

    /**
     * CALL_FUNCTION.
     */
    fun callFunction(opval: Byte) {
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
        val childFrame = fn.getFrame()
        // TODO: Results
        this.childFrame = childFrame
        childFrame.runFrame(posArgs, PyDict.EMPTY)
        this.bytecodePointer += 1
    }
}
