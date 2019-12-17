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

import green.sailor.kython.annotation.GeneratedMethod
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import extensions.error
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_11) // maybe this is causing problems
@SupportedOptions(BuiltinMethodProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class BuiltinMethodProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        roundEnv?.getElementsAnnotatedWith(GeneratedMethod::class.java)?.forEach {
            if (it.kind != ElementKind.METHOD) {
                processingEnv.error("Can only parse methods, not ${it.kind}")
                return false
            }
            generateMethod(it as ExecutableElement)
        }
        return true
    }

    private fun generateMethod(method: ExecutableElement) {
        val srcRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: error("Cannot find source root")

        val annotation = method.getAnnotation(GeneratedMethod::class.java)
        // TODO: Actually generate code
        val func = FunSpec.builder(annotation.to)

        FileSpec
            .builder("", "")
            .addFunction(func.build())
            .build()
            .writeTo(File(srcRoot))
    }
}
