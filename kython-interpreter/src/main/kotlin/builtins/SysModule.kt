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

package green.sailor.kython.interpreter.builtins

import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyDict
import green.sailor.kython.interpreter.pyobject.PyList
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.module.PyBuiltinModule
import green.sailor.kython.interpreter.util.StringDictWrapper
import green.sailor.kython.interpreter.util.dictDelegate
/**
 * Represents the sys built-in module.
 */
@Suppress("unused")
@GenerateMethods
object SysModule : PyBuiltinModule("sys") {
    val version by dictDelegate("version") { PyString("3.9.0") }

    val platform by dictDelegate("platform") {
        PyString(System.getProperty("os.name").toLowerCase())
    }

    val path by dictDelegate("path") {
        PyList(defaultPath() as MutableList<PyObject>)
    }

    val modules by dictDelegate("modules") {
        PyDict.unsafeFromUnVerifiedMap(
            StringDictWrapper(KythonInterpreter.modules as MutableMap<String, PyObject>)
        )
    }
}

/**
 * Creates the default path elements.
 */
fun defaultPath(): MutableList<PyString> {
    return listOf("", "classpath:/Lib").mapTo(mutableListOf()) { PyString(it) }
}
