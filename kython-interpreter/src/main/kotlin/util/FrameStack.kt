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

package green.sailor.kython.interpreter.util

import green.sailor.kython.interpreter.stack.StackFrame
import java.util.*
import kotlin.collections.ArrayList

/**
 * Represents the frame stack for a Kython interpreter thread.
 *
 * This is just a subclass of an [ArrayList] with helper functions.
 */
class FrameStack : ArrayList<StackFrame>(10) {
    /**
     * Pushes a frame onto the stack.
     */
    fun push(frame: StackFrame) = add(frame)

    /**
     * Pops a frame from the stack.
     */
    fun pop() = removeAt(size - 1)

    /**
     * Copies this frame stack, usually for usage in exceptions.
     */
    fun copy(): List<StackFrame> = toMutableList()

    /**
     * Gets the current frame.
     */
    val current: StackFrame get() = get(size - 1)

    /**
     * Gets the caller frame for the current frame.
     */
    val caller: StackFrame? get() = getOrNull(size - 2)
}
