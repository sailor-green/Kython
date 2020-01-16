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

package green.sailor.kython.interpreter.pyobject.function

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.stack.BuiltinStackFrame
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.typeError

/**
 * Represents a built-in function, such as print().
 */
abstract class PyBuiltinFunction(val name: String) : PyFunction() {
    companion object {

        /**
         * Makes a wrapper around a regular function.
         */
        fun wrap(
            name: String,
            signature: PyCallableSignature,
            fn: (Map<String, PyObject>) -> PyObject
        ): PyBuiltinFunction {
            return object : PyBuiltinFunction(name) {
                override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
                    return fn(kwargs)
                }

                override val signature: PyCallableSignature = signature

                override val kotlinClassName: String = fn.javaClass.name.split(".").last()
            }
        }
    }

    object PyBuiltinFunctionType : PyType("BuiltinType") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            typeError("Cannot create builtin instances")
        }
    }

    open val kotlinClassName: String = javaClass.name.split(".").last()

    override fun pyToStr(): PyString = PyString("<built-in function $name>")
    override fun pyGetRepr(): PyString = pyToStr()
    override fun createFrame(): StackFrame = BuiltinStackFrame(this)

    /**
     * Called when the function is called from within a stack frame.
     */
    abstract fun callFunction(kwargs: Map<String, PyObject>): PyObject

    override var type: PyType
        get() = PyBuiltinFunctionType
        set(_) = Exceptions.invalidClassSet(this)
}
