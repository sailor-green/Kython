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

import green.sailor.kython.annotation.*
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.EMPTY
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.util.SliceAwareRange
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.interpreter.util.center
import java.util.*
import kotlin.streams.asSequence

/**
 * Represents the str builtin type.
 */
@GenerateMethods
object PyStringType : PyType("str") {
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

    private val numerics = setOf(
        CharCategory.DECIMAL_DIGIT_NUMBER,
        CharCategory.LETTER_NUMBER,
        CharCategory.OTHER_NUMBER
    )
    /** Wrapped value of PyString self attribute. */
    private val Map<String, PyObject>.selfWrappedString
        get() = this["self"].cast<PyString>().wrappedString

    /** [Int] sequence of code points */
    private val String.codePoints get() = codePoints().asSequence()

    /**
     * Return `true` if all characters in the [string] are alphabetic
     * and there is at least one character, `false` otherwise.
     */
    private fun isAlpha(string: String) = string.isNotEmpty() && string.all { it.isLetter() }

    /**
     * Return `true` if the string is empty or all characters in the [string] are ASCII,
     * `false` otherwise. ASCII characters have code points in the range U+0000-U+007F.
     */
    private fun isAscii(string: String) = string.chars().allMatch { it in (0..0x7F) }

    /**
     * Return `true` if all characters in the [string] are numeric characters,
     * and there is at least one character, `false` otherwise.
     * Numeric characters include digit characters, and all characters that
     * have the Unicode numeric value property, e.g. U+2155, VULGAR FRACTION ONE FIFTH.
     */
    private fun isNumeric(string: String) =
        string.isNotEmpty() && string.all { it.category in numerics }

    /**
     * Return `true` if all characters in the [string] are decimal characters and there is at least
     * one character, `false` otherwise.
     * Decimal characters are those that can be used to form numbers in
     * base 10, e.g. U+0660, ARABIC-INDIC DIGIT ZERO.
     */
    private fun isDecimal(string: String) =
        string.isNotEmpty() && string.all { it.category == CharCategory.DECIMAL_DIGIT_NUMBER }

    /**
     * Return `true` if all characters in the [string] are alphanumeric
     * and there is at least one character
     */
    private fun isAlnum(string: String): Boolean {
        val passesBasic = string.run {
            all { it.isLetter() } || all { it.category in numerics } || isDigit(this)
        }
        return passesBasic && string.isNotEmpty()
    }

    /**
     * Return `true` if all characters in the [string] are digits
     * and there is at least one character, `false` otherwise.
     */
    private fun isDigit(string: String): Boolean {
        // Java uses surrogates due to its UTF-16 encoding.
        // We need to manually iterate over code-points.
        val codePoints = string.codePoints
            .map { CharCategory.valueOf(Character.getType(it)) }
        return string.isNotEmpty() && codePoints.all { it in numerics - CharCategory.LETTER_NUMBER }
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

    /** str.center */
    @ExposeMethod("center")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("width", "POSITIONAL"),
        MethodParam("fillchar", "POSITIONAL"),
        defaults = [Default("fillchar", String::class, " ")]
    )
    fun pyStrCenter(kwargs: Map<String, PyObject>): PyString {
        val width = kwargs["width"].cast<PyInt>()
        val fillchar = kwargs["fillchar"]?.cast<PyString>()!!
        val actualChar = fillchar.wrappedString.single()
        val centered = kwargs.selfWrappedString.center(width.wrappedInt, actualChar)

        return PyString(centered)
    }

    /** str.isalpha */
    @ExposeMethod("isalpha")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsAlpha(kwargs: Map<String, PyObject>): PyBool {
        return PyBool.get(isAlpha(kwargs.selfWrappedString))
    }

    /** str.isascii */
    @ExposeMethod("isascii")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsAscii(kwargs: Map<String, PyObject>): PyBool {
        // Python accepts all chars within U+0000-U+007F.
        return PyBool.get(isAscii(kwargs.selfWrappedString))
    }

    /** str.isdecimal */
    @ExposeMethod("isdecimal")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsDecimal(kwargs: Map<String, PyObject>): PyBool {
        return PyBool.get(isDecimal(kwargs.selfWrappedString))
    }

    /** str.isdigit */
    @ExposeMethod("isdigit")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsDigit(kwargs: Map<String, PyObject>): PyBool {
        return PyBool.get(isDigit(kwargs.selfWrappedString))
    }

    /** str.isalnum */
    @ExposeMethod("isalnum")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsAlphanumeric(kwargs: Map<String, PyObject>): PyBool {
        return PyBool.get(isAlnum(kwargs.selfWrappedString))
    }

    /** str.isnumeric */
    @ExposeMethod("isnumeric")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsNumeric(kwargs: Map<String, PyObject>): PyBool {
        return PyBool.get(isNumeric(kwargs.selfWrappedString))
    }

    /** str.islower */
    @ExposeMethod("islower")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsLower(kwargs: Map<String, PyObject>): PyBool {
        val isLower = kwargs.selfWrappedString.run { isNotEmpty() && all { it.isLowerCase() } }
        return PyBool.get(isLower)
    }

    /** str.isupper */
    @ExposeMethod("isupper")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsUpper(kwargs: Map<String, PyObject>): PyBool {
        val isUpper = kwargs.selfWrappedString.run { isNotEmpty() && all { it.isUpperCase() } }
        return PyBool.get(isUpper)
    }

    /** str.isspace */
    @ExposeMethod("isspace")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsSpace(kwargs: Map<String, PyObject>): PyBool {
        val isSpace = kwargs.selfWrappedString.run { isNotEmpty() && all { it.isWhitespace() } }
        return PyBool.get(isSpace)
    }

    /** str.istitle */
    @ExposeMethod("istitle")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrIsTitle(kwargs: Map<String, PyObject>): PyBool {
        val str = kwargs.selfWrappedString
        return PyBool.get((str.toLowerCase().capitalize() == str) && str.isNotEmpty())
    }

    /** str.title */
    @ExposeMethod("title")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrTitle(kwargs: Map<String, PyObject>): PyString {
        return PyString(kwargs.selfWrappedString.toLowerCase().capitalize())
    }

    /** str.partition */
    @ExposeMethod("partition")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("sep", "POSITIONAL")
    )
    fun pyStrPartition(kwargs: Map<String, PyObject>): PyTuple {
        var separator = kwargs["sep"].cast<PyString>().wrappedString
        val splitResult = kwargs.selfWrappedString.run {
            split(separator, limit = 2).takeIf { it.size == 2 }
                ?: listOf(this, "").also { separator = "" }
        }

        val args = listOf(
            PyString(splitResult[0]),
            PyString(separator),
            PyString(splitResult[1])
        )
        return PyTuple.get(args)
    }

    /** str.rpartition */
    @ExposeMethod("rpartition")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("sep", "POSITIONAL")
    )
    fun pyStrRPartition(kwargs: Map<String, PyObject>): PyTuple {
        val separator = kwargs["sep"].cast<PyString>().wrappedString
        val string = kwargs.selfWrappedString
        val index = string.lastIndexOf(separator)
        val args = if (index == -1) {
            // Not found
            listOf("", "", string)
        } else {
            string.run { listOf(substring(0, index), separator, substring(index + 1)) }
        }

        return PyTuple.get(args.map { PyString(it) })
    }

    /** str.swapcase */
    @ExposeMethod("swapcase")
    @MethodParams(MethodParam("self", "POSITIONAL"))
    fun pyStrSwapCase(kwargs: Map<String, PyObject>): PyString {
        val swapped = kwargs.selfWrappedString
            .map { if (it.isUpperCase()) it.toLowerCase() else it.toUpperCase() }
            .joinToString("")
        return PyString(swapped)
    }

    /** str.zfill */
    @ExposeMethod("zfill")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("width", "POSITIONAL")
    )
    fun pyStrZFill(kwargs: Map<String, PyObject>): PyString {
        val width = kwargs["width"].cast<PyInt>().wrappedInt
        return PyString(kwargs.selfWrappedString.padStart(width.toInt(), '0'))
    }

    /** Attempts to unwrap a PyObject, if it's not EMPTY. **/
    private fun <T : Any> PyObject?.unwrappedOrNull(): T? {
        if (this == null || this === EMPTY) return null
        @Suppress("UNCHECKED_CAST")
        return (this as? PyPrimitive)?.unwrap() as T
    }

    /** str.count **/
    @ExposeMethod("count")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("sub", "POSITIONAL"),
        MethodParam("start", "POSITIONAL"),
        MethodParam("end", "POSITIONAL"),
        defaults = [Default("start", EMPTY::class), Default("end", EMPTY::class)]
    )
    fun pyStrCount(kwargs: Map<String, PyObject>): PyInt {
        val str = kwargs.selfWrappedString
        val sub = kwargs["sub"].cast<PyString>().wrappedString
        val start = kwargs["start"].unwrappedOrNull<Long>()?.toInt() ?: 0
        val end = kwargs["end"].unwrappedOrNull<Long>()?.toInt() ?: str.length
        // str.count supports slices.
        val count = SliceAwareRange(str, start, end).sliced.split(sub).size - 1
        return PyInt(count.toLong())
    }
}
