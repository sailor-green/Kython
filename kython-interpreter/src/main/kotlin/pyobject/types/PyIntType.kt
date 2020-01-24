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

package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.annotation.ExposeMethod
import green.sailor.kython.annotation.GenerateMethods
import green.sailor.kython.annotation.MethodParam
import green.sailor.kython.annotation.MethodParams
import green.sailor.kython.interpreter.callable.ArgType
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.*
import green.sailor.kython.interpreter.util.cast
import green.sailor.kython.interpreter.valueError
import green.sailor.kython.util.bytesToLongBE
import green.sailor.kython.util.longToBytesBE
import green.sailor.kython.util.longToBytesLE

/**
 * Represents the type of an int.
 */
@GenerateMethods
object PyIntType : PyType("int") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val value = kwargs["value"] ?: error("Built-in signature mismatch")
        when (value) {
            // MUST COME FIRST
            // cpython: int(True) == 1, type(int(True)) == int
            is PyBool -> {
                return if (value.wrapped) PyInt.ONE else PyInt.ZERO
            }
            is PyInt -> {
                return value
            }
            is PyString -> { // special case, for int(x, base)
                val base = kwargs["base"]!!.cast<PyInt>()
                try {
                    return PyInt(value.wrappedString.toInt(base.wrappedInt.toInt()).toLong())
                } catch (e: NumberFormatException) {
                    valueError(
                        "Cannot convert '${value.wrappedString}' to int " +
                            "with base ${base.wrappedInt}"
                    )
                }
            }
            else -> {
                return value.pyToInt()
            }
        }
    }

    @ExposeMethod("to_bytes")
    @MethodParams(
        MethodParam("self", "POSITIONAL"),
        MethodParam("size", "POSITIONAL"),
        MethodParam("byteorder", "POSITIONAL")
    )
    fun pyIntToBytes(kwargs: Map<String, PyObject>): PyBytes {
        val self = kwargs["self"].cast<PyInt>().wrappedInt
        val size = kwargs["size"].cast<PyInt>().wrappedInt
        val endian = kwargs["byteorder"].cast<PyString>().wrappedString
        val iSize = size.toInt()

        val workingArray = ByteArray(iSize)

        // todo: wrap error
        val ba = when (endian) {
            "big" -> longToBytesBE(self, workingArray)
            "little" -> longToBytesLE(self, workingArray)
            else -> valueError("Invalid endianness: $endian")
        }
        return PyBytes(ba)
    }

    // TODO: Class methods...
    @ExposeMethod("from_bytes")
    @MethodParams(
        MethodParam("bytes", "POSITIONAL"),
        MethodParam("byteorder", "POSITIONAL")
    )
    fun pyIntFromBytes(kwargs: Map<String, PyObject>): PyInt {
        val bytes = kwargs["bytes"].cast<PyBytes>().wrapped
        val order = kwargs["byteorder"].cast<PyString>().wrappedString

        val ba = when (order) {
            "big" -> bytesToLongBE(bytes)
            "little" -> bytesToLongBE(bytes)
            else -> valueError("Invalid endianness: $order")
        }
        return PyInt(ba)
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "value" to ArgType.POSITIONAL,
            "base" to ArgType.POSITIONAL
        ).withDefaults(
            "base" to PyInt(
                10
            )
        )
    }
}
