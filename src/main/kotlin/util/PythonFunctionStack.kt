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
package green.sailor.kython.util

import green.sailor.kython.interpreter.pyobject.PyObject
import java.util.*

/**
 * Represents the stack for a Python function.
 *
 * This is used over a regular [ArrayDeque] because we need indexing (for Python). It only has the
 * things needed for the implementation.
 */
class PythonFunctionStack(val maxSize: Int) : Collection<PyObject> {
    /** The backing array for this stack. */
    private val backingArray = arrayOfNulls<PyObject>(maxSize)
    /** The internal watermark for this stack. */
    private var watermark = 0

    /** The size of this stack. */
    override val size get() = watermark

    /** The top-most item on the stack. */
    val first get() = backingArray[watermark - 1] ?: error("Stack is empty")

    /** The bottom-most item on the stack. */
    val last get() = backingArray[0] ?: error("Stack is empty")

    /**
     * Pushes an object onto the stack.
     */
    fun push(obb: PyObject) {
        backingArray[watermark] = obb
        watermark += 1
    }

    /**
     * Pops an object from the stack.
     */
    fun pop(): PyObject {
        watermark -= 1
        if (watermark < 0) error("Stack is empty")

        val item = backingArray[watermark] ?: error("Stack is empty")
        // deref object so it gets GC'd earlier
        backingArray[watermark] = null
        return item
    }

    /**
     * Gets an item from the stack.
     */
    fun get(idx: Int): PyObject = backingArray[idx] ?: error("Stack doesn't have an item at $idx")

    // collection items
    override fun contains(element: PyObject): Boolean = backingArray.contains(element)
    // o(n^2) lol
    override fun containsAll(elements: Collection<PyObject>): Boolean =
        elements.all { backingArray.contains(it) }
    override fun isEmpty(): Boolean = watermark == 0
    override fun iterator(): Iterator<PyObject> = object : Iterator<PyObject> {
        var position = 0
        override fun hasNext(): Boolean =
            position < backingArray.size && backingArray[position] != null

        override fun next(): PyObject {
            position += 1
            return backingArray[position - 1] ?: error("Iterator reached end")
        }
    }

}
