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

package green.sailor.kython.interpreter.builtins

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.annotation.Slotted
import green.sailor.kython.generation.generated.dirSlotted
import green.sailor.kython.generation.generated.getattrSlotted
import green.sailor.kython.generation.generated.setattrSlotted
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.module.PyBuiltinModule
import green.sailor.kython.interpreter.toPyObject
import green.sailor.kython.interpreter.util.cast

/**
 * Represents the Kython internal module.
 */
@Slotted("__kython_internal")
object KythonInternalModule : PyBuiltinModule("__kython_internal") {
    override fun pyGetAttribute(name: String): PyObject =
        getattrSlotted(name)

    override fun pySetAttribute(name: String, value: PyObject): PyObject =
        setattrSlotted(name, value)

    override fun pyDir(): PyTuple =
        dirSlotted()

    /** __kython_internal.kotlin_type_name */
    @ExposeMethod("kotlin_type_name")
    @MethodParams(
        MethodParam("thing", "POSITIONAL")
    )
    fun getTypeName(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["thing"].cast<PyObject>()
        val klass = obb::class.java
        return klass.simpleName.toPyObject()
    }
}
