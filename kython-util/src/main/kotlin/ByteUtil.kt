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

@file:JvmName("ByteUtil")
package green.sailor.kython.util

/**
 * Converts a long to a ByteArray,
 */
fun longToBytesBE(long: Long, workingArray: ByteArray? = null): ByteArray {
    var needed = 0
    var x = long
    do {
        x = x shr 8
        needed++
    } while (x != 0L)

    var l = long
    val result = if (workingArray == null) {
        ByteArray(needed)
    } else {
        require(workingArray.size >= needed) { "Working array was too small!" }
        workingArray
    }
    for (i in needed - 1 downTo 0) {
        result[i] = (l and 0xFF).toByte()
        l = l shr 8
    }
    return result
}

/**
 * Converts a long into a ByteArray, in little-endian mode.
 */
fun longToBytesLE(long: Long, workingArray: ByteArray? = null): ByteArray {
    var needed = 0
    var x = long
    do {
        x = x shr 8
        needed++
    } while (x != 0L)

    var l = long
    val result = if (workingArray == null) {
        ByteArray(needed)
    } else {
        require(workingArray.size >= needed) { "Working array was too small!" }
        workingArray
    }
    for (i in 0 until needed) {
        result[i] = (l and 0xFF).toByte()
        l = l shr 8
    }
    return result
}

/**
 * Converts a list of bytes to a long,
 */
fun bytesToLongLE(ba: ByteArray): Long {
    require(ba.size <= 8) { "Cannot convert arrays > 8 bytes to longs" }

    var long = 0L
    for ((idx, byte) in ba.withIndex()) {
        long = long or (byte.toLong() shl (8 * idx))
    }
    return long
}

fun bytesToLongBE(ba: ByteArray): Long {
    require(ba.size <= 8) { "Cannot convert arrays > 8 bytes to longs" }
    var long = 0L
    for ((idx, byte) in ba.reversed().withIndex()) {
        long = long or (byte.toLong() shl (8 * idx))
    }
    return long
}
