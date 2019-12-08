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
import kotlin.math.abs

/**
 * Represents a Python int type. This internally wraps a long,
 */
open class PyInt(val wrappedInt: Long) : PyObject() {
    // default impls
    override fun pyToStr(): PyString = PyString(wrappedInt.toString())
    override fun pyGetRepr(): PyString = pyToStr()

    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyInt) return PyNotImplemented
        return PyBool.get(other.wrappedInt == wrappedInt)
    }
    override fun pyGreater(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrappedInt > other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrappedInt > other.wrapped)
        return PyNotImplemented
    }
    override fun pyLesser(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrappedInt < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrappedInt < other.wrapped)
        return PyNotImplemented
    }
    override fun pyGreaterEquals(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrappedInt < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrappedInt < other.wrapped)
        return PyNotImplemented
    }
    override fun pyLesserEquals(other: PyObject): PyObject {
        if (other is PyInt) return PyBool.get(wrappedInt < other.wrappedInt)
        if (other is PyFloat) return PyBool.get(wrappedInt < other.wrapped)
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
        if (other is PyInt) return PyInt(wrappedInt + other.wrappedInt)
        if (other is PyFloat) return PyFloat(wrappedInt.toDouble() + other.wrapped)
        return PyNotImplemented
    }
    override fun pySub(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyInt) return PyInt(wrappedInt - other.wrappedInt)
        if (other is PyFloat) return PyFloat(wrappedInt.toDouble() - other.wrapped)
        return PyNotImplemented
    }
    override fun pyMul(other: PyObject, reverse: Boolean): PyObject {
        if (other is PyInt) return PyInt(wrappedInt * other.wrappedInt)
        if (other is PyFloat) return PyFloat(wrappedInt.toDouble() * other.wrapped)
        return PyNotImplemented
    }
    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject { // non-floor div
        if (other is PyInt) return PyFloat(wrappedInt.toDouble() / other.wrappedInt.toDouble())
        if (other is PyFloat) return PyFloat(wrappedInt.toDouble() / other.wrapped)
        return PyNotImplemented
    }

    override fun pyToBool(): PyBool = PyBool.get(wrappedInt != 0L)
    override fun pyToInt(): PyInt = this
    override fun pyToFloat(): PyFloat = PyFloat(wrappedInt.toDouble())

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
