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

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.cast
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.util.center
import java.util.*

/**
 * Represents the str builtin type.
 */
@GenerateMethods
object PyStringType : PyType("str") {
    private val Map<String, PyObject>.selfWrappedString
        get() = this["self"].cast<PyString>().wrappedString

    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val arg = kwargs["x"]!!
        if (arg is PyString) {
            return arg
        }

        return if (arg is PyType) {
            arg.pyToStr()
        } else {
            arg.pyToStr()
        }
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "x" to ArgType.POSITIONAL
        )
    }

    /** str.lower() */
    @ExposeMethod("lower")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrLower(kwargs: Map<String, PyObject>): PyString {
        val self = kwargs["self"].cast<PyString>()
        return PyString(self.wrappedString.toLowerCase())
    }

    /** str.upper() */
    @ExposeMethod("upper")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrUpper(kwargs: Map<String, PyObject>): PyString {
        val self = kwargs["self"].cast<PyString>()
        return PyString(self.wrappedString.toUpperCase())
    }

    /** str.capitalize */
    @ExposeMethod("capitalize")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrCapitalize(kwargs: Map<String, PyObject>): PyString {
        return PyString(kwargs.selfWrappedString.capitalize())
    }

    /** str.casefold */
    @ExposeMethod("casefold")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrCasefold(kwargs: Map<String, PyObject>): PyString {
        val folded = kwargs.selfWrappedString.toUpperCase(Locale.US).toLowerCase(Locale.US)
        return PyString(folded)
    }

    @ExposeMethod("center")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("width", "POSITIONAL"),
        MethodParam("fillchar", "POSITIONAL")
    )
    fun pyStringCenter(kwargs: Map<String, PyObject>): PyString {
        val width = kwargs["width"].cast<PyInt>()
        val fillchar = kwargs["fillchar"]?.cast<PyString>()
        val actualChar = fillchar?.wrappedString?.singleOrNull() ?: ' '
        val centered = kwargs.selfWrappedString.center(width.wrappedInt, actualChar)

        return PyString(centered)
    }
}
