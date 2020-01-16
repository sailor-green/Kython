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

package green.sailor.kython.test.pyobject

import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.test.testPrimitive
import java.util.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TestPyString {
    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str lower`(value: String) {
        testPrimitive<PyString>("""result = str("$value").lower()""") {
            resultsIn(value.toLowerCase())
        }
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str upper`(value: String) {
        testPrimitive<PyString>("""result = str("$value").upper()""") {
            resultsIn(value.toUpperCase())
        }
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str capitalize`(value: String) {
        testPrimitive<PyString>("""result = str("$value").capitalize()""") {
            resultsIn(value.capitalize())
        }
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "Grüße"])
    fun `Test str casefold`(value: String) {
        testPrimitive<PyString>("""result = str("$value").casefold()""") {
            resultsIn(value.toUpperCase(Locale.US).toLowerCase(Locale.US))
        }
    }

    @Test
    fun `Test str center width and fillchar`() {
        testPrimitive<PyString>("""result = str("moon").center(10, ":")""") {
            resultsIn(":::moon:::")
        }
    }

    @Test
    // We're not supporting default values for generated functions yet.
    @Disabled
    fun `Test str center only width`() {
        testPrimitive<PyString>("""result = str("moon").center(10)""") {
            resultsIn("   moon   ")
        }
    }

    @Test
    fun `Test str isalpha`() {
        testPrimitive<PyBool>("""result = "abc".isalpha()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "123".isalpha()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str isascii`() {
        testPrimitive<PyBool>("""result = "hello".isascii()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "€".isascii()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str isdecimal`() {
        testPrimitive<PyBool>("""result = "123".isdecimal()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "hey".isdecimal()""") {
            isFalse()
        }

        // ARABIC-INDIC DIGIT ZERO.
        testPrimitive<PyBool>("""result = "٠".isdecimal() """) {
            isTrue()
        }
    }

    @Test
    fun `Test str isdigit`() {
        // KHAROSHTHI DIGIT (UTF-16 surrogates edgecase)
        testPrimitive<PyBool>("""result = "𐩃𐩂".isdigit()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "123".isdigit()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "hey".isdigit()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str isnumeric`() {
        // KHAROSHTHI DIGIT (UTF-16 surrogates edgecase)
        testPrimitive<PyBool>("""result = "𐩃𐩂".isnumeric()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "123".isnumeric()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "hey".isnumeric()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str isalnum`() {
        // KHAROSHTHI DIGIT (UTF-16 surrogates edgecase)
        testPrimitive<PyBool>("""result = "𐩃𐩂".isalnum()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "".isalnum()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "hey".isalnum()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "\n".isalnum()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "2b".isalnum()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str islower`() {
        testPrimitive<PyBool>("""result = "123".islower()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "hey".islower()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "HEY".islower()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str isupper`() {
        testPrimitive<PyBool>("""result = "123".isupper()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "hey".isupper()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "HEY".isupper()""") {
            isTrue()
        }
    }

    @Test
    fun `Test str isspace`() {
        testPrimitive<PyBool>("""result = "".isspace()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = " ".isspace()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "\n".isspace()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "\u200b".isspace()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str istitle`() {
        testPrimitive<PyBool>("""result = "".istitle()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "Hello".istitle()""") {
            isTrue()
        }

        testPrimitive<PyBool>("""result = "HELLO".istitle()""") {
            isFalse()
        }

        testPrimitive<PyBool>("""result = "HEllo".istitle()""") {
            isFalse()
        }
    }

    @Test
    fun `Test str title`() {
        testPrimitive<PyBool>("""result = "hello".title()""") {
            resultsIn("Hello")
        }

        testPrimitive<PyBool>("""result = "HeLlO".title()""") {
            resultsIn("Hello")
        }

        testPrimitive<PyBool>("""result = "\n".title()""") {
            resultsIn("\n")
        }

        testPrimitive<PyBool>("""result = "X".title()""") {
            resultsIn("X")
        }
    }

    @Test
    fun `Test str partition`() {
        testPrimitive<PyTuple>("""result = "hello world friend".partition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("hello", " ", "world friend"))
        }

        testPrimitive<PyTuple>("""result = "hello ".partition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("hello", " ", ""))
        }

        testPrimitive<PyTuple>("""result = "hello".partition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("hello", "", ""))
        }

        testPrimitive<PyTuple>("""result = "hello".partition("hey")""") {
            flattenedPyResultsIn<PyString>(listOf("hello", "", ""))
        }
    }

    @Test
    fun `Test str rpartition`() {
        testPrimitive<PyTuple>("""result = "hello world friend".rpartition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("hello world", " ", "friend"))
        }

        testPrimitive<PyTuple>("""result = "hello ".rpartition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("hello", " ", ""))
        }

        testPrimitive<PyTuple>("""result = "hello".rpartition(" ")""") {
            flattenedPyResultsIn<PyString>(listOf("", "", "hello"))
        }

        testPrimitive<PyTuple>("""result = "hello".rpartition("hey")""") {
            flattenedPyResultsIn<PyString>(listOf("", "", "hello"))
        }
    }

    @Test
    fun `Test str swapcase`() {
        testPrimitive<PyString>("""result = "hello".swapcase()""") {
            resultsIn("HELLO")
        }

        testPrimitive<PyString>("""result = "hELLO".swapcase()""") {
            resultsIn("Hello")
        }

        testPrimitive<PyString>("""result = "\n".swapcase()""") {
            resultsIn("\n")
        }
    }

    @Test
    fun `Test str zfill`() {
        testPrimitive<PyString>("""result = "abc".zfill(3)""") {
            resultsIn("abc")
        }

        testPrimitive<PyString>("""result = "123".zfill(6)""") {
            resultsIn("000123")
        }
    }
}
