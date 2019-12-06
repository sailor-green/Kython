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

import green.sailor.kython.interpreter.functions.magic.ObjectGetattribute
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.typeError

// initialdict:
// take PyString as an example
// PyStringType contains all the methods, e.g. upper/lower/et cetera
// internalDict copies all the method wrappers from PyStringType on creation, binding them to
// the PyString object

/**
 * Represents a Python object. Examples include an int, strings, et cetera, or user-defined objects.
 */
abstract class PyObject {
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
        fun get(obb: Any?) = obb as? PyObject ?: wrapPrimitive(obb)

        /**
         * The default object dict, containing the base implements of certain magic methods.
         */
        val defaultDict: LinkedHashMap<String, PyObject>
            get() = linkedMapOf()
    }

    // exposed attribs
    /** The type of this PyObject. */
    abstract var type: PyType

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
            typeError("Invalid type: ${type.name}")
        }
        return this
    }

    /**
     * Checks if this type is callable. This will check for [PyCallable], or a valid `__call__`.
     */
    open fun kyIsCallable(): Boolean {
        if (this is PyCallable) {
            return true
        }
        return false
    }

    // ==== MAGIC METHODS: INTERFACES ====

    /**
     * Implements dir(some_object).
     */
    open fun pyDir(): PyTuple {
        val dirSet = mutableSetOf<String>().also { set ->
            // set.addAll(magicSlots.createActiveMagicMethodList())
            set.addAll(type.internalDict.keys)
            set.addAll(type.bases.flatMap { it.internalDict.keys })
            set.addAll(internalDict.keys)
        }

        // NB: CPython sorts dir() output for whatever dumb reason.
        // We do too!
        val sorted = dirSet.toList().sorted()
        return PyTuple(sorted.map { s -> PyString(s) })
    }

    // __getattribute__
    /**
     * Delegate for `LOAD_ATTR` on any object.
     *
     * @param name: The name of the attribute to get.
     */
    open fun pyGetAttribute(name: String): PyObject {
        return ObjectGetattribute.pyCall(listOf(PyString(name), this))
    }

    // == __call__

    /**
     * Implements some_object().
     */
    open fun pyCall(
        args: List<PyObject> = listOf(),
        kwargs: Map<String, PyObject> = mapOf()
    ): PyObject {
        if (this is PyCallable) {
            return runCallable(args)
        }
        typeError("This object is not callable")
    }

    // == Conversion ==
    // __bool__
    /**
     * Implements bool(some_object).
     */
    open fun pyToBool(): PyBool = PyBool.TRUE

    // __int__
    /**
     * Implements int(some_object).
     */
    open fun pyToInt(): PyInt = typeError("Cannot convert '${type.name}' to int")

    // __float__
    /**
     * Implements float(some_object).
     */
    open fun pyToFloat(): PyFloat = typeError("Cannot convert '${type.name}' to float")

    // __bytes__
    /**
     * Implements bytes(some_object).
     */
    open fun pyToBytes(): PyBytes = typeError("Cannot convert '${type.name}' to bytes")

    // __str__
    /**
     * Implements str(some_object).
     */
    abstract fun pyGetStr(): PyString

    // __repr__
    /**
     * Implements repr(some_object).
     */
    abstract fun pyGetRepr(): PyString

    // == Comparison operators ==

    // __eq__
    /**
     * Implements some_object == other_object.
     */
    abstract fun pyEquals(other: PyObject): PyObject

    // __neq__
    /**
     * Implements some_object != other_object.
     */
    open fun pyNotEquals(other: PyObject): PyObject {
        val equals = pyEquals(other)
        return if (equals == PyNotImplemented) {
            PyNotImplemented
        } else {
            (equals as? PyBool)?.invert() ?: error("bool returned non-bool")
        }
    }

    // __gt__
    /**
     * Implements some_object > other_object.
     */
    abstract fun pyGreater(other: PyObject): PyObject

    // __lt__
    /**
     * Implements some_object < other_object.
     */
    abstract fun pyLesser(other: PyObject): PyObject

    // __ge__
    open fun pyGreaterEquals(other: PyObject): PyObject {
        TODO()
    }

    // __le__
    open fun pyLesserEquals(other: PyObject): PyObject {
        TODO()
    }

    // == Unary operators == //
    /**
     * Implements ~some_object.
     */
    open fun pyInvert(): PyObject = typeError("'${type.name}' does not support unary inversion")

    /**
     * Implements -some_object.
     */
    open fun pyNegative(): PyObject = typeError("'${type.name}' does not support unary negative")

    /**
     * Implements +some_object.
     */
    open fun pyPositive(): PyObject = typeError("'${type.name}' does not support unary positive")

    // == Binary operators == //

    // __add__
    /**
     * Implements some_object + other_object.
     */
    open fun pyAdd(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __sub__
    /**
     * Implements some_object - other_object.
     */
    open fun pySub(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __mul__
    /**
     * Implements some_object * other_object.
     */
    open fun pyMul(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __matmul__
    /**
     * Implements some_object @ other_object.
     */
    open fun pyMatMul(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __truediv__
    /**
     * Implements some_object / other_object.
     */
    open fun pyDiv(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __floordiv__
    /**
     * Implements some_object // other_object.
     */
    open fun pyFloorDiv(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // == Descriptors == //

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

    /**
     * Gets the string of this object, safely. Used for exceptions, et al.
     */
    fun getPyStringSafe(): PyString = try {
        pyGetStr()
    } catch (e: Throwable) {
        PyString.UNPRINTABLE
    }

    // attributes

    /**
     * The internal [`__dict__`][PyDict] of this object, wrapped. This corresponds to `__dict__`.
     */
    val pyDict: PyDict
        get() {
            val mapTo = LinkedHashMap<PyString, PyObject>(internalDict.size)
            return PyDict(internalDict.mapKeysTo(mapTo) { PyString(it.key) })
        }
}
