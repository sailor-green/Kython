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
package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.Builtins
import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.instruction.Instruction
import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.interpreter.kyobject.KyModule
import green.sailor.kython.interpreter.pyobject.PyCodeObject
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.throwKy

/**
 * Represents a Python function object.
 *
 * @param codeObject: The marshalled code object to transform into a real code object.
 */
class PyUserFunction(codeObject: KyCodeObject) : PyFunction(PyUserFunctionType) {
    object PyUserFunctionType : PyType("function") {
        override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
            return kwargs["code"]?.let {
                if (it !is PyCodeObject) {
                    Exceptions.TYPE_ERROR("Arg 'code' is not a code object").throwKy()
                }
                PyUserFunction(it.wrappedCodeObject)
            } ?: error("Built-in signature mismatch")
        }

        override val signature: PyCallableSignature by lazy {
            PyCallableSignature(
                "code" to ArgType.POSITIONAL,
                "globals" to ArgType.POSITIONAL
            )
        }
    }

    /** The code object for this function. */
    val code = codeObject

    /** The KyModule for this function. */
    lateinit var module: KyModule

    // helper methods
    /**
     * Gets the instruction at the specified index.
     */
    fun getInstruction(idx: Int): Instruction = code.instructions[idx]

    /**
     * Gets a global from the globals for this function.
     */
    fun getGlobal(name: String): PyObject {
        return module.attribs[name] ?: Builtins.BUILTINS_MAP[name]
        ?: Exceptions.NAME_ERROR("Name $name is not defined").throwKy()
    }

    override fun getFrame(): StackFrame = UserCodeStackFrame(this)

    override fun getPyStr(): PyString = PyString("<user function ${code.codename}>")

    override fun getPyRepr(): PyString = getPyStr()

    /**
     * Generates a [PyCallableSignature] for this function.
     */
    fun generateSignature(): PyCallableSignature {
        // ref: inspect._signature_from_function
        // varnames starting format: args, args with defaults, *args, kwonly, **kwargs

        // add args
        val args = mutableListOf<Pair<String, ArgType>>()
        for (x in 0 until code.argCount) {
            val name = code.varnames[x]
            args.add(Pair(name, ArgType.POSITIONAL))
        }

        // todo: with defaults
        // add a *args if we have one
        if (code.flags and KyCodeObject.CO_HAS_VARARGS != 0) {
            val name = code.varnames[code.argCount]
            args.add(Pair(name, ArgType.POSITIONAL_STAR))
        }

        // keyword only
        for (x in 0 until code.kwOnlyArgCount) {
            val offset = code.argCount + x
            val name = code.varnames[offset]
            args.add(Pair(name, ArgType.KEYWORD))
        }

        // **kwargs
        if (code.flags and KyCodeObject.CO_HAS_VARKWARGS != 0) {
            val name = code.varnames[code.argCount + code.kwOnlyArgCount]
            args.add(Pair(name, ArgType.KEYWORD_STAR))
        }

        return PyCallableSignature(*args.toTypedArray())
    }

    override val signature: PyCallableSignature by lazy { generateSignature() }
}
