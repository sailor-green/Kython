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
package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyRootObjectInstance
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents the "root" object singleton, or `object` in Python land.
 *
 * This contains the default implementation of some magic functions
 * (such as `__getattribute__`, `__dir__`, etc) which are shared by all classes (unless overridden).
 */
object PyRootObjectType : PyType("object") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        return PyRootObjectInstance()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY

    // prevents errors
    override val parentTypes: MutableList<PyType> = mutableListOf(this)
}
