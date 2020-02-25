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

package green.sailor.kython.interpreter.callable

import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.typeError
import java.util.*

/**
 * Represents a callable signature. This is created for every function instance (including built-in functions) and is
 * used to validate args and keyword arguments.
 */
class PyCallableSignature(vararg val args: Pair<String, ArgType>) {
    companion object {
        /** The empty signature. */
        val EMPTY =
            PyCallableSignature()

        /** The empty method signature, for builtins. */
        val EMPTY_METHOD =
            PyCallableSignature(
                "self" to ArgType.POSITIONAL
            )

        /** The all-consuming signature. */
        val ALL_CONSUMING =
            PyCallableSignature(
                "args" to ArgType.POSITIONAL_STAR,
                "kwargs" to ArgType.KEYWORD_STAR
            )
    }

    override fun toString(): String {
        return buildString {
            append("PyCallableSignature size=${args.size} ")
            for ((name, arg) in args) {
                append("$name=$arg ")
            }
        }
    }

    /** The default values for keyword arguments. */
    val defaults = mutableMapOf<String, PyObject>()

    /**
     * Loads defaults from a map of default values.
     */
    fun loadDefaults(from: Map<String, PyObject>) {
        defaults.putAll(from)
    }

    /**
     * Applies defaults for this signature.
     */
    fun withDefaults(vararg defaults: Pair<String, PyObject>): PyCallableSignature {
        for ((name, arg) in defaults) {
            this.defaults[name] = arg
        }
        return this
    }

    /** The computed reverse arg mapping. */
    val reverseMapping = mutableMapOf<ArgType, List<String>>().apply {
        for (arg in args) {
            (computeIfAbsent(arg.second) { mutableListOf() } as MutableList).add(arg.first)
        }

        // validate!!
        this[ArgType.POSITIONAL_STAR]?.let {
            if (it.size > 1) typeError("A function cannot have multiple POSITIONAL_STAR")
        }
        this[ArgType.KEYWORD_STAR]?.let {
            if (it.size > 1) typeError("A function cannot have multiple KEYWORD_STAR")
        }
    }

    /**
     * Helper function for implementing CALL_FUNCTION/CALL_FUNCTION_KW.
     *
     * The arguments passed into this function should be the list of arguments popped off below the
     * kwargs tuple, and the kwargs tuple itself. If this is just CALL_FUNCTION, then the kwargTuple
     * should be empty. kwargcount should be the co_kwonlyargcount.
     */
    fun callFunctionGetArgs(
        passedArgs: List<PyObject>,
        kwargTuple: List<String>
    ): Map<String, PyObject> {
        // if our args is empty, we obviously take zero arguments
        // so we can short-circuit all of this and just make sure the passed arg list is empty
        // (and if it isn't, uh oh!)
        if (args.isEmpty()) {
            if (passedArgs.isNotEmpty()) {
                typeError("This function takes no arguments")
            } else {
                return mutableMapOf()
            }
        }

        // make a new deque of the args to pop off of
        val mutArgs = ArrayDeque<PyObject>(passedArgs)
        val finalArgs = mutableMapOf<String, PyObject>()

        var argCount = 0

        // step 1) pair off kwargs to final args
        val pairedKwargs = mutableMapOf<String, PyObject>()
        for (kwarg in kwargTuple.asReversed()) {
            // this should never fail - if it does, it's an interpreter error
            // because call_function's handler didn't pop the right amount, or the bytecode was
            // wrong, or whatever else.
            pairedKwargs[kwarg] = mutArgs.removeFirst()
            argCount += 1
        }

        // step 2) consume all regular args
        for (arg in reverseMapping.getOrDefault(ArgType.POSITIONAL, listOf())) {
            val poppedArg = mutArgs.pollLast()
                ?: pairedKwargs.getOrDefault(arg, defaults[arg])
                ?: typeError("Missing required positional argument $arg")
            finalArgs[arg] = poppedArg
            argCount += 1
        }
        // step 3) consume all positional args left, since all kwargs have been consumed and
        // all regular positional args
        // this is weird with nulls cos we don't wanna
        val argName = reverseMapping[ArgType.POSITIONAL_STAR]?.first()
        val posArgCollector = mutableListOf<PyObject>()
        while (true) {
            val next = mutArgs.pollLast() ?: break
            argCount += 1
            if (argName == null) {
                val argAmtCount = reverseMapping[ArgType.POSITIONAL]?.size ?: 0
                typeError(
                    "Too many positional arguments passed! " +
                    "Expected $argAmtCount, got $argCount"
                )
            }
            posArgCollector.add(next)
        }
        argName?.let { finalArgs[it] = PyTuple.get(posArgCollector) }

        // step 4) validate the collected kwargs, and add them to **kwargs if needed
        val kwCollectedArgName = reverseMapping[ArgType.KEYWORD_STAR]?.first()
        val extraKwargs = mutableMapOf<String, PyObject>()
        val kwargNames = reverseMapping.getOrDefault(ArgType.KEYWORD, listOf()).toMutableSet()
        for ((name, value) in pairedKwargs) {
            if (name in finalArgs) {
                // if it's in finalargs, it was paired off in step 2
                // and it's a positional or keyword
                // therefore we just skip this processing block
                // since it can't be added to finalargs again or **kwargs
                continue
            } else if (name in kwargNames) {
                // if it's already in the list of keyword arguments, just set it in finalargs
                finalArgs[name] = value
            } else {
                if (kwCollectedArgName == null) typeError("Unexpected keyword argument $name")
                extraKwargs[name] = value
            }
            // removing all the resolved kwargs will let us match up to defaults if needed
            kwargNames.remove(name)
        }
        kwCollectedArgName?.let { finalArgs[it] = PyDict.fromAnyMap(extraKwargs) }

        // step 5) match up defaults
        if (kwargNames.isNotEmpty()) {
            for (kwarg in kwargNames) {
                val default = defaults[kwarg]
                    ?: typeError("Missing required keyword argument: $kwarg")
                finalArgs[kwarg] = default
            }
        }

        return finalArgs
    }

    /**
     * Turns a list of arguments to keyword arguments.
     *
     * This function is intended to be used internally; use callFunctionGetArgs for implementing
     * the bytecode instructions.
     */
    fun argsToKwargs(passedArgs: List<PyObject>): Map<String, PyObject> {
        // args are passed right to left
        val finalArgs = mutableMapOf<String, PyObject>().apply { putAll(defaults) }
        val ourArgs = args.iterator()

        for (arg in passedArgs.asReversed()) {
            val argDef = ourArgs.next()
            if (argDef.second != ArgType.POSITIONAL) {
                typeError("Functions called from Kotlin can only take positional arguments!")
            }
            finalArgs[argDef.first] = arg
        }
        // TODO: idk, make this better
        if (finalArgs.size != passedArgs.size) typeError("Missing arguments!")

        return finalArgs
    }
}

object EMPTY : PyUndicted {
    override var type: PyType
        get() = error("You shouldn't be seeing this")
        set(value) = error("You shouldn't be seeing this")
}
