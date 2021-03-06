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
import green.sailor.kython.interpreter.pyobject.types.PyFloatType
import kotlin.math.abs

/**
 * Represents a Python float. This wraps a kotlin Double (CPython wraps a C double,
 * so we're consistent there).
 */
class PyFloat(val wrapped: Double) : PyPrimitive() {
    override fun unwrap(): Double = wrapped

    private val _floatStr by lazy {
        PyString(wrapped.toString())
    }

    override fun pyToStr(): PyString = _floatStr
    override fun pyGetRepr(): PyString = _floatStr

    // NaN is truthy?
    override fun pyToBool(): PyBool = PyBool.get(wrapped != 0.0)
    override fun pyToInt(): PyInt = PyInt(wrapped.toLong())
    override fun pyToFloat(): PyFloat = this

    override fun pyGreater(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrapped > other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrapped > other.wrapped)
        return PyNotImplemented
    }
    override fun pyLesser(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrapped < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrapped < other.wrapped)
        return PyNotImplemented
    }
    override fun pyGreaterEquals(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrapped < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrapped < other.wrapped)
        return PyNotImplemented
    }
    override fun pyLesserEquals(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrapped < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrapped < other.wrapped)
        return PyNotImplemented
    }

    override fun pyPositive(): PyObject {
        if (wrapped > 0.0) return this
        return PyFloat(abs(wrapped))
    }
    override fun pyNegative(): PyObject {
        if (wrapped < 0.0) return this
        return PyFloat(-wrapped)
    }

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyInt) return PyFloat(wrapped + other.wrappedInt.toDouble())
        if (other is PyFloat) return PyFloat(wrapped + other.wrapped)
        return PyNotImplemented
    }
    override fun pySub(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyInt) return PyFloat(wrapped - other.wrappedInt.toDouble())
        if (other is PyFloat) return PyFloat(wrapped - other.wrapped)
        return PyNotImplemented
    }
    override fun pyMul(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyInt) return PyFloat(wrapped * other.wrappedInt)
        if (other is PyFloat) return PyFloat(wrapped * other.wrapped)
        return PyNotImplemented
    }
    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject { // non-floor div
        if (other is PyInt) return PyFloat(wrapped / other.wrappedInt.toDouble())
        if (other is PyFloat) return PyFloat(wrapped / other.wrapped)
        return PyNotImplemented
    }

    override fun hashCode() = wrapped.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PyFloat) return false
        if (wrapped != other.wrapped) return false

        return true
    }

    override var type: PyType
        get() = PyFloatType
        set(_) = Exceptions.invalidClassSet(this)
}
