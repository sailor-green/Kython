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

package green.sailor.kython.test.helpers

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.thread.MainInterpreterThread

/**
 * Helper used for test executions.
 *
 * @param code: The code to run.
 * @param args: Any locals that need to be in the function being ran.
 */
fun KythonInterpreter.testExecInternal(code: String): PyObject {
    val compiled = cpyInterface
        .compile(code)
    val fn = PyUserFunction(compiled)
    val module = KyUserModule(fn, "<test>", code.split(System.lineSeparator()))
    val frame = fn.createFrame()
    if (frame !is UserCodeStackFrame) {
        error("Frame isn't a user code frame, not sure what happened")
    }

    val thread =
        MainInterpreterThread(frame)
    interpreterThreadLocal.set(thread)
    try {
        thread.runStackFrame(frame, mapOf())
    } finally {
        interpreterThreadLocal.remove()
    }
    return frame.locals["result"] ?: error("No result assigned!")
}

/**
 * Executes test code, executing it.
 */
fun <T : PyObject> KythonInterpreter.testExec(
    code: String
) = testExecInternal(code) as T

fun <T : PyObject> String.runPy() = KythonInterpreter.testExec<T>(this)
