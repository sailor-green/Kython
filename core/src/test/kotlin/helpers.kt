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

package green.sailor.kython.test

import green.sailor.kython.interpreter.InterpreterThread
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyPrimitive
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import org.junit.jupiter.api.Assertions

/**
 * Helper used for test executions.
 *
 * @param code: The code to run.
 * @param args: Any locals that need to be in the function being ran.
 */
fun KythonInterpreter.testExec(code: String, args: Map<String, PyObject> = mapOf()): PyObject {
    val compiled = cpyInterface.compile(code)
    val fn = PyUserFunction(compiled)
    val module = KyUserModule(fn, "<test>", code.split(System.lineSeparator()))
    val frame = fn.createFrame()
    if (frame !is UserCodeStackFrame) {
        error("Frame isn't a user code frame, not sure what happened")
    }

    val thread = InterpreterThread(frame)
    interpreterThreadLocal.set(thread)
    try {
        thread.runStackFrame(frame, args)
    } finally {
        interpreterThreadLocal.remove()
    }
    return frame.locals["result"] ?: error("No result assigned!")
}

/**
 * Asserts that this PyObject is truthy.
 */
fun assertTrue(result: PyObject) {
    if (result is PyBool) {
        return Assertions.assertTrue(result.wrapped)
    }
    Assertions.fail<Nothing>("Object was $result, not a boolean")
}

/**
 * Asserts that this PyObject is falsey.
 */
fun assertFalse(result: PyObject) {
    if (result is PyBool) {
        return Assertions.assertFalse(result.wrapped)
    }
    // todo
    Assertions.fail<Nothing>("Object was $result, not a boolean")
}

/**
 * Asserts that this PyObject equals a different object.
 */
fun assertUnwrappedTrue(wrapped: PyObject, fn: (Any?) -> Boolean) {
    if (wrapped !is PyPrimitive) return Assertions.fail<Nothing>("Object was not a primitive")
    return Assertions.assertTrue(fn(wrapped.unwrap()))
}

/**
 * Asserts that this PyObject does not equal a different object.
 */
fun assertUnwrappedFalse(wrapped: PyObject, fn: (Any?) -> Boolean) {
    if (wrapped !is PyPrimitive) return Assertions.fail<Nothing>("Object was not a primitive")
    return Assertions.assertFalse(fn(wrapped.unwrap()))
}
