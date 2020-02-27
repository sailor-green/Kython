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
import green.sailor.kython.interpreter.pyobject.types.PyIntType
import green.sailor.kython.interpreter.util.toComplex
import kotlin.math.abs

/**
 * Represents a Python int type. This internally wraps a long,
 */
open class PyInt(override val wrapped: Long) : PyPrimitive(), PyNumeric<Long, PyInt, PyFloat>,
    Comparable<PyInt> {
    companion object {
        val ZERO = PyInt(0L)
        val ONE = PyInt(1L)
    }

    override fun unwrap(): Any = wrapped

    // default impls
    override fun pyToStr(): PyString = PyString(wrapped.toString())

    override fun pyGetRepr(): PyString = pyToStr()

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
        if (wrapped > 0L) return this
        return PyInt(abs(wrapped))
    }

    override fun pyNegative(): PyObject {
        if (wrapped < 0L) return this
        return PyInt(-wrapped)
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

    override fun pyToBool(): PyBool = PyBool.get(wrapped != 0L)

    override fun pyToInt(): PyInt = this

    override fun pyToFloat(): PyFloat = PyFloat(wrapped.toDouble())

    override fun compareTo(other: PyInt): Int = other.wrapped.compareTo(wrapped)

    override fun compareTo(other: PyFloat): Int = other.wrapped.compareTo(wrapped)

    override fun compareTo(other: PyComplex): Int =
        other.wrapped.compareTo(wrapped.toDouble().toComplex())

    override fun plus(other: PyInt): PyInt = PyInt(wrapped + other.wrapped)

    override fun plus(other: PyFloat): PyFloat = PyFloat(wrapped + other.wrapped)

    override fun plus(other: PyComplex): PyComplex =
        PyComplex(other.wrapped + wrapped.toDouble().toComplex())

    override fun times(other: PyFloat): PyFloat = PyFloat(wrapped.toDouble() * other.wrapped)

    override fun times(other: PyInt): PyInt = PyInt(wrapped * other.wrapped)

    override fun leftHandMinus(actualObj: PyFloat) =
        PyFloat(actualObj.wrapped - wrapped.toDouble())

    override fun leftHandMinus(actualObj: PyInt): PyInt = PyInt(actualObj.wrapped - wrapped)

    override fun leftHandMinus(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped - wrapped.toComplex())

    override fun leftHandDiv(actualObj: PyFloat): PyFloat =
        PyFloat(actualObj.wrapped / wrapped.toDouble())

    override fun leftHandDiv(actualObj: PyInt): PyFloat =
        PyFloat(actualObj.wrapped.toDouble() / wrapped.toDouble())

    override fun leftHandDiv(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped / wrapped.toComplex())

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PyInt) return false

        return wrapped == other.wrapped
    }

    override fun hashCode() = wrapped.hashCode()

    override var type: PyType
        get() = PyIntType
        set(_) = Exceptions.invalidClassSet(this)

    override fun times(other: PyComplex): PyComplex = PyComplex(wrapped.toComplex() * other.wrapped)
}
