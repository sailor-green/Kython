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

package green.sailor.kython.interpreter.thread

import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.util.FrameStack
import java.util.*
import org.apiguardian.api.API

/**
 * The base class for an interpreter thread.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class InterpreterThread(val rootStackFrame: StackFrame) {
    /** The frame stack for this thread. */
    open val frameStack = FrameStack()

    /** The top-most stack frame. */
    open val currentStackFrame: StackFrame? get() = frameStack.current

    /**
     * Pushes a stack frame onto the list of stack frames.
     */
    open fun pushFrame(frame: StackFrame) {
        if (KythonInterpreter.config.debugMode) {
            val info = frame.createStackFrameInfo()
            System.err.println("=== Pushing frame: ${info.name} / ${frame.state} ===")
        }

        frameStack.push(frame)
    }

    /**
     * Pops the top-most stack frame from the list of stack frames.
     */
    open fun popFrame(): StackFrame {
        val topFrame = frameStack.pop()
        if (KythonInterpreter.config.debugMode) {
            val info = topFrame.createStackFrameInfo()
            System.err.println("=== Popped frame: ${info.name} / ${topFrame.state} ===")
        }
        return topFrame
    }

    /**
     * Runs a stack frame on this thread.
     */
    @API(status = API.Status.MAINTAINED)
    open fun runStackFrame(frame: StackFrame, args: Map<String, PyObject>): PyObject {
        pushFrame(frame)

        val result = frame.runFrame(args)

        popFrame()

        return result
    }

    /**
     * Wraps a root frame to handle otherwise unhandled KyErrors.
     */
    @API(status = API.Status.INTERNAL)
    open fun internalWrapTraceback(rootFrame: StackFrame) {
        try {
            runStackFrame(rootFrame, mapOf())
        } catch (e: KyError) {
            if (KythonInterpreter.config.debugMode) {
                throw e
            }

            // bubbled error, means no user code handled it
            val thread = Thread.currentThread()
            System.err.println("Exception in thread ${thread.name}")
            with(e.wrapped) {
                this as PyObject

                System.err.println("\nKython stack (most recent frame last):")
                exceptionFrames.forEach {
                    System.err.println("   " + it.createStackFrameInfo().tracebackString)
                }

                val errorString = args.joinToString(" ") {
                    it.getPyStringSafe().wrappedString
                }
                System.err.println("${type.name}: $errorString")
            }
        }
    }

    /**
     * Runs this thread, wrapping internal errors in a full dump.
     */
    @API(status = API.Status.INTERNAL)
    open fun internalRunThreadWithErrorLogs(rethrow: Boolean = true) {
        try {
            internalWrapTraceback(rootStackFrame)
        } catch (e: Throwable) { // blah blah, bad practice, who cares
            System.err.println("\n▉▉▉  Fatal interpreter error! ▉▉▉")
            if (!rethrow) e.printStackTrace(System.err)
            System.err.println("\nKython stack (most recent frame first):")

            val stacks = frameStack.toList()
            for ((idx, frame) in stacks.withIndex()) {
                System.err.println("Frame $idx:")
                with(frame.createStackFrameInfo()) {
                    System.err.println("   $tracebackString")
                    disassembly?.let { dis ->
                        System.err.println("\nDisassembly:\n$dis")
                    }
                    stack?.let { stack ->
                        val size = stack.size
                        if (size != 0) {
                            System.err.println("Function stack, size: ${stack.size}")
                            stack.forEachIndexed { index, pyObject ->
                                System.err.println("    $index: $pyObject")
                            }
                        } else {
                            System.err.println("Function stack is empty")
                        }
                    }
                    locals.let {
                        System.err.println("Local variables:")
                        for ((name, value) in locals.entries) {
                            System.err.println("    - $name -> $value")
                        }
                    }
                }
                // newline
                System.err.println()
            }
            if (rethrow) throw e
        }
    }

    /**
     * Runs this interpreter thread.
     */
    abstract fun runThread()
}
