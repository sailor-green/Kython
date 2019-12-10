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

package green.sailor.kython.interpreter.pyobject.exception

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents an exception type.
 */
open class PyExceptionType(
    name: String,
    vararg parentTypes: PyExceptionType
) : PyType(name) {
    override val bases: MutableList<PyType> = parentTypes.map { it as PyType }.toMutableList()

    /**
     * Makes a new [PyException] object from the args specified.
     */
    fun makeException(args: PyTuple) = PyException(this, args)

    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val args = kwargs["args"]?.cast<PyTuple>() ?: error("Built-in signature mismatch!")
        return makeException(args)
    }

    override val signature: PyCallableSignature = PyCallableSignature(
        "*args" to ArgType.POSITIONAL_STAR
    )

    operator fun invoke(vararg args: String): PyException {
        return makeException(PyTuple.get(args.map { PyString(it) }))
    }
}
