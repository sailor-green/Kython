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
 * Represents an exception object. This should be subclassed for all built-in exceptions
 * (do not build the Python exception tree with Kotlin subclasses).
 *
 * Each subclass should pass in the appropriate type to the PyObject constructor.
 */
abstract class PyException(val args: PyTuple) : PyObject() {
    /**
     * Represents the type of an exception.
     */
    abstract class PyExceptionType(name: String) : PyType(name) {

    }

    override fun toPyString(): Either<PyException, PyString> {
        require(this.type is PyExceptionType) { "Type of exception was not PyExceptionType!" }
        TODO()
    }

    override fun toPyStringRepr(): Either<PyException, PyString> {
        TODO("not implemented")
    }
}
