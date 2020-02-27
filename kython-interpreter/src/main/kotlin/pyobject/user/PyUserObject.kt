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

package green.sailor.kython.interpreter.pyobject.user

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.functions.magic.DefaultBuiltinFunction
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.pyobject.numeric.PyBool
import green.sailor.kython.interpreter.pyobject.numeric.PyFloat
import green.sailor.kython.interpreter.pyobject.numeric.PyInt
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast

/**
 * Represents a Python user object instance (i.e. an object created from a user type object).
 */
open class PyUserObject(type: PyUserType) : PyObject {
    override var type: PyType = type

    override val internalDict: MutableMap<String, PyObject> = linkedMapOf()

    /** The backing primitive subclass info. */
    val primitiveSubclassBacking = mutableMapOf<PyType, PyObject>()

    /**
     * Implements user_object.__init__().
     */
    open fun pyInit(kwargs: Map<String, PyObject>): PyNone {
        // sets up built-in superclasses
        // this is fugly but... im not sure of a better way
        val unmapped = kwargs.values.toList()
        for (parent in type.mro) {
            if (parent !is PyUserType) {
                parent.kySuperclassInit(this, unmapped)
            }
        }

        type.internalDict["__init__"]?.let {
            if (it !is PyUserFunction) {
                typeError("__init__ was not a function")
            }
            val selfName = it.code.varnames.firstOrNull()
                ?: typeError("__init__ must take null")
            (kwargs as MutableMap)[selfName] = this

            KythonInterpreter.runStackFrame(it.createFrame(), kwargs)
        }
        return PyNone
    }

    override fun kyIsCallable(): Boolean {
        return "__call__" in type.internalDict
    }

    override fun kyGetSignature(): PyCallableSignature {
        return type.internalDict["__call__"]?.kyGetSignature()
            ?: typeError("This object is not callable")
    }

    override fun kyCall(args: List<PyObject>): PyObject {
        return type.internalDict["__call__"]?.pyDescriptorGet(this, type)?.kyCall(args)
            ?: super.kyCall(args)
    }

    // magicMethodX wrappers

    // == attributes == //
    override fun pyGetAttribute(name: String): PyObject =
        magicMethod1(PyString(name), "__getattribute__") {
            super.pyGetAttribute(name)
        }

    override fun pySetAttribute(name: String, value: PyObject): PyObject =
        magicMethod2(PyString(name), value, "__setattr__") {
            super.pySetAttribute(name, value)
        }

    override fun pyGetItem(idx: PyObject): PyObject = magicMethod1(idx, "__getitem__") {
        super.pyGetItem(idx)
    }

    override fun pySetItem(idx: PyObject, value: PyObject): PyNone {
        magicMethod2(idx, value, "__setitem__") { super.pySetItem(idx, value) }
        return PyNone
    }

    override fun pyDir(): PyTuple = magicMethod0("__dir__") { super.pyDir() }

    // == comparison == //
    override fun pyEquals(other: PyObject): PyObject = magicMethod1(other, "__eq__") {
        // TODO: isinstance
        if (type == other.type) PyBool.get(this === other) else PyNotImplemented
    }
    override fun pyNotEquals(other: PyObject): PyObject = magicMethod1(other, "__neq__") {
        super.pyNotEquals(other)
    }
    override fun pyLesser(other: PyObject): PyObject =
        magicMethod1(other, "__lt__") { PyNotImplemented }
    override fun pyGreater(other: PyObject): PyObject =
        magicMethod1(other, "__gt__") { PyNotImplemented }
    override fun pyContains(other: PyObject): PyObject =
        magicMethod1(other, "__contains__") { PyNotImplemented }

    // == unary -- //
    override fun pyNegative(): PyObject = magicMethod0("__neg__") { PyNotImplemented }
    override fun pyPositive(): PyObject = magicMethod0("__pos__") { PyNotImplemented }
    override fun pyInvert(): PyObject = magicMethod0("__invert__") { PyNotImplemented }

    // == mathematical == //
    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__radd__") { PyNotImplemented }
        else magicMethod1(other, "__add__") { PyNotImplemented }

    override fun pySub(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__rsub__") { PyNotImplemented }
        else magicMethod1(other, "__sub__") { PyNotImplemented }

    override fun pyMul(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__rmul__") { PyNotImplemented }
        else magicMethod1(other, "__mul__") { PyNotImplemented }

    override fun pyMatMul(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__rmatmul__") { PyNotImplemented }
        else magicMethod1(other, "__matmul__") { PyNotImplemented }

    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__rtruediv__") { PyNotImplemented }
        else magicMethod1(other, "__truediv__") { PyNotImplemented }

    override fun pyFloorDiv(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__rfloordiv__") { PyNotImplemented }
        else magicMethod1(other, "__floordiv__") { PyNotImplemented }

    // == iterables == //
    override fun pyIter(): PyObject = magicMethod0("__iter__") { super.pyIter() }
    override fun pyNext(): PyObject = magicMethod0("__next__") { super.pyNext() }
    override fun pyLen(): PyInt = magicMethod0("__len__") { super.pyLen() }

    // == conversion == //
    override fun pyGetRepr(): PyString = magicMethod0("__repr__") { super.pyGetRepr() }
    override fun pyToStr(): PyString = magicMethod0("__str__") { super.pyToStr() }
    override fun pyToBool(): PyBool = magicMethod0("__bool__") { PyBool.TRUE }
    override fun pyToInt(): PyInt = magicMethod0("__int__") { super.pyToInt() }
    override fun pyToBytes(): PyBytes = magicMethod0("__bytes__") { super.pyToBytes() }
    override fun pyToFloat(): PyFloat = magicMethod0("__float__") { super.pyToFloat() }
}

/**
 * Helper function for calling a magic method with zero args (e.g. __str__).
 */
inline fun <reified T : PyObject> PyUserObject.magicMethod0(name: String, fallback: () -> T): T {
    // looking directly up in the internal dict for magic methods
    val meth = type.internalDict[name]?.pyDescriptorGet(this, type)
    return if (meth != null && meth !is DefaultBuiltinFunction) {
        // this should be bound!!
        meth.kyCall(listOf()).cast()
    } else {
        fallback()
    }
}

/**
 * Helper function for calling a magic method with one arg (e.g. __eq__).
 */
inline fun <reified T : PyObject> PyUserObject.magicMethod1(
    other: PyObject,
    name: String,
    fallback: () -> T
): T {
    val meth = type.internalDict[name]?.pyDescriptorGet(this, type)
    return if (meth != null && meth !is DefaultBuiltinFunction) {
        // this should be bound!!
        meth.kyCall(listOf(other)).cast()
    } else {
        fallback()
    }
}

inline fun <reified T : PyObject> PyUserObject.magicMethod2(
    other1: PyObject,
    other2: PyObject,
    name: String,
    fallback: () -> T
): T {
    val meth = type.internalDict[name]?.pyDescriptorGet(this, type)
    return if (meth != null && meth !is DefaultBuiltinFunction) {
        // this should be bound!!
        meth.kyCall(listOf(other2, other1)).cast()
    } else {
        fallback()
    }
}
