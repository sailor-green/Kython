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

@file:Suppress("PropertyName")

package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.annotation.Slotted
import green.sailor.kython.generation.generated.dirSlotted
import green.sailor.kython.generation.generated.getattrSlotted
import green.sailor.kython.generation.generated.setattrSlotted
import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.attributeError
import green.sailor.kython.interpreter.pyobject.types.PyPropertyType

/**
 * Property object implementation.
 */
@Slotted("property")
class PyProperty(getter: PyObject) : PyUndicted {
    override var type: PyType
        get() = PyPropertyType
        set(_) = Exceptions.invalidClassSet(this)

    /** Getter callable. */
    var fget: PyObject = getter
    /** Setter callable. */
    var fset: PyObject = PyNone

    override fun pyDescriptorGet(parent: PyObject, klass: PyObject): PyObject {
        // class access returns the property directly
        if (parent === PyNone) {
            return this
        }
        // non-class access returns the getter
        return fget.kyCall(listOf(parent))
    }

    override fun kyHasSet(): Boolean = true
    override fun pyDescriptorSet(instance: PyObject, value: PyObject) {
        if (fset === PyNone) {
            attributeError("This attribute has no setter associated with it")
        }
        fset.kyCall(listOf(value, instance))
    }

    override fun pyGetAttribute(name: String) = getattrSlotted(name)
    override fun pySetAttribute(name: String, value: PyObject) = setattrSlotted(name, value)
    override fun pyDir() = dirSlotted()
}
