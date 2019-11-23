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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.throwKy

/**
 * Represents a method (a function bound to a self object).
 */
class PyMethod(val function: PyCallable, val instance: PyObject) : PyObject(PyMethodType),
    PyCallable {
    object PyMethodType : PyType("method") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            val function = kwargs["function"] ?: error("Built-in signature mismatch")
            val instance = kwargs["instance"] ?: error("Built-in signature mismatch")

            if (function !is PyCallable) {
                Exceptions.TYPE_ERROR.makeWithMessage("Wasn't passed a callable").throwKy()
            }

            return PyMethod(function, instance)
        }

        override val signature: PyCallableSignature by lazy {
            PyCallableSignature(
                "function" to ArgType.POSITIONAL,
                "instance" to ArgType.POSITIONAL
            )
        }


    }

    override fun runCallable(
        args: List<PyObject>,
        kwargsTuple: PyTuple?
    ): PyObject {
        val realArgs = listOf(instance, *args.toTypedArray())
        return super.runCallable(realArgs, kwargsTuple)
    }

    override fun getFrame(): StackFrame {
        return this.function.getFrame()
    }

    override val signature: PyCallableSignature = this.function.signature

    override fun toPyString(): PyString {
        val builder = StringBuilder()

        builder.append("<method '")
        builder.append((this.function as PyObject).getPyStringSafe().wrappedString)
        builder.append("' of '")
        builder.append(this.instance.getPyStringSafe().wrappedString)
        builder.append("'>")

        return PyString(builder.toString())
    }

    override fun toPyStringRepr(): PyString = this.toPyString()

}
