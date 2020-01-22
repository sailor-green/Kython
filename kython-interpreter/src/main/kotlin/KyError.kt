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

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.exception.BuiltinPyException
import green.sailor.kython.interpreter.pyobject.exception.PyException
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents an error from a function. This is used to handle exception flow for Python code.
 * If this class makes it to the root level, it is considered a real exception and will print the
 * Python stack trace.
 */
class KyError(val wrapped: PyException) : RuntimeException() {
    val frames: List<StackFrame> get() = wrapped.exceptionFrames

    override fun toString(): String {
        if (wrapped !is PyObject) error("Exception must be a PyObject!!!!")
        return buildString {
            append("KyError: ")
            append(wrapped.type.name)
            append(": ")
            append(wrapped.args.joinToString { it.getPyStringSafe().wrappedString })
        }
    }
}

/**
 * Throws a PyException as a KyError.
 */
fun BuiltinPyException.throwKy(): Nothing = throw KyError(this)
