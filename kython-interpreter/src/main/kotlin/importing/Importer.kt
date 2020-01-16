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

import green.sailor.kython.interpreter.pyobject.module.PyModule
import org.apiguardian.api.API

/**
 * Represents the interface for importers.
 */
@API(status = API.Status.STABLE)
interface Importer {
    // there is a single global importer per interpreter
    companion object {
        /** The global importer for the interpreter. */
        var CURRENT = SimpleImporter()
    }

    /**
     * Imports a module with the specified name absolutely.
     *
     * @param name: The name to get.
     * @return A [PyModule] representing the module that has been imported.
     */
    @API(status = API.Status.STABLE)
    fun absoluteImport(name: String): PyModule
}
