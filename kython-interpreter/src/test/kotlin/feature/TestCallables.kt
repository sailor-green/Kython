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

package green.sailor.kython.test.feature

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.test.assertUnwrappedTrue
import green.sailor.kython.test.testExec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class `Test callables` {
    @Test
    fun `Test calling a simple function`() {
        val result = KythonInterpreter.testExec("""
            def fn():
                return 1

            result = fn()
        """.trimIndent())
        assertUnwrappedTrue(result) { it == 1L }
    }

    @Test
    fun `Test calling a function with arguments`() {
        val result = KythonInterpreter.testExec("""
            def fn(a, b):
                return a + b

            result = fn(1, 2)
        """.trimIndent())
        assertUnwrappedTrue(result) { it == 3L }
    }

    @Test
    fun `Test calling a function with bad arguments`() {
        val initial = """
            def a(a, b):
                return 1 + 2

        """.trimIndent()

        val error = Assertions.assertThrows(KyError::class.java) {
            KythonInterpreter.testExec(initial + "result = a()")
        }
        Assertions.assertTrue(error.wrapped.type == Exceptions.TYPE_ERROR)

        val error2 = Assertions.assertThrows(KyError::class.java) {
            KythonInterpreter.testExec(initial + "result = a(1, 2, 3)")
        }
        Assertions.assertTrue(error2.wrapped.type == Exceptions.TYPE_ERROR)
    }
}
