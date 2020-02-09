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

package green.sailor.kython.compiler

import com.sun.jna.Memory
import com.sun.jna.Platform
import com.sun.jna.ptr.PointerByReference
import green.sailor.kython.compiler.cpython.jna.LibkythonBridge
import green.sailor.kython.kyc.KycFile
import green.sailor.kython.kyc.UnKyc
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A compiler using an embedded CPython, rather than a subprocess install.
 */
object CPythonCompiler : Compiler {

    val compilerLock = ReentrantLock()

    /**
     * Calls libpython to compile.
     */
    fun compileCommon(code: String, filename: String): KycFile = compilerLock.withLock {
        val output = LibkythonBridge.libkython.kyc_compile(code, filename)

        val ba = output.chunked(2)
            .map { it.toUpperCase().toInt(16).toByte() }
            .toByteArray()
        val buf = ByteBuffer.wrap(ba)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        return UnKyc.parseKycFile(buf)
    }

    override fun compile(path: Path): KycFile {
        val code = Files.readString(path)
        return compileCommon(code, path.toAbsolutePath().toString())
    }

    override fun compileFromString(s: String, filename: String): KycFile {
        return compileCommon(s, filename)
    }

    @JvmStatic fun main(args: Array<String>) {
        val output = LibkythonBridge.libkython.kyc_compile("x = 1", "<module>")
        val ba = output.chunked(2)
            .map { it.toUpperCase().toInt(16).toByte() }
            .toByteArray()
        val buf = ByteBuffer.wrap(ba)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        val kyc = UnKyc.parseKycFile(buf)

        val output2 = LibkythonBridge.libkython.kyc_compile("x = 2", "<module>")
        val ba2 = output2.chunked(2)
            .map { it.toUpperCase().toInt(16).toByte() }
            .toByteArray()
        val buf2 = ByteBuffer.wrap(ba2)
        buf2.order(ByteOrder.LITTLE_ENDIAN)
        val kyc2 = UnKyc.parseKycFile(buf2)

        println("${kyc.code.consts.wrapped}, ${kyc2.code.consts.wrapped}")
    }
}
