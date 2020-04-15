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

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyPrimitive
import green.sailor.kython.interpreter.pyobject.iterators.PyBuiltinIterator
import green.sailor.kython.interpreter.pyobject.iterators.PyEmptyIterator
import green.sailor.kython.interpreter.pyobject.numeric.PyBool
import green.sailor.kython.interpreter.pyobject.numeric.PyInt

/**
 * Superclass for basic collections. Contains base overrides common to all collection types.
 */
abstract class PyCollection(val subobjects: Collection<PyObject>) : PyPrimitive() {
    override fun pyToBool(): PyBool = PyBool.get(subobjects.isNotEmpty())

    override fun pyIter(): PyObject {
        if (subobjects.isEmpty()) return PyEmptyIterator
        return PyBuiltinIterator(subobjects.iterator())
    }

    override fun pyContains(other: PyObject): PyObject {
        return PyBool.get(other in subobjects)
    }

    override fun pyLengthHint(): PyInt =
        PyInt(subobjects.size.toLong())
    override fun pyLen(): PyInt =
        PyInt(subobjects.size.toLong())
}
