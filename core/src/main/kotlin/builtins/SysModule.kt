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

package green.sailor.kython.builtins

import green.sailor.kython.annotation.ExposeField
import green.sailor.kython.annotation.GenerateModule
import green.sailor.kython.interpreter.kyobject.KyBuiltinModule
import green.sailor.kython.interpreter.pyobject.PyString

/**
 * Represents the sys built-in module.
 */
@GenerateModule("sys")
object SysModule : KyBuiltinModule(
    "sys",
    "Contains internal objects used by the interpreter."
) {
    @ExposeField("platform")
    var platform = PyString(System.getProperty("os.name").toLowerCase())
}
