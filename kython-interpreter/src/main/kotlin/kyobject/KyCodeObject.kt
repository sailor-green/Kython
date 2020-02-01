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

package green.sailor.kython.interpreter.kyobject

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.instruction.Instruction
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.instruction.PythonInstruction
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.unwrap
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
    val flags = CodeFlags(original.flags.wrapped)

    /** The raw bytecode for this function. */
    val rawBytecode = original.bytecode.wrapped

    /** The constants for this function. */
    val consts = original.consts.wrapped.map { it.unwrap() }

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
    val instructions = parseInstructions()

    /**
     * Parses the instructions of this code object.
     */
    fun parseInstructions(): Array<Instruction> {
        if (KythonInterpreter.config.debugMode) {
            System.err.println("=== Loading instructions for $codename / $filename ===")
        }

        val instructions = mutableListOf<Instruction>()
        val buf = ByteBuffer.wrap(rawBytecode)

        while (buf.hasRemaining()) {
            // Prevents opcodes >128 from turning into -opcode.
            val opcode = buf.get().toUByte().toInt()
            val opval = buf.get()
            instructions.add(PythonInstruction(InstructionOpcode.get(opcode), opval))
        }

        if (KythonInterpreter.config.debugMode) {
            System.err.println("=== Loaded ${instructions.size} instructions ===")
        }

        return instructions.toTypedArray()
    }

    /**
     * Gets a newline separated disassembly for this code object.
     */
    fun getDisassembly(frame: UserCodeStackFrame): String {
        val padSize = ceil(log(instructions.size.toDouble(), 10.0)).toInt()

        return buildString {
            val jumpTargets = mutableMapOf<Int, MutableList<Int>>()
            var lastLineNum = -1

            for ((idx, instruction) in instructions.withIndex()) {
                if (instruction !is PythonInstruction) TODO()

                val lineNum = lnotab.getLineNumberFromIdx(idx)
                if (lineNum != lastLineNum) {
                    appendln()
                    append("${firstline + lineNum}")
                    append("   ")
                    append(frame.function.module.sourceLines[firstline + lineNum - 1].trimIndent())
                    appendln()
                    lastLineNum = lineNum
                }

                if (instruction.opcode.hasRelJump) {
                    val realAmount = instruction.argument.toInt() / 2
                    val realIdx = idx + realAmount + 1
                    (jumpTargets.computeIfAbsent(realIdx) { mutableListOf() }).add(idx)
                }
                if (instruction.opcode.hasAbsJump) {
                    val realIdx = instruction.argument.toInt() / 2
                    (jumpTargets.computeIfAbsent(realIdx) { mutableListOf() }).add(idx)
                }
                val idxFmt = idx.toString().padStart(padSize, '0')
                append("    $idxFmt ")
                append(instruction.getDisassembly(frame))
                val ourPos = jumpTargets[idx]
                if (ourPos != null) {
                    append(" (jump from: ${ourPos.joinToString { it.toString() }})")
                }

                if (idx == frame.bytecodePointer) {
                    append("  <-- HERE")
                }
                appendln()
            }
        }
    }

    /**
     * Gets the line number of code from the instruction index.
     */
    fun getLineNumber(idx: Int): Int {
        return (firstline - 1) + lnotab.getLineNumberFromIdx(idx)
    }
}
