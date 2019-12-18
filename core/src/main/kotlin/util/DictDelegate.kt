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

package green.sailor.kython.util

import green.sailor.kython.interpreter.pyobject.PyObject
import kotlin.reflect.KProperty

/**
 * Represents a delegate to the internal dictionary of this object.
 */
class DictDelegate<T : PyObject>(
    val propName: String,
    val defaultValue: () -> T
) {
    operator fun getValue(thisRef: PyObject, property: KProperty<*>): T {
        return thisRef.internalDict.computeIfAbsent(propName) { defaultValue() } as T
    }

    operator fun setValue(thisRef: PyObject, property: KProperty<*>, value: T) {
        thisRef.internalDict[propName] = value
    }
}
