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
import green.sailor.kython.interpreter.pyobject.PyList
import green.sailor.kython.interpreter.pyobject.PySet
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class `Test comprehensions` {
    @Test
    fun `Test basic list comprehension`() {
        val code = """
            result = [int(x) for x in ("1", "2", "3", "4")]
        """.trimIndent()
        val result = KythonInterpreter.testExec<PyList>(code).unwrap()
        Assertions.assertTrue(result.map { it.cast<PyInt>().wrappedInt } == listOf(1L, 2L, 3L, 4L))
    }

    @Test
    fun `Test basic set comprehension`() {
        val code = """
            result = {int(x) for x in ("1", "2", "3", "4")}
        """.trimIndent()
        val result = KythonInterpreter.testExec<PySet>(code).unwrap()
        val mapped = result.mapTo(mutableSetOf()) { it.cast<PyInt>().wrappedInt }
        Assertions.assertTrue(mapped == setOf(1L, 2L, 3L, 4L))
    }
}
