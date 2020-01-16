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
import green.sailor.kython.interpreter.cast
import green.sailor.kython.interpreter.getAttribute
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction

/**
 * Represents the default object __getattribute__.
 */
object ObjectGetattribute : PyBuiltinFunction("object.__getattribute__") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"]!!
        val name = kwargs["name"].cast<PyString>()
        val attrName = name.wrappedString
        return self.getAttribute(attrName)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "self" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL
        )
    }
}
