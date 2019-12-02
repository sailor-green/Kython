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
package green.sailor.kython.util

import green.sailor.kython.interpreter.kyobject.KyCodeObject
import green.sailor.kython.kyc.KycFile
import green.sailor.kython.kyc.UnKyc
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the CPython compiler interface.
 */
class CPythonCompiler {
    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    val cpythonExe = if (isWindows) {
        "python.exe"
    } else {
        "python3.8"
    }

    private val kycPyPath = Files.createTempDirectory("kython").resolve("kyc.py")

    init {
        Files.copy(javaClass.classLoader.getResource("kyc.py").openStream(), kycPyPath)
    }

    fun executeCompiler(args: List<String>): KycFile {
        val builder = ProcessBuilder()
        builder.command(
            listOf(
                cpythonExe,
                "-I", // isolate cpython, not using user site or env vars,
                "-S", // no site.py
                kycPyPath.toAbsolutePath().toString()
            ) + args
        )
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)
        val process = builder.start().also { it.waitFor() }

        if (process.exitValue() != 0) {
            error("Compiler didn't exit cleanly")
        }

        val hex = process.inputStream.bufferedReader().readLine()
            ?: error("Compiler returned nothing")
        val ba = hex.chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()
        val buf = ByteBuffer.wrap(ba)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        return UnKyc.parseKycFile(buf)
    }

    /**
     * Compiles from a path.
     */
    fun compile(path: Path): KyCodeObject {
        val args = listOf("--path", path.toAbsolutePath().toString())
        return KyCodeObject(executeCompiler(args).code)
    }

    /**
     * Compiles from a string.
     */
    fun compile(data: String): KyCodeObject {
        return if (!isWindows) {
            val args = listOf("--code", data)
            KyCodeObject(executeCompiler(args).code)
        } else {
            // windows requires a... different approach
            val tmp = Files.createTempFile("kyc-windows-sucks", ".py")
            Files.write(tmp, data.toByteArray(Charset.defaultCharset()))
            val args = listOf("--path", tmp.toAbsolutePath().toString())
            val result = KyCodeObject(executeCompiler(args).code)
            Files.deleteIfExists(tmp)
            result
        }
    }
}
