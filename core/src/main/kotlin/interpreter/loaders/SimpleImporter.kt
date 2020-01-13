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

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.module.PyModule
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Represents the simple pure-Kotlin importer.
 *
 * This does *not* implement the full range of importlib semantics, including loaders. See
 * [BuiltinImportlibImporter] or [PythonImportlibImporter] for that.
 */
class SimpleImporter : Importer {
    /** The import lock held whilst doing imports. */
    val lock = ReentrantLock()

    override fun absoluteImport(name: String): PyModule = lock.withLock {
        // first try and just return it if its in modules
        try {
            return KythonInterpreter.modules[name]!!
        } catch (e: NullPointerException) {}

        // ok, so we have to actually *load* the modules

        TODO()
    }
}
