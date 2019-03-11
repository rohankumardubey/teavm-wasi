/*
 *  Copyright 2019 Alexey Andreev.
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
package org.teavm.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.teavm.common.Mapper;
import org.teavm.model.ClassHolder;
import org.teavm.model.ClassReader;
import org.teavm.model.ClassReaderSource;
import org.teavm.model.MethodReference;
import org.teavm.model.ReferenceCache;

public class MemoryCachedClassReaderSource implements ClassReaderSource, CacheStatus {
    private Map<String, Entry> cache = new HashMap<>();
    private Mapper<String, ClassHolder> mapper;
    private ClassIO classIO;
    private final Set<String> freshClasses = new HashSet<>();

    public MemoryCachedClassReaderSource(ReferenceCache referenceCache, SymbolTable symbolTable,
            SymbolTable fileTable, SymbolTable varTable) {
        classIO = new ClassIO(referenceCache, symbolTable, fileTable, varTable);
    }

    public void setMapper(Mapper<String, ClassHolder> mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isStaleClass(String className) {
        return !freshClasses.contains(className);
    }

    @Override
    public boolean isStaleMethod(MethodReference method) {
        return isStaleClass(method.getClassName());
    }

    @Override
    public ClassReader get(String name) {
        Entry entry = cache.computeIfAbsent(name, className -> {
            ClassHolder cls = mapper.map(name);
            Entry en = new Entry();
            if (cls != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try {
                    classIO.writeClass(output, cls);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                en.data = output.toByteArray();
                en.reader = new WeakReference<>(cls);
            }
            return en;
        });

        if (entry.data == null) {
            return null;
        }

        ClassReader cls = entry.reader.get();
        if (cls == null) {
            ByteArrayInputStream input = new ByteArrayInputStream(entry.data);
            try {
                cls = classIO.readClass(input, name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            entry.reader = new WeakReference<>(cls);
        }
        return cls;
    }

    public void commit() {
        freshClasses.addAll(cache.keySet());
    }

    public void evict(Collection<? extends String> classes) {
        cache.keySet().removeAll(classes);
        freshClasses.removeAll(classes);
    }

    public void invalidate() {
        cache.clear();
        freshClasses.clear();
    }

    class Entry {
        byte[] data;
        WeakReference<ClassReader> reader;
    }
}