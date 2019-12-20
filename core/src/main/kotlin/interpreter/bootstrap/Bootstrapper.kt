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

package green.sailor.kython.interpreter.bootstrap

import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.loaders.JarFileModuleLoader
import green.sailor.kython.interpreter.stack.StackFrame
import green.sailor.kython.interpreter.thread.InterpreterThread

class Bootstrapper private constructor(bsFrame: StackFrame) : InterpreterThread(bsFrame) {
    companion object {
        /**
         * Builds the bootstrapper frame.
         */
        fun build(): Bootstrapper {
            val bootstrapModule = JarFileModuleLoader
                .getModuleNoRun("bootstrap", "__kython_bootstrap")
            addBootstrapFunctions(bootstrapModule.userModule)
            val frame = bootstrapModule.userModule.stackFrame
            return Bootstrapper(frame)
        }
    }

    override fun runThread() {
        try {
            KythonInterpreter.interpreterThreadLocal.set(this)
            runThreadWithErrorView()
        } finally {
            KythonInterpreter.interpreterThreadLocal.remove()
        }
    }
}
