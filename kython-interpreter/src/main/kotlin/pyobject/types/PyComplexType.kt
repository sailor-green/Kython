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

package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.pyobject.numeric.PyBool
import green.sailor.kython.interpreter.pyobject.numeric.PyComplex
import green.sailor.kython.interpreter.pyobject.numeric.PyFloat
import green.sailor.kython.interpreter.pyobject.numeric.PyInt
import green.sailor.kython.interpreter.util.toComplex

object PyComplexType : PyType("complex") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        return when (val value = kwargs["real"] ?: error("Built-in signature mismatch")) {
            is PyInt -> PyComplex(value.wrapped.toComplex())
            is PyFloat -> PyComplex(value.wrapped.toComplex())
            is PyBool -> PyComplex(value.wrapped.toComplex())
            else -> TODO()
        }
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "real" to ArgType.POSITIONAL,
            "imag" to ArgType.POSITIONAL
        ).withDefaults(
            "real" to PyInt(0)
        )
    }
}
