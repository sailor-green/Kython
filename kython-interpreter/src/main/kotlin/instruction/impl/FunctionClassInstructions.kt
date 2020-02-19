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

import green.sailor.kython.interpreter.functions.BuildClassFunction
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.pyobject.internal.PyCodeObject
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast

/**
 * CALL_FUNCTION.
 */
fun UserCodeStackFrame.callFunction(opval: Byte) {
    // CALL_FUNCTION(argc)
    // pops (argc) arguments off the stack (right to left) then invokes a function.
    val toCallWith = mutableListOf<PyObject>()
    for (x in 0 until opval.toInt()) {
        toCallWith.add(stack.pop())
    }

    val fn = stack.pop()
    if (!fn.kyIsCallable()) {
        typeError("'${fn.type.name}' is not callable")
    }

    val result = fn.pyCall(toCallWith)
    stack.push(result)
    bytecodePointer += 1
}

/**
 * MAKE_FUNCTION.
 */
fun UserCodeStackFrame.makeFunction(arg: Byte) {
    // toInt to use bitwise `and`
    val flags = arg.toInt()
    val qualifiedName = stack.pop()
    require(qualifiedName is PyString) { "Function qualified name was not string!" }

    val code = stack.pop()
    require(code is PyCodeObject) { "Function code was not a code object!" }

    val builder = PyUserFunction.Builder(code.wrappedCodeObject)

    if (flags and FunctionFlags.FREEVARS != 0) {
        val tup = stack.pop().cast<PyTuple>()
        builder.closure(tup)
    }

    if (flags and FunctionFlags.ANNOTATIONS != 0) {
        val annotationDict = stack.pop()
    }

    if (flags and FunctionFlags.KEYWORD_DEFAULT != 0) {
        builder.keywordDefaults(stack.pop().cast())
    }

    if (flags and FunctionFlags.POSITIONAL_DEFAULT != 0) {
        builder.positionalDefaults(stack.pop().cast())
    }

    val function = builder.build()
    function.module = this.function.module
    stack.push(function)
    bytecodePointer += 1
}

/**
 * LOAD_BUILD_CLASS.
 */
fun UserCodeStackFrame.loadBuildClass(arg: Byte) {
    stack.push(BuildClassFunction)
    bytecodePointer += 1
}
