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

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.Slotted
import green.sailor.kython.generation.generated.dirSlotted
import green.sailor.kython.generation.generated.getattrSlotted
import green.sailor.kython.generation.generated.setattrSlotted
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyList
import green.sailor.kython.interpreter.pyobject.collection.PySet
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.pyobject.module.PyBuiltinModule
import green.sailor.kython.interpreter.toPyObject
import green.sailor.kython.interpreter.util.StringDictWrapper
import green.sailor.kython.interpreter.util.mapBackedSet

/**
 * Represents the sys built-in module.
 */
@Suppress("unused", "ObjectPropertyName")
@Slotted("sys")
object SysModule : PyBuiltinModule("sys") {

    // == sys object properties == //
    val version: PyString = PyString("3.9.0")
    val platform: PyString = PyString(System.getProperty("os.name").toLowerCase())
    var path: PyList =
        PyList(defaultPath())
    var modules = PyDict.unsafeFromUnVerifiedMap(
        StringDictWrapper(KythonInterpreter.modules as MutableMap<String, PyObject>)
    )

    val __name__ = PyString("sys")

    // TODO keep these updates
    val builtin_module_names = PySet.of(listOf(
        "sys", "_imp", "_io"
    ).mapTo(mapBackedSet()) { it.toPyObject() }, frozen = true)


    // == sys object methods == //
    @ExposeMethod("getswitchinterval")
    fun getSwitchInterval(args: Map<String, PyObject>): PyFloat = PyFloat(0.0)

    override fun pyGetAttribute(name: String): PyObject = getattrSlotted(name)
    override fun pySetAttribute(name: String, value: PyObject): PyObject =
        setattrSlotted(name, value)
    override fun pyDir(): PyTuple = dirSlotted()
}

/**
 * Creates the default path elements.
 */
fun defaultPath(): MutableList<out PyObject> {
    return listOf("", "classpath:Lib").mapTo(mutableListOf()) { PyString(it) }
}
