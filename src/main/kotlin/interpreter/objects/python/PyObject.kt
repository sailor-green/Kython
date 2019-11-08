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

import green.sailor.kython.marshal.MarshalNone
import green.sailor.kython.marshal.MarshalType

/**
 * Represents a Python object. Examples include an int, strings, et cetera, or user-defined objects.
 */
abstract class PyObject() {
    companion object {
        /**
         * Wraps a marshalled object from code into a PyObject.
         */
        fun wrapMarshalled(type: MarshalType): PyObject {
            if (type.wrapped != null) {
                return wrapPrimitive(
                    type.wrapped!!
                )
            }

            // special singletons
            return when (type) {
                is MarshalNone -> PyNone
                else -> error("Unknown type $type")
            }
        }

        /**
         * Wraps a primitive type into a PyObject.
         */
        fun wrapPrimitive(obb: Any): PyObject =
            when (obb) {
                is Short -> PyInt(obb.toLong())
                is Int -> PyInt(obb.toLong())
                is Long -> PyInt(obb.toLong())
                is String -> PyString(obb.toString())
                else -> error { "Don't know how to wrap $obb in a PyObject" }
            }
    }

    /** The type of this PyObject. */
    lateinit var type: PyObject

    constructor(type: PyObject) : this() {
        this.type = type
    }

    /**
     * Turns this object into a PyString. This corresponds to the `__str__` method.
     */
    abstract fun toPyString(): PyString

    /**
     * Turns this object into a PyString when repr() is called. This corresponds to the `__repr__` method.
     */
    abstract fun toPyStringRepr(): PyString
}
