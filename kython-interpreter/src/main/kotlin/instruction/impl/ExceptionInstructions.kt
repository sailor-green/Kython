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
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.exception.BuiltinPyException
import green.sailor.kython.interpreter.pyobject.exception.PyException
import green.sailor.kython.interpreter.pyobject.exception.PyExceptionType
import green.sailor.kython.interpreter.stack.FinallyBlock
import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * Represents the NULL object pushed onto the stack by BEGIN_FINALLY.
 */
val nullFinally = object : PyObject() {
    override var type: PyType
        get() = error("You should never see this")
        set(_) = error("You should never see this")
}

/**
 * SETUP_FINALLY
 */
fun UserCodeStackFrame.setupFinally(opval: Byte) {
    val delta = (opval.toUByte().toInt() / 2) + bytecodePointer + 1
    blockStack.push(FinallyBlock(delta))
    bytecodePointer += 1
}

/**
 * BEGIN_FINALLY
 */
fun UserCodeStackFrame.beginFinally(opval: Byte) {
    stack.push(nullFinally)
    bytecodePointer += 1
}

/**
 * POP_EXCEPT
 */
// todo: actually implement this, i guess.
fun UserCodeStackFrame.popExcept(oval: Byte) {
    bytecodePointer += 1
}

/**
 * POP_BLOCK
 */
fun UserCodeStackFrame.popBlock(oval: Byte) {
    blockStack.pop()
    bytecodePointer += 1
}

/**
 * RERAISE
 */
fun UserCodeStackFrame.reraise(opval: Byte) {
    val excType = stack.pop() as? PyType ?: error("TOS wasn't a type!")
    if (!excType.issubclass(Exceptions.BASE_EXCEPTION)) {
        typeError("${excType.name} is not an Exception type")
    }
    val excVal = stack.pop() as? PyException ?: error("TOS wasn't an exception!")
    val excTb = stack.pop()
    // TODO: properly copy tb or whatever
    excVal.throwKy()
}

/**
 * JUMP_IF_NOT_EXC_MATCH
 */
fun UserCodeStackFrame.jumpIfNotExcMatch(opval: Byte) {
    val tos = stack.pop() as? PyType ?: error("TOS wasn't a type!")
    val exc = stack.pop() as? PyType ?: error("TOS1 wasn't a type!")
    if (!exc.issubclass(Exceptions.BASE_EXCEPTION)) {
        typeError("${exc.name} is not an Exception type")
    }
    val isinstance = exc.issubclass(tos)
    if (!isinstance) {
        bytecodePointer = opval.toUByte().toInt() / 2
    } else {
        bytecodePointer += 1
    }
}
