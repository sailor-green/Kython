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

import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.test.assertUnwrappedEquals
import green.sailor.kython.test.testWithObject
import green.sailor.kython.util.center
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*


class TestPyString {
    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str lower`(value: String) {
        val result = testWithObject<PyString>("""result = str("$value").lower()""")
        assertUnwrappedEquals(result, value.toLowerCase())
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str upper`(value: String) {
        val result = testWithObject<PyString>("""result = str("$value").upper()""")
        assertUnwrappedEquals(result, value.toUpperCase())
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str capitalize`(value: String) {
        val result =
            testWithObject<PyString>("""result = str("$value").capitalize()""")
        assertUnwrappedEquals(result, value.capitalize())
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "Grüße"])
    fun `Test str casefold`(value: String) {
        val result =
            testWithObject<PyString>("""result = str("$value").casefold()""")
        assertUnwrappedEquals(result, value.toUpperCase(Locale.US).toLowerCase(Locale.US))
    }

    @Test
    fun `Test str center width and fillchar`() {
        val result =
            testWithObject<PyString>("""result = str("moon").center(10, ":")""")
        assertUnwrappedEquals(result, "moon".center(10L, ':'))
    }

    @Test
    // We're not supporting default values for generated functions yet.
    @Disabled
    fun `Test str center only width`() {
        val result =
            testWithObject<PyString>("""result = str("moon").center(10)""")
        assertUnwrappedEquals(result, "moon".center(10L, ' '))
    }
}
