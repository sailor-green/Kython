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

package green.sailor.kython.interpreter.pyobject.internal

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.PyUndicted
import kotlin.reflect.KProperty

// this is a VERY naiive implementation of cellvars
// it keeps the locals of the previous function permenantly loaded.
// this will also create GC tree *hell* if you return a closure from a method
// cos it has to permenantly keep the class object loaded.
// TODO: Refine this so it, uh, doesn't do that.
/**
 * Represents a cell object, loaded from a freevar or a closure var.
 *
 * @param localsMap: The local variables of the function being enclosed.
 * @param
 */
open class PyCellObject(
    open val localsMap: MutableMap<String, PyObject>,
    open val name: String
) : PyUndicted {
    private inner class Delegate {
        operator fun getValue(thisRef: PyCellObject, property: KProperty<*>): PyObject {
            return localsMap[name] ?: TODO("Proper error")
        }
        operator fun setValue(thisRef: PyCellObject, property: KProperty<*>, value: PyObject) {
            localsMap[name] = value
        }
    }

    override var type: PyType
        get() = PyCellObjectType
        set(_) = Exceptions.invalidClassSet(this)

    var content: PyObject by Delegate()
}
