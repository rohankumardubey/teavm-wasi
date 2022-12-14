/*
 *  Copyright 2017 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.fs;

import org.teavm.classlib.PlatformDetector;
import org.teavm.classlib.fs.c.CFileSystem;
import org.teavm.classlib.fs.memory.InMemoryVirtualFileSystem;
import org.teavm.classlib.fs.wasi.WasiFileSystem;

public final class VirtualFileSystemProvider {
    private static VirtualFileSystem instance;

    static {
        if (PlatformDetector.isC()) {
            instance = new CFileSystem();
        } else if (PlatformDetector.isWebAssembly()) {
            instance = new WasiFileSystem();
        } else {
            instance = new InMemoryVirtualFileSystem();
        }
    }

    private VirtualFileSystemProvider() {
    }

    public static VirtualFileSystem getInstance() {
        return instance;
    }

    public static void setInstance(VirtualFileSystem instance) {
        VirtualFileSystemProvider.instance = instance;
    }
}
