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

import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * Represents a Kython module. This is the internal working; this is exposed separately as a
 * PyObject to Python code.
 *
 * @param moduleFunction: The [PyUserFunction] that this module is built from.
 * @param filename: The source code filename for this module, exposed as ` __file__`.
 * @param sourceLines: The source lines for this module.
 */
class KyUserModule(
    val moduleFunction: PyUserFunction,
    val filename: String,
    val sourceLines: List<String>
) {
    /** The stack frame for this module's function. */
    val stackFrame = moduleFunction.createFrame() as UserCodeStackFrame

    /** The mapping of attributes of this module. */
    val attribs: LinkedHashMap<String, PyObject> = stackFrame.locals

    init {
        moduleFunction.module = this
    }
}
