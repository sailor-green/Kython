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
package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.pyobject.PyException
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents an error from a function. This is used to handle exception flow for Python code.
 * If this class makes it to the root level, it is considered a real exception and will print the
 * Python stack trace.
 */
class KyError(val wrapped: PyException) : RuntimeException() {
    public val frames: List<StackFrame> get() = wrapped.exceptionFrames

    override fun toString(): String {
        return "KyError: " + this.wrapped.type.name + ": " + this.wrapped.args.subobjects.joinToString { it.getPyStringSafe().wrappedString }
    }
}

/**
 * Throws a PyException as a KyError.
 */
fun PyException.throwKy(): Nothing {
    throw KyError(this)
}
