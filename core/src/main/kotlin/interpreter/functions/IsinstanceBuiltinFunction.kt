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
import green.sailor.kython.interpreter.isinstance
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType
import green.sailor.kython.interpreter.typeError

class IsinstanceBuiltinFunction : PyBuiltinFunction("isinstance") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["obb"] ?: error("Built-in signature mismatch!")
        val ofType = kwargs["of_type"] ?: error("Built-in signature mismatch!")
        if (ofType == PyRootObjectType) return PyBool.TRUE

        val toCheckSet = when (ofType) {
            is PyType -> setOf(ofType)
            is PyTuple -> ofType.subobjects
                .map { it as? PyType ?: typeError("Tuple must be only types") }
                .toSet()
            else -> typeError("of_type must be a type or a tuple of types")
        }
        return PyBool.get(obb.isinstance(toCheckSet))
    }

    override val signature: PyCallableSignature =
        PyCallableSignature(
            "obb" to ArgType.POSITIONAL,
            "of_type" to ArgType.POSITIONAL
        )
}
