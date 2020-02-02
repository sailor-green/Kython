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
import green.sailor.kython.interpreter.keyError
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.PyObjectMap

/**
 * Represents a Python dict, a mapping between PyObject -> PyObject.
 */
class PyDict private constructor(val items: MutableMap<PyObject, PyObject>) : PyPrimitive() {
    companion object {
        /**
         * Creates a new PyDict from any map, wrapping primitive types.
         */
        fun fromAnyMap(map: Map<*, *>): PyDict {
            val newMap = map.entries.associateByTo(
                PyObjectMap(), { get(it.key) }, { get(it.value) }
            )

            return PyDict(newMap)
        }

        /**
         * Makes a new PyDict from a PyObjectMap.
         */
        fun from(map: PyObjectMap) = PyDict(map)

        /**
         * Makes a new PyDict from a raw map type, without casting.
         *
         * Do not use this method if you don't know what you're doing!
         */
        fun unsafeFromUnVerifiedMap(map: MutableMap<PyObject, PyObject>) = PyDict(map)
    }

    override fun unwrap(): MutableMap<PyObject, PyObject> = items

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
    override fun pyContains(other: PyObject): PyObject = PyBool.get(other in items)

    override fun pyGetItem(idx: PyObject): PyObject {
        return items[idx] ?: keyError(idx.pyToStr().wrappedString)
    }

    override fun pySetItem(idx: PyObject, value: PyObject): PyNone {
        items[idx] = value
        return PyNone
    }

    override fun pyHash(): PyInt = typeError("dicts are not hashable - they are mutable")
    override fun pyLen(): PyInt = PyInt(items.size.toLong())

    override var type: PyType
        get() = PyDictType
        set(_) = Exceptions.invalidClassSet(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (items != (other as PyDict).items) return false

        return true
    }

    /**
     * Gets an item from the internal dict.
     */
    fun getItem(key: PyObject): PyObject? = items[key]
}
