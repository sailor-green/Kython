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

import arrow.core.*
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.objects.Exceptions
import green.sailor.kython.interpreter.objects.functions.PyUserFunction
import green.sailor.kython.interpreter.objects.iface.PyCallable
import green.sailor.kython.interpreter.objects.python.PyCodeObject
import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyObject
import green.sailor.kython.interpreter.objects.python.primitives.PyInt
import green.sailor.kython.interpreter.objects.python.primitives.PySet
import green.sailor.kython.interpreter.objects.python.primitives.PyString
import green.sailor.kython.interpreter.objects.python.primitives.PyTuple
import java.util.*

/**
 * Represents a single stack frame on the stack of stack frames.
 *
 * @param function: The function being ran. This may not be a *real* function, but we treat it as if it is.
 */
@Suppress("MemberVisibilityCanBePrivate")
class UserCodeStackFrame(val function: PyUserFunction) : StackFrame() {
    companion object {
        /** Load pools for LOAD/STORE instructions. These represent where the instruction will operate on. */
        enum class LoadPool {
            CONST,
            FAST,
            NAME,
            ATTR,
            GLOBAL
        }

        enum class BinaryOp {
            ADD,
        }

        enum class BuildType {
            TUPLE,
            DICT,
            LIST,
            SET,
            STRING,
        }
    }

    /**
     * The bytecode pointer to the bytecode of the KyFunction.
     *
     * This points to the actual instruction index, not the raw code index.
     */
    var bytecodePointer: Int = 0

    /**
     * The inner stack for this stack frame.
     */
    val stack = ArrayDeque<PyObject>(this.function.code.stackSize)

    /** The local variables for this frame. */
    val locals = mutableMapOf<String, PyObject>()

    override fun getStackFrameInfo(): StackFrameInfo.UserFrameInfo {
        return StackFrameInfo.UserFrameInfo(this)
    }

    /**
     * Gets the source code line number currently being executed.
     */
    fun getLineNo(): Int {
        return this.function.code.getLineNumber(this.bytecodePointer)
    }

    /**
     * Runs this stack frame, executing the function within.
     */
    override fun runFrame(kwargs: Map<String, PyObject>): Either<PyException, PyObject> {
        this.locals.putAll(kwargs)

        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = this.function.getInstruction(this.bytecodePointer)
            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument

            // special case this, because it returns from runFrame
            if (nextInstruction.opcode == InstructionOpcode.RETURN_VALUE) {
                val result = this.returnValue(param)
                return Either.right(result)
            }

            // switch on opcode
            val opcodeResult = when (nextInstruction.opcode) {
                // load ops
                InstructionOpcode.LOAD_FAST -> this.load(LoadPool.FAST, param)
                InstructionOpcode.LOAD_NAME -> this.load(LoadPool.NAME, param)
                InstructionOpcode.LOAD_CONST -> this.load(LoadPool.CONST, param)
                InstructionOpcode.LOAD_GLOBAL -> this.load(LoadPool.GLOBAL, param)
                InstructionOpcode.LOAD_ATTR -> this.load(LoadPool.ATTR, param)

                // store ops
                InstructionOpcode.STORE_NAME -> this.store(LoadPool.NAME, param)
                InstructionOpcode.STORE_FAST -> this.store(LoadPool.FAST, param)

                // build ops
                InstructionOpcode.BUILD_TUPLE -> this.buildSimple(BuildType.TUPLE, param)
                InstructionOpcode.BUILD_STRING -> this.buildSimple(BuildType.STRING, param)
                InstructionOpcode.BUILD_SET -> this.buildSimple(BuildType.SET, param)

                // binary ops
                InstructionOpcode.BINARY_ADD -> this.binaryOp(BinaryOp.ADD, param)

                InstructionOpcode.CALL_FUNCTION -> this.callFunction(param)

                // stack ops
                InstructionOpcode.POP_TOP -> this.popTop(param)
                InstructionOpcode.ROT_TWO -> this.rotTwo(param)
                InstructionOpcode.ROT_THREE -> this.rotThree(param)
                InstructionOpcode.ROT_FOUR -> this.rotFour(param)
                InstructionOpcode.DUP_TOP -> this.dupTop(param)
                InstructionOpcode.DUP_TOP_TWO -> this.dupTopTwo(param)

                InstructionOpcode.MAKE_FUNCTION -> this.makeFunction(param)

                else -> error("Unimplemented opcode $opcode")
            }

            // TODO: Try handling
            if (opcodeResult.isDefined()) {
                // this will never be null, since we call isDefined.
                // we tag ourselves onto the traceback, too.
                val exc = opcodeResult.orNull()!!
                return Either.left(exc)
            }
        }
    }


    // scary instruction implementations
    // this is all below the main class because there's a LOT going on here

    // i don't see how this can ever error...
    fun returnValue(arg: Byte): PyObject {
        return stack.pop()
    }

    /**
     * LOAD_*
     */
    fun load(pool: LoadPool, opval: Byte): Option<PyException> {
        // pool is the type we want to load
        val idx = opval.toInt()
        val loadResult = when (pool) {
            LoadPool.CONST -> Either.right(this.function.code.consts[idx])
            LoadPool.FAST -> {
                val name = this.function.code.varnames[idx]
                Either.right(this.locals[name]!!)
            }
            LoadPool.NAME -> {
                // sometimes a global...
                val name = this.function.code.names[idx]
                val realName = this.locals[name]
                val result = if (realName == null) {
                    val name = this.function.code.names[idx]
                    this.function.getGlobal(name)
                } else {
                    Either.right(realName)
                }
                result
            }
            LoadPool.GLOBAL -> {
                val name = this.function.code.names[idx]
                this.function.getGlobal(name)
            }
            LoadPool.ATTR -> {
                val toGet = this.stack.pop()
                val name = this.function.code.names[idx]
                toGet.pyGetAttribute(name)
            }
            else -> error("Unknown pool for LOAD_X instruction: $pool")  // interpreter error, not python error
        }

        val option: Option<PyException> = loadResult.fold(
            { Some(it) },
            {
                this.stack.push(it)
                this.bytecodePointer += 1
                none()
            }
        )
        return option
    }

    /**
     * STORE_(NAME|FAST).
     */
    fun store(pool: LoadPool, arg: Byte): Option<PyException> {
        val idx = arg.toInt()
        val toGetName = when (pool) {
            LoadPool.NAME -> this.function.code.names
            LoadPool.FAST -> this.function.code.varnames
            else -> error("Can't store items in pool $pool")
        }
        val name = toGetName[idx]
        this.locals[name] = this.stack.pop()
        this.bytecodePointer += 1
        return none()
    }

    /**
     * CALL_FUNCTION.
     */
    fun callFunction(opval: Byte): Option<PyException> {
        // CALL_FUNCTION(argc)
        // pops (argc) arguments off the stack (right to left) then invokes a function.
        val args = opval.toInt()
        val toCallWith = mutableListOf<PyObject>()
        for (x in 0 until args) {
            toCallWith.add(this.stack.pop())
        }

        val fn = this.stack.pop()
        if (fn !is PyCallable) {
            return Some(Exceptions.TYPE_ERROR.makeWithMessage("'${fn.type.name}' is not callable"))
        }

        val childFrame = fn.getFrame()
        val sig = fn.signature

        val argsToPass = sig.getFinalArgs(toCallWith)
        val result = argsToPass.flatMap { KythonInterpreter.runStackFrame(childFrame, it) }

        // errors should be passed down, and results should be put onto the stack
        return result.fold(
            { Some(it) },
            { this.stack.push(it); this.bytecodePointer += 1; none() }
        )
    }

    /**
     * MAKE_FUNCTION.
     */
    fun makeFunction(arg: Byte): Option<PyException> {
        val qualifiedName = this.stack.pop()
        require(qualifiedName is PyString) { "Function qualified name was not string!" }

        val code = this.stack.pop()
        require(code is PyCodeObject) { "Function code was not a code object!" }
        val function = PyUserFunction(code.wrappedCodeObject)
        function.module = this.function.module
        this.stack.push(function)
        this.bytecodePointer += 1
        return none()
    }

    /**
     * POP_TOP.
     */
    fun popTop(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "POP_TOP never has an argument" }
        this.stack.pop()
        this.bytecodePointer += 1
        return none()
    }

    /**
     * ROT_TWO
     */
    fun rotTwo(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "ROT_TWO never has an argument" }
        val top = stack.pop()
        val second = stack.pop()
        stack.push(top)
        stack.push(second)
        return none()
    }

    /**
     * ROT_THREE
     */
    fun rotThree(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "ROT_THREE never has an argument" }
        val top = stack.pop()
        val second = stack.pop()
        val third = stack.pop()
        stack.push(top)
        stack.push(third)
        stack.push(second)
        return none()
    }

    /**
     * ROT_FOUR
     */
    fun rotFour(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "ROT_FOUR never has an argument" }
        val top = stack.pop()
        val second = stack.pop()
        val third = stack.pop()
        val fourth = stack.pop()
        stack.push(top)
        stack.push(fourth)
        stack.push(third)
        stack.push(second)
        return none()
    }

    /**
     * DUP_TOP
     */
    fun dupTop(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "DUP_TOP never has an argument" }
        val top = stack.first
        stack.push(top)
        return none()
    }

    /**
     * DUP_TOP_TWO
     */
    fun dupTopTwo(arg: Byte): Option<PyException> {
        assert(arg.toInt() == 0) { "DUP_TOP_TWO never has an argument" }
        val top = stack.pop()
        val second = stack.pop()
        repeat(2){
            stack.push(second)
            stack.push(top)
        }
        return none()
    }

    /**
     * BINARY_* (ADD, etc)
     */
    fun binaryOp(type: BinaryOp, arg: Byte): Option<PyException> {
        val result: Option<PyException> = when (type) {
            BinaryOp.ADD -> {
                // todo: __add__
                stack.push(PyInt((stack.pop() as PyInt).wrappedInt + (stack.pop() as PyInt).wrappedInt))
                none()
            }
            else -> TODO("Unsupported binary op $type")
        }
        this.bytecodePointer += 1
        return result
    }

    /**
     * BUILD_* (TUPLE, LIST, SET, etc). Does not work for CONST_KEY_MAP!
     */
    fun buildSimple(type: BuildType, arg: Byte): Option<PyException> {
        val count = arg.toInt()
        val built = when (type) {
            BuildType.TUPLE -> {
                PyTuple((0 until count).map { this.stack.pop() }.reversed())
            }
            BuildType.STRING -> {
                val concatString = (0 until count)
                    .map { (this.stack.pop() as PyString).wrappedString }
                    .reversed()
                    .reduce { acc, s -> acc + s }
                PyString(concatString)
            }
            BuildType.SET -> {
                PySet(
                    LinkedHashSet((0 until count)
                        .map { this.stack.pop() }
                        .reversed())
                )
            }
            else -> TODO("Unimplemented build type $type")
        }
        this.stack.push(built)
        this.bytecodePointer += 1
        return none()
    }
}
