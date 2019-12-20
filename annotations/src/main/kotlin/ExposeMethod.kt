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

package green.sailor.kython.annotation

/**
 * Exposes a Kotlin method to the Python world. This will wrap it in a PyBuiltinFunction and add it
 * to the internalDict of the object.
 *
 * @param name: The Python name to expose this method to.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class ExposeMethod(
    val name: String
)

/**
 * Represents a method parameter.
 *
 * @param name: The name of the method parameter.
 * @param argType: The argument type of the method parameter. Will be looked up from ArgType.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class MethodParam(val name: String, val type: String)

/**
 * Represents a collection of method parameters.
 *
 * @param parameters the collection of [MethodParam]s
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class MethodParams(vararg val parameters: MethodParam)
