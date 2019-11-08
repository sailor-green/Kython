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

package green.sailor.kython.interpreter.objects

import arrow.core.Either
import green.sailor.kython.interpreter.instruction.Instruction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.PyString
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.marshal.MarshalCodeObject

/**
 * Represents a Python function object.
 *
 * @param codeObject: The marshalled code object to transform into a real code object.
 */
class KyFunction(codeObject: MarshalCodeObject) : PyCallable, PyObject() {
    /** The code object for this function. */
    val code = KyCodeObject(codeObject)

    // helper methods
    /**
     * Gets the instruction at the specified index.
     */
    fun getInstruction(idx: Int): Instruction {
        return this.code.instructions[idx]
    }

    /**
     * Gets a global from the globals for this function.
     */
    fun getGlobal(name: String): Either<PyException, PyObject> {
        val wrapped = PyString(name)
        if (wrapped in Builtins.BUILTINS_MAP.items) {
            return Either.right(Builtins.BUILTINS_MAP.items[wrapped]!!)
        }
        TODO()
    }

    override fun getFrame(parentFrame: StackFrame): StackFrame =
        UserCodeStackFrame(this).apply { this.parentFrame = parentFrame }

    override fun toPyString(): PyString = PyString("<user function ${code.codename}>")
    override fun toPyStringRepr(): PyString = toPyString()
}
