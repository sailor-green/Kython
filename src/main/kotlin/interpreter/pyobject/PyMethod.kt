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

import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.types.PyMethodType
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents a method (a function bound to a self object).
 */
class PyMethod(
    val function: PyCallable,
    val instance: PyObject
) : PyObject(PyMethodType), PyCallable {

    override fun runCallable(
        args: List<PyObject>,
        kwargsTuple: PyTuple?
    ): PyObject {
        val realArgs = args.toMutableList().apply { add(instance) }
        return super.runCallable(realArgs, kwargsTuple)
    }

    override fun getFrame(): StackFrame {
        return this.function.getFrame()
    }

    override val signature: PyCallableSignature = this.function.signature

    override fun getPyStr(): PyString {
        val builder = StringBuilder()

        builder.append("<method '")
        builder.append((this.function as PyObject).getPyStringSafe().wrappedString)
        builder.append("' of '")
        builder.append(this.instance.getPyStringSafe().wrappedString)
        builder.append("'>")

        return PyString(builder.toString())
    }

    override fun getPyRepr(): PyString = this.getPyStr()
}
