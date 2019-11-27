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
package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.throwKy

object PyBytesType : PyType("bytes") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val value = kwargs["value"] ?: error("Built-in signature mismatch")
        when (value) {
            is PyString -> {
                return PyBytes(value.wrappedString.toByteArray())
            }
            else -> error("Not yet supported")
        }
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "value" to ArgType.POSITIONAL
        )
    }

    /** bytes.__getitem__ */
    val pyBytesIndex = PyBuiltinFunction.wrap(
        "__getitem__",
        PyCallableSignature("index" to ArgType.POSITIONAL)
    ) {
        val self = it["self"]!!.cast<PyBytes>()
        val index = it["index"]!!.cast<PyInt>()
        // TODO: PySlice check
        try {
            PyInt(self.wrapped[index.wrappedInt.toInt()].toLong())
        } catch (e: IndexOutOfBoundsException) {
            Exceptions.INDEX_ERROR("Index ${index.wrappedInt} out of range").throwKy()
        }
    }

    override val initialDict: Map<String, PyObject> by lazy {
        mapOf(
            // magic method impls
            "__getitem__" to pyBytesIndex
        )
    }
}
