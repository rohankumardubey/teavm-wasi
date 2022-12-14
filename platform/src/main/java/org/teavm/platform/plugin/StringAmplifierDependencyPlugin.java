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
package org.teavm.platform.plugin;

import org.teavm.dependency.DependencyAgent;
import org.teavm.dependency.DependencyPlugin;
import org.teavm.dependency.MethodDependency;

public class StringAmplifierDependencyPlugin implements DependencyPlugin {
    @Override
    public void methodReached(DependencyAgent agent, MethodDependency method) {
        if (method.getMethod().getName().equals("amplify")) {
            method.getResult().propagate(agent.getType("java.lang.String"));
        } else {
            method.getResult().propagate(agent.getType("[Ljava/lang/String;"));
            method.getResult().getArrayItem().propagate(agent.getType("java.lang.String"));
        }
    }
}
