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

package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature

/**
 * Represents a Python string. This wraps a regular JVM string.
 */
class PyString(val wrappedString: String) : PyObject(PyStringType) {
    companion object {
        // some common strings
        val UNPRINTABLE =
            PyString("<unprintable>")
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

        val pyUpper = PyBuiltinFunction.wrap("upper", PyCallableSignature.EMPTY_METHOD) {
            val self = it["self"]!!.cast<PyString>()
            PyString(self.wrappedString.toUpperCase())
        }

        override val initialDict: Map<String, PyObject> by lazy {
            mapOf(
                "upper" to pyUpper
            )
        }

        override fun makeMethodWrappers(instance: PyObject): MutableMap<String, PyMethod> {
            val original = super.makeMethodWrappers(instance)
            original["upper"] = PyMethod(pyUpper, instance)
            return original
        }
    }

    override fun toPyString(): PyString = this
    override fun toPyStringRepr(): PyString =
        PyString("'${this.wrappedString}'")

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
