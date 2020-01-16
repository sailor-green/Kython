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

package green.sailor.kython.interpreter.util

import green.sailor.kython.interpreter.pyobject.PyObject
import kotlin.reflect.KProperty

/**
 * Represents a dict delegate.
 */
class DictDelegate(val name: String) {
    operator fun getValue(thisRef: PyObject, prop: KProperty<*>): PyObject {
        return thisRef.internalDict[name] ?: error("Dict entry doesn't exist?")
    }
    operator fun setValue(thisRef: PyObject, prop: KProperty<*>, value: PyObject) {
        thisRef.internalDict[name] = value
    }
}

/**
 * Creates a new dict delegate for this [PyObject]'s internal dict.
 */
fun LinkedHashMap<String, PyObject>.dictDelegate(
    name: String,
    initial: () -> PyObject
): DictDelegate {
    val result = initial()
    this[name] = result
    return DictDelegate(name)
}

/**
 * Creates a new dict delegate for this [PyObject]'s internal dict.
 */
fun PyObject.dictDelegate(name: String, initial: () -> PyObject) =
    internalDict.dictDelegate(name, initial)
