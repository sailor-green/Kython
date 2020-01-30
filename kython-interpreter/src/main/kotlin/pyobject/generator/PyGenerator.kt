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
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.toPyObject

/**
 * Implementation of a PyGenerator.
 */
class PyGenerator(val frame: UserCodeStackFrame) : PyObject() {
    override var type: PyType
        get() = PyGeneratorType
        set(value) = Exceptions.invalidClassSet(this)

    /**
     * Sends an object to the generator frame.
     */
    fun send(value: PyObject): PyObject {
        val result = frame.send(value)
        if (frame.state == UserCodeStackFrame.FrameState.RETURNED) {
            Exceptions.STOP_ITERATION.makeException(PyTuple.of(result)).throwKy()
        }
        return result
    }

    override fun pyGetRepr(): PyString {
        val hc = System.identityHashCode(this).toString(16)
        return "<generator object of ${frame.function.name} at 0x$hc>".toPyObject()
    }

    override fun pyToStr(): PyString = pyGetRepr()
}
