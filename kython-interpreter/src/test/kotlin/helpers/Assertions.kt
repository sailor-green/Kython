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

package green.sailor.kython.test.helpers

import green.sailor.kython.interpreter.pyobject.*
import org.junit.jupiter.api.Assertions

/**
 * Asserts that this PyObject is truthy.
 */
fun assertTrue(result: PyObject) {
    if (result is PyBool) {
        return Assertions.assertTrue(result.wrapped)
    }
    Assertions.fail<Nothing>("Object was $result, not a boolean")
}

/**
 * Asserts that this PyObject is falsey.
 */
fun assertFalse(result: PyObject) {
    if (result is PyBool) {
        return Assertions.assertFalse(result.wrapped)
    }
    Assertions.fail<Nothing>("Object was $result, not a boolean")
}

fun assertUnwrappedEquals(wrapped: PyObject, expected: Any?, calledWith: String = "") {
    if (wrapped !is PyPrimitive) return Assertions.fail<Nothing>("Object was not a primitive")
    Assertions.assertEquals(expected, wrapped.unwrap(), "Called with '$calledWith'")
}

/**
 * Asserts that this PyInt equals a different int.
 */
fun assertUnwrappedEquals(wrapped: PyInt, expected: Long) =
    assertUnwrappedEquals(wrapped as PyObject, expected)

/**
 * Asserts that this PyString equals a different string.
 */
fun assertUnwrappedEquals(wrapped: PyString, expected: String) =
    assertUnwrappedEquals(wrapped as PyObject, expected)

/**
 * Asserts that this PyObject equals a different object.
 */
fun assertUnwrappedTrue(wrapped: PyObject, fn: (Any?) -> Boolean) {
    if (wrapped !is PyPrimitive) return Assertions.fail<Nothing>("Object was not a primitive")
    return Assertions.assertTrue(fn(wrapped.unwrap()))
}

/**
 * Asserts that this PyObject does not equal a different object.
 */
fun assertUnwrappedFalse(wrapped: PyObject, fn: (Any?) -> Boolean) {
    if (wrapped !is PyPrimitive) return Assertions.fail<Nothing>("Object was not a primitive")
    return Assertions.assertFalse(fn(wrapped.unwrap()))
}
