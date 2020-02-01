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

package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.indexError
import green.sailor.kython.interpreter.pyobject.iterators.PyBuiltinIterator
import green.sailor.kython.interpreter.pyobject.iterators.PyEmptyIterator
import green.sailor.kython.interpreter.util.cast

/**
 * Abstract superclass shared between PyList and PyTuple, contains some common methods.
 */
abstract class PyContainer(val subobjects: List<PyObject>) : PyPrimitive() {
    override fun unwrap(): List<PyObject> = subobjects

    override fun pyToBool(): PyBool = PyBool.get(subobjects.isNotEmpty())
    override fun pyIter(): PyObject {
        if (subobjects.isEmpty()) return PyEmptyIterator
        return PyBuiltinIterator(subobjects.listIterator())
    }

    /**
     * Gets the real index from an index, calculating negative slices appropriately.
     */
    fun getRealIndex(idx: Int): Int {
        if (idx >= 0) return idx
        return subobjects.size + idx
    }

    /**
     * Checks the specified index to see if it is valid within this list.
     *
     * You should call [getRealIndex] first before calling this.
     */
    fun verifyIndex(idx: Int): Boolean {
        if (subobjects.isEmpty()) return false
        if (idx < 0) return false
        // Note: 0-offset indexes means that <= is effectively the same as (size + 1) <.
        if (subobjects.size <= idx) return false

        return true
    }

    override fun pyGetItem(idx: PyObject): PyObject {
        val initial = idx.cast<PyInt>().wrappedInt.toInt()
        val realIdx = getRealIndex(initial)
        if (!verifyIndex(realIdx)) {
            indexError("list index $realIdx is out of range (list size: ${subobjects.size})")
        }
        return subobjects[realIdx]
    }

    override fun pyLengthHint(): PyInt = PyInt(subobjects.size.toLong())
    override fun pyLen(): PyInt = PyInt(subobjects.size.toLong())

    override fun pyContains(other: PyObject): PyObject =
        PyBool.get(other in subobjects)
}
