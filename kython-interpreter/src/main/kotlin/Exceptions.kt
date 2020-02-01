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
 */

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.exception.PyExceptionType

/**
 * A nice list of exceptions.
 */
object Exceptions {
    // BaseException and its special subclasses
    val BASE_EXCEPTION = PyExceptionType("BaseException")

    // Root of all other exceptions
    val EXCEPTION = PyExceptionType("Exception", BASE_EXCEPTION)

    // iterator signal
    val STOP_ITERATION = PyExceptionType("StopIteration", EXCEPTION)

    // super bad error
    val SYSTEM_ERROR = PyExceptionType("SystemError", EXCEPTION)

    // main errors
    val ATTRIBUTE_ERROR = PyExceptionType("AttributeError", EXCEPTION)
    val NAME_ERROR = PyExceptionType("NameError", EXCEPTION)
    val TYPE_ERROR = PyExceptionType("TypeError", EXCEPTION)
    val VALUE_ERROR = PyExceptionType("ValueError", EXCEPTION)

    val LOOKUP_ERROR = PyExceptionType("LookupError", EXCEPTION)
    val INDEX_ERROR = PyExceptionType("IndexError", LOOKUP_ERROR)
    val KEY_ERROR = PyExceptionType("KeyError", LOOKUP_ERROR)

    // import errors
    val IMPORT_ERROR = PyExceptionType("ImportError", EXCEPTION)
    val MODULE_NOT_FOUND_ERROR = PyExceptionType("ModuleNotFoundError", IMPORT_ERROR)

    // runtimeerror and its children
    val RUNTIME_ERROR = PyExceptionType("RuntimeError", EXCEPTION)
    val NOT_IMPLEMENTED_ERROR = PyExceptionType("NotImplementedError", RUNTIME_ERROR)

    val EXCEPTION_MAP = mapOf(
        "BaseException" to EXCEPTION,
        "Exception" to EXCEPTION,

        "StopIteration" to STOP_ITERATION,

        "SystemError" to SYSTEM_ERROR,

        "AttributeError" to ATTRIBUTE_ERROR,
        "NameError" to NAME_ERROR,
        "TypeError" to TYPE_ERROR,
        "ValueError" to VALUE_ERROR,
        "IndexError" to INDEX_ERROR,
        "KeyError" to KEY_ERROR,

        "ImportError" to IMPORT_ERROR,
        "ModuleNotFoundError" to MODULE_NOT_FOUND_ERROR,

        "RuntimeError" to RUNTIME_ERROR,
        "NotImplementedError" to NOT_IMPLEMENTED_ERROR
    )

    // helpers for common errors from builtins
    fun invalidClassSet(parent: PyObject): Nothing =
        typeError("Cannot set __class__ on object of type ${parent.type.name}")
}

/**
 * Causes a new TypeError.
 */
fun typeError(message: String): Nothing = Exceptions.TYPE_ERROR(message).throwKy()

/**
 * Causes a new ValueError.
 */
fun valueError(message: String): Nothing = Exceptions.VALUE_ERROR(message).throwKy()

/**
 * Causes a new AttributeError.
 */
fun attributeError(message: String): Nothing = Exceptions.ATTRIBUTE_ERROR(message).throwKy()

/**
 * Causes a new NameError.
 */
fun nameError(message: String): Nothing = Exceptions.NAME_ERROR(message).throwKy()

/**
 * Causes a new IndexError.
 */
fun indexError(message: String): Nothing = Exceptions.INDEX_ERROR(message).throwKy()

/**
 * Causes a new KeyError.
 */
fun keyError(message: String): Nothing = Exceptions.KEY_ERROR(message).throwKy()

/**
 * Causes a new SystemError.
 */
fun systemError(message: String): Nothing = Exceptions.SYSTEM_ERROR(message).throwKy()

/**
 * Ensures an [KyError] is of the specified type.
 */
fun KyError.ensure(type: PyExceptionType) { if (!pyError.type.issubclass(type)) throw this }
