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

package green.sailor.kython.interpreter.objects

import arrow.core.Either
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.*
import green.sailor.kython.interpreter.stack.BuiltinStackFrame
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents a built-in function, such as print().
 */
abstract class KyBuiltinFunction(val name: String) : PyObject(), PyCallable {
    override fun toPyString(): PyString = PyString("<built-in function $name>")
    override fun toPyStringRepr(): PyString = toPyString()

    /**
     * Called when the function is called from within a stack frame.
     */
    abstract fun callFunction(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject>

    override fun getFrame(parentFrame: StackFrame): StackFrame =
        BuiltinStackFrame(this).apply { this.parentFrame = parentFrame }
}
