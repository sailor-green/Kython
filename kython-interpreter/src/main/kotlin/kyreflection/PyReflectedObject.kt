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

package green.sailor.kython.interpreter.kyreflection

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import kotlin.reflect.full.memberProperties

/**
 * Represents a reflection object wrapper.
 */
class PyReflectedObject(val wrapped: Any) : PyObject() {
    override var type: PyType
        get() = PyReflectedType
        set(value) = Exceptions.invalidClassSet(this)

    override fun pyToStr(): PyString = PyString(wrapped.toString())
    override fun pyGetRepr(): PyString {
        return (
            "<java object '${wrapped.javaClass.canonicalName}' " +
            "in module '${wrapped.javaClass.module.name}'>"
        ).toPyObject()
    }

    override fun pyGetAttribute(name: String): PyObject {
        val attr = this.maybeGetAttribute(name)
        if (attr != null) return attr

        val klass = wrapped.javaClass.kotlin
        val property = klass.memberProperties.find { it.name == name }
        if (property === null) {
            attributeError("'${klass.simpleName}' object has no attribute '$name'")
        }
        return PyReflectedObject(property)
    }

}
