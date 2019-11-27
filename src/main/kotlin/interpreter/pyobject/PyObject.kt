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
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.throwKy

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
                is ByteArray -> PyBytes(obb)
                else -> error("Don't know how to wrap $obb in a PyObject")
            }

        /**
         * Gets a PyObject from any object.
         *
         * If the object is already a PyObject, just returns it. Otherwise, tries to wrap it.
         */
        fun get(obb: Any?): PyObject {
            if (obb is PyObject) return obb
            return wrapPrimitive(obb)
        }

        /**
         * The default object dict, containing the base implements of certain magic methods.
         */
        val defaultDict: LinkedHashMap<String, PyObject>
            get() = linkedMapOf()
    }

    constructor(type: PyType) : this() {
        this.type = type
    }

    // attribs
    /** The type of this PyObject. */
    open var type: PyType = PyRootType

    /** The parent types of this PyObject. Exposed as `__bases__`. */
    open val parentTypes = mutableListOf<PyType>(PyRootObjectType)

    /**
     * The initial dict for this PyObject. Copied into internalDict upon instantiating.
     * Built-in methods should be defined on the type object's copy of this.
     */
    open val initialDict: Map<String, PyObject> = mapOf()

    /** The `__dict__` of this PyObject. */
    internal open val internalDict by lazy {
        defaultDict.apply {
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
            Exceptions.TYPE_ERROR("Invalid type: ${type.name}").throwKy()
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
            ObjectGetattribute.pyDescriptorGet(this, type)
        }

        if (getAttribute !is PyCallable) {
            Exceptions.TYPE_ERROR("__getattribute__ is not callable").throwKy()
        }

        return getAttribute.runCallable(listOf(PyString(name)))
    }

    /**
     * Performs special method lookup.
     *
     * @return A [PyObject]? for the special method found, or null if it wasn't found.
     */
    open fun specialMethodLookup(name: String): PyObject? {
        return type.internalDict[name]?.pyDescriptorGet(this, type)
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
     * Returns the [string representation][PyString] of str(). This corresponds to the `__str__` method.
     */
    abstract fun getPyStr(): PyString

    /**
     * Returns the [string representation][PyString] of repr(). This corresponds to the `__repr__` method.
     */
    abstract fun getPyRepr(): PyString

    /**
     * Gets the string of this object, safely. Used for exceptions, et al.
     */
    fun getPyStringSafe(): PyString = try {
        getPyStr()
    } catch (e: Throwable) {
        PyString.UNPRINTABLE
    }

    // attributes

    /**
     * The internal [`__dict__`][PyDict] of this object, wrapped. This corresponds to `__dict__`.
     */
    val pyDict: PyDict
        get() {
            return PyDict(internalDict.mapKeys {
                PyString(
                    it.key
                )
            }.toMutableMap() as LinkedHashMap)
        }
}
