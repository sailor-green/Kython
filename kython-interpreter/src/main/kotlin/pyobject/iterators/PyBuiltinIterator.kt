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
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.PyUndicted
import green.sailor.kython.interpreter.throwKy
import green.sailor.kython.interpreter.typeError

/**
 * Represents a built-in iterator over a Kotlin iterator.
 */
class PyBuiltinIterator(val kotlinIterator: Iterator<PyObject>) : PyUndicted {
    object PyGenericIteratorType : PyType("builtin_iterator") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject =
            typeError("Cannot create new instances of builtin_iterator")
    }

    override var type: PyType
        get() = PyGenericIteratorType
        set(_) = Exceptions.invalidClassSet(this)

    override fun pyNext(): PyObject {
        if (!kotlinIterator.hasNext()) {
            Exceptions.STOP_ITERATION().throwKy()
        }

        return kotlinIterator.next()
    }
}
