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

package green.sailor.kython.test.pyobject

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.test.helpers.testExec
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class `Test bool` {
    @Test
    fun `Test boolean int() call`() {
        val result = KythonInterpreter.testExec<PyInt>("""
            result = int(True)
        """.trimIndent())
        assertTrue(result.wrappedInt == 1L)
    }
}
