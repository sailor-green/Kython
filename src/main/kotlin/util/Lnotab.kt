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

    init {
        buildRanges()
    }

    // maps bytecode idx to line number
    val ranges = mutableListOf<Pair<IntRange, Int>>()

    /**
     * Builds the lnotab ranges.
     */
    fun buildRanges() {
        val it = this.bytes.iterator()
        val tempMap = mutableListOf(Pair(0, 0))

        // https://svn.python.org/projects/python/branches/pep-0384/Objects/lnotab_notes.txt
        // rough translation of dis.findlinestarts from Python 3.7.4

        var lineno = 0
        var addr = 0
        var lastLineNo = 0

        while (it.hasNext()) {
            val addrIncr = it.nextByte().toUByte()
            var lineIncr = it.nextByte()
            if (addrIncr != 0u.toUByte()) {
                if (lineno != lastLineNo) {
                    tempMap.add(Pair(addr, lineno))
                    lastLineNo = lineno
                }
            }
            addr = (addr + addrIncr.toInt())

            if (lineIncr > 0x80) {
                lineIncr = (lineIncr - 0x100.toByte()).toByte()
            }
            lineno += lineIncr
        }
        if (lineno != lastLineNo) {
            tempMap.add(Pair(addr, lineno))
        }

        tempMap.reduce { first, second ->
            this.ranges.add(Pair(IntRange(first.first, second.first - 1), first.second))
            second
        }
    }

    /**
     * Gets the line number from the bytecode index.
     */
    fun getLineNumberFromIdx(idx: Int): Int {
        val realIdx = idx * 2
        for ((range, line) in this.ranges) {
            if (realIdx in range) {
                return line
            }
        }
        return 0
    }

}
