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

package green.sailor.kython.compiler.cpython.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * JNA-side implementation of libkython_bridge.
 *
 * This is very minimal as libkython_bridge only exposes a single function (right now).
 */
public interface LibkythonBridge extends Library {
    LibkythonBridge libkython = Native.load("kython_bridge", LibkythonBridge.class);

    String kyc_compile(String code, String filename);
}
