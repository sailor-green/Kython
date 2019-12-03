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
package green.sailor.kython.interpreter.functions.magic

import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject

private val cmpSig = PyCallableSignature(
    "self" to ArgType.POSITIONAL,
    "other" to ArgType.POSITIONAL
)

/**
 * Represents the default `__dir__` on objects.
 */
object ObjectDir : PyBuiltinFunction("<object.__dir__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in signature mismatch!")
        return self.kyDefaultDir()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

/**
 * Represents the default object __repr__.
 */
object ObjectRepr : PyBuiltinFunction("<object __repr__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        return self.kyDefaultRepr()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

/**
 * Represents the default object str.
 */
object ObjectStr : PyBuiltinFunction("<object __str__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        return self.kyDefaultStr()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}

object ObjectEq : PyBuiltinFunction("<object __eq__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in-signature mismatch!")
        val other = kwargs["other"] ?: error("Built-in signature mismatch!")
        return self.kyDefaultEquals(other)
    }

    override val signature: PyCallableSignature = cmpSig
}

object ObjectBool : PyBuiltinFunction("<object __bool__>") {
    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"] ?: error("Built-in signature mismatch!")
        return self.kyDefaultBool()
    }

    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD
}
