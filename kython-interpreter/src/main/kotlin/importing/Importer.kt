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

import green.sailor.kython.interpreter.pyobject.PyObject
import org.apiguardian.api.API

/**
 * Represents the interface for importers.
 */
@API(status = API.Status.STABLE)
interface Importer {
    // there is a single global importer per interpreter
    companion object {
        /** The global importer for the interpreter. */
        lateinit var CURRENT: Importer

        /**
         * Loads an importer by name.
         */
        fun load(name: String) {
            val klass = Class.forName(name)
                ?: error("Could not find importer $name!")
            val kotlin = klass.kotlin
            val instance = kotlin.constructors.first().call()
            if (instance !is Importer) {
                error("Importer $name is not an instance of Importer!")
            }
            CURRENT = instance
        }
    }

    /**
     * Called once an importer has been loaded to perform setup.
     */
    fun setup() = Unit

    /**
     * Imports a module with the specified name absolutely.
     *
     * @param name: The name to get.
     * @param fromList: A list of items to copy from the module.
     * @return A list of [PyObject] representing the objects beeing imported.
     */
    @API(status = API.Status.STABLE)
    fun absoluteImport(name: String, fromList: List<String>): List<PyObject>
}
