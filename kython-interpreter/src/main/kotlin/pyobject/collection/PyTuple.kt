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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.types.PyTupleType
import green.sailor.kython.interpreter.typeError

/**
 * Represents a python tuple of objects. This is a fixed-size immutable container for other PyObject.
 */
@Suppress("MemberVisibilityCanBePrivate")
class PyTuple private constructor(subobjects: List<PyObject>) : PyContainer(subobjects) {
    companion object {
        /**
         * Represents the empty tuple.
         */
        val EMPTY = PyTuple(listOf())

        /**
         * Gets a new [PyTuple]. This will return the empty tuple for optimisation purposes, if
         * the list is empty.
         */
        fun get(subobjects: List<PyObject>): PyTuple {
            if (subobjects.isEmpty()) {
                return EMPTY
            }

            return PyTuple(subobjects)
        }

        /**
         * Vararg alias for [get].
         */
        fun of(vararg items: PyObject): PyTuple = get(items.toList())
    }

    override fun pyToStr(): PyString {
        if (subobjects.size == 1) {
            return PyString("(${subobjects.first().pyGetRepr()},)")
        }

        return PyString(subobjects.joinToString(prefix = "(", postfix = ")") {
            it.pyGetRepr().wrappedString
        })
    }

    override fun pyGetRepr(): PyString = pyToStr()
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyTuple) return PyNotImplemented
        return PyBool.get(subobjects == other.subobjects)
    }

    override fun pyGreater(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyLesser(other: PyObject): PyObject = TODO("Not implemented")

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        if (other !is PyTuple) return PyNotImplemented
        return get(subobjects + other.subobjects)
    }

    override fun pyHash(): PyInt = PyInt(
        subobjects.map { it.pyHash().wrappedInt }.hashCode().toLong()
    )

    override fun pySetItem(idx: PyObject, value: PyObject): PyNone {
        typeError("tuples are immutable - cannot set items")
    }

    override fun hashCode() = subobjects.hashCode()

    override var type: PyType
        get() = PyTupleType
        set(_) = Exceptions.invalidClassSet(this)
}
