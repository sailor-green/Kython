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

package green.sailor.kython.interpreter.objects

import green.sailor.kython.interpreter.instruction.Instruction
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.marshal.MarshalCodeObject
import java.nio.ByteBuffer

/**
 * Represents a Python code object. (Exposed as __code__ on a function).

 * @param original: The marshalled code object to build from.
 */
class KyCodeObject(original: MarshalCodeObject) {
    /** The argument count for the function. */
    val argCount: Int = original.argCount.wrapped

    /** The number of positional only arguments. */
    val posOnlyArguments: Int = original.posOnlyArgCount.wrapped

    /** The number of keyword only arguments. */
    val kwOnlyArguments: Int = original.kwOnlyArgCount.wrapped

    /** The number of local variables. */
    val localCount = original.localCount.wrapped

    /** The stack size for this function. */
    val stackSize = original.stackSize.wrapped

    /** The raw bytecode for this function. */
    val rawBytecode = original.bytecode.wrapped

    // TODO: Unwrap these into real objects.
    /** The constants for this function. */
    val consts = original.consts.wrapped.map { PyObject.wrapMarshalled(it) }

    /** The names for this function. */
    val names = original.names.wrapped.map { it.wrapped as String }

    /** The varnames for this function. */
    val varnames = original.varnames.wrapped.map { it.wrapped as String }

    /** The free variables for this function. */
    val freevars = original.freevars.wrapped.map { it.wrapped as String }

    /** The cellvars for this function. */
    val cellvars = original.cellvars.wrapped.map { it.wrapped as String }

    /** The filename for this function. */
    val filename = original.filename.wrapped

    /** The code name for this function. */
    val codename = original.codeName.wrapped

    /** The first line for this function. */
    val firstline = original.firstLineNumber.wrapped

    /** The lnotab for this function. */
    val lnotab = original.lnotab.wrapped

    // special properties
    val instructions by lazy { this.parseInstructions() }

    /**
     * Parses the instructions of this code object.
     */
    fun parseInstructions(): Array<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val buf = ByteBuffer.wrap(this.rawBytecode)
        while (buf.hasRemaining()) {
            val opcode = buf.get().toUByte().toInt()  // prevents opcodes >128 from turning into -opcode
            val opval = buf.get()
            instructions.add(Instruction(InstructionOpcode.get(opcode), opval))
        }

        return instructions.toTypedArray()
    }

    /**
     * Gets a newline separated disassembly for this code object.
     */
    fun getDisassembly(frame: UserCodeStackFrame): String {
        return this.instructions
            .withIndex()
            .joinToString("\n") { "    0x${it.index.toString(16)}: ${it.value.getDisassembly(frame)}" }
    }
}
