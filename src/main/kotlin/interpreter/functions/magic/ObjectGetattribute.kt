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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.throwKy

/**
 * Represents the default object __getattribute__.
 */
object ObjectGetattribute : PyBuiltinFunction("object.__getattribute__") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"]!!
        val name = kwargs["name"]?.cast<PyString>() ?: error("Built-in signature mismatch!")
        val attrName = name.wrappedString

        // try and find the object on the dict
        if (attrName in self.internalDict) {
            return self.internalDict[attrName]!!
        }

        // try and find it on the MRO
        val mro = if (self is PyType) self.mro else self.type.mro
        val descriptorSelf = if (self is PyType) PyNone else self
        mro.mapNotNull {
            it.internalDict[attrName]
        }.firstOrNull()?.let { return it.pyDescriptorGet(descriptorSelf, self.type) }

        // can't find it
        Exceptions.ATTRIBUTE_ERROR("Object ${self.type.name} has no attribute $attrName").throwKy()
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "self" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL
        )
    }
}
