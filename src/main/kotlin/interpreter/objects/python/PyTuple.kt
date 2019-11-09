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
 * Represents a python tuple of objects. This is a fixed-size immutable container for other PyObject.
 */
class PyTuple(val subobjects: Collection<PyObject>) : PyObject(PyTupleType) {
    companion object {
        /**
         * Represents the empty tuple.
         */
        val EMPTY = PyTuple(listOf())
    }

    // simple passthrough
    object PyTupleType : PyType("tuple") {
        override fun newInstance(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject> {
            return Either.right(args)
        }
    }

    // ugly but it'll do.
    override fun toPyString(): Either<PyException, PyString> {
        val s = StringBuilder("(")
        for (item in subobjects) {
            val maybeString = item.toPyStringRepr()
            // pass through exceptions
            if (maybeString.isLeft()) return maybeString
            maybeString.map { s.append(it.wrappedString) }
            s.append(", ")
        }
        s.append(")")
        return Either.right(PyString(s.toString()))
    }

    override fun toPyStringRepr(): Either<PyException, PyString> = this.toPyString()


}
