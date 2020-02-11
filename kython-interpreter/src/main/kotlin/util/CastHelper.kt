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

package green.sailor.kython.interpreter.util

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyList
import green.sailor.kython.interpreter.pyobject.collection.PyListType
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.pyobject.dict.PyDictType
import green.sailor.kython.interpreter.pyobject.exception.PyException
import green.sailor.kython.interpreter.pyobject.types.PyStringType
import green.sailor.kython.interpreter.pyobject.user.PyUserObject
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.typeName
import kotlin.reflect.KClass
import org.apiguardian.api.API

typealias KyType = KClass<out PyObject>

/**
 * A cast helper for casting objects around.
 */
object CastHelper {
    val kyTypeMap = mutableMapOf<KyType, PyType>(
        PyString::class to PyStringType,
        PyList::class to PyListType,
        PyDict::class to PyDictType
    )

    /**
     * Casts a PyObject to the specified
     */
    @API(status = API.Status.INTERNAL)
    fun <T : PyObject> cast(klass: KClass<T>, obb: PyObject): T {
        val realType = kyTypeMap[klass]
            ?: Exceptions.SYSTEM_ERROR("No cast info for ${klass.simpleName} specified!").throwKy()

        if (obb !is PyUserObject) {
            typeError("Invalid built-in type: got ${obb.typeName}, expected ${realType.name}")
        }
        return obb.primitiveSubclassBacking[realType] as? T
            ?: typeError("Invalid type: got ${obb.typeName}, expected ${realType.name}")
    }
}

@API(status = API.Status.MAINTAINED)
fun <T : PyObject> PyObject.cast(klass: KClass<T>) = CastHelper.cast(klass, this)

/**
 * Casts a [PyObject] to an object of type [T] if possible, else raises a TypeError [PyException].
 */
@API(status = API.Status.MAINTAINED)
inline fun <reified T : PyObject> PyObject?.cast(): T {
    if (this == null) error("Casting on null?")
    if (this !is T) return CastHelper.cast(T::class, this)
    return this
}
