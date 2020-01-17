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

package green.sailor.kython.test.cmp

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.test.helpers.assertFalse
import green.sailor.kython.test.helpers.assertTrue
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests the `__eq__` method.
 */
class `Test equals` {
    @Test
    fun `Test equals of int`() {
        val result = KythonInterpreter.testExec<PyBool>("result = 1 == 1")
        assertTrue(result)

        val result2 = KythonInterpreter.testExec<PyBool>("result = 1 == 2")
        assertFalse(result2)
    }

    @Test
    fun `Test equals of str`() {
        // "abc" and "ABC" will stored in co_consts differently
        // and two new PyString will be created to wrap them
        // so this is a required test
        val result = KythonInterpreter.testExec<PyBool>(
            """
            result = "abc".upper() == "ABC"
        """.trimIndent()
        )
        assertTrue(result)
    }

    @Test
    fun `Test equals of tuple`() {
        // this should trick the compiler/interpreter into creating two tuple instances
        val result = KythonInterpreter.testExec<PyBool>(
            """
            x = tuple((1,))
            y = tuple((1,))
            result = x == y
        """.trimIndent()
        )
        assertTrue(result)

        val result2 = KythonInterpreter.testExec<PyBool>(
            """
            x = (1,)
            y = (2,)
            result = x == y
        """.trimIndent()
        )
        assertFalse(result2)
    }

    @Test
    fun `Test invalid equals`() {
        val result = KythonInterpreter.testExec<PyBool>(
            """
            result = object() == object()
        """.trimIndent()
        )
        assertFalse(result)
    }
}
