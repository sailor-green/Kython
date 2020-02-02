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
 */

@file:Suppress("SpellCheckingInspection", "unused", "UNUSED_PARAMETER")

package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.instruction.PythonInstruction
import green.sailor.kython.interpreter.instruction.impl.*
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyRootObjectInstance
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.pyobject.generator.PyGenerator
import green.sailor.kython.interpreter.pyobject.internal.PyCellObject
import green.sailor.kython.interpreter.util.PythonFunctionStack
import java.util.*

/**
 * Represents a single stack frame on the stack of stack frames.
 */
@Suppress("MemberVisibilityCanBePrivate")
class UserCodeStackFrame(val function: PyUserFunction) : StackFrame() {
    /**
     * The bytecode pointer to the bytecode of the KyFunction.
     *
     * This points to the actual instruction index, not the raw code index.
     */
    var bytecodePointer: Int = 0

    // debug mode helper
    // this is used in case an instruction is written without emitting a bytecode change
    @Suppress("PropertyName")
    var _lastBytecodePointer: Int = -1

    /**
     * The inner stack for this stack frame.
     */
    val stack = PythonFunctionStack(function.code.stackSize)

    /**
     * The block stack for this stack frame.
     */
    val blockStack = ArrayDeque<Block>()

    /**
     * The local variables for this frame.
     */
    val locals = linkedMapOf<String, PyObject>()

    /**
     * The cell variables for this frame.
     */
    val cellvars = mutableMapOf<String, PyCellObject>()

    /**
     * Gets the source code line number currently being executed.
     */
    val lineNo: Int get() = function.code.getLineNumber(bytecodePointer)

    /**
     * Creates a new [StackFrameInfo] for this stack frame.
     */
    override fun createStackFrameInfo(): StackFrameInfo.UserFrameInfo =
        StackFrameInfo.UserFrameInfo(this)

    /**
     * Unpacks the local variables into varnames for this function.
     */
    fun setupLocals(locals: Map<String, PyObject>) {
        this.locals.putAll(locals)
        // make new cells
        for (cell in this.function.code.cellvars) {
            this.cellvars[cell] = PyCellObject(this.locals, cell)
        }
    }

    /**
     * Runs this stack frame, executing the function within.
     *
     * This should only be used for regular functions!
     */
    override fun runFrame(kwargs: Map<String, PyObject>): PyObject {
        if (function.code.flags.isGenerator) {
            error("Cannot call runFrame on a generator function!")
        }

        setupLocals(kwargs)

        val result = evaluateBytecode()
        if (state !== FrameState.RETURNED) {
            error("Frame ran successfully, but is not in a RETURNED state!")
        }
        return result
    }

    // === GENERATOR API === //

    /**
     * Sends a value to this frame using the generator API.
     *
     * This does NOT perform safety checks; use the generator wrapper for that!
     */
    fun send(value: PyObject): Pair<FrameState, PyObject> {
        if (KythonInterpreter.config.debugMode) {
            System.err.println("=== Sending $value for ${function.code.codename} ===")
        }
        // initially, a generator is in PAUSED
        // and you can only send None
        if (state === FrameState.PAUSED) {
            if (value !== PyNone) {
                typeError("can't send non-None value to a just-started generator")
            }
        }
        if (state.isYielding) {
            stack.push(value)
        }

        KythonInterpreter.pushFrame(this)
        val result = evaluateBytecode()
        KythonInterpreter.popFrame()
        return state to result
    }

    /**
     * UNARY_*
     */
    fun unaryOp(type: UnaryOp, param: Byte) {
        val top = stack.pop()
        when (type) {
            UnaryOp.INVERT -> stack.push(top.pyInvert())
            UnaryOp.NOT -> stack.push(!top.pyToBool())
            UnaryOp.NEGATIVE -> stack.push(top.pyNegative())
            UnaryOp.POSITIVE -> stack.push(top.pyPositive())
        }
        bytecodePointer += 1
    }
}
