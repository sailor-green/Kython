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
package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyMethod
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.typeError

/**
 * Represents the dir(x) built-in function.
 */
class DirBuiltinFunction : PyBuiltinFunction("dir") {
    override val signature: PyCallableSignature = PyCallableSignature("obb" to ArgType.POSITIONAL)

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["obb"] ?: error("Built-in signature mismatch!")
        val dir = obb.pyGetAttribute("__dir__")
        if (!dir.kyIsCallable()) {
            typeError("__dir__ is not callable")
        }

        val args = if (dir is PyMethod) listOf() else listOf(obb)
        return dir.pyCall(args)
    }
}
