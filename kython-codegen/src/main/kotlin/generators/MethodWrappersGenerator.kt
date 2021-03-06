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
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.generation.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

val builtinMethod = ClassName(
    "green.sailor.kython.interpreter.pyobject.function",
    "PyBuiltinFunction"
)

val pyCallableSignature = ClassName(
    "green.sailor.kython.interpreter.callable",
    "PyCallableSignature"
)

val argType = ClassName("green.sailor.kython.interpreter.callable", "ArgType")
val argTypePos = MemberName(argType, "POSITIONAL")
val argTypePosStar = MemberName(argType, "POSITIONAL_STAR")
val argTypeKw = MemberName(argType, "KEYWORD")
val argTypeKwStar = MemberName(argType, "KEYWORD_STAR")

@KotlinPoetMetadataPreview
data class MethodWrapperInfo(
    val original: ImmutableKmClass,
    val builtClass: TypeSpec,
    val wrapperName: String,
    val methodName: String
)

val SPECIAL_SENTINELS = mapOf(
    "green.sailor.kython.interpreter.callable.EMPTY" to CodeBlock.of(
        "%T", ClassName("green.sailor.kython.interpreter.callable", "EMPTY")
    )
)

/**
 * Gets the list of signature code blocks.
 */
@KotlinPoetMetadataPreview
fun getSignatureStatement(anno: MethodParams): List<CodeBlock> {
    val statements = mutableListOf<CodeBlock>()
    // initial CallableSignature
    statements.add(CodeBlock.of("%T(«", pyCallableSignature))
    // each param
    for ((idx, param) in anno.parameters.withIndex()) {
        val argtype = when (param.type.toUpperCase()) {
            "POSITIONAL" -> argTypePos
            "POSITIONAL_STAR" -> argTypePosStar
            "KEYWORD" -> argTypeKw
            "KEYWORD_STAR" -> argTypeKwStar
            else -> error("Unknown arg type ${param.type}")
        }

        // don't add a trailing comma (this can be removed in Kotlin 1.4)
        if (idx == anno.parameters.size - 1) {
            statements.add(CodeBlock.of("%S to %M", param.name, argtype))
        } else {
            statements.add(CodeBlock.of("%S to %M, ", param.name, argtype))
        }
    }

    // initial closing brace
    statements.add(CodeBlock.of("»)\n"))
    if (anno.defaults.isNotEmpty()) {
        // now to add the defaults
        statements.add(CodeBlock.of("«.withDefaults("))
        for ((idx, default) in anno.defaults.withIndex()) {
            // i don't know why i need to type mirror these!!
            // they're standard kotlin classes!!!!
            val mirror = default.getClassMirror { it.type }
            val stmnt = when (mirror.kind) {
                TypeKind.INT, TypeKind.LONG -> CodeBlock.of("%T(%L)", pyInt, default.value)
                TypeKind.BOOLEAN -> CodeBlock.of("%T.get(%L)", pyBool, default.value)
                TypeKind.DECLARED -> {
                    val name = mirror.toString()
                    // terrible!
                    when (name) {
                        "java.lang.String" -> {
                            CodeBlock.of("%T(%S)", pyStr, default.value)
                        }
                        in SPECIAL_SENTINELS -> {
                            SPECIAL_SENTINELS[name] ?: error("????")
                        }
                        else -> {
                            error("Cannot provide $mirror as a default")
                        }
                    }
                }
                else -> error("Cannot provide ${mirror.asTypeName()} as a default")
            }

            statements.add(CodeBlock.of("%S to ", default.forName))
            statements.add(stmnt)
            // don't add a trailing comma (this can be removed in Kotlin 1.4)
            if (idx != anno.defaults.size - 1) {
                statements.add(CodeBlock.of(", "))
            }
        }
        statements.add(CodeBlock.of(")»\n"))
    }

    return statements
}

@KotlinPoetMetadataPreview
fun getTypeSpec(
    target: Pair<TypeElement, ImmutableKmClass>,
    anno: ExposeMethod,
    params: MethodParams?,
    fn: ImmutableKmFunction
): TypeSpec {
    val classname = target.first.asClassName()
    val className = target.second.name.substringAfterLast("/")
    val mangledName = "kython-generated-wrapper\$${className}_${fn.name}"
    val builder = TypeSpec.objectBuilder(mangledName)
    builder.superclass(builtinMethod)
    builder.addSuperclassConstructorParameter("%S", anno.name)

    val ktMethodName = MemberName(classname, fn.name)

    // first off: override fun callFunction
    val callFunction = FunSpec.builder("callFunction").apply {
        addKdoc("Pass-through to call the real function.")
        addModifiers(KModifier.OVERRIDE)

        val mapName = Map::class.asClassName()
            .parameterizedBy(String::class.asClassName(), pyObject)
        // (kwargs: Map<String, PyObject>)
        addParameter(ParameterSpec.builder("kwargs", mapName).build())

        // : PyObject
        returns(KythonProcessor.pyObject)

        addStatement("return %M(kwargs)", ktMethodName)
    }.build()

    builder.addFunction(callFunction)

    // kinda hacky
    val initFunction = FunSpec.builder("__initSignatureHelper").apply {
        addModifiers(KModifier.PRIVATE, KModifier.INLINE)
        addKdoc("Helper function for generating the signature, due to KotlinPoet restrictions.")
        val statements = params?.let { getSignatureStatement(it) } ?: listOf()
        returns(pyCallableSignature)
        addCode("return ")
        if (statements.isEmpty()) {
            addCode(CodeBlock.of("%T.EMPTY", pyCallableSignature))
        } else {
            statements.forEach { addCode(it) }
        }
    }.build()
    builder.addFunction(initFunction)

    val signature = PropertySpec.builder("signature", pyCallableSignature).apply {
        addKdoc("The generated signature for this function.")
        addModifiers(KModifier.OVERRIDE)
        initializer(CodeBlock.of("%N()", initFunction))
    }.build()
    builder.addProperty(signature)

    builder.addKdoc("Generated wrapper for $className#${fn.name} -> ${anno.name}")

    return builder.build()
}

/**
 * Generates a dict setter code block from the specified info.
 */
@KotlinPoetMetadataPreview
fun generateDictSetter(info: MethodWrapperInfo): CodeBlock {
    val realName = ClassInspectorUtil.createClassName(info.original.name)
    return CodeBlock.of("%T.internalDict[%S] = %N\n", realName, info.methodName, info.builtClass)
}

/**
 * Generates method wrappers for the specified target.
 */
@KotlinPoetMetadataPreview
fun generateMethodWrappers(
    env: ProcessingEnvironment,
    target: TypeElement
): List<MethodWrapperInfo> {
    val kmClass = target.toImmutableKmClass()
    // needed to get the annotations out
    // metadata is "safe" but we still need to use this
    val functions = target.enclosedElements
        .filterIsInstance<ExecutableElement>()
        .associateByTo(mutableMapOf()) { it.simpleName.toString() }

    val annoFunctions: MutableList<ImmutableKmFunction> =
        kmClass.functions.toMutableList()

    val typeUtils = env.typeUtils

    var superclass = typeUtils.asElement(target.superclass) as? TypeElement
    while (superclass != null) {
        // only get classes w/ metadata annos
        if (superclass.getAnnotation(Metadata::class.java) == null) {
            break
        }

        val superfunctions = superclass.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .associateByTo(mutableMapOf()) { it.simpleName.toString() }
        val superAnnoFunctions = superclass.toImmutableKmClass().functions

        functions.putAll(superfunctions)
        annoFunctions.addAll(superAnnoFunctions)
        superclass = typeUtils.asElement(superclass.superclass) as? TypeElement
    }
    val className = kmClass.name.substringAfterLast("/")

    val wrappers = mutableListOf<MethodWrapperInfo>()
    for (fn in annoFunctions) {
        val executableElement = functions[fn.name] ?: error("???")
        // find the associated ExposeMethod annotation
        val anno = executableElement.getAnnotation(ExposeMethod::class.java) ?: continue
        val params = executableElement.getAnnotation(MethodParams::class.java)
        val spec = getTypeSpec(Pair(target, kmClass), anno, params, fn)
        val mangledName = "kython-generated-wrapper\$${className}_${fn.name}"
        val info = MethodWrapperInfo(kmClass, spec, mangledName, anno.name)
        wrappers.add(info)
    }
    return wrappers
}
