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

package green.sailor.kython.importing.importlib

import green.sailor.kython.importing.importlib.bootstrap.Bootstrapper
import green.sailor.kython.interpreter.importing.Importer
import green.sailor.kython.interpreter.pyobject.PyObject

/**
 * Represents the importlib importer.
 */
@Suppress("unused")
class ImportlibImporter : Importer {
    var exception: Throwable? = null

    // load bootstrap
    override fun setup() {
        val bootstrapper = Bootstrapper.build()
        // this runs on a second thread as not to pollute the thread locals of the interpreter
        bootstrapper.runThread()
    }

    override fun absoluteImport(name: String, fromList: List<String>): List<PyObject> {
        TODO("not implemented")
    }
}
