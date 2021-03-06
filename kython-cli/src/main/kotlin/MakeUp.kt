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

package green.sailor.kython

import green.sailor.kython.cli.PythonFileCommand
import green.sailor.kython.interpreter.KythonInterpreter
import java.util.concurrent.Callable
import kotlin.system.exitProcess
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

/**
 * Main initialiser for Kython.
 */
@Command(
    name = "kython",
    mixinStandardHelpOptions = true,
    version = [
        "@|cyan Kython 3.8.0|@",
        "@|cyan JVM: \${java.version} (\${java.vendor} \${java.vm.name} \${java.vm.version})|@",
        "@|cyan OS: \${os.name} \${os.version} \${os.arch}|@"
    ],
    subcommands = [
        HelpCommand::class,
        PythonFileCommand::class
    ]
)
object MakeUp : Callable<Int> {
    const val DEFAULT_IMPORTER = "green.sailor.kython.interpreter.importing.SimpleImporter"

    // == CLI PROPERTIES == //
    @CommandLine.Option(
        names = ["-d"],
        description = ["Enables debug mode."]
    )
    @JvmStatic fun setDebugMode(mode: Boolean) {
        KythonInterpreter.config.debugMode = true
    }

    @CommandLine.Option(
        names = ["--importer"],
        description = ["Changes the default importer to use."],
        defaultValue = DEFAULT_IMPORTER,
        type = [String::class]
    )
    lateinit var importer: String

    /**
     * CLI entry point.
     */
    override fun call(): Int {
        // this should never happen
        return 0
    }

    /**
     * JVM entry point.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (KythonInterpreter.config.debugMode) {
            System.err.println("Running Kython in debug mode!")
        }

        exitProcess(CommandLine(this).execute(*args))
    }
}
