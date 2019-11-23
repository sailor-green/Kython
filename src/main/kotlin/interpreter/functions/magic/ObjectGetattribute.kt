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

package green.sailor.kython.interpreter.functions.magic

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.primitives.PyString
import green.sailor.kython.interpreter.throwKy

/**
 * Represents the default object __getattribute__.
 */
object ObjectGetattribute : PyBuiltinFunction("<object.__getattribute__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"]!!
        val name = kwargs["name"]!!
        if (name !is PyString) {
            Exceptions.TYPE_ERROR
                .makeWithMessage("Attribute name must be type str, not ${name.type.name}")
                .throwKy()
        }
        val attrName = name.wrappedString

        // try and find the object on the dict
        if (attrName in self.internalDict) {
            return self.internalDict[attrName]!!
        }

        // if it's in the class dict...
        if (attrName in self.type.internalDict) {
            return self.type.internalDict[attrName]!!
        }

        // try and load `__getattr__`
        val getattrFn = self.specialMethodLookup("__getattr__")
        if (getattrFn != null) {
            if (getattrFn !is PyCallable) {
                Exceptions.TYPE_ERROR.makeWithMessage("__getattr__ is not a callable").throwKy()
            }
            return getattrFn.runCallable(listOf(name))
        }

        Exceptions.NAME_ERROR.makeWithMessage("Object has no attribute $attrName").throwKy()
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "self" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL
        )
    }
}
