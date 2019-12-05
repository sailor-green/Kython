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
package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.types.PyIntType

/**
 * Represents a Python int type. This internally wraps a long,
 */
class PyInt(val wrappedInt: Long) : PyObject() {
    // default impls
    override fun pyGetStr(): PyString = PyString(wrappedInt.toString())
    override fun pyGetRepr(): PyString = pyGetStr()
    override fun pyEquals(other: PyObject, reverse: Boolean): PyObject {
        if (other !is PyInt) {
            return PyNotImplemented
        }
        return PyBool.get(other.wrappedInt == wrappedInt)
    }

    override fun pyToBool(): PyBool = PyBool.get(wrappedInt != 0L)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PyInt) return false

        return wrappedInt == other.wrappedInt
    }

    override fun hashCode(): Int {
        return wrappedInt.hashCode()
    }

    override var type: PyType
        get() = PyIntType
        set(_) = Exceptions.invalidClassSet(this)
}
