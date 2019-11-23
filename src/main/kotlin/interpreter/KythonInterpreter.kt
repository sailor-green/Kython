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

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.kyobject.KyModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.util.CPythonCompiler
import java.nio.file.Path

// todo: config params?

/**
 * Represents the main interpreter object. A number of properties for this object are exposed under the `sys` module.
 * There is only one instance of this; interpreters on threads are ran with a raw StackFrame object on a thread.
 *
 * @param mainFile: The main file to be invoked.
 */
object KythonInterpreter {
    /** The CPython compiler backend. */
    val cpyInterface = CPythonCompiler("/usr/bin/python3")

    /** The root frame thread local storage for each thread. */
    val rootFrameLocal = ThreadLocal<StackFrame>()

    /** The current stack frame for each thread. */
    val currentStackFrameLocal = ThreadLocal<StackFrame>()

    /** The mapping of modules. */
    val modules = mutableMapOf<String, KyModule>()

    /**
     * Gets the root frame for this thread.
     */
    fun getRootFrameForThisThread(): StackFrame {
        return this.rootFrameLocal.get()
    }

    /**
     * Gets the current stack frame for this thread.
     */
    fun getCurrentFrameForThisThread(): StackFrame {
        return this.currentStackFrameLocal.get()
    }

    /**
     * The main entry point for the interpreter.
     */
    @ExperimentalStdlibApi
    fun runPython(path: Path) {
        val fn = this.cpyInterface.compile(path)

        // todo: make this work properly
        val rootFunction = PyUserFunction(fn)
        val module = KyModule(rootFunction, path)
        this.modules["__main__"] = module

        // todo: wrap this
        this.kickoffThread(module.stackFrame, child = false)
    }

    /**
     * Builds a module from a module function.
     * This is the main entry point to import a new source-code module.
     *
     * @param moduleFunction: The module function that has been unmarshalled.
     * @param sourcePath: The source path for the module.
     */
    fun buildModule(moduleFunction: PyUserFunction, sourcePath: Path): KyModule {
        val module = KyModule(moduleFunction, sourcePath)
        this.runStackFrame(module.stackFrame, mapOf())
        return module
    }

    /**
     * Runs a stack frame.
     */
    fun runStackFrame(
        frame: StackFrame,
        args: Map<String, PyObject>
    ): PyObject {
        val parent: StackFrame? = this.currentStackFrameLocal.get()
        if (parent != null) {
            frame.parentFrame = parent
            parent.childFrame = frame
        }
        this.currentStackFrameLocal.set(frame)
        val result = frame.runFrame(args)
        frame.parentFrame = null

        if (parent != null) {
            parent.childFrame = null
            this.currentStackFrameLocal.set(parent)
        }

        return result
    }

    /**
     * Runs a python thread.
     */
    fun runPythonThread(rootFrame: StackFrame) {
        this.rootFrameLocal.set(rootFrame)

        try {
            val result = this.runStackFrame(rootFrame, mapOf())
        } catch (e: KyError) {
            // bubbled error, means no user code handled it
            val error = e.wrapped
            val errorName = error.type.name
            System.err.println("\nKython stack (most recent frame last):")
            for (frame in error.exceptionFrames) {
                val info = frame.getStackFrameInfo()
                System.err.println("   " + info.getTracebackString())
            }
            val builder = StringBuilder()
            for (arg in error.args.subobjects) {
                builder.append(arg.getPyStringSafe().wrappedString)
                builder.append(" ")
            }
            System.err.println("${errorName}: $builder")
        }
    }

    /**
     * Kicks off an interpreter thread.
     *
     * @param child: If this is a child thread. If false, exceptions will be fatal.
     */
    fun kickoffThread(frame: StackFrame, child: Boolean = true) {
        try {
            this.runPythonThread(frame)
        } catch (e: Throwable) {  // blah blah, bad practice, who cares

            System.err.println("Fatal interpreter error!")
            e.printStackTrace(System.err)
            System.err.println("\nKython stack (most recent frame first):")

            val stacks = StackFrame.flatten(frame).reversed()
            for (frame in stacks) {
                val info = frame.getStackFrameInfo()
                System.err.println("   " + info.getTracebackString())
                if (info.hasDisassembly) {
                    val disassembly = info.getDisassembly()
                    System.err.println("Disassembly:\n$disassembly")
                }
                if (info.hasStack) {
                    val stack = info.getStack()
                    System.err.println("Function stack, size: ${stack.size}")
                    for ((idx, pyo) in stack.iterator().withIndex()) {
                        System.err.println("    $idx: $pyo")
                    }
                }
            }

            //if (!child) {
            //    throw e
            //}
        }
    }
}
