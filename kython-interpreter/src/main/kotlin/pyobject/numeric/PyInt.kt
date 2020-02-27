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
import kotlin.math.abs

/**
 * Represents a Python int type. This internally wraps a long,
 */
open class PyInt(val wrappedInt: Long) : PyPrimitive(), PyNumber<PyInt>, Comparable<PyInt> {
    companion object {
        val ZERO = PyInt(0L)
        val ONE = PyInt(1L)
    }

    override fun unwrap(): Any = wrappedInt

    // default impls
    override fun pyToStr(): PyString = PyString(wrappedInt.toString())

    override fun pyGetRepr(): PyString = pyToStr()

    override fun pyEquals(other: PyObject): PyObject {
        if (other is PyNumber<*>) return PyBool.get(other.compareValue(this) == 0)
        return PyNotImplemented
    }

    override fun pyGreater(other: PyObject): PyObject {
        if (other is PyNumber<*>) return PyBool.get(other < this)
        return PyNotImplemented
    }

    override fun pyLesser(other: PyObject): PyObject {
        if (other is PyNumber<*>) return PyBool.get(other > this)
        return PyNotImplemented
    }

    override fun pyGreaterEquals(other: PyObject): PyObject {
        if (other is PyNumber<*>) return PyBool.get(other <= this)
        return PyNotImplemented
    }

    override fun pyLesserEquals(other: PyObject): PyObject {
        if (other is PyNumber<*>) return PyBool.get(other >= this)
        return PyNotImplemented
    }

    override fun pyPositive(): PyObject {
        if (wrappedInt > 0L) return this
        return PyInt(abs(wrappedInt))
    }

    override fun pyNegative(): PyObject {
        if (wrappedInt < 0L) return this
        return PyInt(-wrappedInt)
    }

    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumber<*>) return other + this
        return PyNotImplemented
    }

    override fun pySub(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumber<*>) return other leftHandMinus this
        return PyNotImplemented
    }

    override fun pyMul(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyNumber<*>) return other * this
        return PyNotImplemented
    }

    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject { // non-floor div
        if (other is PyNumber<*>) return other leftHandDiv this
        return PyNotImplemented
    }

    override fun pyToBool(): PyBool = PyBool.get(wrappedInt != 0L)

    override fun pyToInt(): PyInt = this

    override fun pyToFloat(): PyFloat = PyFloat(wrappedInt.toDouble())

    override fun compareTo(other: PyInt): Int = other.wrappedInt.compareTo(wrappedInt)

    override fun compareTo(other: PyFloat): Int = other.wrapped.compareTo(wrappedInt)

    override fun plus(other: PyFloat): PyFloat = PyFloat(wrappedInt + other.wrapped)

    override fun plus(other: PyInt): PyInt = PyInt(wrappedInt + other.wrappedInt)

    override fun times(other: PyFloat): PyFloat = PyFloat(wrappedInt.toDouble() * other.wrapped)

    override fun times(other: PyInt): PyInt = PyInt(wrappedInt * other.wrappedInt)

    override fun leftHandMinus(actualObj: PyFloat) =
        PyFloat(actualObj.wrapped - wrappedInt.toDouble())

    override fun leftHandMinus(actualObj: PyInt): PyInt = PyInt(actualObj.wrappedInt - wrappedInt)

    override fun leftHandDiv(actualObj: PyFloat): PyFloat =
        PyFloat(actualObj.wrapped / wrappedInt.toDouble())

    override fun leftHandDiv(actualObj: PyInt): PyFloat =
        PyFloat(actualObj.wrappedInt.toDouble() / wrappedInt.toDouble())

    override fun compareValue(other: PyFloat): Int = wrappedInt.compareTo(other.wrapped)

    override fun compareValue(other: PyInt): Int = wrappedInt.compareTo(other.wrappedInt)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PyInt) return false

        return wrappedInt == other.wrappedInt
    }

    override fun hashCode() = wrappedInt.hashCode()

    override var type: PyType
        get() = PyIntType
        set(_) = Exceptions.invalidClassSet(this)
}
