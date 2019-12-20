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
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.cast
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.user.PyUserType
import green.sailor.kython.interpreter.typeError

/**
 * Represents the root type. If the type of a PyObject is not set, this will be used.
 */
object PyRootType : PyType("type") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val first = kwargs["name"] ?: error("Built-in signature mismatch!")
        val second = kwargs["bases"] ?: error("Built-in signature mismatch!")
        val third = kwargs["class_body"] ?: error("Built-in signature mismatch!")

        return when {
            second === PyNone && third === PyNone -> {
                // lol
                return PyString(first.type.name)
            }
            else -> {
                val name = first.cast<PyString>()
                // validate bases
                val bases = second.cast<PyTuple>().subobjects.map {
                    it as? PyType ?: typeError("Base is not a type")
                }

                val bodyPyDict = third.cast<PyDict>()
                val body = bodyPyDict.items.mapKeys {
                    it.key.cast<PyString>().wrappedString
                } as LinkedHashMap

                PyUserType(name.wrappedString, bases, body)
            }
        }
    }

    override val signature: PyCallableSignature = PyCallableSignature(
        "name" to ArgType.POSITIONAL,
        "bases" to ArgType.POSITIONAL,
        "class_body" to ArgType.POSITIONAL
    ).withDefaults("bases" to PyNone, "class_body" to PyNone)

    override var type: PyType
        get() = this
        set(_) = Exceptions.invalidClassSet(this)
}
