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
import green.sailor.kython.interpreter.objects.Exceptions
import green.sailor.kython.interpreter.objects.KyCodeObject
import green.sailor.kython.interpreter.objects.functions.magic.ObjectGetattribute
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.primitives.*
import green.sailor.kython.marshal.*

// initialdict:
// take PyString as an example
// PyStringType contains all the methods, e.g. upper/lower/et cetera
// internalDict copies all the method wrappers from PyStringType on creation, binding them to
// the PyString object

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
                val map = linkedMapOf<PyObject, PyObject>()
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
                is Boolean -> if (obb) PyBool.TRUE else PyBool.FALSE
                else -> error("Don't know how to wrap $obb in a PyObject")
            }

        /**
         * Gets the default object dict, containing the base implements of certain magic methods.
         */
        fun getDefaultDict(): LinkedHashMap<String, PyObject> {
            val d = linkedMapOf<String, PyObject>(
                "__getattribute__" to ObjectGetattribute
            )

            return d
        }
    }

    /** The type of this PyObject. */
    open var type: PyType = PyType.PyRootType

    /** The parent types of this PyObject. Exposed as `__bases__`. */
    open val parentTypes = mutableListOf<PyType>()

    /**
     * The initial dict for this PyObject. Copied into internalDict upon instantiating.
     * Built-in methods should be defined on the type object's copy of this.
     */
    open val initialDict: Map<String, PyObject> = mapOf()

    /** The `__dict__` of this PyObject. */
    internal open val internalDict by lazy {
        getDefaultDict().apply {
            // first copy all the parent type method wrappers
            putAll(type.makeMethodWrappers(this@PyObject))
            // then copy the "initial" dictionary
            putAll(initialDict)
        }
    }

    constructor(type: PyType) : this() {
        this.type = type
    }

    // `object.X` implementations
    /**
     * Delegate for `LOAD_ATTR` on any object.
     *
     * @param name: The name of the attribute to get.
     */
    fun pyGetAttribute(name: String): Either<PyException, PyObject> {
        // this will delegate to `__getattribute__`,

        // forcibly do special method lookup
        if (name.startsWith("__") && name.endsWith("__")) {
            val specialMethod = this.specialMethodLookup(name)
            if (specialMethod != null) {
                return Either.right(specialMethod)
            }
        }

        // try and run __getattribute__
        val getAttribute = this.specialMethodLookup("__getattribute__")
            ?: error("__getattribute__ does not exist - this must never happen")

        if (getAttribute !is PyCallable) {
            return Exceptions.TYPE_ERROR.makeWithMessageLeft("__getattribute__ is not callable")
        }

        // todo: proper method wrapping...
        return getAttribute.runCallable(listOf(PyString(name), this))
    }

    /**
     * Performs special method lookup.
     *
     * @return A [PyObject]? for the special method found, or null if it wasn't found.
     */
    fun specialMethodLookup(name: String): PyObject? {
        return this.type.internalDict[name]
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
     * Gets the string of this object, safely. Used for exceptions, et al.
     */
    fun getPyStringSafe(): PyString = this.toPyString().fold({ PyString.UNPRINTABLE }, { it })

    /**
     * Gets the internal `__dict__` of this method, wrapped. This corresponds to `__dict__`.
     */
    fun getPyDict(): PyDict {
        return PyDict(internalDict.mapKeys { PyString(it.key) }.toMutableMap() as LinkedHashMap)
    }
}
