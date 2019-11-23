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

package green.sailor.kython.interpreter.pyobject.primitives

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents an ordered Python set.
 */
class PySet(val wrappedSet: LinkedHashSet<PyObject>) : PyObject(PySetType) {
    object PySetType : PyType("set") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            TODO("not implemented")
        }
    }

    override fun toPyString(): PyString = PyString(
        this.wrappedSet.joinToString(", ") { it.toPyStringRepr().wrappedString }
    )
    override fun toPyStringRepr(): PyString = toPyString()
}
