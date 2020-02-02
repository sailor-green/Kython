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

import green.sailor.kython.annotation.*
import green.sailor.kython.interpreter.callable.EMPTY
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.toNativeList
import green.sailor.kython.interpreter.util.cast

/**
 * Represents the type of a dict.
 */
@GenerateMethods
object PyDictType : PyType("dict") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        // another simple passthrough
        return PyDict.fromAnyMap(kwargs)
    }

    /** dict.update */
    @ExposeMethod("update")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("other_mapping", "POSITIONAL"),
        MethodParam("kwargs", "KEYWORD_STAR"),
        defaults = [Default("other_mapping", EMPTY::class)]
    )
    fun pyDictUpdate(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyDict>()
        val other = kwargs["other_mapping"] ?: error("Built-in signature mismatch!")

        // optimisation case
        if (other !== EMPTY) {
            if (other is PyDict) {
                self.items.putAll(other.items)
            } else {
                val keys = other.pyIter().toNativeList()
                for (key in keys) {
                    val item = other.pyGetItem(key)
                    self.items[key] = item
                }
            }
        }
        val fnKwargs = kwargs["kwargs"].cast<PyDict>()
        self.items.putAll(fnKwargs.items)

        return PyNone
    }
}
