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
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Wraps a user module object.
 */
class PyUserModule(
    val userModule: KyUserModule,
    name: String
) : PyModule(name) {
    object PyUserModuleType : PyType("user_module") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            TODO("not implemented")
        }
    }

    override fun pyToStr(): PyString {
        return PyString("<module '$name' from '${userModule.filename}'>")
    }
    override fun pyGetRepr(): PyString = pyToStr()

    override var type: PyType
        get() = PyUserModuleType
        set(_) = Exceptions.invalidClassSet(this)

    override val internalDict: LinkedHashMap<String, PyObject>
        get() = userModule.attribs
}
