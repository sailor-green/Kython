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
package green.sailor.kython.interpreter.pyobject.iterators

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.throwKy
import green.sailor.kython.interpreter.typeError

/**
 * Represents a tuple iterator.
 */
class PyTupleIterator(val wrappedTuple: PyTuple) : PyObject() {
    object PyTupleIteratorType : PyType("tuple_iterator") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject =
            typeError("Cannot create tuple_iterator instances")
    }

    override var type: PyType
        get() = PyTupleIteratorType
        set(_) = Exceptions.invalidClassSet(this)

    /** The tuple iterator we're actually iterating over. */
    val it = wrappedTuple.subobjects.iterator()

    override fun pyNext(): PyObject {
        if (it.hasNext()) {
            return it.next()
        }
        Exceptions.STOP_ITERATION("").throwKy()
    }
}
