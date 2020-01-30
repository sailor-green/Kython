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

package green.sailor.kython.interpreter.pyobject.generator

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast

/**
 * The type object for generators.
 */
@GenerateMethods
object PyGeneratorType : PyType("generator") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        typeError("Cannot create new instances of generators")
    }

    /** gen.send() */
    @ExposeMethod("send")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("value", "POSITIONAL")
    )
    fun pyGeneratorSend(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyGenerator>()
        val value = kwargs["value"] ?: error("Built-in signature mismatch!")
        return self.send(value)
    }
}
