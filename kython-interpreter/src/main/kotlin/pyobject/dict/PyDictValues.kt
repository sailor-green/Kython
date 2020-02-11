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

package green.sailor.kython.interpreter.pyobject.dict

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.collection.PyCollection
import green.sailor.kython.interpreter.toPyObject
import green.sailor.kython.interpreter.typeError

/**
 * The class for dict values.
 */
class PyDictValues(map: MutableMap<PyObject, PyObject>) : PyCollection(map.values) {
    object PyDictValuesType : PyType("dict_values") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            typeError("Cannot create new instances of this class")
        }
    }

    override fun unwrap(): Collection<PyObject> = subobjects
    override fun pyToStr(): PyString {
        return buildString {
            append("dict_values([")
            subobjects.forEach { append(it.pyToStr().wrappedString); append(", ") }
            append("])")
        }.toPyObject()
    }
    override fun pyGetRepr(): PyString = pyToStr()

    override var type: PyType
        get() = PyDictValuesType
        set(_) = Exceptions.invalidClassSet(this)
}
