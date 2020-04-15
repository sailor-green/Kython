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

import green.sailor.kython.interpreter.pyobject.PyObject

/**
 * Interface for common numeric methods.
 */
interface PyNumeric<T, U, V>
    where U : PyObject, U : Comparable<U>, V : PyObject, V : Comparable<V> {
    /** The internal wrapped value */
    val wrapped: T

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value,
     * a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: U): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value,
     * a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: PyInt): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value,
     * a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: PyFloat): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value,
     * a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: PyComplex): Int

    /** Adds the other value to this value. */
    operator fun plus(other: U): U

    /** Adds the other value to this value. */
    operator fun plus(other: PyInt): U

    /** Adds the other value to this value. */
    operator fun plus(other: PyFloat): V

    /** Adds the other value to this value. */
    operator fun plus(other: PyComplex): PyComplex

    /** Multiplies this value by the other value. */
    operator fun times(other: U): U

    /** Multiplies this value by the other value. */
    operator fun times(other: PyInt): U

    /** Multiplies this value by the other value. */
    operator fun times(other: PyFloat): V

    /** Multiplies this value by the other value. */
    operator fun times(other: PyComplex): PyComplex

    /** Divides the [actualObj] by this value. */
    infix fun leftHandDiv(actualObj: PyInt): V

    /** Divides the [actualObj] by this value. */
    infix fun leftHandDiv(actualObj: PyFloat): V

    /** Divides the [actualObj] by this value. */
    infix fun leftHandDiv(actualObj: PyComplex): PyComplex

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: U): U

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: PyInt): U

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: PyFloat): V

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: PyComplex): PyComplex
}
