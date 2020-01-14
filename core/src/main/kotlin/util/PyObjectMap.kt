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

package green.sailor.kython.util

import green.sailor.kython.interpreter.pyobject.PyNotImplemented
import green.sailor.kython.interpreter.pyobject.PyObject
import org.apache.commons.collections4.map.AbstractLinkedMap

// TODO: key equals proper behaviour
/**
 * Represents a map of PyObjects, hashed and equalled through the Python methods.
 *
 * This should be used over regular LinkedHashMap<PyObject, PyObject>
 */
class PyObjectMap : AbstractLinkedMap<PyObject, PyObject>(
    DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD
) {
    companion object {
        // copied from AbstractHashedMap
        /** The default capacity to use  */
        private const val DEFAULT_CAPACITY = 16
        /** The default threshold to use  */
        private const val DEFAULT_THRESHOLD = 12
        /** The default load factor to use  */
        private const val DEFAULT_LOAD_FACTOR = 0.75f
    }

    /**
     * Overrides the hashability function to call pyHash().
     */
    override fun hash(key: Any?): Int {
        require(key is PyObject) { "Keys must be PyObject" }
        var h = key.pyHash().wrappedInt.toInt()
        h += (h shl 9).inv()
        h = h xor h ushr 14
        h += h shl 4
        h = h xor h ushr 10
        return h
    }

    override fun isEqualKey(key1: Any?, key2: Any?): Boolean {
        return checkEquality(key1, key2)
    }

    override fun isEqualValue(value1: Any?, value2: Any?): Boolean {
        return checkEquality(value1, value2)
    }

    private fun checkEquality(key1: Any?, key2: Any?): Boolean {
        require(key1 is PyObject) { "Keys must be PyObject" }
        require(key2 is PyObject) { "Keys must be PyObject" }
        val isEqual1 = key1.pyEquals(key2)
        return if (isEqual1 === PyNotImplemented) {
            val isEqual2 = key2.pyEquals(key1)
            if (isEqual2 === PyNotImplemented) {
                false
            } else {
                isEqual2.pyToBool().wrapped
            }
        } else {
            isEqual1.pyToBool().wrapped
        }
    }
}
