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

package green.sailor.kython.interpreter.objects.functions

import arrow.core.Either
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.primitives.PyNone
import green.sailor.kython.interpreter.objects.python.primitives.PyTuple
import interpreter.objects.iface.ArgType

/**
 * Represents the print() builtin.
 */
class PrintBuiltinFunction : PyBuiltinFunction("print") {
    override fun callFunction(kwargs: Map<String, PyObject>): Either<PyException, PyObject> {
        // todo: more args

        val sb = StringBuilder()
        val args = kwargs["args"] as PyTuple

        for (arg in args.subobjects) {
            val result = arg.toPyStringRepr()
            if (result.isLeft()) return result
            result.map { sb.append(it.wrappedString) }
        }

        println(sb.toString())
        return Either.right(PyNone)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "args" to ArgType.POSITIONAL_STAR
        )
    }
}
