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
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject

/**
 * Represents the default `__dir__` on objects.
 */
class ObjectDir : PyBuiltinFunction("<object.__dir__>") {
    override val signature: PyCallableSignature = PyCallableSignature.EMPTY_METHOD

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["self"] ?: error("Built-in signature mismatch!")
        return obb.pyDefaultDir()
    }
}
