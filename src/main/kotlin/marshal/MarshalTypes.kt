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

/**
 * The enum for marshal types.
 */
enum class MarshalTypeChars(val c: Char) {
    // simple objects
    NONE('N'),
    FALSE('F'),
    TRUE('T'),

    // numbers
    INT('i'),
    FLOAT('g'),
    COMPLEX('y'),

    // byte types
    BYTESTRING('s'),
    BYTESTRING_INTERNED('t'),

    // unicode types
    ASCIISTRING('a'),
    ASCIISTRING_INTERNED('A'),
    ASCIISTRING_SHORT('z'),
    ASCIISTRING_SHORT_INTERNED('Z'),
    UNICODESTRING('u'),

    // containers
    TUPLE('('),
    SMALL_TUPLE(')'),
    LIST('['),
    DICT('{'),
    SET('<'),
    FROZENSET('>'),

    // CodeType
    CODE('c'),
}

/** The root marshal type. */
sealed class MarshalType() {
    companion object {
        // type references for marshalled objects
        const val NONE = 'N'
        const val FALSE = 'F'
        const val TRUE = 'T'
        const val NULL = '0'
        const val ELLIPSIS = '.'

        // numbers
        const val INT = 'i'

        // "binary" floats/complexes, not the old objects.
        const val FLOAT = 'g'
        const val COMPLEX = 'y'

        // bytestrings, b"abc"
        const val BYTESTRING = 's'
        const val BYTESTRING_INTERNED = 't'
        const val REF = 'r'

        // ascii strings, "abc"
        const val ASCIISTRING = 'a'
        const val ASCIISTRING_INTERNED = 'A'
        const val ASCIISTRING_SHORT = 'z'
        const val ASCIISTRING_SHORT_INTERNED = 'Z'
        const val UNICODESTRING = 'u'

        // containers
        const val TUPLE = '('
        const val SMALL_TUPLE = ')'
        const val LIST = '['
        const val DICT = '{'
        const val SET = '<'
        const val FROZENSET = '>'

        // CodeType
        const val CODE = 'c'
    }

    open val wrapped: Any? = null

    override fun toString(): String = "<Marshalled ${this.wrapped}>"

    /**
     * Ensures this marshalled object is a tuple.
     */
    fun ensureTuple(): MarshalTuple {
        if (this is MarshalTuple) return this
        return MarshalTuple(listOf(this))
    }
}

/** An int. */
class MarshalInt(override val wrapped: Int) : MarshalType()

/** A string. Created from various string objects. */
class MarshalString(override val wrapped: ByteArray) : MarshalType() {
    override fun toString(): String {
        return (this.wrapped as ByteArray).toString(Charsets.UTF_8)
    }

}

/** A unicode string. */
class MarshalUnicodeString(override val wrapped: String) : MarshalType()

/** A marshalled boolean. Technically represented by two values, but we ignore this. */
class MarshalBoolean private constructor(override val wrapped: Boolean) : MarshalType() {
    companion object {
        val TRUE = MarshalBoolean(true)
        val FALSE = MarshalBoolean(false)
    }

}

/** A marshalled None. */
object MarshalNone : MarshalType() {
    override fun toString(): String {
        return "<Marshalled None>"
    }
}

/** A marshal null. */
object MarshalNull : MarshalType() {
    override fun toString(): String {
        return "<Marshalled null>"
    }
}

/** A marshal ellipsis. */
object MarshalEllipsis : MarshalType() {
    override fun toString(): String {
        return "<Marshalled ...>"
    }
}

/** A marshalled float. */
class MarshalFloat(override val wrapped: Double) : MarshalType()

/** A marshalled list. */
class MarshalList(override val wrapped: List<MarshalType>) : MarshalType()

/** A marshalled tuple. */
class MarshalTuple(override val wrapped: List<MarshalType>) : MarshalType()

/** A marshalled dict. */
class MarshalDict(override val wrapped: Map<MarshalType, MarshalType>) : MarshalType()

/** A marshalled code object. */
data class MarshalCodeObject(
    val argCount: MarshalInt,
    val posOnlyArgCount: MarshalInt,
    val kwOnlyArgCount: MarshalInt,
    val localCount: MarshalInt,
    val stackSize: MarshalInt,
    val flags: MarshalInt,

    val bytecode: MarshalString,
    val consts: MarshalTuple,
    val names: MarshalTuple,
    val varnames: MarshalTuple,
    val freevars: MarshalTuple,
    val cellvars: MarshalTuple,

    val filename: MarshalUnicodeString,
    val codeName: MarshalUnicodeString,
    val firstLineNumber: MarshalInt,
    val lnotab: MarshalString
) : MarshalType() {
    override fun toString(): String {
        return "<cpython code object ${codeName}, file $filename>"
    }
}
