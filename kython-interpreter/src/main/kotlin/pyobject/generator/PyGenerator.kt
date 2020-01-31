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

package green.sailor.kython.interpreter.pyobject.generator

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.toPyObject
import green.sailor.kython.interpreter.typeError
import org.apiguardian.api.API

/**
 * Implementation of a generator.
 *
 * This class is *also* used for coroutines (as Python coroutines are just generators),
 * but with some additional checks.
 */
class PyGenerator(val frame: UserCodeStackFrame) : PyObject() {
    override var type: PyType
        get() = PyGeneratorType
        set(value) = Exceptions.invalidClassSet(this)

    // iter(gen) == gen
    override fun pyIter(): PyObject =
        if (frame.function.code.flags.CO_ASYNC_FUNCTION) {
            typeError("Coroutine functions are not iterable")
        } else {
            this
        }

    override fun pyNext(): PyObject =
        if (frame.function.code.flags.CO_ASYNC_FUNCTION) {
            typeError("Coroutine functions are not iterable")
        } else {
            send(PyNone)
        }

    /**
     * Sends directly to the underlying stack frame.
     */
    @API(status = API.Status.INTERNAL)
    fun sendRaw(value: PyObject): Pair<StackFrame.FrameState, PyObject> = frame.send(value)

    /**
     * Sends an object to the generator frame.
     */
    fun send(value: PyObject): PyObject {
        val (state, result) = frame.send(value)
        if (state == StackFrame.FrameState.RETURNED) {
            Exceptions.STOP_ITERATION.makeException(PyTuple.of(result)).throwKy()
        }
        return result
    }

    override fun pyGetRepr(): PyString {
        val hc = System.identityHashCode(this).toString(16)
        val name = if (frame.function.code.flags.CO_ASYNC_FUNCTION) {
            "coroutine"
        } else {
            "generator"
        }

        return "<$name object of ${frame.function.name} at 0x$hc>".toPyObject()
    }

    override fun pyToStr(): PyString = pyGetRepr()
}
