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
 *
 */
package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType

/**
 * Represents a root object instance (i.e. the result of doing `object()`).
 */
class PyRootObjectInstance : PyObject() {
    private val _cached: PyString

    init {
        val ooEnabled = System.getProperty("kython.easteregg.objectobject") == "true"

        _cached = if (ooEnabled) PyString("[object Object]") else PyString("<object object>")
    }

    override fun pyGetRepr(): PyString = pyToStr()
    override fun pyToStr(): PyString = _cached
    override fun pyEquals(other: PyObject): PyObject = PyBool.get(this === other)
    override fun pyGreater(other: PyObject): PyObject = PyNotImplemented
    override fun pyLesser(other: PyObject): PyObject = PyNotImplemented

    override var type: PyType
        get() = PyRootObjectType
        set(_) = Exceptions.invalidClassSet(this)
}
