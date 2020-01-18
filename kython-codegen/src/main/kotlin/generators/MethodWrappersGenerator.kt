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

package green.sailor.kython.generation.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassData
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.generation.KythonProcessor
import green.sailor.kython.generation.extensions.error
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror


val builtinMethod = ClassName(
    "green.sailor.kython.interpreter.pyobject.function",
    "PyBuiltinFunction"
)

val pyObject = ClassName(
    "green.sailor.kython.interpreter.pyobject",
    "PyObject"
)

val pyCallableSignature = ClassName(
    "green.sailor.kython.interpreter.callable",
    "PyCallableSignature"
)

@KotlinPoetMetadataPreview
data class MethodWrapperInfo(
    val original: ImmutableKmClass,
    val builtClass: TypeSpec,
    val wrapperName: String,
    val methodName: String
)

@KotlinPoetMetadataPreview
fun getTypeSpec(
    target: Pair<TypeElement, ImmutableKmClass>,
    anno: ExposeMethod,
    params: MethodParams?,
    fn: ImmutableKmFunction
): TypeSpec {
    val classname = target.first.asClassName()
    val className = target.second.name.split("/").last()
    val mangledName = "kython generated wrapper ${className}_${fn.name}"
    val builder = TypeSpec.objectBuilder(mangledName)
    builder.superclass(builtinMethod)
    builder.addSuperclassConstructorParameter("%S", anno.name)

    val ktMethodName = MemberName(classname, fn.name)

    // first off: override fun callFunction
    val callFunction = FunSpec.builder("callFunction").apply {
        addKdoc("Pass-through to call the real function.")
        addModifiers(KModifier.OVERRIDE)

        val mapName = Map::class.asClassName()
            .parameterizedBy(String::class.asClassName(), KythonProcessor.pyObject)
        // (kwargs: Map<String, PyObject>)
        addParameter(ParameterSpec.builder("kwargs", mapName).build())

        // : PyObject
        returns(KythonProcessor.pyObject)

        addStatement("return %M(kwargs)", ktMethodName)
    }.build()

    builder.addFunction(callFunction)

    val signature = PropertySpec.builder("signature", KythonProcessor.pyCallableSignature).apply {
        addModifiers(KModifier.OVERRIDE)
        val statements = params?.parameters?.map {
            val argtype = when (it.type) {
                "POSITIONAL" -> KythonProcessor.argTypePos
                "POSITIONAL_STAR" -> KythonProcessor.argTypePosStar
                "KEYWORD" -> KythonProcessor.argTypeKw
                "KEYWORD_STAR" -> KythonProcessor.argTypeKwStar
                else -> {
                    error("Unknown arg type ${it.type}")
                }
            }
            CodeBlock.of("%S to %M", it.name, argtype)
        } ?: listOf()
        initializer(
            "%T(${statements.joinToString(", ")})",
            KythonProcessor.pyCallableSignature
        )
    }.build()
    builder.addProperty(signature)

    return builder.build()
}

/**
 * Generates a dict setter code block from the specified info.
 */
@KotlinPoetMetadataPreview
fun generateDictSetter(info: MethodWrapperInfo): CodeBlock {
    val realName = ClassInspectorUtil.createClassName(info.original.name)
    val block = CodeBlock.of(
        "%T.internalDict[%S] = %N\n",
        realName, info.methodName, info.builtClass
    )
    return block
}

/**
 * Generates method wrappers for the specified target.
 */
@KotlinPoetMetadataPreview
fun generateMethodWrappers(target: TypeElement): List<MethodWrapperInfo> {
    // needed to get the annotations out
    // metadata is "safe" but we still need to use this
    val functions = target.enclosedElements
        .filterIsInstance<ExecutableElement>()
        .associateByTo(mutableMapOf()) { it.simpleName.toString() }

    val x = target.toImmutableKmClass()
    val className = x.name.split("/").last()

    val wrappers = mutableListOf<MethodWrapperInfo>()
    for (fn in x.functions) {
        val executableElement = functions[fn.name] ?: error("???")
        // find the associated ExposeMethod annotation
        val anno = executableElement.getAnnotation(ExposeMethod::class.java) ?: continue
        val params = executableElement.getAnnotation(MethodParams::class.java)
        val spec = getTypeSpec(Pair(target, x), anno, params, fn)
        val mangledName = "kython generated wrapper ${className}_${fn.name}"
        val info = MethodWrapperInfo(x, spec, mangledName, anno.name)
        wrappers.add(info)
    }
    return wrappers
}
