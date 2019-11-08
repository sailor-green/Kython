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
 * Represents a Python string. This wraps a regular JVM string.
 */
class PyString(val wrappedString: String) : PyObject(PyStringType) {
    object PyStringType : PyType("str") {

        override fun newInstance(args: PyTuple, kwargs: PyDict): Either<PyException, PyObject> {
            val item = args.subobjects.first()
            return Either.Right(item.toPyString())
        }
    }

    override fun toPyString(): PyString = this
    override fun toPyStringRepr(): PyString = PyString("'$wrappedString'")

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PyString) {
            return false
        }

        return this.wrappedString == other.wrappedString
    }

    override fun hashCode(): Int {
        return wrappedString.hashCode()
    }
}
