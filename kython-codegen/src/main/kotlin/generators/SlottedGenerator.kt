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
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import green.sailor.kython.annotation.Slotted
import green.sailor.kython.generation.*
import green.sailor.kython.generation.extensions.error
import green.sailor.kython.generation.extensions.messager
import javax.lang.model.element.TypeElement
import kotlinx.metadata.KmClassifier

@KotlinPoetMetadataPreview
data class SlotWrapperInfo(
    val methodWrappers: List<MethodWrapperInfo>,
    val getAttr: FunSpec,
    val setAttr: FunSpec,
    val dir: FunSpec
)

val castFn = MemberName(
    "green.sailor.kython.interpreter.util",
    "cast"
)

val dirFn = MemberName(
    "green.sailor.kython.interpreter",
    "dir"
)

val EXCLUDED = setOf("type")

val IS_14 = KotlinVersion.CURRENT.isAtLeast(1, 4)

/**
 * Generates slot wrappers for the specified element.
 *
 * This will generate:
 *  1) A getattr implementation
 *  2) A setattr implementation
 *  3) A list of method wrappers
 */
@KotlinPoetMetadataPreview
fun generateSlotWrappers(element: TypeElement): SlotWrapperInfo {
    // first, just generate all the required method wrappers
    val wrappers = generateMethodWrappers(element)
    val wrapperMapping = wrappers.associateBy({ it.methodName }, { it.builtClass })
    val dirs = mutableListOf<String>()

    val elemClass = element.asClassName()

    val anno = element.getAnnotation(Slotted::class.java)
    val objectName = anno.typeName

    // kotlin metadata
    val md = element.toImmutableKmClass()
    val fields = md.properties.filter { it.isPublic }
    val name = element.simpleName.toString()

    // boilerplate; configure the three functions
    val getAttrB = FunSpec.builder("getattrSlotted")
    getAttrB.receiver(elemClass)
    getAttrB.addParameter(ParameterSpec.builder("name", String::class).build())
    getAttrB.returns(pyObject)
    getAttrB.addModifiers(KModifier.INTERNAL)
    getAttrB.addKdoc("Generated slotted getattribute for $name ($objectName)")
    getAttrB.beginControlFlow("return when (name)")

    val setAttrB = FunSpec.builder("setattrSlotted")
    setAttrB.receiver(elemClass)
    setAttrB.addParameter(ParameterSpec.builder("name", String::class).build())
    setAttrB.addParameter(ParameterSpec.builder("value", pyObject).build())
    setAttrB.returns(pyObject)
    setAttrB.addModifiers(KModifier.INTERNAL)
    setAttrB.addKdoc("Generated slotted setattribute for $name ($objectName)")
    // no return because we explicitly return PyNone at the end
    setAttrB.beginControlFlow("when (name)")

    val dirWrapper = FunSpec.builder("dirSlotted")
    dirWrapper.receiver(elemClass)
    dirWrapper.returns(pyTuple)
    dirWrapper.addModifiers(KModifier.INTERNAL)
    dirWrapper.addKdoc("Generated slotted dir for $name ($objectName)")

    // step 1: loop over fields and add a when branch
    for (field in fields) {
        // special excluded field
        if (field.name in EXCLUDED) {
            continue
        }

        val fieldName = field.name
        val typeClassifier = field.returnType.classifier
        if (typeClassifier !is KmClassifier.Class) {
            messager.error("Unknown classifier: $typeClassifier")
            error("Unknown classifier: $typeClassifier")
        }
        dirs.add(fieldName)

        // getattr
        // TODO: Map primitives to their PyObject type.
        val typeName = ClassInspectorUtil.createClassName(typeClassifier.name)
        // TODO: Normalise
        // name -> {
        val getBlocks = mutableListOf<CodeBlock>()
        getBlocks.add(CodeBlock.of("«%S -> {\n", fieldName))
        getBlocks.add(CodeBlock.of("this.%N\n", fieldName))
        getBlocks.add(CodeBlock.of("»}\n"))
        getBlocks.forEach { getAttrB.addCode(it) }

        // setattr
        // only allow setattr on mutable fields

        val setBlocks = mutableListOf<CodeBlock>()
        setBlocks.add(CodeBlock.of("«%S -> {\n", fieldName))
        if (field.isVar) {
            setBlocks.add(CodeBlock.of("this.%N = value.%M<%T>()\n", fieldName, castFn, typeName))
        } else {
            setBlocks.add(CodeBlock.of(
                "%N(\"attribute '$fieldName' is not writable\")\n", attributeError
            ))
        }
        setBlocks.add(CodeBlock.of("»}\n"))
        setBlocks.forEach { setAttrB.addCode(it) }
    }

    // step 2: loop over methods and add a when branch
    for ((method, type) in wrapperMapping) {
        dirs.add(method)
        val getBlocks = mutableListOf<CodeBlock>()
        getBlocks.add(CodeBlock.of("«%S -> {\n", method))
        getBlocks.add(CodeBlock.of("%N\n", type))
        getBlocks.add(CodeBlock.of("»}\n"))
        getBlocks.forEach { getAttrB.addCode(it) }

        val setBlocks = mutableListOf<CodeBlock>()
        setBlocks.add(CodeBlock.of("«%S -> {\n", method))
        setBlocks.add(CodeBlock.of(
            "%N(\"attribute '$method' is not writable\")\n", attributeError
        ))
        setBlocks.add(CodeBlock.of("»}\n"))
        setBlocks.forEach { setAttrB.addCode(it) }
    }

    // step 3: generate the dir wrapper
    run {
        val dirCodes = mutableListOf<CodeBlock>()
        dirCodes.add(CodeBlock.of("«val items = listOf(\n"))
        for ((x, subitem) in dirs.withIndex()) {
            // trailing comma handling without needing to edit for 1.4
            if (IS_14 || x != dirs.size - 1) {
                dirCodes.add(CodeBlock.of("%S,\n", subitem))
            } else {
                dirCodes.add(CodeBlock.of("%S\n", subitem))
            }
        }
        dirCodes.add(CodeBlock.of("»)\n"))
        dirCodes.add(CodeBlock.of(
            "return %T.get((items + this.%M()).mapTo(mutableListOf()) { s -> %T(s) } )",
            pyTuple, dirFn, pyStr
        ))
        dirCodes.forEach { dirWrapper.addCode(it) }
    }

    // generate else branch for getattr
    val attributeErrorCall = CodeBlock.of(
        "%M(\"'$objectName' has no attribute '\${name}'\")\n",
        attributeError
    )
    val elseBranch = "else -> type.internalDict[name]?.pyDescriptorGet(this, type) ?: \n"
    getAttrB.addCode(elseBranch)
    getAttrB.addCode(attributeErrorCall)

    // setattr does not allow setting supertype values
    setAttrB.addCode(CodeBlock.of("else -> \n"))
    setAttrB.addCode(attributeErrorCall)

    getAttrB.endControlFlow()
    setAttrB.endControlFlow()

    setAttrB.addCode(CodeBlock.of("return %T", pyNone))

    return SlotWrapperInfo(wrappers, getAttrB.build(), setAttrB.build(), dirWrapper.build())
}
