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

package green.sailor.kython.interpreter.pyobject.exception

import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Interface for exceptions, builtin or user.
 */
interface PyException : PyObject {
    /** The list of exception frames for this exception. */
    val exceptionFrames: List<StackFrame>

    /** The list of arguments for this exception. */
    val args: List<PyObject>

    fun throwKy(): Nothing = throw KyError(this)
}
