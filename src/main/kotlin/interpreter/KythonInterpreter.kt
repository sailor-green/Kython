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
@file:Suppress("MemberVisibilityCanBePrivate")

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.kyobject.KyModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.util.CPythonCompiler
import java.nio.file.Files
import java.nio.file.Path

// todo: config params?

/**
 * Represents the main interpreter object. A number of properties for this object are exposed under the `sys` module.
 * There is only one instance of this; interpreters on threads are ran with a raw StackFrame object on a thread.
 *
 */
object KythonInterpreter {
    /** The CPython compiler backend. */
    val cpyInterface = CPythonCompiler()

    /** The root frame thread local storage for each thread. */
    val rootFrameLocal = ThreadLocal<StackFrame>()

    /** The current stack frame for each thread. */
    val currentStackFrameLocal = ThreadLocal<StackFrame>()

    /** The mapping of modules. */
    val modules = mutableMapOf<String, KyModule>()

    /**
     * Gets the root frame for this thread.
     */
    fun getRootFrameForThisThread(): StackFrame = rootFrameLocal.get()

    /**
     * Gets the current stack frame for this thread.
     */
    fun getCurrentFrameForThisThread(): StackFrame = currentStackFrameLocal.get()

    /**
     * Runs Python code from a file.
     */
    fun runPythonFromPath(path: Path) {
        val fn = cpyInterface.compile(path)

        val rootFunction = PyUserFunction(fn)
        val module = KyModule(rootFunction, path.toString(), Files.readAllLines(path))
        modules["__main__"] = module

        kickoffThread(module.stackFrame, child = false)
    }

    /**
     * Runs Python code from a string. Used for `-c` invocation.
     */
    fun runPythonFromString(s: String) {
        val fn = cpyInterface.compile(s)

        val rootFunction = PyUserFunction(fn)
        val module = KyModule(rootFunction, "<code>", s.split(System.lineSeparator()))
        modules["__main__"] = module

        kickoffThread(module.stackFrame, child = false)
    }

    /**
     * Builds a module from a module function.
     * This is the main entry point to import a new source-code module.
     *
     * @param moduleFunction: The module function that has been unmarshalled.
     * @param sourcePath: The source path for the module.
     */
    fun buildModule(moduleFunction: PyUserFunction, sourcePath: Path): KyModule {
        return KyModule(moduleFunction, sourcePath.toString(), Files.readAllLines(sourcePath))
            .also { runStackFrame(it.stackFrame, mapOf()) }
    }

    /**
     * Runs a stack frame.
     */
    fun runStackFrame(frame: StackFrame, args: Map<String, PyObject>): PyObject {
        val parent = currentStackFrameLocal.get()
        parent?.let {
            frame.parentFrame = it
            it.childFrame = frame
        }

        currentStackFrameLocal.set(frame)
        val result = frame.runFrame(args)
        frame.parentFrame = null

        parent?.let {
            it.childFrame = null
            currentStackFrameLocal.set(it)
        }

        return result
    }

    /**
     * Runs the root frame of a thread.
     */
    fun runRootFrame(frame: StackFrame): PyObject {
        rootFrameLocal.set(frame)
        val result = runStackFrame(frame, mapOf())
        rootFrameLocal.remove()
        return result
    }

    /**
     * Wraps a root frame to handle otherwise unhandled KyErrors. Use [kickoffThread] instead if
     * you're invoking the interpreter normally, and [runRootFrame] if you want to just invoke the
     * interpreter.
     */
    fun wrapKythonThread(rootFrame: StackFrame) {
        try {
            runRootFrame(rootFrame)
        } catch (e: KyError) {
            // throw e  // used for debugging
            // bubbled error, means no user code handled it
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
        }
    }

    /**
     * Kicks off an interpreter thread.
     *
     * @param child: If this is a child thread. If false, exceptions will be fatal.
     */
    fun kickoffThread(frame: StackFrame, child: Boolean = true) {
        try {
            wrapKythonThread(frame)
        } catch (e: Throwable) { // blah blah, bad practice, who cares

            System.err.println("Fatal interpreter error!")
            e.printStackTrace(System.err)
            System.err.println("\nKython stack (most recent frame first):")

            val stacks = StackFrame.flatten(frame).reversed()
            stacks.forEach {
                with(it.createStackFrameInfo()) {
                    System.err.println("   $tracebackString")
                    disassembly?.let { dis ->
                        System.err.println("Disassembly:\n$dis")
                    }
                    stack?.let { stack ->
                        System.err.println("Function stack, size: ${stack.size}")
                        stack.forEachIndexed { index, pyObject ->
                            System.err.println("    $index: $pyObject")
                        }
                    }
                }
            }
        }

        // if (!child) {
        //    throw e
        // }
    }
}
