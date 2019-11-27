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
package green.sailor.kython.kyc

import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.interpreter.pyobject.*

/**
 * The enum for marshal types.
 */
enum class KycType(val c: Char) {
    // simple objects
    NONE('N'),
    FALSE('-'),
    TRUE('+'),

    // numbers
    INT('i'),
    LONG('L'),
    FLOAT('f'),
    COMPLEX('y'),

    // byte types
    BYTESTRING('b'),

    // unicode types
    UNICODE_STRING('s'),

    // containers
    TUPLE('t'),
    LIST('l'),
    DICT('d'),
    SET('{'),
    FROZENSET('>'),

    // CodeType
    CODE('c'),

    // root type
    KY_FILE('K');

    companion object {
        fun get(c: Char): KycType = values().find { it.c == c } ?: error("Unknown type '$c'")
    }
}

/** The root marshal type. */
sealed class BaseKycType : Wrappable<PyObject> {
    open val wrapped: Any? = null

    override fun toString(): String = "<Kyc object ${javaClass.simpleName} $wrapped>"

    /**
     * Ensures this marshalled object is a tuple.
     */
    fun ensureTuple(): KycTuple {
        if (this is KycTuple) return this
        return KycTuple(listOf(this))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseKycType

        if (wrapped != other.wrapped) return false

        return true
    }

    override fun hashCode(): Int {
        return wrapped?.hashCode() ?: 0
    }
}

/** An int. */
class KycInt(override val wrapped: Int) : BaseKycType() {
    override fun wrap(): PyInt = PyInt(wrapped.toLong())
}

/** A long. */
class KycLong(override val wrapped: Long) : BaseKycType() {
    override fun wrap(): PyInt = PyInt(wrapped)
}

/** A string. Created from various string objects. */
class KycString(override val wrapped: ByteArray) : BaseKycType() {
    override fun wrap(): PyBytes = PyBytes(wrapped)
}

/** A unicode string. */
class KycUnicodeString(override val wrapped: String) : BaseKycType() {
    override fun wrap(): PyString = PyString(wrapped)
}

/** An encoded boolean. */
class KycBoolean private constructor(override val wrapped: Boolean) : BaseKycType() {
    companion object {
        val TRUE = KycBoolean(true)
        val FALSE = KycBoolean(false)
    }

    override fun wrap(): PyBool = if (wrapped) PyBool.TRUE else PyBool.FALSE
}

/** An encoded None. */
object KycNone : BaseKycType() {
    override fun toString(): String {
        return "<Kycled None>"
    }

    override fun wrap(): PyNone = PyNone
}

/** An encoded ellipsis. */
object KycEllipsis : BaseKycType() {
    override fun toString(): String {
        return "<Kyc encoded ...>"
    }

    override fun wrap(): PyObject = TODO()
}

/** An encoded float. */
class KycFloat(override val wrapped: Double) : BaseKycType() {
    override fun wrap(): PyObject = PyFloat(wrapped)
}

/** An encoded list. */
class KycList(override val wrapped: List<BaseKycType>) : BaseKycType() {
    override fun wrap(): PyObject = TODO()
}

/** An encoded tuple. */
class KycTuple(override val wrapped: List<BaseKycType>) : BaseKycType() {
    override fun wrap(): PyTuple = PyTuple(wrapped.map { it.wrap() })
}

/** An encoded dict. */
class KycDict(override val wrapped: Map<BaseKycType, BaseKycType>) : BaseKycType() {
    override fun wrap(): PyDict {
        val transformed =
            wrapped.entries.associateByTo(linkedMapOf(), { it.key.wrap() }, { it.value.wrap() })
        return PyDict(transformed)
    }
}

/** An encoded code object. */
data class KycCodeObject(
    val argCount: KycInt,
    val posOnlyArgCount: KycInt,
    val kwOnlyArgCount: KycInt,
    val localCount: KycInt,
    val stackSize: KycInt,
    val flags: KycInt,

    val bytecode: KycString,
    val consts: KycTuple,
    val names: KycTuple,
    val varnames: KycTuple,
    val freevars: KycTuple,
    val cellvars: KycTuple,

    val filename: KycUnicodeString,
    val codeName: KycUnicodeString,
    val firstLineNumber: KycInt,
    val lnotab: KycString
) : BaseKycType() {
    override fun toString(): String {
        return "<kyc code object $codeName, file $filename>"
    }

    override fun wrap(): PyObject = PyCodeObject(KyCodeObject(this))
}

class KycFile(
    val pyHash: Long,
    val comment: KycUnicodeString,
    val code: KycCodeObject
) : BaseKycType() {
    override fun wrap(): PyObject = TODO()
}
