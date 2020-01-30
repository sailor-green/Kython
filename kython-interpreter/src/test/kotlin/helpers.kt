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

package green.sailor.kython.test

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.PyBool
import green.sailor.kython.interpreter.pyobject.PyContainer
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyPrimitive
import green.sailor.kython.test.helpers.assertUnwrappedEquals
import green.sailor.kython.test.helpers.testExecInternal
import org.junit.jupiter.api.Assertions

/**
 * Tests the compiler with [code] using the [builder block][block] and unwraps
 * the result into the specified [primitive][PyPrimitive].
 *
 * This function optionally takes [locals][args]
 *
 * @see KythonInterpreter.testExecInternal
 */
inline fun <T : PyPrimitive> testPrimitive(
    code: String,
    args: Map<String, PyObject> = mapOf(),
    block: PyObjectTester<T>.() -> Unit
) =
    PyObjectTester<T>(code, args).block()

/** Asserts whether a given unwrapped[code] result is true. */
internal fun isTrue(code: String) =
    testPrimitive<PyBool>(code) { isTrue() }

/** Asserts whether a given unwrapped[code] result is false. */
internal fun isFalse(code: String) =
    testPrimitive<PyBool>(code) { isFalse() }

/**
 * Helper class used to automatically unwrap PyObject tests results
 * along with assertion methods.
 */
class PyObjectTester<T : PyPrimitive>(
    /** The code to run the interpreter with */
    private val testCode: String,
    /** Potential arguments to pass as locals to the interpreter */
    private val args: Map<String, PyObject>
) {
    /** The compiler result */
    @Suppress("UNCHECKED_CAST")
    private val execResult
        get() = KythonInterpreter.testExecInternal(testCode, withErrorLogs = true) as T

    /**
     * Asserts that a [PyContainer] based result equals [expected].
     * This is done by unwrapping and flattening [execResult] into its JVM core type.
     */
    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified U : PyPrimitive> flattenedPyResultsIn(expected: Any?) {
        val result = execResult
        check(result is PyContainer) { "Result was not a PyContainer" }
        val unwrapped = (result.unwrap() as List<U>).map { it.unwrap() }
        Assertions.assertEquals(expected, unwrapped)
    }

    // General note:
    // We run .testExec each time to exclude possible side-effects.

    /**
     * Asserts that an unwrapped [execResult] equals [expected].
     */
    fun resultsIn(expected: Any?) {
        assertUnwrappedEquals(execResult, expected, testCode)
    }

    /**
     * Asserts that an unwrapped [execResult] is true.
     */
    fun isTrue() {
        assertUnwrappedEquals(execResult, true, testCode)
    }

    /**
     * Asserts that an unwrapped [execResult] is false.
     */
    fun isFalse() {
        assertUnwrappedEquals(execResult, false, testCode)
    }
}
