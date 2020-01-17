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
import green.sailor.kython.test.helpers.*
import org.junit.jupiter.api.Test

class `Test contains` {
    @Test
    fun `Test tuple contains`() {
        assertTrue("result = 2 in (1, 2, 3)".runPy())
        assertFalse("result = 2 in (4, 5, 6)".runPy())
        // not is a simple inversion, so it will work every time contains works
        // so its only tested once (here)
        assertTrue("result = 2 not in (4, 5, 6)".runPy())
    }

    @Test
    fun `Test list contains`() {
        assertTrue("result = 2 in [1, 2, 3]".runPy())
        assertFalse("result = 2 in [4, 5, 6]".runPy())
    }

    @Test
    fun `Test set contains`() {
        assertTrue("result = 2 in {1, 2, 3}".runPy())
        assertFalse("result = 2 in {4, 5, 6}".runPy())
    }

    @Test
    fun `Test dict contains`() {
        assertTrue("result = 2 in {1: 1, 2: 2, 3: 3}".runPy())
        assertFalse("result = 2 in {4: 4, 5: 5, 6: 6}".runPy())
    }

    @Test
    fun `Test string contains`() {
        assertTrue("result = 'abc' in 'abcdef'".runPy())
        assertFalse("result = 'bad' in 'hatsune miku'".runPy())
    }
}
