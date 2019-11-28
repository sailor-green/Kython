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
package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.stack.StackFrame

/**
 * Represents an exception object. This should be subclassed for all built-in exceptions
 * (do not build the Python exception tree with Kotlin subclasses).
 *
 * Each subclass should pass in the appropriate type to the PyObject constructor.
 */
abstract class PyException(val args: PyTuple) : PyObject() {
    /**
     * Represents the type of an exception.
     */
    abstract class PyExceptionType(name: String) : PyType(name) {
        /**
         * Internal method for getting an exception instance without using newInstance.
         */
        abstract fun interpreterGetExceptionInstance(args: List<PyString>): PyException

        /**
         * Internal method for interpreter Python errors.
         */
        fun makeWithMessage(message: String): PyException {
            val args = listOf(PyString(message))
            return interpreterGetExceptionInstance(args)
        }

        operator fun invoke(message: String) = makeWithMessage(message)

        fun typeSubclassOf(name: String): PyExceptionType {
            return makeExceptionType(name, listOf(this))
        }

        override var type: PyType
            get() = PyRootType
            set(_) = error("Cannot set the type of this object")
    }

    companion object {
        /**
         * Helper for making a new exception type.
         *
         * @param name: The name of the exception, e.g. NameError.
         * @param bases: The bases for
         */
        fun makeExceptionType(name: String, bases: List<PyExceptionType>): PyExceptionType {
            return object : PyExceptionType(name) {
                override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
                    val args = kwargs["args"] as PyTuple
                    val strings = args.subobjects.map { it.getPyStr() }

                    return interpreterGetExceptionInstance(strings)
                }

                override fun interpreterGetExceptionInstance(args: List<PyString>): PyException {
                    // used to capture the outer this
                    val innerClassType = this
                    val instance = object : PyException(PyTuple(args)) {
                        init {
                            parentTypes.addAll(bases)
                        }

                        override var type: PyType
                            get() = innerClassType
                            set(_) {}
                    }
                    return instance
                }
            }
        }
    }

    /**
     * The list of exception frames this stack frame has travelled down.
     */
    val exceptionFrames: List<StackFrame>

    init {
        // this builds the traceback by walking down from the root frame to the last frame
        val frames = StackFrame.flatten(KythonInterpreter.getRootFrameForThisThread())
        exceptionFrames = frames
    }

    override fun getPyStr(): PyString {
        require(type is PyExceptionType) { "Type of exception was not PyExceptionType!" }
        TODO()
    }

    override fun getPyRepr(): PyString {
        TODO("not implemented")
    }
}
