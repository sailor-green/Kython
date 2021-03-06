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
import green.sailor.kython.interpreter.pyobject.iterators.PyBuiltinIterator
import green.sailor.kython.interpreter.pyobject.iterators.PyEmptyIterator
import green.sailor.kython.interpreter.pyobject.types.PyStringType
import green.sailor.kython.interpreter.valueError

/**
 * Represents a Python string. This wraps a regular JVM string.
 */
class PyString private constructor(val wrappedString: String) : PyPrimitive() {
    companion object {
        // some common strings
        val UNPRINTABLE = PyString("<unprintable>")

        val EMPTY = PyString("")

        operator fun invoke(s: String): PyString {
            if (s.isEmpty()) return EMPTY
            return PyString(s)
        }
    }

    override fun unwrap(): String = wrappedString
    override fun pyToStr(): PyString = this
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
    override fun pyMul(other: PyObject, reverse: Boolean): PyObject {
        if (other !is PyInt) return PyNotImplemented
        return PyString(wrappedString.repeat(other.wrappedInt.toInt()))
    }
    override fun pyContains(other: PyObject): PyObject {
        if (other !is PyString) return PyNotImplemented
        return PyBool.get(other.wrappedString in wrappedString)
    }

    override fun pyIter(): PyObject {
        if (wrappedString.isEmpty()) {
            return PyEmptyIterator
        }

        return PyBuiltinIterator(object : Iterator<PyObject> {
            val realIterator = wrappedString.iterator()
            override fun hasNext(): Boolean = realIterator.hasNext()
            override fun next(): PyObject = try {
                PyString(realIterator.nextChar().toString())
            } catch (e: StringIndexOutOfBoundsException) {
                throw NoSuchElementException(e.message)
            }
        })
    }

    override fun pyLen(): PyInt = PyInt(wrappedString.length.toLong())

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PyString) {
            return false
        }

        return wrappedString == other.wrappedString
    }

    override fun hashCode(): Int = wrappedString.hashCode()

    override var type: PyType
        get() = PyStringType
        set(_) = Exceptions.invalidClassSet(this)
}
