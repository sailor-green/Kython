package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.*
import java.lang.IndexOutOfBoundsException

object PyBytesType : PyType("bytes") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val value = kwargs["value"] ?: error("Built-in signature mismatch")
        when (value) {
            is PyString -> {
                return PyBytes(value.wrappedString.toByteArray())
            }
            else -> error("Not yet supported")
        }
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "value" to ArgType.POSITIONAL
        )
    }

    /** bytes.__getitem__ */
    val pyBytesIndex = PyBuiltinFunction.wrap("__getitem__", PyCallableSignature("index" to ArgType.POSITIONAL)) {
        val self = it["self"]!!.cast<PyBytes>()
        val index = it["index"]!!.cast<PyInt>()
        // TODO: PySlice check
        try {
            PyInt(self.wrapped[index.wrappedInt.toInt()].toLong())
        } catch (e: IndexOutOfBoundsException) {
            Exceptions.INDEX_ERROR("index out of range")
        }
    }

    override val initialDict: Map<String, PyObject> by lazy {
        mapOf(
            // magic method impls
            "__getitem__" to pyBytesIndex
        )
    }
}