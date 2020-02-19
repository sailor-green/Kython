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

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.instruction.PythonInstruction
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyRootObjectInstance
import green.sailor.kython.interpreter.pyobject.generator.PyGenerator
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * Evaluates bytecode for a [UserCodeStackFrame]. This is the main interpreter loop.
 */
fun UserCodeStackFrame.evaluateBytecode(): PyObject {
    if (KythonInterpreter.config.debugMode) {
        System.err.println("=== Evaluating frame for ${function.code.codename} / " +
            "${function.code.filename} ===")
    }

    state = StackFrame.FrameState.RUNNING
    while (true) {
        // simple fetch decode execute loop
        // maybe this could be pipelined.
        val nextInstruction = function.getInstruction(bytecodePointer)
        if (KythonInterpreter.config.debugMode) {
            val stream = System.err
            if (_lastBytecodePointer == bytecodePointer) {
                stream.println("WARNING: Bytecode pointer is unchanged from last instruction")
                stream.println("WARNING: You may have forgotten a bytecodePointer += 1!")
            }

            stream.println("idx: $bytecodePointer | Next instruction: $nextInstruction")
            _lastBytecodePointer = bytecodePointer
        }
        if (nextInstruction !is PythonInstruction) TODO("Intristic instruction evaluation")

        val opcode = nextInstruction.opcode
        val param = nextInstruction.argument

        // switch on opcode
        // Reference: https://docs.python.org/3/library/dis.html#python-bytecode-instructions
        try { when (opcode) {
            // == Special Instructions == //
            InstructionOpcode.RETURN_VALUE -> {
                val returned = stack.pop()
                state = StackFrame.FrameState.RETURNED
                return returned
            }

            InstructionOpcode.YIELD_VALUE -> {
                // This will be the value that was sent to us, via gen.send()
                val yielded = stack.pop()
                state = StackFrame.FrameState.YIELDING

                // when we resume, we want to go to the next instruction
                // (or else we yield endlessly)
                bytecodePointer += 1
                return yielded
            }

            InstructionOpcode.YIELD_FROM -> {
                // A note to all bytecode readers:
                // A YIELD_FROM is always preceded by a LOAD_CONST None.
                // This is the initial value to send to the generator to start the generator.
                // Afterwards, the value just sent to us will be on TOS (via send) so we send
                // that up.
                val toSend = stack.pop()
                // note: always keep the generator on the stack.
                // optimise slightly: if generator, always just send
                val gen = stack.last
                if (gen is PyGenerator) {
                    val (state, yielded) = gen.sendRaw(toSend)
                    // if the state is yielding, we update our state to signify we are
                    // ALSO yielding, then return the yielded value
                    // we do NOT update our pointer because we want to return to YIELD_FROM
                    // immediately afterwards.
                    if (state.isYielding) {
                        this.state = StackFrame.FrameState.YIELD_FROM
                        return yielded
                    } else {
                        // state is non-yielding, so we continue onwards
                        // pop off the generator
                        stack.pop()
                        stack.push(yielded)
                        bytecodePointer += 1
                    }
                } else {
                    // not a generator; we go through the standard iteration flow
                    // the object is *already* an iterator (due to get_yield_from_iter)
                    // two paths taken here:
                    // 1) StopIteration, we continue to the next instruction
                    // 2) no StopIteration, we simply yield the value
                    try {
                        val yielded = gen.pyNext()
                        state = StackFrame.FrameState.YIELD_FROM
                        return yielded
                    } catch (e: KyError) {
                        e.ensure(Exceptions.STOP_ITERATION)
                        // pop off the iterator
                        stack.pop()
                    }
                }
            }

            // == Regular Instructions == //

            // exceptions
            InstructionOpcode.SETUP_FINALLY -> setupFinally(param)
            InstructionOpcode.POP_EXCEPT -> popExcept(param)
            InstructionOpcode.POP_BLOCK -> popBlock(param)
            InstructionOpcode.RERAISE -> reraise(param)
            InstructionOpcode.JUMP_IF_NOT_EXC_MATCH -> jumpIfNotExcMatch(param)

            // import ops
            InstructionOpcode.IMPORT_NAME -> importName(param)
            InstructionOpcode.IMPORT_FROM -> importFrom(param)

            // load ops
            InstructionOpcode.LOAD_FAST -> load(LoadPool.FAST, param)
            InstructionOpcode.LOAD_NAME -> load(LoadPool.NAME, param)
            InstructionOpcode.LOAD_CONST -> load(LoadPool.CONST, param)
            InstructionOpcode.LOAD_GLOBAL -> load(LoadPool.GLOBAL, param)
            InstructionOpcode.LOAD_ATTR -> load(LoadPool.ATTR, param)
            InstructionOpcode.LOAD_METHOD -> load(LoadPool.METHOD, param)

            InstructionOpcode.GET_YIELD_FROM_ITER -> getYieldFromIter(param)

            // store ops
            InstructionOpcode.STORE_NAME -> storeLocal(LoadPool.NAME, param)
            InstructionOpcode.STORE_FAST -> storeLocal(LoadPool.FAST, param)
            InstructionOpcode.STORE_GLOBAL -> storeGlobal(param)
            InstructionOpcode.STORE_ATTR -> storeAttr(param)

            // closure awfulness
            InstructionOpcode.STORE_DEREF -> storeDeref(param)
            InstructionOpcode.LOAD_DEREF -> loadDeref(param)
            InstructionOpcode.LOAD_CLOSURE -> loadClosure(param)

            // delete ops
            InstructionOpcode.DELETE_FAST -> delete(LoadPool.FAST, param)
            InstructionOpcode.DELETE_NAME -> delete(LoadPool.NAME, param)

            // build ops
            InstructionOpcode.BUILD_TUPLE -> buildSimple(BuildType.TUPLE, param)
            InstructionOpcode.BUILD_LIST -> buildSimple(BuildType.LIST, param)
            InstructionOpcode.BUILD_STRING -> buildSimple(BuildType.STRING, param)
            InstructionOpcode.BUILD_MAP -> buildSimple(BuildType.DICT, param)
            InstructionOpcode.BUILD_SET -> buildSimple(BuildType.SET, param)
            InstructionOpcode.BUILD_CONST_KEY_MAP -> buildConstKeyMap(param)

            // unpacking
            InstructionOpcode.LIST_APPEND -> listAppend(param)
            InstructionOpcode.LIST_EXTEND -> listExtend(param)
            InstructionOpcode.LIST_TO_TUPLE -> listToTuple(param)
            InstructionOpcode.SET_UPDATE -> setUpdate(param)
            InstructionOpcode.UNPACK_SEQUENCE -> unpackSequence(param)

            // comprehension
            InstructionOpcode.SET_ADD -> setAdd(param)
            InstructionOpcode.MAP_ADD -> mapAdd(param)

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

            InstructionOpcode.BINARY_SUBSCR -> getItem(param)
            InstructionOpcode.STORE_SUBSCR -> setItem(param)
            // InstructionOpcode.DELETE_SUBSCR -> deleteItem()

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

            // meta
            InstructionOpcode.MAKE_FUNCTION -> makeFunction(param)
            InstructionOpcode.LOAD_BUILD_CLASS -> loadBuildClass(param)

            // comparison operators
            InstructionOpcode.COMPARE_OP -> compareOp(param)
            InstructionOpcode.IS_OP -> isOp(param)
            InstructionOpcode.CONTAINS_OP -> containsOp(param)

            InstructionOpcode.NOP -> Unit

            else -> {
                if (KythonInterpreter.config.debugMode) {
                    error("Unimplemented opcode $opcode")
                } else {
                    Exceptions.SYSTEM_ERROR("Unimplemented opcode $opcode").throwKy()
                }
            }
        } } catch (e: KyError) {
            // == Generator Path == //
            // StopIteration
            val isGenerator = function.code.flags.isGenerator
            if (isGenerator && e.pyError.isinstance(Exceptions.STOP_ITERATION)) {
                TODO("StopIteration -> RuntimeError")
            }

            // == Regular path == //

            // if no block, just throw through
            if (blockStack.isEmpty()) {
                state = StackFrame.FrameState.ERRORED
                throw e
            }

            // if yes block, jump to where the finally says we should jump
            // traceback, excval, exctype
            stack.push(PyRootObjectInstance()) // TODO
            stack.push(e.pyError)
            stack.push(e.pyError.type)
            val tobs = blockStack.pop()
            bytecodePointer = tobs.delta
        }
    }
}
