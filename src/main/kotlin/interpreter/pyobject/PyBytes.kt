package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.pyobject.types.PyBytesType

class PyBytes(val wrapped: ByteArray) : PyObject(PyBytesType) {
    override fun pyStr(): PyString = pyRepr()

    override fun pyRepr(): PyString {
        val inner = PyString("...")
        return PyString("b${inner.pyRepr().wrappedString}")
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PyBytes) return false
        return this.wrapped.contentEquals(other.wrapped)
    }

    override fun hashCode(): Int {
        return wrapped.contentHashCode()
    }
}
