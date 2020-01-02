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

import green.sailor.kython.interpreter.stack.UserCodeStackFrame

/**
 * LOAD_*
 */
fun UserCodeStackFrame.load(pool: LoadPool, opval: Byte) {
    // pool is the type we want to load
    val idx = opval.toInt()
    val loadResult = when (pool) {
        LoadPool.CONST -> function.code.consts[idx]
        LoadPool.FAST -> {
            val name = function.code.varnames[idx]
            locals[name] ?: error("Tried to load uninitialised varname $name")
        }
        LoadPool.NAME -> {
            // sometimes a global...
            val name = function.code.names[idx]
            locals[name] ?: function.getGlobal(name)
        }
        LoadPool.GLOBAL -> {
            val name = function.code.names[idx]
            function.getGlobal(name)
        }
        LoadPool.ATTR -> {
            val toGetFrom = stack.pop()
            val name = function.code.names[idx]
            toGetFrom.pyGetAttribute(name)
        }
        LoadPool.METHOD -> {
            // load_method sucks shit, for the record.
            // we just treat this as an attribute load, for functions only
            // because we already generated all the method wrappers anyway.
            val toGetFrom = stack.pop()
            val name = function.code.names[idx]
            toGetFrom.pyGetAttribute(name)
        }
    }

    stack.push(loadResult)
    bytecodePointer += 1
}

/**
 * STORE_(NAME|FAST).
 */
fun UserCodeStackFrame.store(pool: LoadPool, arg: Byte) {
    val idx = arg.toInt()
    val toGetName = when (pool) {
        LoadPool.NAME -> function.code.names
        LoadPool.FAST -> function.code.varnames
        else -> error("Can't store items in pool $pool")
    }
    val name = toGetName[idx]
    locals[name] = stack.pop()
    bytecodePointer += 1
}

/**
 * STORE_ATTR.
 */
fun UserCodeStackFrame.storeAttr(arg: Byte) {
    val name = function.code.names[arg.toInt()]
    val toStoreOn = stack.pop()
    val toStore = stack.pop()
    toStoreOn.pySetAttribute(name, toStore)
    bytecodePointer += 1
}

/**
 * DELETE_(NAME|FAST).
 */
fun UserCodeStackFrame.delete(pool: LoadPool, opval: Byte) {
    val idx = opval.toInt()
    when (pool) {
        LoadPool.FAST -> {
            val name = function.code.varnames[idx]
            locals.remove(name) ?: error("Local $name was not present!")
        }
        LoadPool.NAME -> {
            when (val name = function.code.names[idx]) {
                in locals -> locals.remove(name)
                in function.module.attribs -> function.module.attribs.remove(name)
                else -> error("Name $name was not present!")
            }
        }
        else -> error("Unknown pool to delete from: $pool")
    }
    bytecodePointer += 1
}
