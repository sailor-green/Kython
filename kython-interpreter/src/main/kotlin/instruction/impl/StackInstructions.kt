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

import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * POP_TOP.
 */
fun UserCodeStackFrame.popTop(arg: Byte) {
    assert(arg.toInt() == 0) { "POP_TOP never has an argument" }

    stack.pop()
    bytecodePointer += 1
}

/**
 * ROT_TWO
 */
fun UserCodeStackFrame.rotTwo(arg: Byte) {
    assert(arg.toInt() == 0) { "ROT_TWO never has an argument" }

    val top = stack.pop()
    val second = stack.pop()
    stack.push(top)
    stack.push(second)
    bytecodePointer += 1
}

/**
 * ROT_THREE
 */
fun UserCodeStackFrame.rotThree(arg: Byte) {
    assert(arg.toInt() == 0) { "ROT_THREE never has an argument" }

    val top = stack.pop()
    val second = stack.pop()
    val third = stack.pop()
    stack.push(top)
    stack.push(third)
    stack.push(second)
    bytecodePointer += 1
}

/**
 * ROT_FOUR
 */
fun UserCodeStackFrame.rotFour(arg: Byte) {
    assert(arg.toInt() == 0) { "ROT_FOUR never has an argument" }

    val top = stack.pop()
    val second = stack.pop()
    val third = stack.pop()
    val fourth = stack.pop()
    stack.push(top)
    stack.push(fourth)
    stack.push(third)
    stack.push(second)

    bytecodePointer += 1
}

/**
 * DUP_TOP
 */
fun UserCodeStackFrame.dupTop(arg: Byte) {
    assert(arg.toInt() == 0) { "DUP_TOP never has an argument" }
    val top = stack.first
    stack.push(top)

    bytecodePointer += 1
}

/**
 * DUP_TOP_TWO
 */
fun UserCodeStackFrame.dupTopTwo(arg: Byte) {
    assert(arg.toInt() == 0) { "DUP_TOP_TWO never has an argument" }
    val top = stack.pop()
    val second = stack.pop()
    repeat(2) {
        stack.push(second)
        stack.push(top)
    }

    bytecodePointer += 1
}
