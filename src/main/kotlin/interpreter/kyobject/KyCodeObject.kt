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
package green.sailor.kython.interpreter.kyobject

import green.sailor.kython.interpreter.instruction.Instruction
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.kyc.KycCodeObject
import green.sailor.kython.util.Lnotab
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.log

/**
 * Represents a Python code object. (Exposed as __code__ on a function).

 * @param original: The marshalled code object to build from.
 */
@Suppress("unused")
class KyCodeObject(original: KycCodeObject) {
    companion object {
        // code flags

        // not sure
        const val CO_OPTIMISED = 1
        const val CO_NEWLOCALS = 2

        // function has an *args argument
        const val CO_HAS_VARARGS = 4 // CO_VARARGS

        // function has a **kwargs argument
        const val CO_HAS_VARKWARGS = 8 // CO_VARKWARGS

        // function is nested
        const val CO_NESTED = 16

        // function is a generator
        const val CO_GENERATOR = 32

        // not sure
        const val CO_NOFREE = 64

        // async function
        const val CO_ASYNC_FUNCTION = 128 // CO_COROUTINE

        // not sure
        const val CO_ITERABLE_ASYNC_FUNCTION = 256

        // async generator
        const val CO_ASYNC_GENERATOR = 256
    }

    /** The argument count for the function. */
    val argCount: Int = original.argCount.wrapped

    /** The number of positional only arguments. Always zero. */
    val posOnlyArgCount: Int = original.posOnlyArgCount.wrapped

    /** The number of keyword only arguments. */
    val kwOnlyArgCount: Int = original.kwOnlyArgCount.wrapped

    /** The number of local variables. */
    val localCount = original.localCount.wrapped

    /** The stack size for this function. */
    val stackSize = original.stackSize.wrapped

    /** The flags for this function. */
    val flags = original.flags.wrapped

    /** The raw bytecode for this function. */
    val rawBytecode = original.bytecode.wrapped

    // TODO: Unwrap these into real objects.
    /** The constants for this function. */
    val consts = original.consts.wrapped.map { it.wrap() }

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
    val lnotab = Lnotab(original.lnotab.wrapped)

    // special properties
    val instructions by lazy { this.parseInstructions() }

    /**
     * Parses the instructions of this code object.
     */
    fun parseInstructions(): Array<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val buf = ByteBuffer.wrap(this.rawBytecode)
        while (buf.hasRemaining()) {
            val opcode = buf.get().toUByte().toInt() // prevents opcodes >128 from turning into -opcode
            val opval = buf.get()
            instructions.add(Instruction(InstructionOpcode.get(opcode), opval))
        }

        return instructions.toTypedArray()
    }

    /**
     * Gets a newline separated disassembly for this code object.
     */
    fun getDisassembly(frame: UserCodeStackFrame): String {
        val builder = StringBuilder()
        val padSize = ceil(log(this.instructions.size.toDouble(), 10.0)).toInt()

        for ((idx, ins) in this.instructions.withIndex()) {
            val idxFmt = idx.toString().padStart(padSize, '0')
            builder.append("    $idxFmt ")
            builder.append(ins.getDisassembly(frame))
            // add a nice arrow
            if (idx == frame.bytecodePointer) {
                builder.append("  <-- HERE")
            }
            builder.append("\n")
        }

        return builder.toString()
    }

    /**
     * Gets the line number of code from the instruction index.
     */
    fun getLineNumber(idx: Int): Int {
        return (this.firstline - 1) + this.lnotab.getLineNumberFromIdx(idx)
    }
}
