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

import green.sailor.kython.interpreter.objects.Exceptions
import green.sailor.kython.interpreter.objects.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.primitives.PyString
import green.sailor.kython.interpreter.objects.python.primitives.PyTuple
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.throwKy
import interpreter.objects.iface.ArgType

/**
 * Represents a python type (i.e. a class).
 */
abstract class PyType(val name: String) : PyObject(), PyCallable {
    /**
     * Represents the root type. If the type of a PyObject is not set, this will be useed.
     */
    object PyRootType : PyType("type") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            // one-arg form
            val args = kwargs["args"] as PyTuple

            if (args.subobjects.size == 1) {
                return args.subobjects.first().type
            }

            // TODO: Three arg type version
            Exceptions.NOT_IMPLEMENTED_ERROR
                .makeWithMessage("Three-arg form of type not impl'd yet")
                .throwKy()
        }

        // root type doesn't make method wrappers because we have no type
        override val internalDict: LinkedHashMap<String, PyObject> by lazy {
            val map = linkedMapOf<String, PyObject>().apply { putAll(getDefaultDict()) }
            map
        }

        override fun makeMethodWrappers(instance: PyObject): MutableMap<String, PyMethod> {
            return mutableMapOf()
        }
    }
    /**
     * Creates a new instance of the object represented by this type.
     *
     * This is the behind the scenes work for `object.__new__(class, *args, **kwargs)`. This is (generally) not called
     * from Kotlin land.
     */
    val builtinFunctionWrapper by lazy {
        object : PyBuiltinFunction(name) {
            override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
                return newInstance(kwargs)
            }

            override val signature: PyCallableSignature by lazy {
                PyCallableSignature(
                    "args" to ArgType.POSITIONAL_STAR,
                    "kwargs" to ArgType.KEYWORD_STAR
                )
            }
        }
    }

    /**
     * Makes method wrappers for this type. These will be automatically copied into the object dict.
     */
    open fun makeMethodWrappers(instance: PyObject): MutableMap<String, PyMethod> {
        return PyRootType.makeMethodWrappers(instance)
    }

    /**
     * Makes a new instance of this builtin.
     *
     * @param kwargs: The arguments that were called for this object.
     */
    abstract fun newInstance(kwargs: Map<String, PyObject>): PyObject

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "args" to ArgType.POSITIONAL_STAR,
            "kwargs" to ArgType.KEYWORD_STAR
        )
    }

    override fun getFrame(): StackFrame {
        return this.builtinFunctionWrapper.getFrame()
    }

    private val _pyString by lazy {
        PyString("<class $name>")
    }

    // default impls
    override fun toPyString(): PyString = this._pyString
    override fun toPyStringRepr(): PyString = this._pyString
}
