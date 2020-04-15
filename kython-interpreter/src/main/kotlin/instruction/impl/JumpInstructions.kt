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

import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * JUMP_ABSOLUTE
 */
fun UserCodeStackFrame.jumpAbsolute(arg: Byte) {
    // goes through unsigned so this is never set negative.
    bytecodePointer = arg.toUByte().toInt() / 2
}

/**
 * JUMP_FORWARD
 */
fun UserCodeStackFrame.jumpForward(arg: Byte) {
    bytecodePointer += 1
    bytecodePointer += arg.toInt() / 2
}

/**
 * POP_JUMP_IF_X
 */
fun UserCodeStackFrame.popJumpIf(arg: Byte, compare: Boolean) {
    val tos = stack.pop()
    if (tos.pyToBool().wrappedBool == compare) {
        bytecodePointer = arg.toInt() / 2
    } else {
        // move onto the next instruction
        bytecodePointer += 1
    }
}
