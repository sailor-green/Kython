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

package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyProperty
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast

/**
 * Type for property objects.
 */
@GenerateMethods
object PyPropertyType : PyType("property") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val getter = kwargs["getter"] ?: error("Built-in signature mismatch!")
        if (!getter.kyIsCallable()) {
            typeError("'${getter.type.name}' is not callable")
        }

        return PyProperty(getter)
    }

    override val signature = PyCallableSignature(
        "getter" to ArgType.POSITIONAL,
        "setter" to ArgType.POSITIONAL,
        "deleter" to ArgType.POSITIONAL,
        "doc" to ArgType.POSITIONAL
    ).withDefaults(
        "setter" to PyNone,
        "deleter" to PyNone,
        "doc" to PyNone
    )


    @ExposeMethod("setter")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("setter", "POSITIONAL")
    )
    fun pyPropertySetter(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyProperty>()
        val setter = kwargs["setter"] ?: error("Built-in signature mismatch!")
        if (!setter.kyIsCallable()) {
            typeError("'${setter.type.name}' is not callable")
        }

        // Weird detail: Property objects are immutable.
        // That means setting a setter actually makes a *new* object, and replaces the old
        // property with one with a setter.
        // It's also why the setter needs to be the same name.
        val new = PyProperty(self.fget)
        new.fset = setter
        return new
    }
}
