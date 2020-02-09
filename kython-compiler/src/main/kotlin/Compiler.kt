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

import green.sailor.kython.kyc.KycFile
import java.nio.file.Path

/**
 * The main interface to a Python compiler.
 */
interface Compiler {
    companion object {
        /** The current compiler instance. */
        var CURRENT: Compiler = CPythonCompiler
            private set

        /**
         * Compiles a file from a path using a compiler backend.
         */
        fun compile(path: Path): KycFile {
            return CURRENT?.compile(path) ?: error("No compiler has been loaded!")
        }


    }

    /**
     * Compiles a [Path] into a [KycFile].
     */
    fun compile(path: Path): KycFile

    /**
     * Compiles a [String] into a [KycFile].
     */
    fun compileFromString(s: String, filename: String): KycFile
}
