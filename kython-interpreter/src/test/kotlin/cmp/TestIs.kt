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
import green.sailor.kython.test.assertFalse
import green.sailor.kython.test.assertTrue
import green.sailor.kython.test.testExec
import org.junit.jupiter.api.Test

/**
 * Tests the `is` operator.
 */
class `Test is` {
    @Test
    fun `Test is on a single instance`() {
        val result = KythonInterpreter.testExec("""
            x = object()
            y = x
            result = x is y
        """.trimIndent())
        assertTrue(result)
    }

    @Test
    fun `Test is on different instances`() {
        val result = KythonInterpreter.testExec("""
            x = object()
            y = object()
            result = x is y
        """.trimIndent())
        assertFalse(result)
    }
}
