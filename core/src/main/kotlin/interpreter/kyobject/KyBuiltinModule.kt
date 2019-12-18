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

package green.sailor.kython.interpreter.kyobject

import green.sailor.kython.annotation.GenerateModule

/**
 * Represents a builtin module object. Built-in modules (like sys, _thread, _imp) should extend
 * this module, and mark it with [GenerateModule] to generate the appropriate [PyBuiltinModule]
 * object.
 *
 * @param name: The name of this module.
 * @param docString: The docstring for this module.
 */
abstract class KyBuiltinModule(val name: String, val docString: String)
