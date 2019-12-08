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
package green.sailor.kython.interpreter.pyobject.user

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.functions.magic.DefaultBuiltinFunction
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.typeError

/**
 * Represents a Python user object instance (i.e. an object created from a user type object).
 */
open class PyUserObject(type: PyUserType) : PyObject() {
    override var type: PyType = type

    /**
     * Implements user_object.__init__().
     */
    open fun pyInit(kwargs: Map<String, PyObject>) {
        type.internalDict["__init__"]?.let {
            if (it !is PyUserFunction) {
                typeError("__init__ was not a function")
            }
            val selfName = it.code.varnames.firstOrNull()
                ?: typeError("__init__ must take null")
            (kwargs as MutableMap)[selfName] = this

            KythonInterpreter.runStackFrame(it.createFrame(), kwargs)
        }
    }

    override fun kyIsCallable(): Boolean {
        return "__call__" in type.internalDict
    }

    override fun pyCall(args: List<PyObject>, kwargs: Map<String, PyObject>): PyObject {
        return type.internalDict["__call__"]?.pyDescriptorGet(this, type)?.pyCall(args, kwargs)
            ?: super.pyCall(args, kwargs)
    }

    // magicMethodX wrappers

    override fun pyGetAttribute(name: String): PyObject =
        magicMethod1(PyString(name), "__getattribute__") {
            super.pyGetAttribute(name)
        }

    override fun pyDir(): PyTuple = magicMethod0("__dir__") { super.pyDir() }

    // == comparison == //
    override fun pyEquals(other: PyObject): PyObject = magicMethod1(other, "__eq__") {
        // TODO: isinstance
        if (type == other.type) PyBool.get(this === other) else PyNotImplemented
    }
    override fun pyNotEquals(other: PyObject): PyObject = magicMethod1(other, "__neq__") {
        super.pyNotEquals(it)
    }
    override fun pyLesser(other: PyObject): PyObject =
        magicMethod1(other, "__lt__") { PyNotImplemented }
    override fun pyGreater(other: PyObject): PyObject =
        magicMethod1(other, "__gt__") { PyNotImplemented }

    // == unary //
    override fun pyNegative(): PyObject = magicMethod0("__neg__") { PyNotImplemented }
    override fun pyPositive(): PyObject = magicMethod0("__pos__") { PyNotImplemented }
    override fun pyInvert(): PyObject = magicMethod0("__invert__") { PyNotImplemented }

    // == mathematical == //
    override fun pyAdd(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__add__") { PyNotImplemented }
        else magicMethod1(other, "__radd__") { PyNotImplemented }

    override fun pySub(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__sub__") { PyNotImplemented }
        else magicMethod1(other, "__rsub__") { PyNotImplemented }

    override fun pyMul(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__mul__") { PyNotImplemented }
        else magicMethod1(other, "__rmul__") { PyNotImplemented }

    override fun pyMatMul(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__matmul__") { PyNotImplemented }
        else magicMethod1(other, "__rmatmul__") { PyNotImplemented }

    override fun pyDiv(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__truediv__") { PyNotImplemented }
        else magicMethod1(other, "__rtruediv__") { PyNotImplemented }

    override fun pyFloorDiv(other: PyObject, reverse: Boolean): PyObject =
        if (reverse) magicMethod1(other, "__floordiv__") { PyNotImplemented }
        else magicMethod1(other, "__rfloordiv__") { PyNotImplemented }

    // == iterables == //
    override fun pyIter(): PyObject = magicMethod0("__iter__") { super.pyIter() }
    override fun pyNext(): PyObject = magicMethod0("__next__") { super.pyNext() }

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
        meth.pyCall(listOf()).cast()
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
    fallback: (PyObject) -> T
): T {
    val meth = type.internalDict[name]?.pyDescriptorGet(this, type)
    return if (meth != null && meth !is DefaultBuiltinFunction) {
        // this should be bound!!
        meth.pyCall(listOf(other)).cast()
    } else {
        fallback(other)
    }
}
