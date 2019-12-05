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
import green.sailor.kython.interpreter.pyobject.types.PyBytesType

/**
 * Represents a Python bytes object. This wraps a regular JVM ByteArray.
 */
class PyBytes(val wrapped: ByteArray) : PyObject() {
    override fun pyGetStr(): PyString = pyGetRepr()

    override fun pyGetRepr(): PyString {
        val inner = PyString(wrapped.joinToString("") {
            if (it in 32..126) it.toChar().toString()
            else "\\x" + it.toUByte().toString(16).padStart(2, '0')
        })
        return PyString("b${inner.pyGetRepr().wrappedString}")
    }

    override fun pyToBool(): PyBool = PyBool.get(wrapped.isNotEmpty())
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyBytes) {
            return PyNotImplemented
        }
        return PyBool.get(wrapped.contentEquals(other.wrapped))
    }

    override var type: PyType
        get() = PyBytesType
        set(_) = Exceptions.invalidClassSet(this)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PyBytes) return false
        return this.wrapped.contentEquals(other.wrapped)
    }

    override fun hashCode(): Int = wrapped.contentHashCode()
}
