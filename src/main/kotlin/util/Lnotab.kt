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
 *
 */

package green.sailor.kython.util


/**
 * A wrapper around a co_lnotab used to get line number offsets.
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
class Lnotab(val bytes: ByteArray) {

    /**
     * Gets the line number from a bytecode index.
     */
    fun getLineNumberFromIdx(idx: Int): Int {
        // loosely transliterated from
        // https://github.com/python/cpython/blob/4a2edc34a405150d0b23ecfdcb401e7cf59f4650/Objects/codeobject.c

        val it = this.bytes.iterator()
        var size = this.bytes.size / 2
        var line = 0
        var addr = 0
        while (true) {
            size -= 1
            if (size < 0) break

            addr += it.next()
            if (addr > idx * 2) break
            line += it.next()
        }

        return line
    }

}
