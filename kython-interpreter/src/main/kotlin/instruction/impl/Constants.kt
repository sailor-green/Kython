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

package green.sailor.kython.interpreter.instruction.impl

object FunctionFlags {
    const val POSITIONAL_DEFAULT = 1
    const val KEYWORD_DEFAULT = 2
    const val ANNOTATIONS = 4
    const val FREEVARS = 8
}

object CompareOp {
    const val LESS = 0
    const val LESS_EQUAL = 1
    const val EQUAL = 2
    const val NOT_EQUAL = 3
    const val GREATER = 4
    const val GREATER_EQUAL = 5
    const val CONTAINS = 6
    const val NOT_CONTAINS = 7
    const val IS = 8
    const val IS_NOT = 9
    const val EXCEPTION_MATCH = 10
}
