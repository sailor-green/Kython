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

package green.sailor.kython.interpreter

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType
import green.sailor.kython.interpreter.pyobject.types.PyRootType

/**
 * Helper function to iterate over an iterator.
 */
fun PyObject.iterate(): List<PyObject> {
    val items = mutableListOf<PyObject>()
    while (true) {
        try {
            items.add(pyNext())
        } catch (e: KyError) {
            if (e.wrapped.isinstance(setOf(Exceptions.STOP_ITERATION))) break
            throw e
        }
    }
    return items
}

/**
 * Checks if this PyObject is an instance of other types.
 */
fun PyObject.isinstance(others: Set<PyType>): Boolean {
    // these are always true for their respective conditions
    if (PyRootObjectType in others) return true
    if (this is PyType && PyRootType in others) return true

    return type.mro.toSet().intersect(others).isNotEmpty()
}

/**
 * Checks if this PyType is a subclass of another type.
 */
fun PyType.issubclass(others: Set<PyType>) = setOf(this).issubclass(others)

tailrec fun Collection<PyType>.issubclass(others: Set<PyType>): Boolean {
    if (this.isEmpty()) return false
    if (PyRootType in others) return true

    val bases = this.flatMap { it.bases }
    if (others.intersect(bases).isNotEmpty()) return true
    return bases.issubclass(others)
}

// helper functions
/**
 * Casts this [PyObject] to its concrete subclass, raising a PyException if it fails.
 */
inline fun <reified T : PyObject> PyObject?.cast(): T {
    if (this == null) error("Casting on null?")
    if (this !is T) {
        typeError("Invalid type: ${type.name}")
    }
    return this
}
