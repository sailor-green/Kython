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
 *
 */

package green.sailor.kython.interpreter.kyobject

import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import java.nio.file.Path

/**
 * Represents a Kython module. This is the internal working; this is exposed separately as a
 * PyObject to Python code.
 *
 * @param moduleFunction: The [PyUserFunction] that this module is built from.
 * @param path: The source code path this module is from.
 */
class KyModule(val moduleFunction: PyUserFunction, val path: Path) {
    /** The stack frame for this module's function. */
    val stackFrame = (moduleFunction.getFrame() as UserCodeStackFrame)

    /** The mapping of attributes of this module. */
    val attribs = stackFrame.locals

    init {
        moduleFunction.module = this
    }
}
