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
class Lnotab(val bytes: ByteArray) {
    // todo: make this more efficient...
    // this'll do for now.

    /**
     * Gets the line number for the bytecode index.
     */
    fun getLineNumberFromIdx(instructionIdx: Int): Int {
        val realIndex = (instructionIdx - 2) * 2
        val it = this.bytes.iterator()

        // https://svn.python.org/projects/python/branches/pep-0384/Objects/lnotab_notes.txt

        var lineno = 0
        var addr = 0

        while (it.hasNext()) {
            val addrIncr = it.nextByte()
            val lineIncr = it.nextByte()
            addr += addrIncr
            if (addr > realIndex) {
                return lineno
            }
            lineno += lineIncr
        }
        error("Instruction $instructionIdx past end of bytecode")
    }

}
