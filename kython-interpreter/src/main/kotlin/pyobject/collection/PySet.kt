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

package green.sailor.kython.interpreter.pyobject.collection

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.toNativeList
import green.sailor.kython.interpreter.util.PyObjectMap
import green.sailor.kython.util.explode
import org.apache.commons.collections4.set.MapBackedSet

/**
 * Represents a Python set.
 */
open class PySet internal constructor(
    wrappedSet: MutableSet<out PyObject>,
    val frozen: Boolean
) : PyCollection(wrappedSet) {
    companion object {
        /**
         * Creates a new [PySet] of the specified collection.
         *
         * This copies all of the items in the collection.
         */
        @JvmOverloads
        fun of(items: Collection<PyObject>, frozen: Boolean = false): PySet {
            val set = MapBackedSet.mapBackedSet(PyObjectMap(), PyNone)
            set.addAll(items)
            return PySet(set, frozen)
        }

        /**
         * Creates a new [PySet] from the [MapBackedSet] object specified.
         */
        @JvmOverloads
        fun of(set: MapBackedSet<out PyObject, *>, frozen: Boolean = false): PySet =
            PySet(set, frozen)
    }

    val wrappedSet: MutableSet<PyObject> get() = subobjects as MutableSet<PyObject>
    override fun unwrap(): MutableSet<PyObject> = wrappedSet

    override fun pyToStr(): PyString = PyString(
        "{" + wrappedSet.joinToString(", ") { it.pyGetRepr().wrappedString } + "}"
    )
    override fun pyGetRepr(): PyString = pyToStr()

    override fun pyGreater(other: PyObject): PyObject = TODO("Not implemented")
    override fun pyLesser(other: PyObject): PyObject = TODO("Not implemented")

    /**
     * Implements set updating from another iterable object.
     */
    fun update(other: PyObject) {
        if (frozen) error("This set is frozen")

        when (other) {
            is PySet -> wrappedSet.addAll(other.wrappedSet)
            is PyContainer -> wrappedSet.addAll(other.subobjects)
            is PyString -> wrappedSet.addAll(
                other.wrappedString.explode().map { PyString(it) }
            )
            else -> wrappedSet.addAll(other.pyIter().toNativeList())
        }
    }

    /**
     * Copies this PySet.
     */
    fun copy(): PySet {
        return of(unwrap(), frozen)
    }

    override var type: PyType
        get() =
            if (frozen) PySetType
            else PyFrozenSetType
        set(_) = Exceptions.invalidClassSet(this)
}
