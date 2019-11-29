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

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KyError
import green.sailor.kython.interpreter.functions.PyUserFunction
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.instruction.InstructionOpcode
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.throwKy
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
            METHOD,
            GLOBAL
        }

        enum class BinaryOp {
            ADD,
            POWER,
            MULTIPLY,
            MATRIX_MULTIPLY,
            FLOOR_DIVIDE,
            TRUE_DIVIDE,
            MODULO,
            SUBTRACT,
            SUBSCR,
            LSHIFT,
            RSHIFT,
            AND,
            XOR,
            OR,
            STORE_SUBSCR,
            DELETE_SUBSCR
        }

        enum class UnaryOp {
            POSITIVE,
            NEGATIVE,
            INVERT,
            NOT
        }

        enum class BuildType {
            TUPLE,
            DICT,
            LIST,
            SET,
            STRING,
        }

        object FunctionFlags {
            const val POSITIONAL_DEFAULT = 1
            const val KEYWORD_DEFAULT = 2
            const val ANNOTATIONS = 4
            const val FREEVARS = 8
        }

        object CompareOp {
            const val LESS = 0
            const val LESS_EQUAL = 1
            const val EQUAL = 2
            const val NOT_EQUAL = 3
            const val GREATER = 4
            const val GREATER_EQUAL = 5
            const val CONTAINS = 6
            const val NOT_CONTAINS = 7
            const val IS = 8
            const val IS_NOT = 9
            const val EXCEPTION_MATCH = 10
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
    val stack = ArrayDeque<PyObject>(function.code.stackSize)

    /** The local variables for this frame. */
    val locals = mutableMapOf<String, PyObject>()

    override fun createStackFrameInfo(): StackFrameInfo.UserFrameInfo {
        return StackFrameInfo.UserFrameInfo(this)
    }

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
        if (fn !is PyCallable) {
            Exceptions.TYPE_ERROR("'${obj.type.name}'.$magicName is not callable.").throwKy()
        }
        val result = if (param != null) fn.runCallable(listOf(param)) else fn.runCallable(listOf())
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
     * Runs this stack frame, executing the function within.
     */
    override fun runFrame(kwargs: Map<String, PyObject>): PyObject {
        locals.putAll(kwargs)

        while (true) {
            // simple fetch decode execute loop
            // maybe this could be pipelined.
            val nextInstruction = function.getInstruction(bytecodePointer)
            val opcode = nextInstruction.opcode
            val param = nextInstruction.argument
            // special case this, because it returns from runFrame
            if (nextInstruction.opcode == InstructionOpcode.RETURN_VALUE) {
                return returnValue(param)
            }

            // switch on opcode
            // Reference: https://docs.python.org/3/library/dis.html#python-bytecode-instructions
            try {
                when (nextInstruction.opcode) {
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

                    // build ops
                    InstructionOpcode.BUILD_TUPLE -> buildSimple(BuildType.TUPLE, param)
                    InstructionOpcode.BUILD_STRING -> buildSimple(BuildType.STRING, param)
                    InstructionOpcode.BUILD_SET -> buildSimple(BuildType.SET, param)

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

                    // import ops
                    InstructionOpcode.IMPORT_NAME -> importName(param)
                    InstructionOpcode.IMPORT_FROM -> importFrom(param)
                    InstructionOpcode.IMPORT_STAR -> importStar(param)

                    // stack ops
                    InstructionOpcode.POP_TOP -> popTop(param)
                    InstructionOpcode.ROT_TWO -> rotTwo(param)
                    InstructionOpcode.ROT_THREE -> rotThree(param)
                    InstructionOpcode.ROT_FOUR -> rotFour(param)
                    InstructionOpcode.DUP_TOP -> dupTop(param)
                    InstructionOpcode.DUP_TOP_TWO -> dupTopTwo(param)

                    // Unary operations
                    InstructionOpcode.UNARY_POSITIVE -> unaryOp(UnaryOp.POSITIVE, param)
                    InstructionOpcode.UNARY_NEGATIVE -> unaryOp(UnaryOp.NEGATIVE, param)
                    InstructionOpcode.UNARY_NOT -> unaryOp(UnaryOp.NOT, param)
                    InstructionOpcode.UNARY_INVERT -> unaryOp(UnaryOp.INVERT, param)

                    InstructionOpcode.GET_ITER -> getIter(param)
                    InstructionOpcode.GET_YIELD_FROM_ITER -> getYieldIter(param)

                    InstructionOpcode.MAKE_FUNCTION -> makeFunction(param)

                    InstructionOpcode.COMPARE_OP -> compareOp(param)
                    else -> error("Unimplemented opcode $opcode")
                }
            } catch (e: Throwable) {
                throw e
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
    fun load(pool: LoadPool, opval: Byte) {
        // pool is the type we want to load
        val idx = opval.toInt()
        val loadResult = when (pool) {
            LoadPool.CONST -> function.code.consts[idx]
            LoadPool.FAST -> {
                val name = function.code.varnames[idx]
                locals[name]!!
            }
            LoadPool.NAME -> {
                // sometimes a global...
                val name = function.code.names[idx]
                locals[name] ?: function.getGlobal(name)
            }
            LoadPool.GLOBAL -> {
                val name = function.code.names[idx]
                function.getGlobal(name)
            }
            LoadPool.ATTR -> {
                val toGetFrom = stack.pop()
                val name = function.code.names[idx]
                toGetFrom.pyGetAttribute(name)
            }
            LoadPool.METHOD -> {
                // load_method sucks shit, for the record.
                // we just treat this as an attribute load, for functions only
                // because we already generated all the method wrappers anyway.
                val toGetFrom = stack.pop()
                val name = function.code.names[idx]
                toGetFrom.pyGetAttribute(name)
            }
        }

        stack.push(loadResult)
        bytecodePointer += 1
    }

    /**
     * STORE_(NAME|FAST).
     */
    fun store(pool: LoadPool, arg: Byte) {
        val idx = arg.toInt()
        val toGetName = when (pool) {
            LoadPool.NAME -> function.code.names
            LoadPool.FAST -> function.code.varnames
            else -> error("Can't store items in pool $pool")
        }
        val name = toGetName[idx]
        locals[name] = stack.pop()
        bytecodePointer += 1
    }

    /**
     * CALL_FUNCTION.
     */
    fun callFunction(opval: Byte) {
        // CALL_FUNCTION(argc)
        // pops (argc) arguments off the stack (right to left) then invokes a function.
        val toCallWith = mutableListOf<PyObject>()
        for (x in 0 until opval.toInt()) {
            toCallWith.add(stack.pop())
        }

        val fn = stack.pop()
        if (fn !is PyCallable) {
            // todo
            Exceptions.TYPE_ERROR("'${fn.type.name}' is not callable").throwKy()
        }

        val result = fn.runCallable(toCallWith)
        stack.push(result)
        bytecodePointer += 1
    }

    /**
     * MAKE_FUNCTION.
     */
    fun makeFunction(arg: Byte) {
        // toInt to use bitwise `and`
        val flags = arg.toInt()
        val qualifiedName = stack.pop()
        require(qualifiedName is PyString) { "Function qualified name was not string!" }

        val code = stack.pop()
        require(code is PyCodeObject) { "Function code was not a code object!" }
        if (flags and FunctionFlags.FREEVARS != 0) {
            val freevarCellsTuple = stack.pop()
        }
        if (flags and FunctionFlags.ANNOTATIONS != 0) {
            val annotationDict = stack.pop()
        }
        if (flags and FunctionFlags.KEYWORD_DEFAULT != 0) {
            val kwOnlyParamDefaultDict = stack.pop()
        }
        if (flags and FunctionFlags.POSITIONAL_DEFAULT != 0) {
            val positionalParamDefaultTuple = stack.pop()
        }
        val function = PyUserFunction(code.wrappedCodeObject)
        function.module = function.module
        stack.push(function)
        bytecodePointer += 1
    }

    // Imports
    /**
     * IMPORT_NAME
     */
    fun importName(arg: Byte) {
        TODO("Implement IMPORT_NAME")
    }

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

    /**
     * IMPORT_STAR
     */
    fun importStar(arg: Byte) {
        val module = stack.pop()

        // TODO: Use the real __dir__
        module.kyDefaultDir().internalDict.forEach {
            if (!it.key.startsWith("_")) {
                locals[it.key] = it.value
            }
        }
        bytecodePointer += 1
    }

    /**
     * POP_TOP.
     */
    fun popTop(arg: Byte) {
        assert(arg.toInt() == 0) { "POP_TOP never has an argument" }

        stack.pop()
        bytecodePointer += 1
    }

    /**
     * ROT_TWO
     */
    fun rotTwo(arg: Byte) {
        assert(arg.toInt() == 0) { "ROT_TWO never has an argument" }

        val top = stack.pop()
        val second = stack.pop()
        stack.push(top)
        stack.push(second)
        bytecodePointer += 1
    }

    /**
     * ROT_THREE
     */
    fun rotThree(arg: Byte) {
        assert(arg.toInt() == 0) { "ROT_THREE never has an argument" }

        val top = stack.pop()
        val second = stack.pop()
        val third = stack.pop()
        stack.push(top)
        stack.push(third)
        stack.push(second)
        bytecodePointer += 1
    }

    /**
     * ROT_FOUR
     */
    fun rotFour(arg: Byte) {
        assert(arg.toInt() == 0) { "ROT_FOUR never has an argument" }

        val top = stack.pop()
        val second = stack.pop()
        val third = stack.pop()
        val fourth = stack.pop()
        stack.push(top)
        stack.push(fourth)
        stack.push(third)
        stack.push(second)

        bytecodePointer += 1
    }

    /**
     * DUP_TOP
     */
    fun dupTop(arg: Byte) {
        assert(arg.toInt() == 0) { "DUP_TOP never has an argument" }
        val top = stack.first
        stack.push(top)

        bytecodePointer += 1
    }

    /**
     * DUP_TOP_TWO
     */
    fun dupTopTwo(arg: Byte) {
        assert(arg.toInt() == 0) { "DUP_TOP_TWO never has an argument" }
        val top = stack.pop()
        val second = stack.pop()
        repeat(2) {
            stack.push(second)
            stack.push(top)
        }

        bytecodePointer += 1
    }

    /**
     * BINARY_* (ADD, etc)
     */
    fun binaryOp(type: BinaryOp, arg: Byte) {
        val o1 = stack.pop()
        val o2 = stack.pop()
        val magic = when (type) {
            BinaryOp.ADD -> "__add__"
            BinaryOp.LSHIFT -> "__lshift__"
            BinaryOp.POWER -> "__pow__"
            BinaryOp.MULTIPLY -> "__mul__"
            BinaryOp.MATRIX_MULTIPLY -> "__matmul__"
            BinaryOp.FLOOR_DIVIDE -> "__floordiv__"
            BinaryOp.TRUE_DIVIDE -> "__truediv__"
            BinaryOp.MODULO -> "__mod__"
            BinaryOp.SUBTRACT -> "__sub__"
            BinaryOp.SUBSCR -> "__getitem__"
            BinaryOp.RSHIFT -> "__rshift__"
            BinaryOp.AND -> "__and__"
            BinaryOp.XOR -> "__xor__"
            BinaryOp.OR -> "__or__"
            else -> error("This should never happen!")
        }
        // Use __r to transform __add__ to __radd__, which is the inverse.
        // If this does not exist (e.g. on getitem) it will be ignored.
        magicMethod(o1, magic, o2, "__r" + magic.substring(2))
        bytecodePointer += 1
    }

    /**
     * INPLACE_*
     */
    fun inplaceOp(type: BinaryOp, arg: Byte) {
        val o1 = stack.pop()
        val o2 = stack.pop()
        val magic = when (type) {
            BinaryOp.ADD -> "__iadd__"
            BinaryOp.LSHIFT -> "__ilshift__"
            BinaryOp.POWER -> "__ipow__"
            BinaryOp.MULTIPLY -> "__imul__"
            BinaryOp.MATRIX_MULTIPLY -> "__imatmul__"
            BinaryOp.FLOOR_DIVIDE -> "__ifloordiv__"
            BinaryOp.TRUE_DIVIDE -> "__itruediv__"
            BinaryOp.MODULO -> "__imod__"
            BinaryOp.SUBTRACT -> "__isub__"
            BinaryOp.SUBSCR -> "__igetitem__"
            BinaryOp.RSHIFT -> "__irshift__"
            BinaryOp.AND -> "__iand__"
            BinaryOp.XOR -> "__ixor__"
            BinaryOp.OR -> "__ior__"
            BinaryOp.STORE_SUBSCR -> "__setitem__"
            BinaryOp.DELETE_SUBSCR -> "__delitem__"
        }
        magicMethod(o1, magic, o2)
        bytecodePointer += 1
    }

    /**
     * COMPARE_OP
     */
    fun compareOp(arg: Byte) {
        val top = stack.pop()
        val second = stack.pop()
        with(CompareOp) {
            when (arg.toInt()) {
                LESS -> magicMethod(top, "__lt__", second, "__ge__")
                LESS_EQUAL -> magicMethod(top, "__le__", second, "__gt__")
                GREATER -> magicMethod(top, "__gt__", second, "__le__")
                GREATER_EQUAL -> magicMethod(top, "__ge__", second, "__lt__")
                EQUAL -> magicMethod(top, "__eq__", second, "__eq__")
                NOT_EQUAL -> magicMethod(top, "__ne__", second, "__ne__")
                CONTAINS -> magicMethod(top, "__contains__", second)
                NOT_CONTAINS -> {
                    magicMethod(top, "__contains__", second)
                    stack.push(if (stack.pop() == PyBool.TRUE) PyBool.FALSE else PyBool.TRUE)
                }
                IS -> stack.push(if (top === second) PyBool.TRUE else PyBool.FALSE)
                IS_NOT -> stack.push(if (top !== second) PyBool.TRUE else PyBool.FALSE)
                EXCEPTION_MATCH -> TODO("exception match COMPARE_OP")
                else -> Exceptions.RUNTIME_ERROR("Invalid parameter for COMPARE_OP: $arg").throwKy()
            }
        }
    }

    // Unary operators
    /**
     * GET_YIELD_ITER
     */
    fun getYieldIter(param: Byte) {
        TODO("Implement GET_YIELD_ITER")
    }

    /**
     * GET_ITER
     */
    fun getIter(param: Byte) {
        val top = stack.pop()
        magicMethod(top, "__iter__")
        bytecodePointer += 1
    }

    /**
     * UNARY_*
     */
    fun unaryOp(type: UnaryOp, param: Byte) {
        val top = stack.pop()
        when (type) {
            UnaryOp.INVERT -> {
                magicMethod(top, "__invert__")
            }
            UnaryOp.NOT -> {
                magicMethod(top, "__bool__")
                val truthy = stack.pop() // magicMethod pushes to stack
                stack.push(if (truthy == PyBool.TRUE) PyBool.FALSE else PyBool.TRUE)
            }
            UnaryOp.NEGATIVE -> {
                magicMethod(top, "__neg__")
            }
            UnaryOp.POSITIVE -> {
                magicMethod(top, "__pos__")
            }
        }
        bytecodePointer += 1
    }

    /**
     * BUILD_* (TUPLE, LIST, SET, etc). Does not work for CONST_KEY_MAP!
     */
    fun buildSimple(type: BuildType, arg: Byte) {
        val count = arg.toInt()
        val built = when (type) {
            BuildType.TUPLE -> {
                PyTuple((0 until count).map { stack.pop() }.reversed())
            }
            BuildType.STRING -> {
                val concatString = (0 until count)
                    .map { (stack.pop() as PyString).wrappedString }
                    .reversed()
                    .reduce { acc, s -> acc + s }
                PyString(concatString)
            }
            BuildType.SET -> {
                PySet(
                    LinkedHashSet((0 until count)
                        .map { stack.pop() }
                        .reversed())
                )
            }
            else -> TODO("Unimplemented build type $type")
        }
        stack.push(built)
        bytecodePointer += 1
    }
}
