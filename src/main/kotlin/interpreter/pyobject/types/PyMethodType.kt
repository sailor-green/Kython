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
package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyMethod
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.typeError

/**
 * Represents the type of a method (types.MethodType).
 */
object PyMethodType : PyType("method") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val function = kwargs["function"] ?: error("Built-in signature mismatch")
        val instance = kwargs["instance"] ?: error("Built-in signature mismatch")

        if (!function.kyIsCallable()) {
            typeError("Method first param must be a callable")
        }

        // TODO
        return PyMethod(function as PyCallable, instance)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "function" to ArgType.POSITIONAL,
            "instance" to ArgType.POSITIONAL
        )
    }
}
