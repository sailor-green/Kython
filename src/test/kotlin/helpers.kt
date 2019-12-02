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
package green.sailor.kython.test

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * Helper used for test executions.
 *
 * @param code: The code to run.
 * @param args: Any locals that need to be in the function being ran.
 */
fun KythonInterpreter.testExec(code: String, args: Map<String, PyObject> = mapOf()): PyObject {
    val compiled = cpyInterface.compile(code)
    val fn = PyUserFunction(compiled)
    val frame = fn.createFrame()
    if (frame !is UserCodeStackFrame) {
        error("Frame isn't a user code frame, not sure what happened")
    }

    // these functions won't return anything, they're exec()
    // instead a `result` should be assigned to
    runStackFrame(frame, args)
    return frame.locals["result"] ?: error("No result assigned!")
}
