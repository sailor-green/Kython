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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.iterators.PyBuiltinIterator
import green.sailor.kython.interpreter.pyobject.iterators.PyEmptyIterator
import green.sailor.kython.interpreter.pyobject.types.PySetType
import green.sailor.kython.interpreter.toNativeList
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.util.explode

/**
 * Represents a Python set.
 */
class PySet(val wrappedSet: MutableSet<PyObject>) : PyPrimitive() {
    override fun unwrap(): Set<PyObject> = wrappedSet

    override fun pyHash(): PyInt = typeError("sets are not hashable - they are mutable")

    override fun pyToStr(): PyString = PyString(
        "{" + wrappedSet.joinToString(", ") { it.pyGetRepr().wrappedString } + "}"
    )

    override fun pyGetRepr(): PyString = pyToStr()
    override fun pyToBool(): PyBool = PyBool.get(wrappedSet.isNotEmpty())
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PySet) {
            return PyNotImplemented
        }
        return PyBool.get(wrappedSet == other.wrappedSet)
    }

    override fun pyGreater(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyLesser(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyContains(other: PyObject): PyObject = PyBool.get(other in wrappedSet)

    override fun pyLen(): PyInt = PyInt(wrappedSet.size.toLong())
    override fun pyIter(): PyObject {
        if (wrappedSet.isEmpty()) {
            return PyEmptyIterator
        }
        return PyBuiltinIterator(wrappedSet.iterator())
    }

    /**
     * Implements set updating from another iterable object.
     */
    fun update(other: PyObject) {
        when (other) {
            is PySet -> wrappedSet.addAll(other.wrappedSet)
            is PyContainer -> wrappedSet.addAll(other.subobjects)
            is PyString -> wrappedSet.addAll(
                other.wrappedString.explode().map { PyString(it) }
            )
            else -> wrappedSet.addAll(other.pyIter().toNativeList())
        }
    }

    override var type: PyType
        get() = PySetType
        set(_) = Exceptions.invalidClassSet(this)
}
