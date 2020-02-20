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
import green.sailor.kython.interpreter.pyError
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.test.helpers.assertUnwrappedEquals
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class `Test callables` {
    @Test
    fun `Test calling a simple function`() {
        val result = KythonInterpreter.testExec<PyInt>("""
            def fn():
                return 1

            result = fn()
        """.trimIndent())
        assertUnwrappedEquals(result, 1L)
    }

    @Test
    fun `Test calling a function with arguments`() {
        val result = KythonInterpreter.testExec<PyInt>("""
            def fn(a, b):
                return a + b

            result = fn(1, 2)
        """.trimIndent())
        assertUnwrappedEquals(result, 3L)
    }

    @Test
    fun `Test calling a function with bad arguments`() {
        val initial = """
            def a(a, b):
                return 1 + 2

        """.trimIndent()

        val error = Assertions.assertThrows(KyError::class.java) {
            KythonInterpreter.testExec<Nothing>(initial + "result = a()")
        }
        Assertions.assertTrue(error.pyError.type == Exceptions.TYPE_ERROR)

        val error2 = Assertions.assertThrows(KyError::class.java) {
            KythonInterpreter.testExec<Nothing>(initial + "result = a(1, 2, 3)")
        }
        Assertions.assertTrue(error2.pyError.type == Exceptions.TYPE_ERROR)
    }

    @Test
    fun `Test calling a function with default arguments`() {
        val first = KythonInterpreter.testExec<PyInt>("""
            def fn(a, b=1, c=2):
                return a + b + c

            result = fn(1)
        """.trimIndent())
        assertUnwrappedEquals(first, 4L)

        val second = KythonInterpreter.testExec<PyInt>("""
            def fn(a, b=1, c=2):
                return a + b + c

            result = fn(1, 3)
        """.trimIndent())
        assertUnwrappedEquals(second, 6L)
    }
}
