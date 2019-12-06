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
package green.sailor.kython.interpreter.pyobject.user

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents a Python user type, i.e. one created from type(name, bases, dict).
 */
class PyUserType(name: String, bases: List<PyType>, dict: Map<String, PyObject>) : PyType(name) {
    /** __bases__ */
    override val bases = bases.toMutableList()

    /** __dict__ */
    override val initialDict: Map<String, PyObject> = dict

    // <type>.__call__(*args, **kwargs)
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        // effectively, object.__new__
        // TODO: `__new__`
        val newObject = PyUserObject(this)
        newObject.pyInit(kwargs)
        return newObject
    }
}
