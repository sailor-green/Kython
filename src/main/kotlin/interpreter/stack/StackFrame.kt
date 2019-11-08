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

import green.sailor.kython.interpreter.objects.python.PyDict
import green.sailor.kython.interpreter.objects.python.PyTuple

/**
 * Base class for a stack frame. This can either be an abstract class stack frame, or a
 */
abstract class StackFrame {
    companion object {
        /**
         * Flattens the list of stack frames down.
         */
        fun flatten(root: StackFrame): List<StackFrame> {
            val frames = mutableListOf(root)
            while (true) {
                val child = frames.last().childFrame ?: break
                frames.add(child)
            }

            return frames
        }
    }

    /**
     * The child frame for this stack frame, if any.
     */
    var childFrame: StackFrame? = null

    /**
     * The parent frame for this stack frame, if any.
     */
    var parentFrame: StackFrame? = null

    /**
     * Runs this stack frame, invoking the function underneath.
     */
    abstract fun runFrame(args: PyTuple, kwargs: PyDict): InterpreterResult

    /**
     * Gets the stack frame information for this stack frame.
     */
    abstract fun getStackFrameInfo(): StackFrameInfo
}
