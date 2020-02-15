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

package green.sailor.kython.interpreter.pyobject.collection

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.util.cast

/**
 * Represents the base type for a set object.
 */
abstract class PyBaseSetType(name: String) : PyType(name) {
    /** set.copy() */
    @ExposeMethod("copy")
    @MethodParams(
        MethodParam("self", "POSITIONAL")
    )
    open fun pySetCopy(kwargs: Map<String, PyObject>): PySet {
        val self = kwargs["self"].cast<PySet>()
        return self.copy()
    }
}
