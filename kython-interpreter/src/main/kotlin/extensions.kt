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
import green.sailor.kython.interpreter.pyobject.exception.PyException
import green.sailor.kython.interpreter.pyobject.internal.PyCodeObject
import green.sailor.kython.interpreter.pyobject.types.PyRootObjectType
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.util.FakeDict
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
            if (e.pyError.isinstance(setOf(Exceptions.STOP_ITERATION))) break
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
        if (backing.isEmpty()) {
            error("Next called whilst there was no next")
        }
        return backing.removeAt(0)
    }
}

/**
 * Checks if this PyObject is an instance of a type.
 */
fun PyObject.isinstance(other: PyType) = isinstance(setOf(other))

/**
 * Checks if this PyObject is an instance of other types.
 */
fun PyObject.isinstance(others: Set<PyType>): Boolean {
    // these are always true for their respective conditions
    if (PyRootObjectType in others) return true
    if (this is PyType && PyRootType in others) return true

    return type.mro.toSet().intersect(others).isNotEmpty()
}

fun PyException.asObject() = this as PyObject
val KyError.pyError get() = wrapped.asObject()

/** Checks if this PyType is a subclass of another type. */
fun PyType.issubclass(other: PyType): Boolean {
    if (this === other) return true
    return issubclass(setOf(other))
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
 * Implements the default get attribute for an object.
 */
fun PyObject.maybeGetAttribute(attrName: String): PyObject? {
        // special case
    if (attrName == "__dict__") {
        return this.pyDict
    }

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

    return null
}

/**
 * Implements the default get attribute for an object, but returns an error if it can't be found.
 */
fun PyObject.getAttribute(attrName: String): PyObject {
    maybeGetAttribute(attrName)?.let { return it }
    // can't find it
    attributeError("Object '${type.name}' has no attribute '$attrName'")
}

/**
 * Attempts to find an attribute on the MRO.
 */
fun PyType.findOnMro(name: String): PyObject? {
    val internal = internalDict[name]
    if (internal != null) return internal
    return mro.find { name in it.internalDict }?.internalDict?.get(name)
}

/**
 * Implements the default set attribute for an object.
 */
fun PyObject.setAttribute(attrName: String, value: PyObject): PyObject {
    val existing = type.internalDict[attrName]
    if (existing != null && existing.kyHasSet()) {
        existing.pyDescriptorSet(this, value)
    } else {
        if (internalDict === FakeDict) {
            attributeError("Object '${type.name}' has no attribute '$attrName'")
        }
        internalDict[attrName] = value
    }
    return PyNone
}

/**
 * Implements the default dir for PyObject instances.
 */
fun PyObject.dir(): List<String> {
    val dirSet = mutableSetOf<String>().also { set ->
        // set.addAll(magicSlots.createActiveMagicMethodList())
        set.addAll(type.internalDict.keys)
        set.addAll(type.bases.flatMap { it.internalDict.keys })
        set.addAll(internalDict.keys)
    }

    // NB: CPython sorts dir() output for whatever dumb reason.
    // We do too!
    return dirSet.toList().sorted()
}

/** Helper property for getting the type name of an object. */
val PyObject.typeName get() = type.name

fun Any?.toPyObject() = PyObject.wrapPrimitive(this)
fun String.toPyObject() = PyString(this)
fun Int.toPyObject() = PyInt(this.toLong())
