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
import green.sailor.kython.interpreter.pyobject.types.PyComplexType
import green.sailor.kython.interpreter.util.Complex
import green.sailor.kython.interpreter.util.plus
import green.sailor.kython.interpreter.util.toComplex

class PyComplex(override val wrapped: Complex) :
    PyNumeric<Complex, PyComplex, PyComplex>,
    Comparable<PyComplex>, PyPrimitive() {

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

    private val complexStr by lazy { PyString(wrapped.toString()) }

    override fun pyToStr(): PyString = complexStr
    override fun pyGetRepr(): PyString = complexStr

    override fun pyToBool(): PyBool = PyBool.get(wrapped.real != 0.0 || wrapped.imaginary != 0.0)

    override fun pyPositive(): PyObject {
        // ??
        return PyFloat(wrapped.abs)
    }

    override fun pyNegative(): PyObject {
        // Weird flip, but alright.
        val comp = wrapped.copy(real = -wrapped.real, imaginary = -wrapped.imaginary)
        return PyComplex(comp)
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

    override fun unwrap(): Complex = wrapped

    override fun hashCode() = wrapped.hashCode()

    override fun compareTo(other: PyInt): Int =
        wrapped.compareTo(other.wrapped.toDouble().toComplex())

    override fun compareTo(other: PyFloat): Int = wrapped.compareTo(other.wrapped.toComplex())

    override fun compareTo(other: PyComplex): Int = wrapped.compareTo(other.wrapped)

    override fun plus(other: PyFloat): PyComplex = PyComplex(other.wrapped.plus(wrapped))

    override fun plus(other: PyInt): PyComplex = PyComplex(wrapped + other.wrapped.toComplex())

    override fun plus(other: PyComplex): PyComplex = PyComplex(wrapped.plus(other.wrapped))

    override fun times(other: PyFloat): PyComplex = PyComplex(wrapped * other.wrapped)

    override fun times(other: PyInt): PyComplex = PyComplex(wrapped * other.wrapped.toComplex())

    override fun times(other: PyComplex): PyComplex = PyComplex(other.wrapped * wrapped)

    override fun leftHandMinus(actualObj: PyFloat): PyComplex =
        PyComplex(actualObj.wrapped.toComplex() - wrapped)

    override fun leftHandMinus(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped - wrapped)

    override fun leftHandMinus(actualObj: PyInt): PyComplex =
        PyComplex(actualObj.wrapped.toComplex() - wrapped)

    override fun leftHandDiv(actualObj: PyFloat): PyComplex =
        PyComplex(actualObj.wrapped.toComplex() / wrapped)

    override fun leftHandDiv(actualObj: PyComplex): PyComplex =
        PyComplex(actualObj.wrapped / wrapped)

    override fun leftHandDiv(actualObj: PyInt): PyComplex =
        PyComplex(actualObj.wrapped.toComplex() / wrapped)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PyComplex

        if (wrapped != other.wrapped) return false

        return true
    }

    override var type: PyType
        get() = PyComplexType
        set(_) = Exceptions.invalidClassSet(this)
}
