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

package green.sailor.kython.interpreter.objects.python

import arrow.core.Either

/**
 * Represents a Python dict, a mapping between PyObject -> PyObject.
 */
class PyDict(val items: MutableMap<out PyObject, out PyObject>) : PyObject(PyDictType) {
    companion object {
        /** Represents the empty dict. */
        val EMPTY = PyDict(mutableMapOf())
    }

    object PyDictType : PyType("dict") {
        override fun newInstance(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject> {
            // another simple passthrough
            return Either.right(kwargs)
        }
    }

    override fun toPyString(): Either<PyException, PyString> {
        TODO("not implemented")
    }

    override fun toPyStringRepr(): Either<PyException, PyString> {
        TODO("not implemented")
    }

    /**
     * Gets an item from the internal dict.
     */
    fun getItem(key: PyObject): PyObject? {
        return this.items[key]
    }

}
