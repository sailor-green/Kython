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
import green.sailor.kython.interpreter.typeError

class PyList(subobjects: MutableList<PyObject>) : PyContainer(subobjects) {
    override fun pyToStr(): PyString {
        return PyString("[" + subobjects.joinToString {
            it.pyGetRepr().wrappedString
        } + "]")
    }

    override fun pyGetRepr(): PyString = pyToStr()

    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyList) return PyNotImplemented
        return PyBool.get(subobjects == other.subobjects)
    }
    override fun pyGreater(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyLesser(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyHash(): PyInt = typeError("lists are not hashable - they are mutable")

    override var type: PyType
        get() = PyListType
        set(_) = Exceptions.invalidClassSet(this)
}
