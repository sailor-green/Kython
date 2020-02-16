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

import green.sailor.kython.interpreter.*
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.pyobject.collection.PyList
import green.sailor.kython.interpreter.pyobject.collection.PySet
import green.sailor.kython.interpreter.pyobject.collection.PyTuple
import green.sailor.kython.interpreter.pyobject.dict.PyDict
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.util.PyObjectMap
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.interpreter.util.mapBackedSet

/**
 * BUILD_CONST_KEY_MAP
 */
fun UserCodeStackFrame.buildConstKeyMap(arg: Byte) {
    val argCount = arg.toInt()
    val tuple = stack.pop().cast<PyTuple>()
    val args = (0 until argCount).map { stack.pop() }.asReversed().iterator()
    val collected =
        tuple.subobjects.associateWithTo(PyObjectMap()) { args.next() }
    stack.push(PyDict.from(collected))
    bytecodePointer += 1
}

/**
 * BUILD_* (TUPLE, LIST, SET, etc). Does not work for CONST_KEY_MAP!
 */
fun UserCodeStackFrame.buildSimple(type: BuildType, arg: Byte) {
    val count = arg.toInt()
    val built = when (type) {
        BuildType.TUPLE -> {
            PyTuple.get((0 until count).map { stack.pop() }.reversed())
        }
        BuildType.LIST -> {
            PyList((0 until count).map { stack.pop() }.reversed().toMutableList())
        }
        BuildType.STRING -> {
            val concatString = (0 until count)
                .map { (stack.pop() as PyString).wrappedString }
                .reversed()
                .joinToString(separator = "")
            PyString(concatString)
        }
        BuildType.SET -> {
            PySet.of((0 until count).map { stack.pop() }.reversed())
        }
        BuildType.DICT -> {
            val items = (0 until count * 2)
                .map { stack.pop() }
                .reversed()
                .chunked(2)
                .map { it[0] to it[1] }
            val map = PyObjectMap().apply {
                putAll(items)
            }
            PyDict.from(map)
        }
        else -> TODO("Unimplemented build type $type")
    }
    stack.push(built)
    bytecodePointer += 1
}

/**
 * LIST_EXTEND
 */
fun UserCodeStackFrame.listExtend(arg: Byte) {
    // second.extend(first)
    // this modifies TOS1 in-place
    val first = stack.pop()
    val second = stack.pop().cast<PyList>()

    second.extend(first)
    stack.push(second)
    bytecodePointer += 1
}

/**
 * LIST_TO_TUPLE
 */
fun UserCodeStackFrame.listToTuple(arg: Byte) {
    val list = stack.pop().cast<PyList>()
    val tuple = PyTuple.get(list.unwrap())
    stack.push(tuple)
    bytecodePointer += 1
}

fun UserCodeStackFrame.setUpdate(arg: Byte) {
    // second.update(first)
    val first = stack.pop()
    val second = stack.pop().cast<PySet>()

    second.update(first)
    stack.push(second)
    bytecodePointer += 1
}

// (*a, *b)
/**
 * BUILD_*_UNPACK
 */
fun UserCodeStackFrame.buildUnpack(type: BuildType, arg: Byte) {
    val count = arg.toInt()
    val built = when (type) {
        BuildType.TUPLE -> {
            val items = (0 until count)
                .map { stack.pop() }.asReversed()
                .flatMap { it.pyIter().toNativeList() }
            PyTuple.get(items)
        }
        BuildType.LIST -> {
            val items = (0 until count)
                .map { stack.pop() }.asReversed()
                .flatMapTo(mutableListOf()) { it.pyIter().toNativeList() }
            PyList(items)
        }
        BuildType.SET -> {
            val items = (0 until count)
                .map { stack.pop() }.asReversed()
                .flatMapTo(mapBackedSet()) { it.pyIter().toNativeList() }
            PySet.of(items)
        }
        else -> TODO("Unimplemented build type $type")
    }
    stack.push(built)
    bytecodePointer += 1
}

/**
 * LIST_APPEND
 */
fun UserCodeStackFrame.listAppend(param: Byte) {
    // the stack isn't a stack!
    // !!!!
    val toAdd = stack.pop()
    val list = stack[stack.size - param.toInt()]
    if (list !is PyList) error("Cannot LIST_APPEND on non-list $list")
    if (list.subobjects !is MutableList) error("List is immutable?")
    list.subobjects.add(toAdd)

    bytecodePointer += 1
}

/**
 * SET_ADD
 */
fun UserCodeStackFrame.setAdd(param: Byte) {
    val toAdd = stack.pop()
    val set = stack[stack.size - param.toInt()]
    if (set !is PySet) error("Cannot SET_ADD on non-set $set")
    set.wrappedSet.add(toAdd)

    bytecodePointer += 1
}

/**
 * MAP_ADD
 */
fun UserCodeStackFrame.mapAdd(param: Byte) {
    val toAdd = stack.pop()
    val name = stack.pop()
    val dict = stack[stack.size - param.toInt()]
    if (dict !is PyDict) error("Cannot MAP_ADD on non-dict $dict")
    dict.items[name] = toAdd
}

/**
 * UNPACK_SEQUENCE
 */
fun UserCodeStackFrame.unpackSequence(param: Byte) {
    val from_ = stack.pop()
    val iterator = from_.pyIter()
    val count = param.toInt()

    // todo: we could get around this by just putting onto the stack offset instead...
    val toPush = arrayOfNulls<PyObject>(count)

    // weird instruction...
    var popped = 0
    while (true) {
        val next = try {
            iterator.pyNext()
        } catch (e: KyError) {
            e.ensure(Exceptions.STOP_ITERATION)
            // if StopIteration was raised before we had enough items, it's an error
            if (popped != count) {
                valueError("Iterator has not enough items " +
                    "(expected to unpack $count items, got $popped items)")
            }
            break
        }
        popped += 1

        // this will only happen if the iterator does not raise a StopIteration
        // we have popped too many items off, so we raise an error
        if (popped > count) {
            valueError("Iterable has too many items (expected to unpack $count items)")
        }

        toPush[count - popped] = next
    }
    toPush.forEach { stack.push(it!!) }

    bytecodePointer += 1
}
