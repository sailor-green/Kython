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

package green.sailor.kython.interpreter

import green.sailor.kython.MakeUp
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.StackFrame
import kotlin.system.exitProcess

/**
 * Represents an interpreter thread.
 */
class InterpreterThread(
    /** The root stack frame for this thread. */
    var rootStackFrame: StackFrame
) : Runnable {
    // lazy to avoid creating a new thread for the main thread, when it's not needed.
    /** The wrapped Java thread object. */
    val thread by lazy { Thread(this) }

    /** The current executing stack frame for this thread. */
    var currentStackFrame: StackFrame? = null

    fun start() = thread.start()

    override fun run() {
        KythonInterpreter.interpreterThreadLocal.set(this)
        kickoffThread(rootStackFrame)
    }

    /**
     * Runs a stack frame on this thread.
     */
    fun runStackFrame(frame: StackFrame, args: Map<String, PyObject>): PyObject {
        val parent = currentStackFrame
        parent?.let {
            frame.parentFrame = it
            it.childFrame = frame
        }

        currentStackFrame = frame
        val result = frame.runFrame(args)
        frame.parentFrame = null

        parent?.let {
            it.childFrame = null
            currentStackFrame = it
        }

        return result
    }

    /**
     * Wraps a root frame to handle otherwise unhandled KyErrors. Use [kickoffThread] instead if
     * you're invoking the interpreter normally, and [runRootFrame] if you want to just invoke the
     * interpreter.
     */
    fun wrapTraceback(rootFrame: StackFrame, rootThread: Boolean) {
        try {
            runStackFrame(rootFrame, mapOf())
        } catch (e: KyError) {
            if (MakeUp.debugMode) {
                throw e
            }

            // bubbled error, means no user code handled it
            val thread = Thread.currentThread()
            System.err.println("Exception in thread ${thread.name}")
            with(e.wrapped) {
                System.err.println("\nKython stack (most recent frame last):")
                exceptionFrames.forEach {
                    System.err.println("   " + it.createStackFrameInfo().tracebackString)
                }

                val errorString = args.subobjects.joinToString(" ") {
                    it.getPyStringSafe().wrappedString
                }
                System.err.println("${type.name}: $errorString")
            }

            // exit with status code 1 if we hit an error
            if (rootThread) {
                exitProcess(1)
            }
        }
    }

    /**
     * Kicks off an interpreter thread.
     *
     * @param child: If this is a child thread. If false, exceptions will be fatal.
     */
    fun kickoffThread(frame: StackFrame, root: Boolean = true) {
        try {
            wrapTraceback(frame, root)
        } catch (e: Throwable) { // blah blah, bad practice, who cares
            System.err.println("Fatal interpreter error!")
            e.printStackTrace(System.err)
            System.err.println("\nKython stack (most recent frame first):")

            val stacks = StackFrame.flatten(frame).reversed()
            stacks.forEachIndexed { idx, it ->
                System.err.println("Frame $idx:")
                with(it.createStackFrameInfo()) {
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
                }
                // newline
                System.err.println()
            }
        }
    }
}
