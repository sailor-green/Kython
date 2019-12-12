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

package green.sailor.kython.interpreter.pyobject.iterators

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents the range() builtin.
 */
class PyRange(val start: Long, val stop: Long, val step: Long = 1) : PyObject() {
    override var type: PyType
        get() = PyRangeType
        set(_) = Exceptions.invalidClassSet(this)

    override val internalDict: LinkedHashMap<String, PyObject> = super.internalDict.apply {
        put("start", PyInt(start))
        put("stop", PyInt(stop))
        put("step", PyInt(step))
    }

    override fun pyIter(): PyObject {
        return PyRangeIterator(this)
    }
}
