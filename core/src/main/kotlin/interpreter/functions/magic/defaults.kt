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

package green.sailor.kython.interpreter.functions.magic

import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.PyObject

private val cmpSig =
    PyCallableSignature(
        "self" to ArgType.POSITIONAL,
        "other" to ArgType.POSITIONAL
    )

/**
 * Superclass for all "default" builtins - used to signify if an object method will just call the
 * PyObject method.
 */
abstract class DefaultBuiltinFunction(name: String) : PyBuiltinFunction(name)

/**
 * Represents the default `__dir__` on objects.
 */
object ObjectDir : DefaultBuiltinFunction("<object.__dir__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in signature mismatch!")
        return self.pyDir()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

/**
 * Represents the default object __repr__.
 */
object ObjectRepr : DefaultBuiltinFunction("<object __repr__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        return self.pyGetRepr()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

/**
 * Represents the default object str.
 */
object ObjectStr : DefaultBuiltinFunction("<object __str__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        return self.pyToStr()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

object ObjectEq : DefaultBuiltinFunction("<object __eq__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        val other = kwargs["other"] ?: error("Built-in signature mismatch!")
        return self.pyEquals(other)
    }

    override val signature: PyCallableSignature = cmpSig
}
