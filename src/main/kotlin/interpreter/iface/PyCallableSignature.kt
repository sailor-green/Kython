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
package green.sailor.kython.interpreter.iface

import green.sailor.kython.interpreter.pyobject.PyDict
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.typeError

/**
 * Represents a callable signature. This is created for every function instance (including built-in functions) and is
 * used to validate args and keyword arguments.
 */
class PyCallableSignature(vararg val args: Pair<String, ArgType>) {
    companion object {
        /** The empty signature. */
        val EMPTY = PyCallableSignature()

        /** The empty method signature, for builtins. */
        val EMPTY_METHOD = PyCallableSignature(
            "self" to ArgType.POSITIONAL
        )

        /** The all-consuming signature. */
        val ALL_CONSUMING = PyCallableSignature(
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
     * Applies defaults for this signature.
     */
    fun withDefaults(vararg defaults: Pair<String, PyObject>): PyCallableSignature {
        for ((name, arg) in defaults) {
            this.defaults[name] = arg
        }
        return this
    }

    /**
     * Gets the arguments for this signature.
     *
     * @param args: The arguments provided on the stack, from right to left.
     * @param kwargsTuple: The keyword arguments tuple, if there is any.
     *
     * @return A map of arg -> object to call the function with. This will be loaded into
     *         NAMES on the function object.
     */
    fun getFinalArgs(args: List<PyObject>, kwargsTuple: PyTuple? = null): Map<String, PyObject> {
        // complicated...
        // CALL_FUNCTION does BOS(BOS-1, BOS-2, BOS-3, etc)
        // CALL_FUNCTION_KW does the same, but TOS is a tuple containing keyword arguments
        // fuck CALL_FUNCTION_EX for now.
        val finalMap = defaults.toMutableMap()

        if (kwargsTuple == null) {
            // if no kwargs (i.e. call_function), we just nicely iterate over the signature args and
            // match them with the function call args
            val argsIt = args.asReversed().iterator()
            loop@for ((name, type) in this.args) {
                when (type) {
                    ArgType.POSITIONAL -> {
                        // raises a java error if the iterator is empty
                        try {
                            val arg = argsIt.next()
                            finalMap[name] = arg
                        } catch (e: NoSuchElementException) {
                            if (name !in finalMap) {
                                typeError("No value provided for arg $name")
                            }
                        }
                    }
                    ArgType.POSITIONAL_STAR -> {
                        finalMap[name] = PyTuple.get(argsIt.asSequence().toList())
                    }

                    // keyword args are NOT allowed for this function
                    ArgType.KEYWORD -> {
                        // this is just for the better error message
                        if (name in finalMap) {
                            continue@loop // default
                        }
                        try {
                            val arg = argsIt.next()
                        } catch (e: NoSuchElementException) {
                            if (name !in finalMap) {
                                typeError("No value provided for arg $name")
                            }
                        }
                        typeError(
                            "This function takes $name as a keyword, " +
                                "not a positional argument"
                        )
                    }

                    // keyword stars are just given an empty dict
                    ArgType.KEYWORD_STAR -> finalMap[name] = PyDict(linkedMapOf())
                }
            }

            // make sure too many args weren't passed
            if (argsIt.hasNext()) {
                val remaining = argsIt.asSequence().toList().size
                typeError(
                    "Passed too many arguments! Expected ${args.size}, " +
                    "got ${finalMap.size + remaining}"
                )
            }
        }

        return finalMap
    }
}
