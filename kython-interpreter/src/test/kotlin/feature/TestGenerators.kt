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

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.pyobject.numeric.PyInt
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.test.helpers.assertUnwrappedEquals
import green.sailor.kython.test.helpers.testExec
import green.sailor.kython.test.helpers.testExecErrors
import org.junit.jupiter.api.Test

/**
 * Tests the generator feature.
 */
class `Test generators`() {
    @Test
    fun `Test generator receive`() {
        val result = KythonInterpreter.testExec<PyInt>("""
            def gen():
                yield 1

            g = gen()
            result = g.send(None)
        """.trimIndent())
        assertUnwrappedEquals(result, 1L)
    }

    @Test
    fun `Test generator send`() {
        try {
            KythonInterpreter.testExecErrors<PyInt>(
                """
                def gen():
                    x = yield
                    return x

                g = gen()
                g.send(None)
                g.send(1)
            """.trimIndent()
            )
        } catch (e: KyError) {
            e.ensure(Exceptions.STOP_ITERATION)
            val result = e.wrapped.args.first().cast<PyInt>()
            assertUnwrappedEquals(result, 1L)
        }
    }

    @Test
    fun `Test generator iteration`() {
        val result = KythonInterpreter.testExec<PyInt>("""
            def gen():
                yield 1

            for x in gen():
                result = x
        """.trimIndent())
        assertUnwrappedEquals(result, 1L)
    }
}
