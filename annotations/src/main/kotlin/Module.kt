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
 * Exposes a KyModule as a PyBuiltinModule.
 * Code will be automatically generated for these builtin modules to add them to the modules dict.
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class GenerateModule(
    val name: String
)

/**
 * Exposes a field on a module to Python.
 */
@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class ExposeField(
    val name: String
)
