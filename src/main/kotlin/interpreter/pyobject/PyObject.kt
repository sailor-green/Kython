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
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.functions.magic.ObjectGetattribute
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.throwKy
import green.sailor.kython.kyc.*

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
        fun wrapKyc(type: BaseKycType): PyObject {
            // unwrap tuples and dicts from their inner types
            if (type is KycTuple) {
                return PyTuple(type.wrapped.map {
                    wrapKyc(
                        it
                    )
                })
            } else if (type is KycDict) {
                val map = linkedMapOf<PyObject, PyObject>()
                for ((key, value) in type.wrapped.entries) {
                    map[wrapKyc(key)] = wrapKyc(value)
                }
                return PyDict(map)
            }

            if (type.wrapped != null) {
                return wrapPrimitive(type.wrapped!!)
            }

            // special singletons
            return when (type) {
                is KycNone -> PyNone
                is KycCodeObject -> PyCodeObject(KyCodeObject(type))
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
                is String -> PyString(
                    obb.toString()
                )
                is Boolean -> if (obb) PyBool.TRUE else PyBool.FALSE
                else -> error("Don't know how to wrap $obb in a PyObject")
            }

        /**
         * Gets the default object dict, containing the base implements of certain magic methods.
         */
        fun getDefaultDict(): LinkedHashMap<String, PyObject> {
            val d = linkedMapOf<String, PyObject>()
            return d
        }
    }

    constructor(type: PyType) : this() {
        this.type = type
    }

    // attribs
    /** The type of this PyObject. */
    open var type: PyType = PyRootType

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
            // then copy the "initial" dictionary
            putAll(initialDict)
        }
    }

    // helper functions
    /**
     * Casts this [PyObject] to its concrete subclass, raising a PyException if it fails.
     */
    inline fun <reified T : PyObject> cast(): T {
        if (this !is T) {
            Exceptions.TYPE_ERROR.makeWithMessage("Invalid type: ${this.type.name}").throwKy()
        }
        return this
    }


    // `object.X` implementations
    /**
     * Delegate for `LOAD_ATTR` on any object.
     *
     * @param name: The name of the attribute to get.
     */
    open fun pyGetAttribute(name: String): PyObject {
        // this will delegate to `__getattribute__`,

        // try and run __getattribute__
        val getAttribute = try {
            ObjectGetattribute.runCallable(
                listOf(PyString("__getattribute__"), this)
            )
        } catch (e: KyError) {
            ObjectGetattribute.pyDescriptorGet(this, this.type)
        }

        if (getAttribute !is PyCallable) {
            Exceptions.TYPE_ERROR.makeWithMessage("__getattribute__ is not callable").throwKy()
        }

        return getAttribute.runCallable(listOf(PyString(name)))
    }

    /**
     * Performs special method lookup.
     *
     * @return A [PyObject]? for the special method found, or null if it wasn't found.
     */
    open fun specialMethodLookup(name: String): PyObject? {
        val type = this.type ?: PyRootType
        return type.internalDict[name]
    }

    // == Descriptors ==

    /**
     * Implements `__get__` for this object.
     *
     * @param parent: The parent instance.
     * @param klass: The parent class.
     */
    open fun pyDescriptorGet(parent: PyObject, klass: PyObject): PyObject = this

    /**
     * Implements `__set__` for this object.
     */
    open fun pyDescriptorSet(): PyObject {
        TODO()
    }

    /**
     * Implements `__set_name__` for this object.
     */
    open fun pyDescriptorSetName(): PyObject {
        TODO()
    }

    // == str() / repr() ==
    /**
     * Turns this object into a PyString. This corresponds to the `__str__` method.
     */
    abstract fun pyStr(): PyString

    /**
     * Turns this object into a PyString when repr() is called. This corresponds to the `__repr__` method.
     */
    abstract fun pyRepr(): PyString


    /**
     * Gets the string of this object, safely. Used for exceptions, et al.
     */
    fun getPyStringSafe(): PyString = try {
        this.pyStr()
    } catch (e: Throwable) {
        PyString.UNPRINTABLE
    }

    // attributes

    /**
     * Gets the internal `__dict__` of this object, wrapped. This corresponds to `__dict__`.
     */
    fun pyGetDict(): PyDict {
        return PyDict(internalDict.mapKeys {
            PyString(
                it.key
            )
        }.toMutableMap() as LinkedHashMap)
    }
}
