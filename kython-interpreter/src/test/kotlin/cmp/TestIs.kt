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
import org.junit.jupiter.api.Test

/**
 * Tests the `is` operator.
 */
class `Test is` {
    @Test
    fun `Test is on a single instance`() {
        val result = KythonInterpreter.testExec<PyBool>("""
            x = object()
            y = x
            result = x is y
        """.trimIndent())
        assertTrue(result)
    }

    @Test
    fun `Test is on different instances`() {
        val result = KythonInterpreter.testExec<PyBool>("""
            x = object()
            y = object()
            result = x is y
        """.trimIndent())
        assertFalse(result)
    }

    @Test
    fun `Test is None`() {
        val result = KythonInterpreter.testExec<PyBool>("""
            x = print()
            result = x is None
        """.trimIndent())
        assertTrue(result)

        val result2 = KythonInterpreter.testExec<PyBool>("""
            result = 1 is None
        """.trimIndent())
        assertFalse(result2)
    }

    @Test
    fun `Test boolean singletons`() {
        val result = KythonInterpreter.testExec<PyBool>("""
            x = 1 == 2
            result = x is False
        """.trimIndent())
        assertTrue(result)

        val result2 = KythonInterpreter.testExec<PyBool>("""
            x = 2 == 2
            result = x is True
        """.trimIndent())
        assertTrue(result2)
    }
}
