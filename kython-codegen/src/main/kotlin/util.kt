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

package green.sailor.kython.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

val pyObject = ClassName("green.sailor.kython.interpreter.pyobject", "PyObject")
val pyInt = ClassName("green.sailor.kython.interpreter.pyobject", "PyInt")
val pyStr = ClassName("green.sailor.kython.interpreter.pyobject", "PyString")
val pyBool = ClassName("green.sailor.kython.interpreter.pyobject", "PyBool")
val pyTuple = ClassName("green.sailor.kython.interpreter.pyobject", "PyTuple")
val pyNone = ClassName("green.sailor.kython.interpreter.pyobject", "PyNone")

val attributeError = MemberName("green.sailor.kython.interpreter", "attributeError")

/**
 * Gets a class mirror from an annotation.
 */
fun <T> T.getClassMirror(field: (T) -> KClass<*>): TypeMirror {
    return try {
        field(this)
        error("Must never happen")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    } catch (e: MirroredTypesException) {
        e.typeMirrors.first()
    }
}
