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

package green.sailor.kython.interpreter.pyobject.module

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.typeError


abstract class PyBuiltinModule(name: String) : PyModule(name) {
    object BuiltinModuleType : PyType("builtin_module") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject =
            typeError("Cannot make new instances of builtin modules.")
    }

    override var type: PyType
        get() = BuiltinModuleType
        set(_) = Exceptions.invalidClassSet(this)
}
