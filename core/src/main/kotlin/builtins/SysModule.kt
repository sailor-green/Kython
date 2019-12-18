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
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.module.PyBuiltinModule

/**
 * Represents the sys built-in module.
 */
@GenerateMethods
object SysModule : PyBuiltinModule("_kython_sys") {
    @ExposeField("platform")
    val platform = PyString(System.getProperty("os.name").toLowerCase())
}
