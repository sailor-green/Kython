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

@file:Suppress("SpellCheckingInspection", "unused", "UNUSED_PARAMETER")

package green.sailor.kython.interpreter.stack

import green.sailor.kython.MakeUp
import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.instruction.PythonInstruction
import green.sailor.kython.interpreter.instruction.impl.*
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyRootObjectInstance
import green.sailor.kython.interpreter.throwKy
import green.sailor.kython.util.PythonFunctionStack
import java.util.*

/**
 * Represents a single stack frame on the stack of stack frames.
 *
 * @param function: The function being ran. This may not be a *real* function, but we treat it as if it is.
 */
@Suppress("MemberVisibilityCanBePrivate")
class UserCodeStackFrame(val function: PyUserFunction) : StackFrame() {
    /**
     * The bytecode pointer to the bytecode of the KyFunction.
     *
     * This points to the actual instruction index, not the raw code index.
     */
    var bytecodePointer: Int = 0

    // debug mode helper
    // this is used in case an instruction is written without emitting a bytecode change
    @Suppress("PropertyName")
    var _lastBytecodePointer: Int = -1

    /**
     * The inner stack for this stack frame.
     */
    val stack = PythonFunctionStack(function.code.stackSize)

    /**
     * The block stack for this stack frame.
     */
    val blockStack = ArrayDeque<Block>()

    /**
     * The local variables for this frame.
     */
    val locals = linkedMapOf<String, PyObject>()

    /**
     * Gets the source code line number currently being executed.
     */
    val lineNo: Int get() = function.code.getLineNumber(bytecodePointer)

    /**
     * Utility function for calling magic methods
     */
    @JvmOverloads
    fun magicMethod(obj: PyObject, magicName: String, param: PyObject? = null) {
        val fn = obj.pyGetAttribute(magicName)
        if (!fn.kyIsCallable()) {
            Exceptions.TYPE_ERROR("'${obj.type.name}'.$magicName is not callable.").throwKy()
        }
        val result = if (param != null) fn.kyCall(listOf(param)) else fn.kyCall(listOf())
        stack.push(result)
    }

    fun magicMethod(obj: PyObject, magicName: String, param: PyObject, fallback: String) {
        try {
            magicMethod(obj, magicName, param)
        } catch (e: KyError) {
            try {
                magicMethod(param, fallback, obj)
            } catch (_: KyError) {
                throw e
            }
        }
    }

    /**
     * Creates a new [StackFrameInfo] for this stack frame.
     */
    override fun createStackFrameInfo(): StackFrameInfo.UserFrameInfo =
        StackFrameInfo.UserFrameInfo(this)

    /**
     * Runs this stack frame, executing the function within.
     */
    override fun runFrame(kwargs: Map<String, PyObject>): PyObject {
        locals.putAll(kwargs)

        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = function.getInstruction(bytecodePointer)
            if (MakeUp.debugMode) {

                if (_lastBytecodePointer == bytecodePointer) {
                    System.err.println(
                        "WARNING: Bytecode pointer is unchanged from last instruction"
                    )
                    System.err.println("WARNING: You may have forgotten a bytecodePointer += 1!")
                }

                System.err.println("idx: $bytecodePointer | Next instruction: $nextInstruction")
                _lastBytecodePointer = bytecodePointer
            }
            if (nextInstruction !is PythonInstruction) TODO("Intristic instruction evaluation")

            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument
            // special case this, because it returns from runFrame
            if (opcode == InstructionOpcode.RETURN_VALUE) {
                return stack.pop()
            }

            // switch on opcode
            // Reference: https://docs.python.org/3/library/dis.html#python-bytecode-instructions
            try { when (opcode) {
                // block ops
                InstructionOpcode.SETUP_FINALLY -> setupFinally(param)
                InstructionOpcode.POP_EXCEPT -> popExcept(param)
                InstructionOpcode.POP_BLOCK -> popBlock(param)
                InstructionOpcode.RERAISE -> reraise(param)

                // import ops
                InstructionOpcode.IMPORT_NAME -> importName(param)

                // load ops
                InstructionOpcode.LOAD_FAST -> load(LoadPool.FAST, param)
                InstructionOpcode.LOAD_NAME -> load(LoadPool.NAME, param)
                InstructionOpcode.LOAD_CONST -> load(LoadPool.CONST, param)
                InstructionOpcode.LOAD_GLOBAL -> load(LoadPool.GLOBAL, param)
                InstructionOpcode.LOAD_ATTR -> load(LoadPool.ATTR, param)
                InstructionOpcode.LOAD_METHOD -> load(LoadPool.METHOD, param)

                // store ops
                InstructionOpcode.STORE_NAME -> store(LoadPool.NAME, param)
                InstructionOpcode.STORE_FAST -> store(LoadPool.FAST, param)
                InstructionOpcode.STORE_ATTR -> storeAttr(param)

                // delete ops
                InstructionOpcode.DELETE_FAST -> delete(LoadPool.FAST, param)
                InstructionOpcode.DELETE_NAME -> delete(LoadPool.NAME, param)

                // build ops
                InstructionOpcode.BUILD_TUPLE -> buildSimple(BuildType.TUPLE, param)
                InstructionOpcode.BUILD_LIST -> buildSimple(BuildType.LIST, param)
                InstructionOpcode.BUILD_STRING -> buildSimple(BuildType.STRING, param)
                InstructionOpcode.BUILD_SET -> buildSimple(BuildType.SET, param)
                InstructionOpcode.BUILD_CONST_KEY_MAP -> buildConstKeyMap(param)

                InstructionOpcode.LIST_APPEND -> listAppend(param)
                /*InstructionOpcode.SET_ADD -> setAdd(param)
                InstructionOpcode.MAP_ADD -> mapAdd(param)*/

                // binary ops
                InstructionOpcode.BINARY_ADD -> binaryOp(BinaryOp.ADD, param)
                InstructionOpcode.BINARY_POWER -> binaryOp(BinaryOp.POWER, param)
                InstructionOpcode.BINARY_MULTIPLY -> binaryOp(BinaryOp.MULTIPLY, param)
                InstructionOpcode.BINARY_MATRIX_MULTIPLY ->
                    binaryOp(BinaryOp.MATRIX_MULTIPLY, param)
                InstructionOpcode.BINARY_FLOOR_DIVIDE ->
                    binaryOp(BinaryOp.FLOOR_DIVIDE, param)
                InstructionOpcode.BINARY_TRUE_DIVIDE ->
                    binaryOp(BinaryOp.TRUE_DIVIDE, param)
                InstructionOpcode.BINARY_MODULO -> binaryOp(BinaryOp.MODULO, param)
                InstructionOpcode.BINARY_SUBTRACT -> binaryOp(BinaryOp.SUBTRACT, param)
                InstructionOpcode.BINARY_SUBSCR -> binaryOp(BinaryOp.SUBSCR, param)
                InstructionOpcode.BINARY_LSHIFT -> binaryOp(BinaryOp.LSHIFT, param)
                InstructionOpcode.BINARY_RSHIFT -> binaryOp(BinaryOp.RSHIFT, param)
                InstructionOpcode.BINARY_AND -> binaryOp(BinaryOp.AND, param)
                InstructionOpcode.BINARY_XOR -> binaryOp(BinaryOp.XOR, param)
                InstructionOpcode.BINARY_OR -> binaryOp(BinaryOp.OR, param)

                // inplace binary ops
                InstructionOpcode.INPLACE_ADD -> inplaceOp(BinaryOp.ADD, param)
                InstructionOpcode.INPLACE_POWER -> inplaceOp(BinaryOp.POWER, param)
                InstructionOpcode.INPLACE_MULTIPLY -> inplaceOp(BinaryOp.MULTIPLY, param)
                InstructionOpcode.INPLACE_MATRIX_MULTIPLY ->
                    inplaceOp(BinaryOp.MATRIX_MULTIPLY, param)
                InstructionOpcode.INPLACE_FLOOR_DIVIDE ->
                    inplaceOp(BinaryOp.FLOOR_DIVIDE, param)
                InstructionOpcode.INPLACE_TRUE_DIVIDE ->
                    inplaceOp(BinaryOp.TRUE_DIVIDE, param)
                InstructionOpcode.INPLACE_MODULO -> inplaceOp(BinaryOp.MODULO, param)
                InstructionOpcode.INPLACE_SUBTRACT -> inplaceOp(BinaryOp.SUBTRACT, param)
                InstructionOpcode.INPLACE_LSHIFT -> inplaceOp(BinaryOp.LSHIFT, param)
                InstructionOpcode.INPLACE_RSHIFT -> inplaceOp(BinaryOp.RSHIFT, param)
                InstructionOpcode.INPLACE_AND -> inplaceOp(BinaryOp.AND, param)
                InstructionOpcode.INPLACE_XOR -> inplaceOp(BinaryOp.XOR, param)
                InstructionOpcode.INPLACE_OR -> inplaceOp(BinaryOp.OR, param)
                InstructionOpcode.STORE_SUBSCR -> inplaceOp(BinaryOp.STORE_SUBSCR, param)
                InstructionOpcode.DELETE_SUBSCR -> inplaceOp(BinaryOp.DELETE_SUBSCR, param)

                // fundamentally the same thing.
                InstructionOpcode.CALL_METHOD -> callFunction(param)
                InstructionOpcode.CALL_FUNCTION -> callFunction(param)

                // stack ops
                InstructionOpcode.POP_TOP -> popTop(param)
                InstructionOpcode.ROT_TWO -> rotTwo(param)
                InstructionOpcode.ROT_THREE -> rotThree(param)
                InstructionOpcode.ROT_FOUR -> rotFour(param)
                InstructionOpcode.DUP_TOP -> dupTop(param)
                InstructionOpcode.DUP_TOP_TWO -> dupTopTwo(param)

                // jump ops
                InstructionOpcode.JUMP_ABSOLUTE -> jumpAbsolute(param)
                InstructionOpcode.JUMP_FORWARD -> jumpForward(param)
                InstructionOpcode.POP_JUMP_IF_FALSE -> popJumpIf(param, false)
                InstructionOpcode.POP_JUMP_IF_TRUE -> popJumpIf(param, true)

                // Unary operations
                InstructionOpcode.UNARY_POSITIVE -> unaryOp(UnaryOp.POSITIVE, param)
                InstructionOpcode.UNARY_NEGATIVE -> unaryOp(UnaryOp.NEGATIVE, param)
                InstructionOpcode.UNARY_NOT -> unaryOp(UnaryOp.NOT, param)
                InstructionOpcode.UNARY_INVERT -> unaryOp(UnaryOp.INVERT, param)

                // iteration
                InstructionOpcode.GET_ITER -> getIter(param)
                InstructionOpcode.FOR_ITER -> forIter(param)
                InstructionOpcode.GET_YIELD_FROM_ITER -> getYieldIter(param)

                InstructionOpcode.NOP -> Unit

                // meta
                InstructionOpcode.MAKE_FUNCTION -> makeFunction(param)
                InstructionOpcode.LOAD_BUILD_CLASS -> loadBuildClass(param)

                InstructionOpcode.COMPARE_OP -> compareOp(param)
                else -> error("Unimplemented opcode $opcode")
            } } catch (e: KyError) {
                // if no block, just throw through
                if (blockStack.isEmpty()) throw e

                // if yes block, jump to where the finally says we should jump
                // traceback, excval, exctype
                stack.push(PyRootObjectInstance()) // TODO
                stack.push(e.wrapped)
                stack.push(e.wrapped.type)
                val tobs = blockStack.pop()
                bytecodePointer = tobs.delta
            }
        }
    }

    // scary instruction implementations
    // this is all below the main class because there's a LOT going on her

    /**
     * IMPORT_FROM
     */
    fun importFrom(arg: Byte) {
        val module = stack.last
        val attrName = function.code.names[arg.toInt()]
        val attr = module.pyGetAttribute(attrName)
        stack.push(attr)
        bytecodePointer += 1
    }

    // Unary operators
    /**
     * GET_YIELD_ITER
     */
    fun getYieldIter(param: Byte) {
        TODO("Implement GET_YIELD_ITER")
    }

    /**
     * UNARY_*
     */
    fun unaryOp(type: UnaryOp, param: Byte) {
        val top = stack.pop()
        when (type) {
            UnaryOp.INVERT -> stack.push(top.pyInvert())
            UnaryOp.NOT -> stack.push(!top.pyToBool())
            UnaryOp.NEGATIVE -> stack.push(top.pyNegative())
            UnaryOp.POSITIVE -> stack.push(top.pyPositive())
        }
        bytecodePointer += 1
    }
}
