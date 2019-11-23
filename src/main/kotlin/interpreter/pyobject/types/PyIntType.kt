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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.throwKy

/**
 * Represents the type of an int.
 */
object PyIntType : PyType("int") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val value = kwargs["value"] ?: error("Built-in signature mismatch")
        return if (value is PyInt) {
            value
        } else {
            // TODO: `__int__`
            if (value !is PyString) {
                val typeName = value.type.name

                Exceptions.TYPE_ERROR
                    .makeWithMessage(
                        "int() argument must be a string, a bytes-like object, " +
                        "or a number, not '$typeName'"
                    ).throwKy()
            } else {
                val pyBase = kwargs["base"] as PyInt
                val base = pyBase.wrappedInt

                val converted = value.wrappedString.toInt(radix = base.toInt())
                PyInt(converted.toLong())
            }
        }
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "value" to ArgType.POSITIONAL,
            "base" to ArgType.POSITIONAL
        ).withDefaults(
            "base" to PyInt(
                10
            )
        )
    }
}
