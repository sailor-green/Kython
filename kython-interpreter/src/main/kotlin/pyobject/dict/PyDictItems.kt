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

package green.sailor.kython.interpreter.pyobject.dict

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.collection.PyBaseSetType
import green.sailor.kython.interpreter.pyobject.collection.PySet
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.iterators.PyBuiltinIterator
import green.sailor.kython.interpreter.typeError

/**
 * Represents the items of a dict.
 */
class PyDictItems(val map: Map<PyObject, PyObject>) : PySet(EntrySetMapper(map), true) {
    class EntrySetMapper(val map: Map<PyObject, PyObject>) : MutableSet<PyObject> {
        val entryView = map.entries

        override val size: Int get() = entryView.size
        override fun add(element: PyObject) = typeError("This set is frozen")
        override fun addAll(elements: Collection<PyObject>) = typeError("This set is frozen")
        override fun clear() = typeError("This set is frozen")
        override fun remove(element: PyObject) = typeError("This set is frozen")
        override fun removeAll(elements: Collection<PyObject>) = typeError("This set is frozen")
        override fun retainAll(elements: Collection<PyObject>) = typeError("This set is frozen")
        override fun contains(element: PyObject): Boolean {
            if (element !is PyTuple) return false
            if (element.subobjects.size != 2) return false
            val key = element.unwrap()[0]
            val value = element.unwrap()[1]

            return map.containsKey(key) && map[key]!!.pyEquals(value).pyToBool().wrapped
        }
        override fun containsAll(elements: Collection<PyObject>) = elements.all {
            contains(it)
        }
        override fun isEmpty(): Boolean = map.isEmpty()

        override fun iterator(): MutableIterator<PyObject> = object : MutableIterator<PyObject> {
            val realIterator = map.iterator()
            override fun hasNext(): Boolean = realIterator.hasNext()

            override fun next() = PyTuple.getPair(realIterator.next().toPair())

            override fun remove() = typeError("This set is frozen")
        }
    }

    override fun pyIter(): PyObject = PyBuiltinIterator(subobjects.iterator())

    object PyDictItems : PyBaseSetType("dict_items")

    override var type: PyType
        get() = PyDictItems
        set(value) = Exceptions.invalidClassSet(this)
}
