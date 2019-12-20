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

package green.sailor.kython.test

import green.sailor.kython.util.longToBytesBE
import green.sailor.kython.util.longToBytesLE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestByteUtil {
    @Test
    fun `Test long to bytes, big endian`() {
        val long = 0x123456L
        val expected = byteArrayOf(18, 52, 86)
        val toBytes = longToBytesBE(long)
        assertEquals(toBytes.size, 3)
        assertTrue(toBytes.contentEquals(expected))
    }

    @Test
    fun `Test long to bytes, little endian`() {
        val long = 0x123456L
        val expected = byteArrayOf(86, 52, 18)
        val toBytes = longToBytesLE(long)
        assertEquals(toBytes.size, 3)
        assertTrue(toBytes.contentEquals(expected))
    }
}
