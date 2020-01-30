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
import green.sailor.kython.interpreter.pyobject.types.PyListType
import green.sailor.kython.interpreter.toNativeList
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.util.explode

class PyList(subobjects: MutableList<PyObject>) : PyContainer(subobjects) {
    companion object {
        fun empty() = PyList(mutableListOf())
    }

    override fun pyToStr(): PyString {
        return PyString("[" + subobjects.joinToString {
            it.pyGetRepr().wrappedString
        } + "]")
    }

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        val other = other.cast<PyList>()
        return PyList((subobjects + other.subobjects) as MutableList<PyObject>)
    }

    override fun pyGetRepr(): PyString = pyToStr()

    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyList) return PyNotImplemented
        return PyBool.get(subobjects == other.subobjects)
    }

    override fun pyGreater(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyLesser(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyHash(): PyInt = typeError("lists are not hashable - they are mutable")

    /**
     * Implements list extension from another iterable object.
     */
    fun extend(other: PyObject) {
        val ourSubs = subobjects as MutableList
        when (other) {
            is PyContainer -> ourSubs.addAll(other.subobjects)
            is PySet -> ourSubs.addAll(other.wrappedSet)
            is PyString -> ourSubs.addAll(
                other.wrappedString.explode().map { PyString(it) }
            )
            else -> ourSubs.addAll(other.pyIter().toNativeList())
        }
    }

    override var type: PyType
        get() = PyListType
        set(_) = Exceptions.invalidClassSet(this)
}
