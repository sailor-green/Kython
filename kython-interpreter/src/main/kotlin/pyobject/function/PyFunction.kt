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

import green.sailor.kython.interpreter.callable.PyCallable
import green.sailor.kython.interpreter.pyobject.*

/**
 * Represents a Python function instance.
 */
abstract class PyFunction(val name: String) : PyObject, PyCallable {
    // overridden for method binding
    override fun pyDescriptorGet(parent: PyObject, klass: PyObject): PyObject {
        return if (parent is PyNone) this else PyMethod(this, parent)
    }

    override fun pyEquals(other: PyObject): PyObject = PyBool.get(this === other)
    override fun pyGreater(other: PyObject): PyObject = PyNotImplemented
    override fun pyLesser(other: PyObject): PyObject = PyNotImplemented
}
