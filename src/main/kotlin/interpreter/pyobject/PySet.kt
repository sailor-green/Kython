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
package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.types.PySetType

/**
 * Represents an ordered Python set.
 */
class PySet(val wrappedSet: LinkedHashSet<PyObject>) : PyObject() {

    override fun pyGetStr(): PyString = PyString(
        "{" + wrappedSet.joinToString(", ") { it.pyGetRepr().wrappedString } + "}"
    )

    override fun pyGetRepr(): PyString = pyGetStr()
    override fun pyToBool(): PyBool = PyBool.get(wrappedSet.isNotEmpty())
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PySet) {
            return PyNotImplemented
        }
        return PyBool.get(wrappedSet == other.wrappedSet)
    }

    override var type: PyType
        get() = PySetType
        set(_) = Exceptions.invalidClassSet(this)
}
