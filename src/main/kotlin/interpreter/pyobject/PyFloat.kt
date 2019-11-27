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

import green.sailor.kython.interpreter.pyobject.types.PyFloatType

/**
 * Represents a Python float. This wraps a kotlin Double (CPython wraps a C double,
 * so we're consistent there).
 */
class PyFloat(val wrapped: Double) : PyObject(PyFloatType) {
    private val _floatStr by lazy {
        PyString(wrapped.toString())
    }

    override fun getPyStr(): PyString = _floatStr
    override fun getPyRepr(): PyString = _floatStr
}
