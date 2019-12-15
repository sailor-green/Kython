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
import green.sailor.kython.interpreter.pyobject.types.PyDictType
import green.sailor.kython.interpreter.typeError

/**
 * Represents a Python dict, a mapping between PyObject -> PyObject.
 */
class PyDict(val items: LinkedHashMap<out PyObject, out PyObject>) : PyPrimitive() {
    companion object {
        /**
         * Creates a new PyDict from any map, wrapping primitive types.
         */
        fun fromAnyMap(map: Map<*, *>): PyDict {
            val newMap = map.entries.associateByTo(
                linkedMapOf(), { get(it.key) }, { get(it.value) }
            )

            return PyDict(newMap)
        }
    }

    override fun unwrap(): Any = items

    override fun pyToStr(): PyString {
        val joined = items.entries.joinToString {
            it.key.pyGetRepr().wrappedString + ": " + it.value.pyGetRepr().wrappedString
        }
        return PyString("{$joined}")
    }
    override fun pyGetRepr(): PyString = pyToStr()
    override fun pyToBool(): PyBool = PyBool.get(items.isNotEmpty())
    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyDict) {
            return PyNotImplemented
        }
        return PyBool.get(items == other.items)
    }
    override fun pyGreater(other: PyObject): PyObject = PyNotImplemented
    override fun pyLesser(other: PyObject): PyObject = PyNotImplemented

    override fun pyHash(): PyInt = typeError("lists are not hashable - they are mutable")
    override fun pyLen(): PyInt = PyInt(items.size.toLong())

    override var type: PyType
        get() = PyDictType
        set(_) = Exceptions.invalidClassSet(this)

    /**
     * Gets an item from the internal dict.
     */
    fun getItem(key: PyObject): PyObject? = items[key]
}
