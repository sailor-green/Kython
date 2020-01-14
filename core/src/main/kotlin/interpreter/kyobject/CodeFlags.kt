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
inline class CodeFlags(val flags: Int) {
    val CO_OPTIMISED get() = (flags and 1) != 0

    /** Marks if this function will get a new dict for f_locals. */
    val CO_NEWLOCALS get() = (flags and 2) != 0
    /** Marks if this function has a *args argument. */
    val CO_HAS_VARARGS get() = (flags and 4) != 0
    /** Marks if this function has a **kwargs argument */
    val CO_HAS_VARKWARGS get() = (flags and 8) != 0
    /** Marks if this function is nested. */
    val CO_NESTED get() = (flags and 16) != 0
    /** Marks if this function is a generator. */
    val CO_GENERATOR get() = (flags and 32) != 0
    /** Marks if this function has no free/cell variables. */
    val CO_NOFREE get() = (flags and 64) != 0
    /** Marks if this function is an async function. */
    val CO_ASYNC_FUNCTION get() = (flags and 128) != 0
    // unsure
    val CO_ITERABLE_ASYNC_FUNCTION get() = (flags and 256) != 0
    /** Marks if this function is an async generator. */
    val CO_ASYNC_GENERATOR get() = (flags and 512)
}

infix fun CodeFlags.and(p: Int) = flags and p
infix fun CodeFlags.or(p: Int) = flags or p
