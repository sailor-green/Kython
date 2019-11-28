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

import green.sailor.kython.interpreter.pyobject.types.PyBoolType

/**
 * Represents a Python boolean.
 */
class PyBool private constructor(val wrapped: Boolean) : PyObject() {
    companion object {
        // The TRUE instance of this.
        val TRUE = PyBool(true)
        // The FALSE instance of this.
        val FALSE = PyBool(false)
    }

    private val cachedTrueString = PyString("True")
    private val cachedFalseString = PyString("False")

    override var type: PyType
        get() = PyBoolType
        set(_) = error("Cannot change the type of this object")

    override fun getPyStr(): PyString = if (wrapped) cachedTrueString else cachedFalseString
    override fun getPyRepr(): PyString = getPyStr()
}
