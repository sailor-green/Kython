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
import green.sailor.kython.test.assertUnwrappedTrue
import green.sailor.kython.test.testWithObject
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TestPyString {
    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str_lower`(value: String) {
        val result = testWithObject<PyString>("""result = str("$value").lower()""")
        assertUnwrappedTrue(result) { it == value.toLowerCase() }
    }

    @ParameterizedTest(name = "For value {0}")
    @ValueSource(strings = ["test", "randomString", "123", "", "1Hello", "Hey!"])
    fun `Test str_upper`(value: String) {
        val result = testWithObject<PyString>("""result = str("$value").upper()""")
        assertUnwrappedTrue(result) { it == value.toUpperCase() }
    }
}
