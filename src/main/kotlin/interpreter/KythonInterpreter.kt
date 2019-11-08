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

import green.sailor.kython.interpreter.objects.KyFunction
import green.sailor.kython.interpreter.objects.python.PyDict
import green.sailor.kython.interpreter.objects.python.PyTuple
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.marshal.Marshaller
import green.sailor.kython.util.CPythonInterface
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

// todo: config params?

/**
 * Represents the main interpreter object. A number of properties for this object are exposed under the `sys` module.
 * There is only one instance of this; interpreters on threads are ran with a raw StackFrame object on a thread.
 *
 * @param mainFile: The main file to be invoked.
 */
object KythonInterpreter {

    lateinit var path: Path

    fun configure(path: Path) {
        this.path = path
    }

    /** The CPython compiler backend. */
    @ExperimentalStdlibApi
    val cpyInterface = CPythonInterface(Paths.get("."))

    /**
     * The main entry point for the interpreter.
     */
    @ExperimentalStdlibApi
    fun runPython() {
        val version = cpyInterface.version
        if (version.minor < 6) {
            System.err.println("Required at least Python 3.6, got ${version.major}.${version.minor}.${version.patch}")
            exitProcess(1)
        }
        println("Using CPython ${version.major}.${version.minor}.${version.patch} for bytecode compilation")

        // compile all in working directory
        println("Calling CPython to produce bytecode...")
        cpyInterface.compileAllFiles()

        // todo: make this work properly
        val mainFile = cpyInterface.getPycFilename(this.path.fileName.toString())
        val marshalled = Marshaller.parsePycFile(Paths.get(mainFile))

        val rootFunction = KyFunction(marshalled)

        // todo: wrap this
        this.kickoffThread(rootFunction, child = false)
    }

    /**
     * Runs a python thread.
     */
    fun runPythonThread(rootFrame: UserCodeStackFrame) {
        rootFrame.runFrame(PyTuple.EMPTY, PyDict.EMPTY)
    }

    /**
     * Kicks off an interpreter thread.
     *
     * @param child: If this is a child thread. If false, exceptions will be fatal.
     */
    fun kickoffThread(function: KyFunction, child: Boolean = true) {
        val frame = UserCodeStackFrame(function)
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
