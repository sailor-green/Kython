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

package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.callable.PyCallable
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.collection.PyList
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.pyobject.numeric.PyBool
import green.sailor.kython.interpreter.pyobject.numeric.PyFloat
import green.sailor.kython.interpreter.pyobject.numeric.PyInt
import green.sailor.kython.interpreter.util.PyObjectMap
import java.util.*

/**
 * Represents a Python object. Examples include an int, strings, et cetera, or user-defined objects.
 */
interface PyObject {
    companion object {
        /**
         * Wraps a primitive type into a PyObject.
         */
        fun wrapPrimitive(obb: Any?): PyObject =
            when (obb) {
                null -> PyNone
                is Short -> PyInt(
                    obb.toLong()
                )
                is Int -> PyInt(
                    obb.toLong()
                )
                is Long -> PyInt(
                    obb
                )
                is String -> PyString(obb)
                is Boolean -> PyBool.get(obb)
                is ByteArray -> PyBytes(obb)
                is MutableList<*> -> PyList(obb.mapTo(mutableListOf()) { get(it) })
                is Map<*, *> -> PyDict.fromAnyMap(obb)
                else -> error("Don't know how to wrap $obb in a PyObject")
            }

        /**
         * Gets a PyObject from any object.
         *
         * If the object is already a PyObject, just returns it. Otherwise, tries to wrap it.
         */
        fun get(obb: Any?) = obb as? PyObject ?: wrapPrimitive(obb)
    }

    // exposed attribs
    /** The type of this PyObject. */
    var type: PyType

    /** The `__dict__` of this PyObject. */
    val internalDict: MutableMap<String, PyObject>

    /**
     * Checks if this type is callable. This will check for [PyCallable], or a valid `__call__`.
     */
    fun kyIsCallable(): Boolean {
        if (this is PyCallable) {
            return true
        }
        return false
    }

    /**
     * Gets the signature for this type, if callable.
     */
    fun kyGetSignature(): PyCallableSignature {
        if (this is PyCallable) return signature
        typeError("This object is not callable")
    }

    /**
     * Calls this object from Kotlin. This should only be used for simple functions, such as magic
     * methods - use pyCall() to call from the Python bytecode layer.
     *
     * @param args: The arguments to pass in that will be transformed into keyword arguments.
     */
    fun kyCall(
        args: List<PyObject> = listOf()
    ): PyObject {
        val sig = kyGetSignature()
        val transformed = sig.argsToKwargs(args)
        val us = this as PyCallable
        val frame = us.createFrame()
        return KythonInterpreter.runStackFrame(frame, transformed)
    }

    // ==== MAGIC METHODS: INTERFACES ====

    // __hash__
    /**
     * Implements hash(some_object).
     */
    fun pyHash(): PyInt {
        // this should be Object.hashcode()
        return PyInt(
            Objects.hashCode(
                this
            ).toLong()
        )
    }

    // __dir__
    /**
     * Implements dir(some_object).
     */
    fun pyDir(): PyTuple {
        val sorted = dir()
        return PyTuple
            .get(sorted.map { s -> PyString(s) })
    }

    // __getattribute__
    /**
     * Implements some_object.some_attribute.
     */
    fun pyGetAttribute(name: String): PyObject {
        return getAttribute(name)
    }

    // __setattr__
    /**
     * Implements some_object.some_attribute = other_object.
     */
    fun pySetAttribute(name: String, value: PyObject): PyObject {
        return setAttribute(name, value)
    }

    // __call__

    /**
     * Implements some_object() from the bytecode layer.
     */
    fun pyCall(
        args: List<PyObject>,
        kwargTuple: List<String> = listOf()
    ): PyObject {
        val sig = kyGetSignature()
        val transformed = sig.callFunctionGetArgs(args, kwargTuple)
        val us = this as PyCallable
        val frame = us.createFrame()
        return KythonInterpreter.runStackFrame(frame, transformed)
    }

    // __enter__
    /**
     * Implements the `__enter__` portion of `with some_object`.
     */
    fun pyWithEnter(): PyObject = attributeError("__enter__")

    // __exit__
    /**
     * Implements the `__exit__` portion of `with some_object`.
     */
    fun pyWithExit(): PyObject = attributeError("__exit__")

    // == Conversion ==
    // __bool__
    /**
     * Implements bool(some_object).
     */
    fun pyToBool(): PyBool = PyBool.TRUE

    // __int__
    /**
     * Implements int(some_object).
     */
    fun pyToInt(): PyInt = typeError("Cannot convert '${type.name}' to int")

    // __float__
    /**
     * Implements float(some_object).
     */
    fun pyToFloat(): PyFloat = typeError("Cannot convert '${type.name}' to float")

    // __bytes__
    /**
     * Implements bytes(some_object).
     */
    fun pyToBytes(): PyBytes = typeError("Cannot convert '${type.name}' to bytes")

    // __str__
    /**
     * Implements str(some_object).
     */
    fun pyToStr(): PyString {
        val hashCode = System.identityHashCode(this).toString(16)

        return PyString("<object '${type.name}' at 0x$hashCode>")
    }

    // __repr__
    /**
     * Implements repr(some_object).
     */
    fun pyGetRepr(): PyString = pyToStr()

    // == Comparison operators ==

    // __eq__
    /**
     * Implements some_object == other_object.
     */
    fun pyEquals(other: PyObject): PyObject = PyBool.get(this === other)

    // __neq__
    /**
     * Implements some_object != other_object.
     */
    fun pyNotEquals(other: PyObject): PyObject {
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
    fun pyGreater(other: PyObject): PyObject = PyNotImplemented

    // __lt__
    /**
     * Implements some_object < other_object.
     */
    fun pyLesser(other: PyObject): PyObject = PyNotImplemented

    // __ge__
    /**
     * Implements some_object >= other_object.
     */
    fun pyGreaterEquals(other: PyObject): PyObject = PyNotImplemented

    // __le__
    /**
     * Implements some_object <= other_object.
     */
    fun pyLesserEquals(other: PyObject): PyObject = PyNotImplemented

    // __contains__
    /**
     * Implements some_object in other_object.
     */
    fun pyContains(other: PyObject): PyObject = PyNotImplemented

    // == Unary operators == //
    // __invert__
    /**
     * Implements ~some_object.
     */
    fun pyInvert(): PyObject = typeError("'${type.name}' does not support unary inversion")

    // __neg__
    /**
     * Implements -some_object.
     */
    fun pyNegative(): PyObject = typeError("'${type.name}' does not support unary negative")

    // __pos__
    /**
     * Implements +some_object.
     */
    fun pyPositive(): PyObject = typeError("'${type.name}' does not support unary positive")

    // == Binary operators == //

    // __add__
    /**
     * Implements some_object + other_object.
     */
    fun pyAdd(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __sub__
    /**
     * Implements some_object - other_object.
     */
    fun pySub(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __mul__
    /**
     * Implements some_object * other_object.
     */
    fun pyMul(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __matmul__
    /**
     * Implements some_object @ other_object.
     */
    fun pyMatMul(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __truediv__
    /**
     * Implements some_object / other_object.
     */
    fun pyDiv(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __floordiv__
    /**
     * Implements some_object // other_object.
     */
    fun pyFloorDiv(other: PyObject, reverse: Boolean = false): PyObject = PyNotImplemented

    // __getitem__
    /**
     * Implements some_object\[other_object\].
     */
    fun pyGetItem(idx: PyObject): PyObject =
        typeError("'${type.name}' object is not subscriptable")

    // __setitem__
    /**
     * Implements some_object\[name\] = other_object.
     */
    fun pySetItem(idx: PyObject, value: PyObject): PyNone =
        typeError("'${type.name}' object does not support item assignment")

    // __delitem__
    /**
     * Implements del some_object\[other_object\].
     */
    fun pyDelItem(idx: PyObject): PyNone =
        typeError("'${type.name}' object is not subscriptable")

    // == Iterators/iterables ==
    // __iter__
    /**
     * Implements iter(some_object).
     */
    fun pyIter(): PyObject = typeError("'${type.name}' object is not iterable")

    // __next__
    /**
     * Implements next(some_object).
     */
    fun pyNext(): PyObject = typeError("'${type.name}' object is not an iterator")

    // __len__
    /**
     * Implements len(some_object).
     */
    fun pyLen(): PyInt = typeError("object of type '${type.name}' has no len")

    // __length_hint__
    /**
     * Used internally for optimisation.
     */
    fun pyLengthHint(): PyInt = PyInt.ZERO

    // == Descriptors == //

    // __get__
    /**
     * Implements `__get__` for this object.
     *
     * @param parent: The parent instance.
     * @param klass: The parent class.
     */
    fun pyDescriptorGet(parent: PyObject, klass: PyObject): PyObject = this

    /**
     * If this object has a `__set__` descriptor.
     */
    fun kyHasSet(): Boolean = false

    // __set__
    /**
     * Implements `__set__` for this object.
     */
    fun pyDescriptorSet(instance: PyObject, value: PyObject) = Unit

    // __set_name__
    /**
     * Implements `__set_name__` for this object.
     */
    fun pyDescriptorSetName(): PyObject {
        TODO()
    }

    /**
     * Gets the string of this object, safely. Used for exceptions, et al.
     */
    fun getPyStringSafe(): PyString = try {
        pyToStr()
    } catch (e: Throwable) {
        PyString.UNPRINTABLE
    }

    // attributes

    /**
     * The internal [`__dict__`][PyDict] of this object, wrapped. This corresponds to `__dict__`.
     */
    val pyDict: PyDict
        get() {
            val mapTo = PyObjectMap()
            return PyDict.from(internalDict.mapKeysTo(mapTo) { PyString(it.key) })
        }
}
