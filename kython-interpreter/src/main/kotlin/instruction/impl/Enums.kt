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

/**
 * Load pools for LOAD/STORE instructions.
 * These represent where the instruction will operate on.
 */
enum class LoadPool {
    CONST,
    FAST,
    NAME,
    ATTR,
    METHOD,
    GLOBAL
}

/**
 * Build types for BUILD_ instructions.
 */
enum class BuildType {
    TUPLE,
    DICT,
    LIST,
    SET,
    STRING,
}

/**
 * Enumeration for the list of binary operators.
 */
enum class BinaryOp {
    ADD,
    POWER,
    MULTIPLY,
    MATRIX_MULTIPLY,
    FLOOR_DIVIDE,
    TRUE_DIVIDE,
    MODULO,
    SUBTRACT,
    SUBSCR,
    LSHIFT,
    RSHIFT,
    AND,
    XOR,
    OR,
    STORE_SUBSCR,
    DELETE_SUBSCR
}

/**
 * Enumeration for the list of unary operators.
 */
enum class UnaryOp {
    /** +thing */
    POSITIVE,
    /** -thing */
    NEGATIVE,
    /** ~thing */
    INVERT,
    /** not thing */
    NOT
}
