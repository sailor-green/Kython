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

package green.sailor.kython.interpreter.instruction.impl

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.isinstance
import green.sailor.kython.interpreter.pyError
import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * GET_ITER
 */
fun UserCodeStackFrame.getIter(param: Byte) {
    val top = stack.pop()
    val iter = top.pyIter()
    stack.push(iter)
    bytecodePointer += 1
}

/**
 * FOR_ITER
 */
fun UserCodeStackFrame.forIter(param: Byte) {
    // we only peek off the top in order to get the iterator, to save allocations
    // since it's gonna be pushed back on immediately in the case the iterator has items
    val iterator = stack.first
    bytecodePointer += try {
        val next = iterator.pyNext()
        stack.push(next)

        // next instruction
        1
    } catch (e: KyError) {
        if (e.pyError.isinstance(setOf(Exceptions.STOP_ITERATION))) {
            // jump past the for
            stack.pop()
            (param.toInt() / 2) + 1
        } else {
            throw e
        }
    }
}
