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
interface PyNumber<T> where T : PyObject, T : Comparable<T> {
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

    /** Adds the other value to this value. */
    operator fun plus(other: PyInt): T

    /** Adds the other value to this value. */
    operator fun plus(other: PyFloat): PyFloat

    /** Multiplies this value by the other value. */
    operator fun times(other: PyInt): T

    /** Multiplies this value by the other value. */
    operator fun times(other: PyFloat): PyFloat

    /** Divides the [actualObj] by this value. */
    infix fun leftHandDiv(actualObj: PyInt): PyFloat

    /** Divides the [actualObj] by this value. */
    infix fun leftHandDiv(actualObj: PyFloat): PyFloat

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: PyInt): T

    /** Subtracts the [actualObj] by this value. */
    infix fun leftHandMinus(actualObj: PyFloat): PyFloat

    /** Compare values of [other] and this value. */
    infix fun compareValue(other: PyInt): Int

    /** Compare values of [other] and this value. */
    fun compareValue(other: PyFloat): Int

}
