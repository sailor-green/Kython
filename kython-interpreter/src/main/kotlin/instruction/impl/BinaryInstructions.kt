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

package green.sailor.kython.interpreter.instruction.impl

import green.sailor.kython.interpreter.pyobject.PyNotImplemented
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.typeError

/**
 * Implements binary operator actions.
 *
 * The first callback passed should invoke the appropriate function on TOS and TOS1, and return the
 * [PyObject] from that function. The second callback
 */
private fun UserCodeStackFrame.implBinaryOp(
    cb: (PyObject, PyObject) -> PyObject,
    cb2: (PyObject, PyObject) -> PyObject
): PyObject {
    val tos = stack.pop()
    val tos1 = stack.pop()
    val first = cb(tos1, tos)
    if (first !== PyNotImplemented) {
        return first
    }

    val second = cb2(tos, tos1)
    if (second !== PyNotImplemented) {
        return second
    }

    typeError("Operation not supported between ${tos.type.name} and ${tos1.type.name}")
}

/**
 * BINARY_* (ADD, etc)
 */
fun UserCodeStackFrame.binaryOp(type: BinaryOp, arg: Byte) {
    val toPush = when (type) {
        BinaryOp.ADD -> implBinaryOp(
            { a, b -> a.pyAdd(b) }, { a, b -> a.pyAdd(b, reverse = true) }
        )
        BinaryOp.SUBTRACT -> implBinaryOp(
            { a, b -> a.pySub(b) }, { a, b -> a.pySub(b, reverse = true) }
        )
        BinaryOp.MULTIPLY -> implBinaryOp(
            { a, b -> a.pyMul(b) }, { a, b -> a.pyMul(b, reverse = true) }
        )
        BinaryOp.MATRIX_MULTIPLY -> implBinaryOp(
            { a, b -> a.pyMatMul(b) }, { a, b -> a.pyMatMul(b, reverse = true) }
        )
        BinaryOp.TRUE_DIVIDE -> implBinaryOp(
            { a, b -> a.pyDiv(b) }, { a, b -> a.pyDiv(b, reverse = true) }
        )
        BinaryOp.FLOOR_DIVIDE -> implBinaryOp(
            { a, b -> a.pyFloorDiv(b) }, { a, b -> a.pyFloorDiv(b, reverse = true) }
        )
        /*BinaryOp.LSHIFT -> "__lshift__"
        BinaryOp.POWER -> "__pow__"
        BinaryOp.MODULO -> "__mod__"
        BinaryOp.SUBSCR -> "__getitem__"
        BinaryOp.RSHIFT -> "__rshift__"
        BinaryOp.AND -> "__and__"
        BinaryOp.XOR -> "__xor__"
        BinaryOp.OR -> "__or__"*/
        else -> error("This should never happen!")
    }
    stack.push(toPush)
    bytecodePointer += 1
}

/**
 * INPLACE_*
 */
fun UserCodeStackFrame.inplaceOp(type: BinaryOp, arg: Byte) {
    TODO()
}
