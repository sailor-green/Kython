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

package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.objects.python.PyNone
import green.sailor.kython.interpreter.objects.python.PyObject

/**
 * Represents a result of an instruction or stack frame.
 *
 * This can be one of three types:
 *  - An InterpreterResultReturn, which indicates a value was returned from the *frame*,
 *  - An InterpreterResultError, which should be tagged and passed down the frames until someone catches it or it errors.
 *  - An InterpreterResultNoAction, which indicates nothing happened (used for instructions).
 */
sealed class InterpreterResult

class InterpreterResultError(val exception: PyObject) : InterpreterResult()


class InterpreterResultReturn(val result: PyObject) : InterpreterResult() {
    /** Represents a None return. Used for builtins. */
    companion object {
        val NONE_RETURN = InterpreterResultReturn(PyNone)
    }
}

object InterpreterResultNoAction : InterpreterResult()
