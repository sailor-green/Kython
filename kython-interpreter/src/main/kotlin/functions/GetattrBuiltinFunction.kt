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

package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.ensure
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyRootObjectInstance
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction
import green.sailor.kython.interpreter.util.cast

/* getattr(object, name[, default]) */
class GetattrBuiltinFunction : PyBuiltinFunction("getattr") {
    companion object {
        @JvmField val DEFAULT_SENTINEL = PyRootObjectInstance()
    }

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val obb = kwargs["object"].cast<PyObject>()
        val attr = kwargs["attribute"].cast<PyString>().wrappedString
        val default = kwargs["default"].cast<PyObject>()

        return try {
            obb.pyGetAttribute(attr)
        } catch (e: KyError) {
            e.ensure(Exceptions.ATTRIBUTE_ERROR)
            if (default === DEFAULT_SENTINEL) {
                throw e
            }
            default
        }
    }

    override val signature: PyCallableSignature = PyCallableSignature(
        "object" to ArgType.POSITIONAL,
        "attribute" to ArgType.POSITIONAL,
        "default" to ArgType.POSITIONAL
    ).withDefaults("default" to DEFAULT_SENTINEL)
}
