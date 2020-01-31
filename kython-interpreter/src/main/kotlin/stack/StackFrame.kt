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

package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.pyobject.PyObject
import java.util.*

/**
 * Base class for a stack frame. This can either be an abstract class stack frame, or a
 */
abstract class StackFrame {
    companion object {
        /**
         * Flattens the list of stack frames down.
         *
         * @param root: The root stack frame to flatten all the children to.
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
     * The enum of states a running frame can be in.
     */
    enum class FrameState(val isYielding: Boolean) {
        /**
         * This frame is paused, and is not executing.
         *
         * Used when a frame is first created.
         */
        PAUSED(false),

        /**
         * This frame is running an instruction.
         */
        RUNNING(false),

        /**
         * This frame is not running, and has yielded a value.
         */
        YIELDING(true),

        /**
         * This frame is not running, and has yield from'd a value.
         */
        YIELD_FROM(true),

        /**
         * This frame has returned a value, and cannot be re-used.
         */
        RETURNED(false),

        /**
         * This frame has had an uncaught error.
         */
        ERRORED(false)
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
     * The [FrameState] this stack frame is in.
     */
    var state = FrameState.PAUSED

    /**
     * Runs this stack frame, invoking the function underneath.
     */
    abstract fun runFrame(kwargs: Map<String, PyObject>): PyObject

    /**
     * Gets the stack frame information for this stack frame.
     */
    abstract fun createStackFrameInfo(): StackFrameInfo
}
