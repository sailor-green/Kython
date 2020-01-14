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

package green.sailor.kython.interpreter.importing

import green.sailor.kython.builtins.SysModule
import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.module.PyModule
import green.sailor.kython.interpreter.pyobject.module.PyUserModule
import java.nio.file.Paths
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// TODO: Package interactions......

/**
 * Represents the simple pure-Kotlin importer.
 *
 * This does *not* implement the full range of importlib semantics, including loaders. See
 * [BuiltinImportlibImporter] or [PythonImportlibImporter] for that.
 */
class SimpleImporter : Importer {
    /** The import lock held whilst doing imports. */
    val lock = ReentrantLock()

    /**
     * Imports a module from the specified path in sys.path.
     */
    fun importFrom(path: String, name: String): PyModule {
        // loaded from the classpath
        if (path.startsWith("classpath:")) {
            // try and find the module from the jar path
            val realPath = path.removePrefix("classpath:")
            val absolutePath = realPath + name.replace(".", "/")
            return JarFileModuleLoader.getLibModule(absolutePath)
        }

        val resolvedPath = Paths.get(
            path, name.replace(".", "/") + ".py"
        ).toAbsolutePath()
        val compiled = KythonInterpreter.cpyInterface.compile(resolvedPath)
        val moduleFn = PyUserFunction(compiled)
        val module = KythonInterpreter.buildModule(moduleFn, resolvedPath)
        val userModule = PyUserModule(module, name)
        KythonInterpreter.modules[name] = userModule
        return userModule
    }

    override fun absoluteImport(name: String): PyModule = lock.withLock {
        // first try and just return it if its in modules
        try {
            return KythonInterpreter.modules[name]!!
        } catch (e: NullPointerException) {}

        // ok, so we have to actually *load* the modules
        // try and find the package in the specified places
        for (item in SysModule.path.pyIter().iterate()) {
            val unwrapped = item.cast<PyString>().unwrap()
            try {
                return importFrom(unwrapped, name)
            } catch (e: KyError) {
                e.ensure(Exceptions.IMPORT_ERROR)
            }
        }

        Exceptions.MODULE_NOT_FOUND_ERROR(name).throwKy()
    }
}
