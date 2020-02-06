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

package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyTuple
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction
import green.sailor.kython.interpreter.pyobject.function.PyUserFunction
import green.sailor.kython.interpreter.pyobject.internal.PyCellObject
import green.sailor.kython.interpreter.pyobject.types.PyRootType
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.util.PyObjectMap
import green.sailor.kython.interpreter.util.cast

/**
 * Represents the `__build_class__` builtin, used to create new classes.
 */
object BuildClassFunction : PyBuiltinFunction("__build_class__") {

    private class GrossHackClassCellVar(
        override var localsMap: MutableMap<String, PyObject>,
        override var name: String
    ) : PyCellObject(linkedMapOf(), "")

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val clsFn = kwargs["class_body"].cast<PyUserFunction>()
        val name = kwargs["name"].cast<PyString>()
        val bases = kwargs["bases"].cast<PyTuple>()

        // Note to self:
        // Sometimes, classes have an implicit `__class__` in their cellvars.
        // (ECH)
        // So if cellvar length is > 0, we make a cell for the class object, which we update
        // afterwards.

        val cell = if ("__classcell__" in clsFn.code.names) {
            GrossHackClassCellVar(linkedMapOf(), "__class__")
        } else {
            null
        }

        // TODO: __prepare__
        // build the class body dict
        val items = PyObjectMap()
        val bodyDict = PyDict.from(items)
        val frame = clsFn.createFrame()
        if (frame !is UserCodeStackFrame) {
            Exceptions.SYSTEM_ERROR("Cannot pass builtin function").throwKy()
        }

        val newKwargs = if (clsFn.code.argCount > 0) {
            clsFn.kyGetSignature().argsToKwargs(listOf(bodyDict))
        } else {
            mapOf()
        }

        cell?.let { frame.cellvars["__class__"] = it }
        KythonInterpreter.runStackFrame(frame, newKwargs)
        items.putAll(frame.locals.mapKeys { PyString(it.key) })

        // figure out the metaclass
        val kws = kwargs["keywords"]!!.cast<PyDict>().internalDict
        val metaclass = kws.getOrDefault("metaclass", PyRootType)
        // type(name, bases, class_dict)
        val newType = metaclass.kyCall(listOf(bodyDict, bases, name))
        cell?.localsMap = newType.internalDict
        return newType
    }

    override val signature: PyCallableSignature =
        PyCallableSignature(
            "class_body" to ArgType.POSITIONAL,
            "name" to ArgType.POSITIONAL,
            "bases" to ArgType.POSITIONAL_STAR,
            "keywords" to ArgType.KEYWORD_STAR
        )
}
