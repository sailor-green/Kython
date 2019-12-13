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
package green.sailor.kython.interpreter.pyobject.iterators

import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType

object PyRangeType : PyType("range") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val argStart = kwargs["start"]?.cast<PyInt>() ?: error("Built-in signature mismatch!")
        val argStop = kwargs["stop"] ?: error("Built-in signature mismatch!")
        return if (argStop === PyNone) {
            val start = 0L
            val stop = argStart.cast<PyInt>().wrappedInt
            PyRange(start, stop)
        } else {
            val start = argStart.wrappedInt
            val stop = argStop.cast<PyInt>().wrappedInt
            PyRange(start, stop)
        }
    }

    override val signature: PyCallableSignature = PyCallableSignature(
        "start" to ArgType.POSITIONAL,
        "stop" to ArgType.POSITIONAL,
        "step" to ArgType.POSITIONAL
    ).withDefaults("stop" to PyNone, "step" to PyNone)
}
