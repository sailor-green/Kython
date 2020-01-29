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

@file:JvmName("InstructionImpls")
@file:JvmMultifileClass
package green.sailor.kython.interpreter.instruction.impl

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyNotImplemented
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.exception.PyExceptionType
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.util.cast

/**
 * Implements comparison operator behaviour, handling PyNotImplemented as appropriately.
 *
 * @param cbFirst: The first operation to call.
 * @param cbSecond: If cbFirst returned PyNotImplemented, the fallback operation.
 * @param shouldError: If this should error, for example in the case of lt/gt.
 */
private fun UserCodeStackFrame.implCompareOp(
    cbFirst: (PyObject, PyObject) -> PyObject,
    cbSecond: ((PyObject, PyObject) -> PyObject)? = null,
    shouldError: Boolean = false
): PyBool {
    val realSecond = cbSecond ?: cbFirst
    val tos = stack.pop()
    val tos1 = stack.pop()

    // try obb1.__magic__(obb2)
    val first = cbFirst(tos, tos1)
    if (first !is PyNotImplemented) {
        return first.pyToBool()
    }

    // try obb2.__magic__(obb1)
    val second = realSecond(tos1, tos)
    if (second !is PyNotImplemented) {
        return second.pyToBool()
    }

    if (!shouldError) {
        return PyBool.FALSE
    } else {
        typeError(
            "Operation not supported between " +
            "'${first.type.name}' and ${second.type.name}"
        )
    }
}

private fun UserCodeStackFrame.implCompareOp(cbFirst: (PyObject, PyObject) -> PyObject): PyBool =
    implCompareOp(cbFirst, null)

/**
 * COMPARE_OP
 */
fun UserCodeStackFrame.compareOp(arg: Byte) {
    val toPush = with(CompareOp) { when (arg.toInt()) {
        LESS -> implCompareOp(
            { tos, tos1 -> tos.pyLesser(tos1) },
            { tos, tos1 -> tos.pyGreaterEquals(tos1) },
            shouldError = true
        )
        LESS_EQUAL -> implCompareOp(
            { tos, tos1 -> tos.pyLesserEquals(tos1) },
            { tos, tos1 -> tos.pyGreater(tos1) },
            shouldError = true
        )
        GREATER -> implCompareOp(
            { tos, tos1 -> tos.pyGreater(tos1) },
            { tos, tos1 -> tos.pyLesserEquals(tos1) },
            shouldError = true
        )
        GREATER_EQUAL -> implCompareOp(
            { tos, tos1 -> tos.pyGreaterEquals(tos1) },
            { tos, tos1 -> tos.pyLesser(tos1) },
            shouldError = true
        )
        EQUAL -> implCompareOp { tos, tos1 -> tos.pyEquals(tos1) }
        NOT_EQUAL -> implCompareOp { tos, tos1 -> tos.pyNotEquals(tos1) }
        EXCEPTION_MATCH -> {
            // TOS is the name we just loaded, which is the one we want to compare
            val top = stack.pop().cast<PyExceptionType>()
            // This is the actual exception type duplicated on the stack
            val second = stack.pop().cast<PyExceptionType>()
            PyBool.get(second.issubclass(setOf(top)))
        }
        else -> Exceptions.RUNTIME_ERROR("Invalid parameter for COMPARE_OP: $arg").throwKy()
    } }
    stack.push(toPush)

    bytecodePointer += 1
}

/**
 * IS_OP
 */
fun UserCodeStackFrame.isOp(arg: Byte) {
    val toPush = when (IsOp.byId(arg)) {
        IsOp.IS -> {
            val top = stack.pop()
            val second = stack.pop()
            if (top === second) PyBool.TRUE else PyBool.FALSE
        }
        IsOp.IS_NOT -> {
            val top = stack.pop()
            val second = stack.pop()
            if (top !== second) PyBool.TRUE else PyBool.FALSE
        }
    }
    stack.push(toPush)
    bytecodePointer += 1
}

/**
 * CONTAINS_OP
 */
fun UserCodeStackFrame.containsOp(arg: Byte) {
    val toPush = when (ContainsOp.byId(arg)) {
        ContainsOp.IN -> implCompareOp { tos, tos1 -> tos.pyContains(tos1) }
        ContainsOp.NOT_IN -> implCompareOp { tos, tos1 -> tos.pyContains(tos1).pyToBool().invert() }
    }
    stack.push(toPush)
    bytecodePointer += 1
}
