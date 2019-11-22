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
 *
 */

package green.sailor.kython.marshal

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.experimental.and

/*
NOTES ON INTERNING:

You may think, haha, the intern table is an array! Not so fast. It's a Map. But with int keys.
When an intern flag is seen, the counter is reserved *then*, so if you intern something once it's
created, things will CRASH. Instead, you reserve the index for the item, and the next item is slotted
into the next slot in the map.

Not a good explanation, but whatever. Basically:

 1) If intern flag is set, get the current intern count as this object's intern offset
 2) Increment current intern count by one
 3) Once the object is constructed, store it in map[offset].

Could be done with a list, but I don't care. Also, code objects are ALWAYS interned for some reason.
So, explicitly set their intern flag on.
 */


/**
 * Object that supports marshalling and demarshalling.
 */
open class Marshaller(protected val buf: ByteBuffer) {
    companion object {
        /**
         * Parses a pyc file from a Path.
         */
        fun parsePycFile(path: Path): MarshalCodeObject {
            val bytes = Files.readAllBytes(path)
            val buf = ByteBuffer.wrap(bytes)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            return parsePycFile(buf)
        }

        /**
         * Parses a pyc file from a ByteBuffer.
         */
        fun parsePycFile(data: ByteBuffer): MarshalCodeObject {
            // TODO: Magic number detection to only read 3 ints instead of 4...

            // magic number
            val magicNum = data.int
            // bitfield
            val bitfield = data.int
            // two words are dependant on the bitfield, but we don't actually care
            val b3 = data.int
            val b4 = data.int

            // now it's the marshal body

            val marshaller = Marshaller(data)
            return (marshaller.unmarshal() as MarshalCodeObject)
        }
    }

    // this is very stupid
    var lastInternIdx = 0

    /** The intern table... */
    val INTERN_TABLE = mutableMapOf<Int, MarshalType>()

    /**
     * Unmarshals from a byte buffer.
     */
    fun unmarshal(): MarshalType = this.readObject()

    /**
     * Reads an object from the stream.
     */
    fun readObject(): MarshalType {
        val byte = buf.get()

        val type = (byte and (-0x81).toByte()).toChar()
        val flag = (byte and 0x80.toByte()).toInt()
        var shouldIntern = flag != 0
        val internIdx = this.lastInternIdx
        if (shouldIntern) {
            this.lastInternIdx += 1
        }

        val result = when (type) {
            // simple types...
            MarshalType.FALSE -> MarshalBoolean.FALSE
            MarshalType.TRUE -> MarshalBoolean.TRUE
            MarshalType.NONE -> MarshalNone
            MarshalType.ELLIPSIS -> MarshalEllipsis
            MarshalType.NULL -> MarshalNull


            // string types
            MarshalType.ASCIISTRING, MarshalType.UNICODESTRING -> this.readString(false)
            MarshalType.ASCIISTRING_INTERNED -> {
                shouldIntern = true
                this.readString(false)
            }
            MarshalType.ASCIISTRING_SHORT -> this.readString(true)
            MarshalType.ASCIISTRING_SHORT_INTERNED -> {
                shouldIntern = true
                this.readString(short = true)
            }

            // byte types
            MarshalType.BYTESTRING -> this.readByteString()
            MarshalType.BYTESTRING_INTERNED -> this.readByteStringInterned()

            // string ref
            MarshalType.REF -> this.readStringRef()

            // number types
            MarshalType.INT -> this.readInt()
            MarshalType.FLOAT -> this.readFloat()

            // container types
            MarshalType.TUPLE -> this.readTuple(small = false)
            MarshalType.SMALL_TUPLE -> this.readTuple(small = true)
            MarshalType.LIST -> this.readList()
            MarshalType.DICT -> this.readDict()

            // code type
            MarshalType.CODE -> {
                shouldIntern = true
                this.readCode()
            }

            else -> error("Unknown marshal type: $type")
        }

        if (shouldIntern) {
            this.INTERN_TABLE.put(internIdx, result)
        }

        return result
    }

    /**
     * Reads an int from the stream.
     */
    fun readInt(): MarshalInt {
        return MarshalInt(buf.int)
    }

    /**
     * Reads a float from the stream.
     */
    fun readFloat(): MarshalFloat {
        return MarshalFloat(buf.double)
    }

    /**
     * Reads a string from the stream.
     *
     * @param short: If this is a short string (TYPE_SHORT_ASCII).
     */
    fun readString(short: Boolean = false): MarshalUnicodeString {
        val size = if (!short) {
            buf.int
        } else {
            buf.get().toInt()
        }

        val ca = ByteArray(size)
        for (x in 0 until size) {
            val b = buf.get()
            ca[x] = b
        }

        return MarshalUnicodeString(ca.toString(Charsets.UTF_8))
    }

    /**
     * Reads a byte string from the stream.
     */
    fun readByteString(): MarshalString {
        val size = buf.int

        val ca = ByteArray(size)
        for (x in 0 until size) {
            val b = buf.get()
            ca[x] = b
        }

        return MarshalString(ca)
    }

    /**
     * Reads an interned byte string from the stream.
     */
    fun readByteStringInterned(): MarshalString {
        val bs = this.readByteString()
        //this.INTERN_TABLE.add(bs)
        return bs
    }

    /** Reads an interned string ref. */
    fun readStringRef(): MarshalType {
        val idx = buf.int
        return this.INTERN_TABLE[idx]!!
    }

    /**
     * Gets a sized container (tuple, list, set, etc) from the stream.
     *
     * @param small: If this is a "small" container (TYPE_SMALL_TUPLE).
     */
    fun getSizedContainer(small: Boolean = false): List<MarshalType> {
        val size = if (!small) {
            buf.int
        } else {
            buf.get().toInt()
        }
        val arr = arrayOfNulls<MarshalType>(size)

        // loop over and read a new object off
        for (i in 0 until size) {
            arr[i] = this.readObject()
        }

        // it shouldn't be a problem, but just in case...
        val filtered = arr.filterNotNull()
        check(filtered.size == size) { "Marshalled container didn't have $size elements but ${filtered.size}" }
        return filtered
    }

    /**
     * Reads a tuple from the stream.
     *
     * @param small: Passed to getSizedContainer().
     */
    fun readTuple(small: Boolean = false): MarshalTuple {
        val arr = this.getSizedContainer(small)
        return MarshalTuple(arr)
    }

    /**
     * Reads a list from the stream.
     */
    fun readList(): MarshalList {
        val arr = this.getSizedContainer()
        return MarshalList(arr.toList())  // inefficient, but, oh well.
    }

    /**
     * Reads a dict from the stream.
     */
    fun readDict(): MarshalDict {
        val map = hashMapOf<MarshalType, MarshalType>()

        // dict is stored in k:v k:v k:v null
        // null signifies the end of the dict...
        // so we stop reading keys when we get one.
        while (true) {
            val key = this.readObject()
            if (key == MarshalNone) {
                break
            }
            val value = this.readObject()
            map[key] = value
        }

        return MarshalDict(map)
    }

    // the fun one...
    /**
     * Reads a code object from the stream.
     */
    @Suppress("LocalVariableName")
    fun readCode(): MarshalCodeObject {
        // python 3.8's marshal.c

        // simple int values
        val co_argcount = this.readInt()
        // TODO: co_posonlyargcount
        val co_posonlyargcount = this.readInt()
        val co_kwonlyargcount = this.readInt()
        val co_nlocals = this.readInt()
        val co_stacksize = this.readInt()
        val co_flags = this.readInt()

        // more complex values
        val co_code = this.readObject()  // it says w_object, but this is reasonably only a bytestring.
        val co_consts = this.readObject().ensureTuple()
        val co_names = this.readObject().ensureTuple()
        val co_varnames = this.readObject().ensureTuple()
        val co_freevars = this.readObject().ensureTuple()
        val co_cellvars = this.readObject().ensureTuple()
        val co_filename = this.readObject()
        val co_name = this.readObject()
        val co_firstlineno = this.readInt()
        val lnotab = this.readObject()

        return MarshalCodeObject(
            co_argcount, MarshalInt(0), co_kwonlyargcount, co_nlocals, co_stacksize, co_flags,

            (co_code as MarshalString), co_consts, co_names,
            co_varnames, co_freevars, co_cellvars,

            (co_filename as MarshalUnicodeString), (co_name as MarshalUnicodeString),
            co_firstlineno, (lnotab as MarshalString)
        )
    }
}
