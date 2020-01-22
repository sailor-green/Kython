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

package green.sailor.kython.interpreter.pyobject.user

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.issubclass
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.exception.PyException
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.typeError


class PyUserException(type: PyUserType) : PyUserObject(type), PyException {
    override val exceptionFrames: List<StackFrame> =
        StackFrame.flatten(KythonInterpreter.getRootFrameForThisThread())

    override val args: List<PyObject> = listOf()

    init {
        if (!type.issubclass(Exceptions.BASE_EXCEPTION)) {
            typeError("Attempted to instantiate an exception type that isn't an exception!")
        }
    }
}
