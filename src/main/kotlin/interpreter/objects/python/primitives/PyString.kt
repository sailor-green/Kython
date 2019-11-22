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

package green.sailor.kython.interpreter.objects.python.primitives

import green.sailor.kython.interpreter.objects.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.PyMethod
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.PyType
import interpreter.objects.iface.ArgType

/**
 * Represents a Python string. This wraps a regular JVM string.
 */
class PyString(val wrappedString: String) : PyObject(PyStringType) {
    companion object {
        // some common strings
        val UNPRINTABLE = PyString("<unprintable>")
    }

    object PyStringType : PyType("str") {
        override fun newInstance(args: Map<String, PyObject>): PyObject {
            val arg = args["x"]!!
            return arg.toPyString()
        }

        override val signature: PyCallableSignature by lazy {
            PyCallableSignature(
                "x" to ArgType.POSITIONAL
            )
        }

        override val initialDict: Map<String, PyObject> by lazy {
            mapOf(
                "upper" to StringUpper
            )
        }

        override fun makeMethodWrappers(instance: PyObject): MutableMap<String, PyMethod> {
            val original = super.makeMethodWrappers(instance)
            original["upper"] = PyMethod(StringUpper, instance)
            return original
        }
    }

    object StringUpper : PyBuiltinFunction("str.upper") {
        override val signature = PyCallableSignature.EMPTY_METHOD
        override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
            val self = kwargs["self"]!! as PyString
            return PyString(self.wrappedString.toUpperCase())
        }
    }

    override fun toPyString(): PyString = this
    override fun toPyStringRepr(): PyString = PyString("'${this.wrappedString}'")

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PyString) {
            return false
        }

        return this.wrappedString == other.wrappedString
    }

    override fun hashCode(): Int {
        return wrappedString.hashCode()
    }
}
