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

package green.sailor.kython.importing.importlib.bootstrap

import green.sailor.kython.interpreter.builtins.SysModule
import green.sailor.kython.interpreter.callable.PyCallableSignature
import green.sailor.kython.interpreter.importing.JarFileModuleLoader
import green.sailor.kython.interpreter.kyobject.KyUserModule
import green.sailor.kython.interpreter.pyobject.PyNone
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.function.PyBuiltinFunction

private fun wrap(name: String, fn: () -> PyObject) =
    PyBuiltinFunction
        .wrap(name, PyCallableSignature.EMPTY) { fn() }

private val loadSys = wrap("__load_kython_sys") { SysModule }
// todo
private val loadImp = wrap("__load_imp") { PyNone }

private val loadBootstrapExternal = wrap("__load_bootstrap_external") {
    JarFileModuleLoader.getClasspathModule(
        "Lib/importlib/_bootstrap_external", "importlib._bootstrap_external"
    )
}
private val loadBootstrap = wrap("__load_bootstrap") {
    JarFileModuleLoader.getClasspathModule(
        "Lib/importlib/_bootstrap", "importlib._bootstrap"
    )
}

/**
 * Adds bootstrap functions to the bootstrap module.
 */
internal fun addBootstrapFunctions(bootstrap: KyUserModule) {
    bootstrap.attribs.apply {
        put("__load_kython_sys", loadSys)
        put("__load_imp", loadImp)
        put("__load_bootstrap_external", loadBootstrapExternal)
        put("__load_bootstrap", loadBootstrap)
    }
}
