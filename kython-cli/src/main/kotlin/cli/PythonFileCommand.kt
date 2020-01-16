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

package green.sailor.kython.cli

import green.sailor.kython.interpreter.KythonInterpreter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(
    name = "file",
    description = ["Runs a Python file"]
)
object PythonFileCommand : Callable<Int> {
    @Parameters(index = "0", description = ["The Python file to run."])
    var filename: String = ""

    @Parameters(arity = "0..*", description = ["The arguments to provide to the file."])
    var args: MutableList<String> = mutableListOf()

    override fun call(): Int {
        val path = Paths.get(filename).toAbsolutePath()
        if (!Files.exists(path)) {
            System.err.println("Cannot open file $filename: Does not exist")
            return 1
        }
        if (Files.isDirectory(path)) {
            System.err.println("Cannot open file $filename: File is a directory")
            return 1
        }
        if (!Files.isReadable(path)) {
            System.err.println("Cannot open file $filename: File is not readable")
            return 1
        }

        KythonInterpreter.runPythonFromPath(Paths.get(
            filename
        ).toAbsolutePath())
        return 0
    }
}
