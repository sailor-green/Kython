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
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.types.PyMethodType

/**
 * Represents a method (a function bound to a self object).
 */
class PyMethod(
    val function: PyObject,
    val instance: PyObject
) : PyObject {
    override fun kyIsCallable(): Boolean = true
    override fun kyGetSignature(): PyCallableSignature = function.kyGetSignature()

    override val internalDict: MutableMap<String, PyObject> get() = function.internalDict

    override fun pyCall(args: List<PyObject>, kwargTuple: List<String>): PyObject {
        val realArgs = args.toMutableList().also { it.add(instance) }
        return function.pyCall(realArgs, kwargTuple)
    }

    override fun kyCall(args: List<PyObject>): PyObject {
        return function.kyCall(args.toMutableList().also { it.add(instance) })
    }

    override fun pyToStr(): PyString {
        val output = buildString {
            append("<method '")
            append((function).getPyStringSafe().wrappedString)
            append("' of '")
            append(instance.getPyStringSafe().wrappedString)
            append("'>")
        }

        return PyString(output)
    }
    override fun pyGetRepr(): PyString = pyToStr()
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyMethod) {
            return PyNotImplemented
        }
        return PyBool.get(function == other.function && instance == other.instance)
    }

    override fun pyGreater(other: PyObject): PyObject = PyNotImplemented
    override fun pyLesser(other: PyObject): PyObject = PyNotImplemented

    override var type: PyType
        get() = PyMethodType
        set(_) = Exceptions.invalidClassSet(this)
}
