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

package green.sailor.kython.interpreter.kyreflection

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.attributeError
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.user.PyUserObject
import green.sailor.kython.interpreter.typeError
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.util.isKotlinClass
import kotlin.reflect.full.memberProperties
import org.apache.commons.beanutils.PropertyUtils

@GenerateMethods
object PyReflectedType : PyType("PyReflectedObject") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        typeError("Cannot directly create new instances of PyReflectedObject.")
    }

    override fun kySuperclassInit(instance: PyUserObject, args: List<PyObject>): PyNone {
        typeError("This class cannot be subclassed")
    }

    /* PyReflectedObject.as_python_object */
    @ExposeMethod("as_python_object")
    @MethodParams(
        MethodParam("self", "POSITIONAL")
    )
    fun methAsPythonObject(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyReflectedObject>()
        return wrapPrimitive(self.wrapped)
    }

    /* PyReflectedObject.java_type */
    @ExposeMethod("java_type")
    @MethodParams(
        MethodParam("self", "POSITIONAL")
    )
    fun methGetJavaType(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyReflectedObject>()
        return PyReflectedObject(self.wrapped::class.java)
    }

    /* PyReflectedObject.get_property */
    @ExposeMethod("get_property")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("name", "POSITIONAL")
    )
    fun methGetProperty(kwargs: Map<String, PyObject>): PyObject {
        val self = kwargs["self"].cast<PyReflectedObject>()
        val name = kwargs["name"].cast<PyString>().wrappedString

        val klass = self.wrapped.javaClass
        if (klass.isKotlinClass()) {
            val kclass = klass.kotlin
            val property = kclass.memberProperties.find { it.name == name }

            if (property === null) {
                attributeError("'${klass.simpleName}' object has no attribute '$name'")
            }

            val obb = property.get(self.wrapped) ?: return PyNone
            return PyReflectedObject(obb)
        }

        val prop = PropertyUtils.getProperty(self.wrapped, name)
        return PyReflectedObject(prop)
    }

    /* PyReflectedObjectg.new_instance */
    @ExposeMethod("new_instance")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("args", "POSITIONAL_STAR")
    )
    fun methNewInstance(kwargs: Map<String, PyObject>): PyObject {
        // hopefully a KClass<*>
        val self = kwargs["self"].cast<PyReflectedObject>()
        if (self.wrapped !is Class<*>) {
            typeError("Cannot create new instance of non-class reflected object")
        }
        // unwrap params
        val args = kwargs["args"].cast<PyTuple>()
        val unwrappedParams = args.subobjects
            .map { it as? PyPrimitive ?: typeError("$it is not a primitive type") }
            .map { it.unwrap() }
        val unwrappedKlasses = unwrappedParams.map { it.javaClass }.toTypedArray()

        val klass = self.wrapped
        val ctor = klass.getConstructor(*unwrappedKlasses)
        val instance = ctor.newInstance(*unwrappedParams.toTypedArray())
        return PyReflectedObject(instance)
    }
}
