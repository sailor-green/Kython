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

package green.sailor.kython.interpreter.pyobject.numeric

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.types.PyFloatType
import green.sailor.kython.interpreter.util.plus
import green.sailor.kython.interpreter.util.toComplex
import kotlin.math.abs

/**
 * Represents a Python float. This wraps a kotlin Double (CPython wraps a C double,
 * so we're consistent there).
 */
class PyFloat(override val wrapped: Double) : PyPrimitive(), PyNumeric<Double, PyFloat, PyFloat>,
    Comparable<PyFloat> {
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

    override fun pyEquals(other: PyObject): PyObject {
        if (other is PyNumeric<*, *, *>) return PyBool.get(other.compareTo(this) == 0)
        return PyNotImplemented
    }

    override fun pyGreater(other: PyObject): PyObject {
        if (other is PyNumeric<*, *, *>) return PyBool.get(other < this)
        return PyNotImplemented
    }

    override fun pyLesser(other: PyObject): PyObject {
        if (other is PyNumeric<*, *, *>) return PyBool.get(other > this)
        return PyNotImplemented
    }

    override fun pyGreaterEquals(other: PyObject): PyObject {
        if (other is PyNumeric<*, *, *>) return PyBool.get(other <= this)
        return PyNotImplemented
    }

    override fun pyLesserEquals(other: PyObject): PyObject {
        if (other is PyNumeric<*, *, *>) return PyBool.get(other >= this)
        return PyNotImplemented
    }

    override fun pyPositive(): PyObject {
        if (wrapped > 0) return this
        return PyFloat(abs(wrapped))
    }

    override fun pyNegative(): PyObject {
        if (wrapped < 0) return this
        return PyFloat(-wrapped)
    }

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumeric<*, *, *>) return other + this
        return PyNotImplemented
    }

    override fun pySub(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumeric<*, *, *>) return other leftHandMinus this
        return PyNotImplemented
    }

    override fun pyMul(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumeric<*, *, *>) return other * this
        return PyNotImplemented
    }

    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject { // non-floor div
        if (other is PyNumeric<*, *, *>) return other leftHandDiv this
        return PyNotImplemented
    }

    override fun hashCode() = wrapped.hashCode()

    override fun compareTo(other: PyInt): Int = other.wrapped.compareTo(wrapped)

    override fun compareTo(other: PyFloat): Int = other.wrapped.compareTo(wrapped)

    override fun compareTo(other: PyComplex): Int = other.wrapped.compareTo(wrapped.toComplex())

    override fun plus(other: PyFloat): PyFloat = PyFloat(wrapped + other.wrapped)

    override fun plus(other: PyInt): PyFloat = PyFloat(wrapped + other.wrapped)

    override fun plus(other: PyComplex): PyComplex = PyComplex(wrapped.plus(other.wrapped))

    override fun times(other: PyFloat): PyFloat = PyFloat(wrapped * other.wrapped)

    override fun times(other: PyInt): PyFloat = PyFloat(wrapped * other.wrapped)

    override fun times(other: PyComplex): PyComplex = PyComplex(other.wrapped * wrapped)

    override fun leftHandMinus(actualObj: PyFloat) = PyFloat(actualObj.wrapped - wrapped)

    override fun leftHandMinus(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped - wrapped.toComplex())

    override fun leftHandMinus(actualObj: PyInt): PyFloat = PyFloat(actualObj.wrapped - wrapped)

    override fun leftHandDiv(actualObj: PyFloat): PyFloat = PyFloat(actualObj.wrapped / wrapped)

    override fun leftHandDiv(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped / wrapped.toComplex())

    override fun leftHandDiv(actualObj: PyInt): PyFloat =
        PyFloat(actualObj.wrapped.toDouble() / wrapped)

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
