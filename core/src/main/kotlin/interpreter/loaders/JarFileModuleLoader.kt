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

package green.sailor.kython.interpreter.loaders

import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.module.PyUserModule
import green.sailor.kython.kyc.UnKyc
import java.nio.file.Paths

/**
 * Represents a jar file module loader, loading py files from jar files.
 */
object JarFileModuleLoader {
    /**
     * Attempts to load a module from the classpath.
     * This will re-enter the interpreter to run the module file!!
     *
     * @param name: The absolute name of the module to load.
     * @param args: Any arguments to inject into the module (e.g. for shim objects).
     */
    fun getModule(name: String, args: List<PyObject> = listOf()): PyUserModule {
        val path = javaClass.classLoader.getResource(name)?.toURI()?.let { Paths.get(it) }
            ?: error("Could not find resource $name")
        // todo: not assume these are all compiled kyc files
        val file = UnKyc.parseKycFile(path)
        val moduleFunction = PyUserFunction(KyCodeObject(file.code))
        val kyModule = KyUserModule(moduleFunction, path.fileName.toString(), listOf())
        moduleFunction.kyCall(args)
        return PyUserModule(kyModule, file.code.codeName.wrapped)
    }
}
