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

package green.sailor.kython.interpreter.objects.python

import arrow.core.Either
import green.sailor.kython.interpreter.objects.KyCodeObject
import green.sailor.kython.interpreter.objects.python.primitives.*
import green.sailor.kython.marshal.*

/**
 * Represents a Python object. Examples include an int, strings, et cetera, or user-defined objects.
 */
abstract class PyObject() {
    companion object {
        /**
         * Wraps a marshalled object from code into a PyObject.
         */
        fun wrapMarshalled(type: MarshalType): PyObject {
            // unwrap tuples and dicts from their inner types
            if (type is MarshalTuple) {
                return PyTuple(type.wrapped.map { wrapMarshalled(it) })
            } else if (type is MarshalDict) {
                val map = mutableMapOf<PyObject, PyObject>()
                for ((key, value) in type.wrapped.entries) {
                    map[wrapMarshalled(key)] = wrapMarshalled(value)
                }
                return PyDict(map)
            }

            if (type.wrapped != null) {
                return wrapPrimitive(type.wrapped!!)
            }

            // special singletons
            return when (type) {
                is MarshalNone -> PyNone
                is MarshalCodeObject -> PyCodeObject(KyCodeObject(type))
                else -> error("Unknown type $type")
            }
        }

        /**
         * Wraps a primitive type into a PyObject.
         */
        fun wrapPrimitive(obb: Any?): PyObject =
            when (obb) {
                is Short -> PyInt(obb.toLong())
                is Int -> PyInt(obb.toLong())
                is Long -> PyInt(obb.toLong())
                is String -> PyString(obb.toString())
                else -> error { "Don't know how to wrap $obb in a PyObject" }
            }
    }

    /** The type of this PyObject. */
    var type: PyType = PyType.PyRootType

    /** The parent types of this PyObject. Exposed as `__bases__`. */
    val parentTypes = mutableListOf<PyType>()

    /** The `__dict__` of this PyObject. */
    private val internalDict = mutableMapOf<String, PyObject>()

    constructor(type: PyType) : this() {
        this.type = type
    }

    // `object.X` implementations
    /**
     * Delegate for `LOAD_ATTR` on any object.
     */
    fun pyGetAttribute(name: String): PyObject? {
        // TODO: Metaclasses, oh god

        // special method lookup
        if (name.startsWith("__") and name.endsWith("__")) {
            return this.type.specialMethodLookup(name)
        }

        // try and find it on our dict, e.g. `__init__`
        if (name in this.internalDict) {
            return this.internalDict[name]!!
        }

        // delegate to the type object
        return this.type.pyGetAttribute(name)
    }

    /**
     * Performs special method lookup.
     */
    fun specialMethodLookup(name: String): PyObject? {
        TODO("Special method lookup")
    }

    /**
     * Turns this object into a PyString. This corresponds to the `__str__` method.
     */
    abstract fun toPyString(): Either<PyException, PyString>

    /**
     * Turns this object into a PyString when repr() is called. This corresponds to the `__repr__` method.
     */
    abstract fun toPyStringRepr(): Either<PyException, PyString>

    /**
     * Gets the internal `__dict__` of this method, wrapped. This corresponds to `__dict__`.
     */
    fun getPyDict(): PyDict {
        return PyDict(internalDict.mapKeys { PyString(it.key) }.toMutableMap())
    }
}
