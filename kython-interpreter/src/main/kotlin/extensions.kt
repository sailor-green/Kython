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

import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.util.PyObjectMap
import green.sailor.kython.kyc.*

/**
 * Turns a [BaseKycType] into a [PyObject]
 */
fun BaseKycType.unwrap(): PyObject =
    when (this) {
        KycNone -> PyNone
        is KycList -> PyList(wrapped.mapTo(mutableListOf()) { it.unwrap() })
        is KycTuple -> PyTuple.get(wrapped.map { it.unwrap() })
        is KycDict -> PyDict.from(
            wrapped.entries.associateByTo(PyObjectMap(), { it.key.unwrap() }, { it.value.unwrap() })
        )
        is KycSet -> PySet(wrapped.mapTo(mutableSetOf()) { it.unwrap() })
        is KycCodeObject -> PyCodeObject(KyCodeObject(this))
        else -> PyObject.wrapPrimitive(this.wrapped)
    }

/**
 * Helper function to iterate over an iterator.
 */
fun PyObject.toNativeList(): MutableList<PyObject> {
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
 * Turns a PyObject (e.g. a List) into an iterator.
 */
fun PyObject.asIterator(): Iterator<PyObject> = object : Iterator<PyObject> {
    val backing = mutableListOf<PyObject>()
    val builtinIterator = pyIter()

    override fun hasNext(): Boolean {
        try {
            val next = builtinIterator.pyNext()
            backing.add(next)
            return true
        } catch (e: KyError) {
            e.ensure(Exceptions.STOP_ITERATION)
        }
        return false
    }

    override fun next(): PyObject {
        if (!hasNext()) {
            error("Next called whilst there was no next")
        }
        return backing.removeAt(0)
    }
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
    if (isEmpty()) return false
    if (PyRootType in others) return true

    val bases = flatMap { it.bases }
    if (others.intersect(bases).isNotEmpty()) return true
    return bases.issubclass(others)
}

/**
 * Implements the defaults get attribute for an object.
 */
fun PyObject.getAttribute(attrName: String): PyObject {
    // try and find the object on the dict
    if (attrName in internalDict) {
        return if (this is PyType) {
            internalDict[attrName]!!.pyDescriptorGet(PyNone, this)
        } else {
            internalDict[attrName]!!.pyDescriptorGet(this, type)
        }
    }

    // try and find it on the MRO
    val (mro, descriptorSelf) =
        if (this is PyType) mro to PyNone else type.mro to this

    mro.mapNotNull {
        it.internalDict[attrName]
    }.firstOrNull()?.let { return it.pyDescriptorGet(descriptorSelf, type) }

    // can't find it
    Exceptions.ATTRIBUTE_ERROR("Object ${type.name} has no attribute $attrName").throwKy()
}

/**
 * Implements the default set attribute for an object.
 */
fun PyObject.setAttribute(attrName: String, value: PyObject): PyObject {
    val existing = type.internalDict[attrName]
    if (existing != null && existing.kyHasSet()) {
        existing.pyDescriptorSet(this, value)
    } else {
        internalDict[attrName] = value
    }
    return PyNone
}

// helper functions
/**
 * Casts this [PyObject] to its concrete subclass, raising a PyException if it fails.
 */
inline fun <reified T : PyObject> PyObject?.cast(): T {
    if (this == null) error("Casting on null?")
    return this as? T ?: typeError("Invalid type: ${type.name}")
}
