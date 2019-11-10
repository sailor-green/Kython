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

package green.sailor.kython.interpreter.objects.iface

import arrow.core.Either
import arrow.core.flatMap
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.primitives.PyTuple
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents a callable, be it a builtin or a regular function.
 */
interface PyCallable {
    /**
     * Gets a runnable stack frame for this callable.
     */
    fun getFrame(): StackFrame

    /**
     * The signature for this function.
     */
    val signature: PyCallableSignature

    /**
     * A shortcut for running this function.
     */
    fun runCallable(
        args: List<PyObject>,
        kwargsTuple: PyTuple? = null
    ): Either<PyException, PyObject> {
        return signature.getFinalArgs(args, kwargsTuple)
            .flatMap { KythonInterpreter.runStackFrame(this.getFrame(), it) }
    }
}
