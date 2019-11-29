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
package green.sailor.kython.interpreter.kyobject

import green.sailor.kython.interpreter.functions.magic.ObjectDir
import green.sailor.kython.interpreter.functions.magic.ObjectGetattribute
import green.sailor.kython.interpreter.functions.magic.ObjectRepr
import green.sailor.kython.interpreter.functions.magic.ObjectStr
import green.sailor.kython.interpreter.iface.PyCallable
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject

/**
 * A wrapper that holds references to magic method callables, such as `__getattribute__` et cetera.
 *
 * @param bound: If this is on an instance, instead of a type.
 */
@Suppress("MemberVisibilityCanBePrivate")
class KyMagicMethods(val bound: Boolean) {
    // list of magic methods and their defaults
    // TODO: This is very sparse right now.

    // == builtins with defaults == //

    // __getattribute__
    var tpGetAttribute: PyCallable = ObjectGetattribute
    // __dir__
    var tpDir: PyCallable = ObjectDir

    // __str__
    var tpStr: PyCallable? = ObjectStr

    // __repr__
    val tpRepr: PyCallable? = ObjectRepr

    // == builtins without defaults == //
    // these can be null.

    /**
     * Turns a magic method name into a magic method.
     */
    fun nameToMagicMethod(name: String): PyCallable? {
        return when (name) {
            "__getattribute__" -> tpGetAttribute
            "__dir__" -> tpDir
            "__str__" -> tpStr
            "__repr__" -> tpRepr
            else -> null
        }
    }

    /**
     * Binds a magic method into a function, if binding is enabled.
     */
    fun nameToMagicMethodBound(parent: PyObject, name: String): PyCallable? {
        val meth = nameToMagicMethod(name) ?: return null
        val descriptorParent = if (bound) parent else PyNone
        return (meth as PyObject).pyDescriptorGet(descriptorParent, parent.type) as PyCallable
    }

    /**
     * Creates the list of "active" magic methods, i.e. the magic methods that exist on this object.
     *
     * This is *only* useful for dir() and the likes. Do *NOT* use this for other purposes;
     * you should be checking the method you actually want yourself.
     */
    @Suppress("UnnecessaryVariable")
    fun createActiveMagicMethodList(): List<String> {
        // these will always exist!
        val initial = listOf(
            "__getattribute__",
            "__dir__"
        )

        // todo: other magic methods that could be set
        return initial
    }
}
