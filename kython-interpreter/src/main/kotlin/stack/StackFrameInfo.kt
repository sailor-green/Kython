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

package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.util.PythonFunctionStack

/**
 * Represents stack frame information for a specific stack frame.
 */
abstract class StackFrameInfo {

    /** The name of this stack frame. */
    abstract val name: String

    /** The filename of this stack frame. */
    abstract val filename: String

    /** The disassmebly for this frame */
    open val disassembly: String? = null

    /** The stack for this frame */
    open val stack: PythonFunctionStack? = null

    /** Gets the traceback string for this stack frame, un-indented. */
    abstract val tracebackString: String

    /** Gets the local variables for this stack frame. */
    open val locals: Map<String, PyObject> = mapOf()

    class UserFrameInfo(val frame: UserCodeStackFrame) : StackFrameInfo() {
        override val name get() = frame.function.code.codename
        override val filename get() = frame.function.code.filename
        override val disassembly get() = frame.function.code.getDisassembly(frame)
        override val stack get() = frame.stack
        override val locals get() = frame.locals

        override val tracebackString: String
            get() {
                val sourceLines = frame.function.module.sourceLines
                val lineNo = frame.lineNo
                val sourceLine = sourceLines[lineNo].trimIndent()

                return with(frame) {
                    buildString {
                        append("File ${function.code.filename}, ")
                        append("instruction idx ${frame.bytecodePointer}, ")
                        append("line ${lineNo + 1}, ")
                        append("in ${function.code.codename}\n")
                        append("    $sourceLine")
                    }
                }
            }
    }

    class BuiltinFrameInfo(val frame: BuiltinStackFrame) : StackFrameInfo() {
        override val name: String get() = frame.builtinFunction.name

        override val filename get() = frame.kotlinFunctionClassName

        override val tracebackString: String
            get() = "File <builtin>, in ${frame.builtinFunction.name}, at <kotlin $filename>"
    }
}
