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

import green.sailor.kython.interpreter.functions.*
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyNotImplemented
import green.sailor.kython.interpreter.pyobject.iterators.PyRangeType
import green.sailor.kython.interpreter.pyobject.types.*

/**
 * Represents the builtins.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Builtins {
    val PRINT = PrintBuiltinFunction()
    val REPR = ReprBuiltinFunction()
    val LOCALS = LocalsBuiltinFunction()
    val DIR = DirBuiltinFunction()
    val ITER = IterBuiltinFunction()
    val CALLABLE = CallableBuiltinFunction()
    val ISINSTANCE = IsinstanceBuiltinFunction()
    val LEN = LenBuiltinFunction()
    val ID = IdBuiltinFunction()

    val OBJECT = PyRootObjectType
    val TYPE = PyRootType
    val INT_TYPE = PyIntType
    val STRING_TYPE = PyStringType
    val FLOAT_TYPE = PyFloatType
    val NONE = PyNone
    val NONE_TYPE = PyNoneType
    val NOT_IMPLEMENTED = PyNotImplemented
    val NOT_IMPLEMENTED_TYPE = PyNotImplemented.PyNotImplementedType
    val TUPLE_TYPE = PyTupleType
    val DICT_TYPE = PyDictType
    val BOOL_TYPE = PyBoolType
    val BYTES_TYPE = PyBytesType
    val LIST_TYPE = PyListType
    val RANGE_TYPE = PyRangeType

    /** The PyDict map of builtins. */
    val BUILTINS_MAP = linkedMapOf(
        "print" to PRINT,
        "repr" to REPR,
        "locals" to LOCALS,
        "dir" to DIR,
        "iter" to ITER,
        "callable" to CALLABLE,
        "isinstance" to ISINSTANCE,
        "len" to LEN,
        "id" to ID,

        // class types
        "object" to OBJECT,
        "type" to TYPE,
        "int" to INT_TYPE,
        "float" to FLOAT_TYPE,
        "str" to STRING_TYPE,
        "tuple" to TUPLE_TYPE,
        "list" to LIST_TYPE,
        "dict" to DICT_TYPE,
        "bool" to BOOL_TYPE,
        "bytes" to BYTES_TYPE,
        "range" to RANGE_TYPE,

        // specials
        "None" to NONE,
        "True" to PyBool.TRUE,
        "False" to PyBool.FALSE,
        "NotImplemented" to PyNotImplemented
    ).apply { this.putAll(Exceptions.EXCEPTION_MAP) }
}
