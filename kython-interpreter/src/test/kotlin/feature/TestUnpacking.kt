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
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests unpacking instructions.
 */
class `Test unpacking` {
    @Test
    fun `Test BUILD_TUPLE_UNPACK`() {
        val code = """
            x = (1, 2)
            y = (4, 5)
            result = (*x, *y)
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyTuple>(code).subobjects
        val mapped = result.map { it.cast<PyInt>().wrappedInt }
        Assertions.assertEquals(listOf(1L, 2L, 4L, 5L), mapped)
    }

    @Test
    fun `Test BUILD_SET_UNPACK`() {
        val code = """
            x = {1, 2}
            y = {4, 5}
            result = {*x, *y}
        """.trimIndent()
        val result = KythonInterpreter.testExec<PySet>(code).wrappedSet
        val mapped = result.mapTo(mutableSetOf()) { it.cast<PyInt>().wrappedInt }
        Assertions.assertEquals(setOf(1L, 2L, 4L, 5L), mapped)
    }

    @Test
    fun `Test BUILD_LIST_UNPACK`() {
        val code = """
            x = [1, 2]
            y = [4, 5]
            result = [*x, *y]
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyList>(code).subobjects
        val mapped = result.map { it.cast<PyInt>().wrappedInt }
        Assertions.assertEquals(listOf(1L, 2L, 4L, 5L), mapped)
    }
}
