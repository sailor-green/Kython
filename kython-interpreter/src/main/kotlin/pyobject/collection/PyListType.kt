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

import green.sailor.kython.annotation.*
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.EMPTY
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.indexError
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.user.PyUserObject
import green.sailor.kython.interpreter.toNativeList
import green.sailor.kython.interpreter.util.cast

/**
 * Represents the type of a list.
 */
@GenerateMethods
object PyListType : PyType("list") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val iterator = kwargs["x"]?.pyIter() ?: error("Built-ih signature mismatch!")
        val items = iterator.toNativeList()
        return PyList(items)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "x" to ArgType.POSITIONAL
        )
    }

    override fun kySuperclassInit(instance: PyUserObject, args: List<PyObject>): PyNone {
        if (args.isEmpty()) {
            instance.primitiveSubclassBacking[this] = PyList.empty()
        } else {
            val list = args.first().pyIter()
            instance.primitiveSubclassBacking[this] = PyList(list.toNativeList())
        }
        return PyNone
    }

    /** list.append(item) */
    @ExposeMethod("append")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("item", "POSITIONAL")
    )
    fun pyListAppend(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyList>()
        val value = kwargs["item"] ?: error("Built-in signature mismatch!")
        (self.subobjects as MutableList).add(value)
        return PyNone
    }

    /** list.pop() */
    @ExposeMethod("pop")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("index", "POSITIONAL"),
        defaults = [Default("index", EMPTY::class)]
    )
    fun pyListPop(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyList>()
        if (self.subobjects.isEmpty()) indexError("Cannot pop from empty list")

        val indexArg = kwargs["index"]
        return if (indexArg === EMPTY) {
            (self.subobjects as MutableList).removeAt(self.subobjects.size - 1)
        } else {
            val idx = indexArg.cast<PyInt>().wrappedInt.toInt()
            val realIdx = self.getRealIndex(idx)
            if (!self.verifyIndex(realIdx)) {
                indexError("list index $idx is out of range (list size: ${self.subobjects.size})")
            }
            (self.subobjects as MutableList).removeAt(realIdx)
        }
    }

    /** list.extend() */
    @ExposeMethod("extend")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("other", "POSITIONAL")
    )
    fun pyListExtend(kwargs: Map<String, PyObject>): PyNone {
        val self = kwargs["self"].cast<PyList>()
        val other = kwargs["other"].cast<PyObject>()
        self.extend(other)
        return PyNone
    }

    // magic methods
    /** list.__str__() */
    @ExposeMethod("__str__")
    @MethodParams(
        MethodParam("self", "POSITIONAL")
    )
    fun pyListStr(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyList>()
        return self.pyToStr()
    }

    @ExposeMethod("__repr__")
    @MethodParams(
        MethodParam("self", "POSITIONAL")
    )
    fun pyListRepr(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyList>()
        return self.pyGetRepr()
    }
}
