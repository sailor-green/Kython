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

import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.iterate
import green.sailor.kython.interpreter.pyobject.PyList
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType

/**
 * Represents the type of a list.
 */
object PyListType : PyType("list") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val iterator = kwargs["x"]?.pyIter() ?: error("Built-ih signature mismatch!")
        val items = iterator.iterate()
        return PyList(items)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "x" to ArgType.POSITIONAL
        )
    }
}