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

package green.sailor.kython.interpreter.stack

import green.sailor.kython.interpreter.objects.KyBuiltinFunction
import green.sailor.kython.interpreter.objects.python.PyDict
import green.sailor.kython.interpreter.objects.python.PyTuple

/**
 * Represents a built-in stack frame.
 */
class BuiltinStackFrame(val builtinFunction: KyBuiltinFunction) : StackFrame() {
    override fun runFrame(args: PyTuple, kwargs: PyDict): InterpreterResult {
        return this.builtinFunction.callFunction(args, kwargs)
    }

    override fun getStackFrameInfo(): StackFrameInfo.BuiltinFrameInfo {
        return StackFrameInfo.BuiltinFrameInfo(this)
    }
}
