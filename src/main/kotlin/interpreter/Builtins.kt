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

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.functions.DirBuiltinFunction
import green.sailor.kython.interpreter.functions.LocalsBuiltinFunction
import green.sailor.kython.interpreter.functions.PrintBuiltinFunction
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.primitives.*

/**
 * Represents the builtins.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Builtins {
    val PRINT = PrintBuiltinFunction()
    val LOCALS = LocalsBuiltinFunction()
    val DIR = DirBuiltinFunction()

    val TYPE = PyType.PyRootType
    val INT_TYPE = PyInt.PyIntType
    val STRING_TYPE = PyString.PyStringType
    val NONE = PyNone
    val NONE_TYPE = PyNone.PyNoneType
    val TUPLE_TYPE = PyTuple.PyTupleType
    val DICT_TYPE = PyDict.PyDictType
    val BOOL_TYPE = PyBool.PyBoolType

    /** The PyDict map of builtins. */
    val BUILTINS_MAP = linkedMapOf(
        "print" to PRINT,
        "locals" to LOCALS,
        "dir" to DIR,

        // class types
        "type" to TYPE,
        "int" to INT_TYPE,
        "str" to STRING_TYPE,
        "tuple" to TUPLE_TYPE,
        "dict" to DICT_TYPE,

        // specials
        "None" to NONE
    ).apply { this.putAll(Exceptions.EXCEPTION_MAP) }
}
