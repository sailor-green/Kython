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

package green.sailor.kython.util

/**
 * Returns a centered [String] with a padding of [width] using [padWith]
 * as padding character.
 */
fun String.center(width: Long, padWith: Char): String {
    if (isEmpty()) return this
    val string = this

    return buildString {
        for (i in 0 until (width - string.length) / 2) {
            append(padWith)
        }
        // Append our string
        append(string)
        while (length < width) {
            append(padWith)
        }
    }
}

private val numerics = setOf(
    CharCategory.DECIMAL_DIGIT_NUMBER,
    CharCategory.LETTER_NUMBER,
    CharCategory.OTHER_NUMBER
)

// Funcs for pyStrIsAlphanumeric and core stdlib funcs.

fun isAlpha(string: String) = string.isNotEmpty() && string.all { it.isLetter() }
fun isAscii(string: String) = string.chars().allMatch { it in (0..0x7F) }
fun isNumeric(string: String) = string.isNotEmpty() && string.all { it.category in numerics }
fun isDecimal(string: String) =
    string.isNotEmpty() && string.all { it.category == CharCategory.DECIMAL_DIGIT_NUMBER }

// Optimised to avoid calling isNotEmpty() multiple times.
fun isAlnum(string: String): Boolean = string.run {
    (all { it.isLetter() } || all { it.category in numerics } || isDigit(this)) &&
        isNotEmpty()
}

// Java uses surrogates due to its UTF-16 encoding, we need to manually iterate over code-points.
fun isDigit(string: String) = string.isNotEmpty() && string.codePoints()
    .toArray()
    .map { CharCategory.valueOf(Character.getType(it)) }
    .all { it in numerics - CharCategory.LETTER_NUMBER }
