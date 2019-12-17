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

package green.sailor.kython.generation.builtins

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.generation.extensions.error
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions(BuiltinMethodProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class BuiltinMethodProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        // helper constants
        val builtinMethod = ClassName(
            "green.sailor.kython.interpreter.functions",
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

        val argType = ClassName("green.sailor.kython.interpreter.callable", "ArgType")
        val argTypePos = MemberName(argType, "POSITIONAL")
        val argTypePosStar = MemberName(argType, "POSITIONAL_STAR")
        val argTypeKw = MemberName(argType, "KEYWORD")
        val argTypeKwStar = MemberName(argType, "KEYWORD_STAR")
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(ExposeMethod::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        // ??
        if (roundEnv == null) return false

        for (element in roundEnv.getElementsAnnotatedWith(GenerateMethods::class.java)) {
            if (element.kind != ElementKind.CLASS) {
                processingEnv.error("Can only process classes, not $element")
                return false
            }
            generateBuiltinWrappers(element as TypeElement)
        }
        return true
    }

    fun generateBuiltinWrappers(klass: TypeElement) {
        val srcRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: error("Cannot find source root")

        // this builds a big function that is used to add everything to the internalDict
        val className = klass.asClassName()

        val builder = FileSpec.builder(
            packageName = "green.sailor.kython.generation.generated",
            fileName = "GeneratedMethodsFor${className.simpleName}"
        )

        val typeSpecs = mutableMapOf<String, TypeSpec>()

        // processes all methods
        for (i in klass.enclosedElements) {
            // we only process methods, and only ones also annotated with ExposeMethod
            if (i.kind != ElementKind.METHOD) continue
            val exposeMethod =
                i.getAnnotation(ExposeMethod::class.java) ?: continue
            val rawMethodName = exposeMethod.name
            val mangledName = "kython generated wrapper $rawMethodName"
            val kotlinMethodName = MemberName(className, i.simpleName.toString())

            // override fun callFunction
            val callFunction = FunSpec.builder("callFunction").apply {
                addKdoc("Pass-through to call the real function.")
                addModifiers(KModifier.OVERRIDE)

                val mapName = Map::class.asClassName()
                    .parameterizedBy(String::class.asClassName(), pyObject)
                // (kwargs: Map<String, PyObject>)
                addParameter(ParameterSpec.builder("kwargs", mapName).build())

                // : PyObject
                returns(pyObject)

                addStatement("return %M(kwargs)", kotlinMethodName)
            }.build()

            // override val signature
            val signature = PropertySpec.builder("signature", pyCallableSignature).apply {
                addModifiers(KModifier.OVERRIDE)
                val sigAnnos = i.getAnnotationsByType(MethodParam::class.java)
                val statements = mutableListOf<CodeBlock>()
                for (annotation in sigAnnos) {
                    val argtype = when (annotation.argType) {
                        "POSITIONAL" -> argTypePos
                        "POSITIONAL_STAR" -> argTypePosStar
                        "KEYWORD" -> argTypeKw
                        "KEYWORD_STAR" -> argTypePosStar
                        else -> {
                            processingEnv.error("Unknown arg type ${annotation.argType}")
                            error("Unknown arg type")
                        }
                    }
                    statements += CodeBlock.of("%S to %M", annotation.name, argtype)
                }
                initializer("%T(${statements.joinToString(", ")})", pyCallableSignature)
            }.build()

            val methodKlass = TypeSpec.objectBuilder(mangledName).apply {
                addModifiers(KModifier.PRIVATE)
                addKdoc("Generated wrapper for $rawMethodName -> ${i.simpleName}")
                // PyBuiltinFunction(name)
                superclass(builtinMethod)
                addSuperclassConstructorParameter("%S", rawMethodName)

                addFunction(callFunction)
                addProperty(signature)
            }.build()
            typeSpecs[exposeMethod.name] = methodKlass
            builder.addType(methodKlass)
        }

        // add a function that wraps it all up
        val adderFunction = FunSpec.builder("addBuiltinsFor${className.simpleName}").apply {
            addKdoc("Adds all the builtin wrapper methods to the type object.")
            for ((methodName, spec) in typeSpecs) {
                val newClassName = ClassName(builder.packageName, spec.name!!)
                addStatement("%T.internalDict[%S] = %T", className, methodName, newClassName)
                addStatement("") // newline
            }
        }.build()
        builder.addFunction(adderFunction)

        builder.addComment("""
            Automatically generated method wrapper file! Do not edit!
            This was generated for ${className.canonicalName}.
            """.trimIndent())
        builder.build().writeTo(Paths.get(srcRoot))
    }
}
