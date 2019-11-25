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

    /** If this frame has disassembly. */
    open val hasDisassembly: Boolean = false

    /** If this frame has a stack. */
    open val hasStack: Boolean = false

    open fun getDisassembly(): String {
        error("This frame has no valid disassembly (is a builtin?)")
    }

    open fun getStack(): Deque<PyObject> {
        error("This frame has no stack (is a builtin?)")
    }

    /** Gets the traceback string for this stack frame, unindented. */
    abstract fun getTracebackString(): String

    class UserFrameInfo(val frame: UserCodeStackFrame) : StackFrameInfo() {
        override val name: PyString
            get() = PyString(frame.function.code.codename)

        override val filename: PyString
            get() = PyString(frame.function.code.filename)

        override val hasDisassembly: Boolean = true
        override val hasStack: Boolean = true

        override fun getDisassembly(): String {
            return this.frame.function.code.getDisassembly(this.frame)
        }

        override fun getStack(): Deque<PyObject> {
            return this.frame.stack
        }

        override fun getTracebackString(): String {
            val module = this.frame.function.module
            val sourceFile = module.path
            val sourceLines = Files.readAllLines(sourceFile)
            val lineNo = this.frame.getLineNo()
            val sourceLine = sourceLines[lineNo].trimIndent()

            return "File ${frame.function.code.filename}, " +
                    "instruction idx ${frame.bytecodePointer}, " +
                    "line ${frame.getLineNo()}, " +
                    "in ${frame.function.code.codename}\n" +
                    "    $sourceLine"
        }
    }

    class BuiltinFrameInfo(val frame: BuiltinStackFrame) : StackFrameInfo() {
        override val name: PyString
            get() = PyString(frame.builtinFunction.name)

        override val filename: PyString
            get() = PyString("<builtin>")

        override fun getTracebackString(): String {
            return "File <builtin>, in ${frame.builtinFunction.name}"
        }
    }
}
