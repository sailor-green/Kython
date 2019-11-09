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

package green.sailor.kython.interpreter.objects.python.primitives

import arrow.core.Either
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.PyType

/**
 * Represents the Python None.
 */
object PyNone : PyObject(PyNoneType) {
    object PyNoneType : PyType("NoneType") {
        override fun newInstance(args: Map<String, PyObject>): Either<PyException, PyObject> {
            return Either.Right(PyNone)
        }

        override val signature: PyCallableSignature = PyCallableSignature.EMPTY
    }

    private val noneString = PyString("None")

    override fun toPyString(): Either<PyException, PyString> =
        Either.right(noneString)

    override fun toPyStringRepr(): Either<PyException, PyString> =
        Either.right(noneString)
}
