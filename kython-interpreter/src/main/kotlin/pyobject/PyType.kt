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

package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallable
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.pyobject.user.PyUserObject
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.typeError

/**
 * Represents a python type (i.e. a class).
 */
abstract class PyType(val name: String) : PyObject(), PyCallable {
    /**
     * Creates a new instance of the object represented by this type.
     *
     * This is the behind the scenes work for `object.__new__(class, *args, **kwargs)`. This is (generally) not called
     * from Kotlin land.
     */
    val builtinFunctionWrapper by lazy {
        PyBuiltinFunction.wrap(name, PyCallableSignature.ALL_CONSUMING, this::newInstance)
    }

    /** A list of parent classes for this PyType. */
    open val bases = mutableListOf<PyType>()

    /** The method resolution order of this PyType. */
    open val mro: List<PyType> by lazy {
        // mro always begins with our own type, so we add it first
        val parents = mutableListOf(this)
        // then search recursively for all of our base types parents
        parents.addAll(getMroParents())

        parents
    }

    /**
     * Called to implement user-type subclasses.
     */
    open fun kySuperclassInit(instance: PyUserObject, args: List<PyObject>): PyNone =
        typeError("Type $name does not support subclassing!")

    /**
     * Gets the method resolution order parents of this object.
     */
    open fun getMroParents(): List<PyType> {
        val mro = mutableListOf<PyType>()
        for (base in bases) {
            mro.add(base)
            mro.addAll(base.getMroParents())
        }
        return mro
    }

    /**
     * Makes a new instance of this builtin.
     *
     * @param kwargs: The arguments that were called for this object.
     */
    abstract fun newInstance(kwargs: Map<String, PyObject>): PyObject

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "args" to ArgType.POSITIONAL_STAR,
            "kwargs" to ArgType.KEYWORD_STAR
        )
    }

    override fun createFrame(): StackFrame = builtinFunctionWrapper.createFrame()

    private val _pyString by lazy {
        PyString("<class '$name'>")
    }

    // default impls
    override fun pyToStr(): PyString = _pyString
    override fun pyGetRepr(): PyString = _pyString

    override fun pyEquals(other: PyObject): PyObject {
        if (other !is PyType) {
            return PyBool.FALSE
        }
        return PyBool.get(this === other)
    }

    override fun pyGreater(other: PyObject): PyObject = PyNotImplemented
    override fun pyLesser(other: PyObject): PyObject = PyNotImplemented

    override var type: PyType
        get() = PyRootType
        set(_) = Exceptions.invalidClassSet(this)
}
