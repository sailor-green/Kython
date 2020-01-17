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

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.test.helpers.assertUnwrappedEquals
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Test

/**
 * Tests closures (nested functions).
 */
class `Test closures` {
    @Test
    fun `Test basic load deref`() {
        val code = """
            def a():
                x = 1
                def b():
                    return x
                return b

            result = a()()
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyInt>(code)
        assertUnwrappedEquals(result, 1L)
    }

    @Test
    fun `Test load deref after a store`() {
        val code = """
            def a():
                x = 1
                def b():
                    return x
                x = 2
                return b

            result = a()()
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyInt>(code)
        assertUnwrappedEquals(result, 2L)
    }

    @Test
    fun `Test closures different scope`() {
        val code = """
            def a():
                x = 1
                def b():
                    x = 3
                b()
                return x

            result = a()
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyInt>(code)
        assertUnwrappedEquals(result, 1L)
    }

    @Test
    fun `Test load deref nonlocal`() {
        val code = """
            def a():
                x = 1
                def b():
                    nonlocal x
                    x = 3
                b()
                return x
            result = a()

        """.trimIndent()
        val result = KythonInterpreter.testExec<PyInt>(code)
        assertUnwrappedEquals(result, 3L)
    }
}
