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

package green.sailor.kython.interpreter.pyobject.collection

import green.sailor.kython.interpreter.indexError
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyNotImplemented
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.util.cast

/**
 * Abstract superclass shared between PyList and PyTuple, contains some common methods.
 */
abstract class PyContainer(subobjects: List<PyObject>) :
    PyCollection(subobjects), Collection<PyObject> by subobjects {
    override fun unwrap(): List<PyObject> = subobjects as List<PyObject>

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
        return (subobjects as List<PyObject>)[realIdx]
    }

    /**
     * Checks if two containers are equal to each other.
     */
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyContainer) return PyNotImplemented
        return PyBool.get(subobjects.zip(other.subobjects).all {
            it.first.pyEquals(it.second).pyToBool().wrapped
        })
    }
}
