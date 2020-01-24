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

package green.sailor.kython.interpreter.functions.magic

import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.setAttribute
import green.sailor.kython.interpreter.util.cast

/**
 * The default implementation of `__setattr__`.
 */
object ObjectSetattribute : DefaultBuiltinFunction("object.__getattribute__") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in signature mismatch!")
        val name = kwargs["name"].cast<PyString>()
        val value = kwargs["value"] ?: error("Built-in signature mismatch!")
        return self.setAttribute(name.wrappedString, value)
    }

    override val signature: PyCallableSignature =
        PyCallableSignature(
            "self" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL,
            "value" to ArgType.POSITIONAL
        )
}
