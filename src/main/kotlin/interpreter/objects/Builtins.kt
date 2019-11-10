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

package green.sailor.kython.interpreter.objects

import arrow.core.Either
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.objects.functions.LocalsBuiltinFunction
import green.sailor.kython.interpreter.objects.functions.PrintBuiltinFunction
import green.sailor.kython.interpreter.objects.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.objects.iface.PyCallableSignature
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.PyType
import green.sailor.kython.interpreter.objects.python.primitives.*
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents the builtins.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Builtins {
    val PRINT = PrintBuiltinFunction()
    val LOCALS = LocalsBuiltinFunction()
    val DEBUG_PRINTFRAMES = object : PyBuiltinFunction("debug_printframes") {
        override val signature: PyCallableSignature = PyCallableSignature.EMPTY
        override fun callFunction(kwargs: Map<String, PyObject>): Either<PyException, PyObject> {
            println(StackFrame.flatten(KythonInterpreter.getRootFrameForThisThread()))
            return Either.left(Exceptions.BASE_EXCEPTION.makeWithMessage("Debug"))
        }
    }

    val TYPE = PyType.PyRootType
    val INT_TYPE = PyInt.PyIntType
    val STRING_TYPE = PyString.PyStringType
    val NONE = PyNone
    val NONE_TYPE = PyNone.PyNoneType
    val TUPLE_TYPE = PyTuple.PyTupleType
    val DICT_TYPE = PyDict.PyDictType

    /** The PyDict map of builtins. */
    val BUILTINS_MAP = PyDict(
        mutableMapOf(
            PyString("print") to PRINT,
            PyString("locals") to LOCALS,

            // class types
            PyString("type") to TYPE,
            PyString("int") to INT_TYPE,
            PyString("str") to STRING_TYPE,
            PyString("tuple") to TUPLE_TYPE,
            PyString("dict") to DICT_TYPE,

            // specials
            PyString("None") to NONE
        ).apply { this.putAll(Exceptions.EXCEPTION_MAP) }
    )
}
