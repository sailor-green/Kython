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

package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.user.PyUserType
import green.sailor.kython.interpreter.typeError

/**
 * Represents the root type. If the type of a PyObject is not set, this will be used.
 */
object PyRootType : PyType("type") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val args = kwargs["args"]!!.cast<PyTuple>()

        return when (args.subobjects.size) {
            1 -> {
                args.subobjects.first().type
            }
            3 -> {
                val name = args.subobjects[0].cast<PyString>()
                // validate bases
                val bases = args.subobjects[1].cast<PyTuple>().subobjects.map {
                    it as? PyType ?: typeError("Base is not a type")
                }

                val bodyPyDict = args.subobjects[2].cast<PyDict>()
                val body = bodyPyDict.items.mapKeys {
                    it.key.cast<PyString>().wrappedString
                } as LinkedHashMap

                PyUserType(name.wrappedString, bases, body)
            }
            else -> {
                typeError("type() takes 1 or 3 arguments")
            }
        }
    }

    override var type: PyType
        get() = this
        set(_) = Exceptions.invalidClassSet(this)
}
