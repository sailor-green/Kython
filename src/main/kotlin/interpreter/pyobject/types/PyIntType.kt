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
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.throwKy

/**
 * Represents the type of an int.
 */
object PyIntType : PyType("int") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val value = kwargs["value"] ?: error("Built-in signature mismatch")
        when (value) {
            // MUST COME FIRST
            // cpython: int(True) == 1, type(int(True)) == int
            //
            // since PyBool *is* a PyInt, normally, this would pass through to the PyInt and return
            // the value directly.
            // but instead, we explicitly check for it, then re-wrap it.
            // i would like to not have to re-wrap it, but im not sure how that would work...
            is PyBool -> {
                return PyInt(value.wrappedInt)
            }
            is PyInt -> {
                return value
            }
            is PyString -> { // special case, for int(x, base)
                val base = kwargs["base"]!!.cast<PyInt>()
                try {
                    return PyInt(value.wrappedString.toInt(base.wrappedInt.toInt()).toLong())
                } catch (e: NumberFormatException) {
                    Exceptions.VALUE_ERROR(
                        "Cannot convert '${value.wrappedString}' to int " +
                            "with base ${base.wrappedInt}"
                    ).throwKy()
                }
            }
            else -> {
                TODO()
                // return value.pyToInt()
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
