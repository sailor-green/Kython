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

import green.sailor.kython.interpreter.cast
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString

/**
 * Represents a special Map type that wraps a string-keyed dict, e.g. sys.modules or ``__dict__``.
 */
class StringDictWrapper(val wrapped: MutableMap<String, PyObject>) :
    MutableMap<PyObject, PyObject> {
    override val entries: MutableSet<MutableMap.MutableEntry<PyObject, PyObject>>
        get() {
            return wrapped.entries.mapTo(mutableSetOf()) {
                object : MutableMap.MutableEntry<PyObject, PyObject> {
                    override val key: PyObject = PyString(it.key)
                    override val value = it.value
                    override fun setValue(newValue: PyObject): PyObject =
                        throw NotImplementedError()
                }
            }
        }

    private val PyObject.string get() = cast<PyString>().wrappedString

    override val keys: MutableSet<PyObject>
        get() = wrapped.keys.mapTo(mutableSetOf()) { PyString(it) }
    override val values: MutableCollection<PyObject> get() = wrapped.values
    override val size: Int get() = wrapped.size

    override fun clear() = wrapped.clear()
    override fun containsKey(key: PyObject): Boolean = wrapped.containsKey(key.string)

    override fun containsValue(value: PyObject): Boolean = wrapped.containsValue(value)
    override fun get(key: PyObject): PyObject? = wrapped[key.string]
    override fun isEmpty(): Boolean = wrapped.isEmpty()
    override fun put(key: PyObject, value: PyObject): PyObject? = wrapped.put(key.string, value)

    override fun putAll(from: Map<out PyObject, PyObject>) =
        wrapped.putAll(from.mapKeys { it.key.string })

    override fun remove(key: PyObject): PyObject? = wrapped.remove(key.string)
}
