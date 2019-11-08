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

package green.sailor.kython.interpreter.objects.python

import arrow.core.Either
import green.sailor.kython.interpreter.objects.KyBuiltinFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents a python type (i.e. a class).
 */
abstract class PyType(val name: String) : PyObject(), PyCallable {
    /**
     * Creates a new instance of the object represented by this type.
     *
     * This is the behind the scenes work for `object.__new__(class, *args, **kwargs)`. This is (generally) not called
     * from Kotlin land.
     */
    val builtinFunctionWrapper by lazy {
        object : KyBuiltinFunction(name) {
            override fun callFunction(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject> {
                return newInstance(args, kwargs)
            }
        }
    }

    abstract fun newInstance(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject>

    override fun getFrame(parent: StackFrame): StackFrame {
        return this.builtinFunctionWrapper.getFrame(parent)
    }

    private val _pyString by lazy {
        PyString("<class $name>")
    }

    // default impls
    override fun toPyString(): PyString = this._pyString

    override fun toPyStringRepr(): PyString = this._pyString
}
