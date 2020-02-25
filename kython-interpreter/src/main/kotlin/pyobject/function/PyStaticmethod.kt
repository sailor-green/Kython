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

package green.sailor.kython.interpreter.pyobject.function

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.PyUndicted
import green.sailor.kython.interpreter.util.cast

/**
 * Represents a static method object.
 */
class PyStaticmethod(val wrapped: PyObject) : PyUndicted {
    object PyStaticmethodType : PyType("staticmethod") {
        override val signature = PyCallableSignature(
            "function" to ArgType.POSITIONAL
        )

        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            val fn = kwargs["function"].cast<PyObject>()
            return PyStaticmethod(fn)
        }
    }

    override var type: PyType
        get() = PyStaticmethodType
        set(_) = Exceptions.invalidClassSet(this)

    override fun pyDescriptorGet(parent: PyObject, klass: PyObject): PyObject {
        return wrapped
    }
}
