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

package green.sailor.kython.interpreter.kyobject

/**
 * Type safe wrapper class over code object flags.
 */
@Suppress("PropertyName", "unused")
inline class CodeFlags(val flags: Int) {
    inline val CO_OPTIMISED get() = (flags and 1) != 0

    /** Marks if this function will get a new dict for f_locals. */
    inline val CO_NEWLOCALS get() = (flags and 2) != 0
    /** Marks if this function has a *args argument. */
    inline val CO_HAS_VARARGS get() = (flags and 4) != 0
    /** Marks if this function has a **kwargs argument */
    inline val CO_HAS_VARKWARGS get() = (flags and 8) != 0
    /** Marks if this function is nested. */
    inline val CO_NESTED get() = (flags and 16) != 0
    /** Marks if this function is a generator. */
    inline val CO_GENERATOR get() = (flags and 32) != 0
    /** Marks if this function has no free/cell variables. */
    inline val CO_NO_FREE_VARS get() = (flags and 64) != 0
    /** Marks if this function is an async function. */
    inline val CO_ASYNC_FUNCTION get() = (flags and 128) != 0
    // unsure
    inline val CO_ITERABLE_ASYNC_FUNCTION get() = (flags and 256) != 0
    /** Marks if this function is an async generator. */
    inline val CO_ASYNC_GENERATOR get() = (flags and 512) != 0

    /**
     * If this function is some sort of generator.
     */
    inline val isGenerator get() = (
        CO_GENERATOR ||
        CO_ASYNC_FUNCTION ||
        CO_ITERABLE_ASYNC_FUNCTION ||
        CO_ASYNC_GENERATOR
    )
}

infix fun CodeFlags.and(p: Int) = flags and p
infix fun CodeFlags.or(p: Int) = flags or p
