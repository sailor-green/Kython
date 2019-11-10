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

package green.sailor.kython.interpreter.objects.functions.magic

import arrow.core.Either
import green.sailor.kython.interpreter.objects.Exceptions
import green.sailor.kython.interpreter.objects.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.primitives.PyString
import interpreter.objects.iface.ArgType

/**
 * Represents the default object __getattribute__.
 */
object ObjectGetattribute : PyBuiltinFunction("<object __getattribute__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): Either<PyException, PyObject> {
        val self = kwargs["self"]!!
        val name = kwargs["name"]!!
        if (name !is PyString) {
            return Exceptions.TYPE_ERROR.makeWithMessageLeft("Attribute name must be string")
        }
        val attrName = name.wrappedString

        // try and find the object on the dict
        if (attrName in self.internalDict) {
            return Either.right(self.internalDict[attrName]!!)
        }

        // if it's in the class dict...
        if (attrName in self.type.internalDict) {
            return Either.right(self.type.internalDict[attrName]!!)
        }

        // try and load `__getattr__`
        val getattrFn = self.specialMethodLookup("__getattr__")
        if (getattrFn != null) {
            if (getattrFn !is PyCallable) {
                return Exceptions.TYPE_ERROR.makeWithMessageLeft("__getattr__ is not a callable")
            }
            return getattrFn.runCallable(listOf(name))
        }

        // can't load
        return Exceptions.NAME_ERROR.makeWithMessageLeft("Object has no attribute $attrName")

    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "self" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL
        )
    }
}
