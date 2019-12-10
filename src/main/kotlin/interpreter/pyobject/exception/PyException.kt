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

package green.sailor.kython.interpreter.pyobject.exception

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents an Exception instance.
 */
open class PyException(private val excType: PyExceptionType, val args: PyTuple) : PyObject() {
    /**
     * The list of exception frames this stack frame has travelled down.
     */
    val exceptionFrames: List<StackFrame> =
        StackFrame.flatten(KythonInterpreter.getRootFrameForThisThread())

    override val internalDict: LinkedHashMap<String, PyObject> =
        super.internalDict.apply { put("args", args) }

    override var type: PyType
        get() = excType
        set(_) = Exceptions.invalidClassSet(this)
}

fun PyException.throwKy(): Nothing = throw KyError(this)
