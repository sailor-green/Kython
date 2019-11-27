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
package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import java.nio.file.Files
import java.util.*

/**
 * Represents stack frame information for a specific stack frame.
 */
abstract class StackFrameInfo {

    /** The name of this stack frame. */
    abstract val name: PyString

    /** The filename of this stack frame. */
    abstract val filename: PyString

    /** The disassmebly for this frame */
    open val disassembly: String? = null

    /** The stack for this frame */
    open val stack: Deque<PyObject>? = null

    /** Gets the traceback string for this stack frame, unindented. */
    abstract val tracebackString: String

    class UserFrameInfo(val frame: UserCodeStackFrame) : StackFrameInfo() {
        override val name: PyString
            get() = PyString(frame.function.code.codename)

        override val filename: PyString get() = PyString(frame.function.code.filename)

        override val disassembly: String get() = frame.function.code.getDisassembly(frame)

        override val stack: Deque<PyObject> get() = frame.stack

        override val tracebackString: String
            get() {
                val sourceLines = Files.readAllLines(frame.function.module.path)
                val lineNo = frame.lineNo
                val sourceLine = sourceLines[lineNo].trimIndent()

                return with(frame) {
                    buildString {
                        append("File ${function.code.filename}, ")
                        append("instruction idx ${frame.bytecodePointer}, ")
                        append("line $lineNo, ")
                        append("in ${function.code.codename}\n")
                        append("    $sourceLine")
                    }
                }
            }
    }

    class BuiltinFrameInfo(val frame: BuiltinStackFrame) : StackFrameInfo() {
        override val name: PyString
            get() = PyString(frame.builtinFunction.name)

        override val filename: PyString
            get() = PyString("<builtin>")

        override val tracebackString: String
            get() {
                return "File <builtin>, in ${frame.builtinFunction.name}"
            }
    }
}
