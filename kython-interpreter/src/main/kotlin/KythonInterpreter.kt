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

@file:Suppress("MemberVisibilityCanBePrivate")

package green.sailor.kython.interpreter

import green.sailor.kython.generation.generated.addAllMethods
import green.sailor.kython.interpreter.builtins.KythonInternalModule
import green.sailor.kython.interpreter.builtins.SysModule
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.pyobject.module.PyModule
import green.sailor.kython.interpreter.pyobject.module.PyUserModule
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.thread.InterpreterThread
import green.sailor.kython.interpreter.thread.MainInterpreterThread
import green.sailor.kython.interpreter.util.CPythonCompiler
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the main interpreter object. A number of properties for this object are exposed under
 * the `sys` module.
 *
 * This is a singleton encapsulating several [InterpreterThread] objects.
 */
object KythonInterpreter {
    /** The CPython compiler backend. */
    val cpyInterface = CPythonCompiler()

    /** The mapping of modules. */
    val modules = mutableMapOf<String, PyModule>()

    /** The thread-local for the current [InterpreterThread]. */
    val interpreterThreadLocal = ThreadLocal<InterpreterThread>()

    /** The config for the current interpreter. */
    val config = InterpreterConfig()

    /**
     * The list of threads for this interpreter.
     *
     * Index 0 is always the main thread object.
     */
    val threads = mutableListOf<InterpreterThread>()

    init {
        // add all the generated builtins
        addAllMethods()

        // add builtin modules
        modules["sys"] = SysModule
        modules["__kython_internal"] = KythonInternalModule
    }

    /**
     * Gets the root frame for this thread.
     */
    fun getRootFrameForThisThread(): StackFrame = interpreterThreadLocal.get().rootStackFrame

    /**
     * Gets the current stack frame for this thread.
     */
    fun getCurrentFrameForThisThread(): StackFrame =
        interpreterThreadLocal.get().currentStackFrame
            ?: error("There is nothing running on this thread?")

    /**
     * Runs a stack frame.
     */
    fun runStackFrame(frame: StackFrame, args: Map<String, PyObject>): PyObject {
        val thread = interpreterThreadLocal.get()
        return thread.runStackFrame(frame, args)
    }

    /**
     * Runs the main thread.
     */
    fun runMainThread(frame: StackFrame) {
        // temporarily disabled!
        // if (!MakeUp.noBootstrap) {
        //     val bootstrap = Bootstrapper.build()
        //     bootstrap.runThread()
        // }

        // TODO: Configure importers.

        val interpreterThread = MainInterpreterThread(frame)
        threads.add(interpreterThread)
        // dont kick off a separate thread
        interpreterThread.runThread()
    }

    /**
     * Runs Python code from a file.
     */
    fun runPythonFromPath(path: Path) {
        val fn = cpyInterface.compile(path)

        val rootFunction =
            PyUserFunction(fn)
        val module = KyUserModule(rootFunction, path.toString(), Files.readAllLines(path))
        module.attribs["__name__"] = PyString("__main__")
        modules["__main__"] = PyUserModule(module, "__main__")

        runMainThread(module.stackFrame)
    }

    /**
     * Runs Python code from a string. Used for `-c` invocation.
     */
    fun runPythonFromString(s: String) {
        val fn = cpyInterface.compile(s)

        val rootFunction = PyUserFunction(fn)
        val module = KyUserModule(rootFunction, "<code>", s.split(System.lineSeparator()))
        modules["__main__"] = PyUserModule(module, "__main__")

        runMainThread(module.stackFrame)
    }

    /**
     * Builds a module from a module function.
     * This is the main entry point to import a new source-code module.
     *
     * @param moduleFunction: The module function that has been unmarshalled.
     * @param sourcePath: The source path for the module.
     */
    fun buildModule(moduleFunction: PyUserFunction, sourcePath: Path): KyUserModule {
        return KyUserModule(moduleFunction, sourcePath.toString(), Files.readAllLines(sourcePath))
            .also { runStackFrame(it.stackFrame, mapOf()) }
    }
}
