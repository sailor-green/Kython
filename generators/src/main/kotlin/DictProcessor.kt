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

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import green.sailor.kython.annotation.ExposeField
import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.generation.extensions.error
import green.sailor.kython.generation.extensions.note
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Generated
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.AGGREGATING
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING

/**
 * Annotation processor that
 */
@SupportedOptions(
    DictProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME,
    "org.gradle.annotation.processing.aggregating"
)
class DictProcessor : AbstractProcessor() {
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
        return mutableSetOf(GenerateMethods::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        // ??
        if (roundEnv == null || roundEnv.processingOver()) return false
        val srcRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: error("Cannot find source root")

        processingEnv.note(
            "Note: None of these warnings are really warnings, note logging is just too verbose"
        )

        val wrappers = mutableMapOf<ClassName, Map<String, TypeSpec>>()
        val fields = mutableListOf<CodeBlock>()
        for (klass in roundEnv.getElementsAnnotatedWith(GenerateMethods::class.java)) {
            if (klass.kind != ElementKind.CLASS) {
                processingEnv.error("Can only process classes, not $klass")
                return false
            }
            val className = (klass as TypeElement).asClassName()
            wrappers[className] = generateBuiltinWrappers(klass)
            fields.addAll(generateFieldSetters(klass))
        }

        val builder = FileSpec.builder(
            packageName = "green.sailor.kython.generation.generated",
            fileName = "BuiltinWrappers"
        )
        wrappers.flatMap { it.value.values }.forEach { builder.addType(it) }

        // add a function that wraps it all up
        val adderMethodsFunction = FunSpec.builder("addAllMethods").apply {
            addKdoc("Adds builtin wrapper methods to objects dicts")
            for ((className, submap) in wrappers) {
                for ((methodName, spec) in submap) {
                    val newClassName = ClassName(builder.packageName, spec.name!!)
                    addStatement("%T.internalDict[%S] = %T", className, methodName, newClassName)
                    addStatement("") // newline
                }
            }
        }.build()

        // build the addAllFields function
        val adderFieldsFunction = FunSpec.builder("addAllFields").apply {
            addKdoc("Adds all the fields to object dicts.")
            fields.map { addCode(it) }
        }.build()

        builder.apply {
            addFunction(adderMethodsFunction)
            addFunction(adderFieldsFunction)
            addComment("This file is automatically generated! Do not edit.")
        }

        val file = builder.build()
        file.writeTo(Paths.get(srcRoot))

        return true
    }

    /**
     * Generates the list of field setter codeblocks.
     */
    fun generateFieldSetters(klass: TypeElement): List<CodeBlock> {
        val className = klass.asClassName()
        val items = mutableListOf<CodeBlock>()
        for (i in klass.enclosedElements) {
            val exposeField = i.getAnnotation(ExposeField::class.java) ?: continue
            val member = MemberName(className, i.simpleName.toString())
            val codeBlock = CodeBlock.of(
                "%T.internalDict[%S] = %M",
                className, exposeField.name, member
            )
            items.add(codeBlock)
        }
        return items
    }

    /**
     * Generates a mapping of name -> wrapper function [TypeSpec].
     */
    fun generateBuiltinWrappers(klass: TypeElement): Map<String, TypeSpec> {
        // this builds a big function that is used to add everything to the internalDict
        val className = klass.asClassName()
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
                val sigAnno = i.getAnnotation(MethodParams::class.java)
                val statements = sigAnno.parameters.map {
                    val argtype = when (it.type) {
                        "POSITIONAL" -> argTypePos
                        "POSITIONAL_STAR" -> argTypePosStar
                        "KEYWORD" -> argTypeKw
                        "KEYWORD_STAR" -> argTypeKwStar
                        else -> {
                            processingEnv.error("Unknown arg type ${it.type}")
                            error("Unknown arg type")
                        }
                    }
                    CodeBlock.of("%S to %M", it.name, argtype)
                }
                initializer(
                    "%T(${statements.joinToString(", ")})",
                    pyCallableSignature
                )
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
            processingEnv.note("Generated wrapper class for ${exposeMethod.name}")
            typeSpecs[exposeMethod.name] = methodKlass
        }

        return typeSpecs
    }
}
