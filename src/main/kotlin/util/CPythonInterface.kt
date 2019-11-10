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

package green.sailor.kython.util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Represents the interface to CPython, used for compiling files into bytecode.
 */
@ExperimentalStdlibApi
class CPythonInterface(val workingDir: Path) {
    companion object {
        /**
         * Runs a CPython command, and returns the stdout.
         */
        fun runCPythonCommand(args: List<String>, workingDir: Path): String {
            val builder = ProcessBuilder()
            builder.command(listOf("python3") + args)
            builder.directory(workingDir.toFile())
            val process = builder.start()

            val exit = process.waitFor()
            if (exit != 0) {
                val stderr = process.errorStream.readAllBytes().decodeToString()
                error("CPython returned exit code $exit: $stderr")
            }
            return process.inputStream.readAllBytes().decodeToString()
        }

    }

    data class CPythonVersion(val major: Int, val minor: Int, val patch: Int) {
        val fullVersion: String = "${major}.${minor}.$patch"
        val usefulVersion: String = "${major}.$minor"
        val pycIdentifier: String = "cpython-$major$minor"
    }

    /** The CPython version being used. */
    public val version = getCPythonVersion()

    /**
     * Gets the CPython version.
     */
    fun getCPythonVersion(): CPythonVersion {
        val stdout = runCPythonCommand(listOf("-V"), workingDir).dropLast(1)
        // let's just make sure
        require(stdout.startsWith("Python")) { "CPython returned bizarre value $stdout" }

        val split = stdout.split(" ")[1].split(".").map { it.toInt() }
        return CPythonVersion(split[0], split[1], split[2])
    }

    /**
     * Compiles all Python files using CPython.
     */
    fun compileAllFiles(entrypoint: Path) {
        runCPythonCommand(listOf("-m", "compileall", entrypoint.parent.toString()), this.workingDir)
    }

    /**
     * Gets the pyc filename of an input file.
     */
    fun getPycFilename(input: String): String {
        return "__pycache__/${input}.${version.pycIdentifier}.pyc"
    }

    /**
     * Gets the Path of a compiled CPython module, from a full package specification.
     */
    fun getCompiledFilename(module: String): Path {
        // split by package
        val subpackages = module.split(".")
        val resolved = if (subpackages.size == 1) {
            // raw file
            this.workingDir.resolve(subpackages[0])
        } else {
            val final = subpackages.last()
            var currentDir = this.workingDir
            for (pkg in subpackages.dropLast(1)) {
                currentDir = currentDir.resolve(pkg)
                assert(Files.exists(currentDir)) { "tried to load $currentDir when it doesn't exist" }
            }

            currentDir.resolve(final)
        }

        // test the final for a .py, or if its a subdir
        val testFile = Paths.get("$resolved.py")
        val realFile = if (Files.exists(testFile)) {
            // __pycache__ is in same dir as resolved...
            val parent = testFile.parent
            val fname = testFile.fileName.toString().dropLast(3)
            parent.resolve(getPycFilename(fname))
        } else {
            // __pycache__ is in the module directory
            resolved.resolve(getPycFilename("__init__"))
        }

        assert(Files.exists(realFile))
        return realFile
    }
}
