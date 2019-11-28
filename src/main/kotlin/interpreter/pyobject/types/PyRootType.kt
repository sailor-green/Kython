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
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.throwKy

/**
 * Represents the root type. If the type of a PyObject is not set, this will be used.
 */
object PyRootType : PyType("type") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        // one-arg form
        val args = kwargs["args"]!!.cast<PyTuple>()

        if (args.subobjects.size == 1) {
            return args.subobjects.first().type
        }

        // TODO: Three arg type version
        Exceptions.NOT_IMPLEMENTED_ERROR("Three-arg form of type not impl'd yet")
            .throwKy()
    }

    override var type: PyType
        get() = this
        set(_) = error("Cannot set the type of this function")
}
