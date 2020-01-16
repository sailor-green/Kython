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
 * Exposes a module as a builtin module. These modules are read-only Kotlin-code implementations.
 *
 * It is recommended you expose these modules as a "hidden" module and create a pure-python shim
 * around them, instead of exposing them directly, to keep with vanilla Python behaviour.
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class BuiltinModule(
    val name: String
)
