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

package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyObject

/**
 * Represents the id() builtin function.
 */
class IdBuiltinFunction : PyBuiltinFunction("id") {
    override val signature: PyCallableSignature =
        PyCallableSignature("obb" to ArgType.POSITIONAL)

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["obb"] ?: error("Built-in signature mismatch!")
        return PyInt(System.identityHashCode(obb).toLong())
    }
}