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

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.cli.PythonFileCommand
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.types.PyStringType
import java.util.concurrent.Callable
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
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
    // == JAVA PROPERTIES == //
    @JvmStatic
    val debugMode = System.getProperty("kython.interpreter.debug") == "true"

    // == CLI PROPERTIES == //

    /**
     * CLI entry point.
     */
    override fun call(): Int {
        // this should never happen
        return 0
    }

    /**
     * Builds the builtin method objects from annotations.
     */
    @Suppress("unused")
    fun buildBuiltinMethods() {
        System.err.println("!! Running runtime method processing!")
        // TODO: Add all of these
        val toProcess = listOf(
            PyStringType::class
        )

        for (type in toProcess) {
            System.err.println("Processing $type")
            val methods = type.declaredFunctions
            for (method in methods) {
                val expose = method.findAnnotation<ExposeMethod>() ?: continue
                val params = method.annotations.filterIsInstance<MethodParam>()

                // build signature from the param list
                val pairs = params.map { param ->
                    val realArgType = ArgType.valueOf(param.argType)
                    param.name to realArgType
                }
                val signature = PyCallableSignature(*pairs.toTypedArray())
                val builtinMethod =
                    PyBuiltinFunction.wrap(expose.name, signature) { kwargs ->
                        method.call(type.objectInstance, kwargs) as PyObject
                    }
                System.err.println("Generated method ${expose.name} from ${method.name}")
                type.objectInstance!!.internalDict[expose.name] = builtinMethod
            }
        }
    }

    /**
     * JVM entry point.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (debugMode) {
            System.err.println("Running Kython in debug mode!")
            System.err.println("Processing builtin method annotations...")
        }

        exitProcess(CommandLine(this).execute(*args))
    }
}
