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
package green.sailor.kython.interpreter.instruction

import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * Represents a single bytecode instruction.
 */
data class Instruction(val opcode: InstructionOpcode, val argument: Byte) {
    /**
     * Gets the formatted disassembly.
     */
    fun getDisassembly(frame: UserCodeStackFrame): String {
        var base = "${opcode.name} $argument "
        val iArg = argument.toInt()

        // look up a value in the three lists
        if (opcode.hasConst) {
            base += "(${frame.function.code.consts[iArg]}) "
        }
        if (opcode.hasName) {
            // maybe make this print the value in the table?
            base += "(${frame.function.code.names[iArg]})"
        }
        if (opcode.hasLocal) {
            base += "(${frame.function.code.varnames[iArg]})"
        }

        // add a real idx field
        if (opcode.hasAbsJump) {
            val realIdx = argument.toUByte().toInt() / 2
            base += "(jump to: $realIdx)"
        }
        if (opcode.hasRelJump) {
            val realAmount = argument.toInt() / 2
            val ourPos = frame.function.code.instructions.indexOf(this)
            val realIdx = ourPos + realAmount + 1
            base += "(jump to: $realIdx)"
        }
        return base
    }
}
