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

/**
 * Represents a fake dict singleton used for objects that have statically generated get/setattrs.
 *
 * This object (should) never actually be used; it's solely to save memory.
 */
object FakeDict : MutableMap<String, PyObject> {
    override val entries: MutableSet<MutableMap.MutableEntry<String, PyObject>> = mutableSetOf()
    override val size: Int = 0
    override val keys: MutableSet<String> = mutableSetOf()
    override fun isEmpty(): Boolean = true
    override val values: MutableCollection<PyObject> = mutableSetOf()
    override fun containsKey(key: String): Boolean = false
    override fun containsValue(value: PyObject): Boolean = false
    override fun get(key: String): PyObject? = null
    override fun clear() = Unit
    override fun putAll(from: Map<out String, PyObject>) = error("Cannot add to this dict")
    override fun remove(key: String): PyObject? = error("Cannot remove from this dict")
    override fun put(key: String, value: PyObject): PyObject? = error("Cannot add to this dict")
}
