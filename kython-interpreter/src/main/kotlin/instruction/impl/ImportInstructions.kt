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

@file:JvmName("InstructionImpls")
@file:JvmMultifileClass
package green.sailor.kython.interpreter.instruction.impl

import green.sailor.kython.interpreter.importing.Importer
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.util.cast

/**
 * IMPORT_NAME
 */
fun UserCodeStackFrame.importName(arg: Byte) {
    val idx = arg.toUByte().toInt()
    val name = function.code.names[idx]

    val fromObb = stack.pop()
    val from = if (fromObb === PyNone) listOf()
    else {
        val tup = fromObb.cast<PyTuple>()
        tup.subobjects.map { it.cast<PyString>().wrappedString }
    }

    val level = stack.pop().cast<PyInt>()

    if (level.wrappedInt == 0L) {
        // absolute import
        val items = Importer.CURRENT.absoluteImport(name, from)
        items.forEach { stack.push(it) }
    } else {
        TODO("Relative imports")
    }

    bytecodePointer += 1
}

/**
 * IMPORT_FROM
 */
fun UserCodeStackFrame.importFrom(arg: Byte) {
    val module = stack.last
    val attrName = function.code.names[arg.toInt()]
    val attr = module.pyGetAttribute(attrName)
    stack.push(attr)
    bytecodePointer += 1
}
