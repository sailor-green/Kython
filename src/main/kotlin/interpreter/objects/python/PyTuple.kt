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
 *
 */

package green.sailor.kython.interpreter.objects.python

/**
 * Represents a python tuple of objects. This is a fixed-size immutable container for other PyObject.
 */
class PyTuple(val subobjects: Collection<PyObject>) : PyObject() {
    companion object {
        /**
         * Represents the empty tuple.
         */
        val EMPTY = PyTuple(listOf())
    }

    // ugly but it'll do.
    override fun toPyString(): PyString {
        val s = StringBuilder("(")
        for (item in subobjects) {
            s.append(item.toPyString().wrappedString)
            s.append(", ")
        }
        s.append(")")
        return PyString(s.toString())
    }

    override fun toPyStringRepr(): PyString {
        val s = StringBuilder("(")
        for (item in subobjects) {
            s.append(item.toPyStringRepr().wrappedString)
            s.append(", ")
        }
        s.append(")")
        return PyString(s.toString())
    }
}
