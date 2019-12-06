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
import green.sailor.kython.interpreter.pyobject.types.PyStringType
import green.sailor.kython.interpreter.valueError

/**
 * Represents a Python string. This wraps a regular JVM string.
 */
class PyString(val wrappedString: String) : PyObject() {
    companion object {
        // some common strings
        val UNPRINTABLE =
            PyString("<unprintable>")
    }

    override fun pyGetStr(): PyString = this
    override fun pyGetRepr(): PyString = PyString("'$wrappedString'")
    override fun pyToBool(): PyBool = PyBool.get(wrappedString.isNotEmpty())
    override fun pyToInt(): PyInt = try {
        PyInt(wrappedString.toLong())
    } catch (e: NumberFormatException) {
        valueError("Cannot convert '$wrappedString' to int")
    }
    override fun pyToFloat(): PyFloat = try {
        PyFloat(wrappedString.toDouble())
    } catch (e: NumberFormatException) {
        valueError("Cannot convert '$wrappedString' to float")
    }

    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyString) {
            return PyNotImplemented
        }
        return PyBool.get(wrappedString == other.wrappedString)
    }
    override fun pyGreater(other: PyObject): PyObject {
        if (other !is PyString) return PyNotImplemented
        return PyBool.get(wrappedString > other.wrappedString)
    }
    override fun pyLesser(other: PyObject): PyObject {
        if (other !is PyString) return PyNotImplemented
        return PyBool.get(wrappedString < other.wrappedString)
    }

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyString) return PyString(wrappedString + other.wrappedString)
        return PyNotImplemented
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PyString) {
            return false
        }

        return wrappedString == other.wrappedString
    }

    override fun hashCode(): Int {
        return wrappedString.hashCode()
    }

    override var type: PyType
        get() = PyStringType
        set(_) = Exceptions.invalidClassSet(this)
}
