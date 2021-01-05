/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.conjure.cli;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

@SuppressWarnings({"CatchAndPrintStackTrace", "JdkObsolete"})
@Platforms(Platform.HOSTED_ONLY.class)
final class RuntimeReflectionRegistrationFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Common standard library classes
        registerClassForReflection(String.class);
        registerClassForReflection(HashSet.class);
        registerClassForReflection(HashMap.class);
        registerClassForReflection(LinkedHashMap.class);
        registerClassForReflection(ArrayList.class);
        registerClassForReflection(LinkedList.class);
        registerClassForReflection(ConcurrentHashMap.class);
        for (Path jarPath : access.getApplicationClassPath()) {
            String jarFileName = jarPath.getFileName().toString();
            if (!jarFileName.endsWith(".jar")) {
                // Only scan jars
                continue;
            }
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class")
                            // Exclude multirelease files, the default jvm version class name is sufficient.
                            && !name.startsWith("META-INF")
                            // Exclude graal components
                            && !name.contains("com/oracle/svm")
                            // Includes incorrect repackaging with malformed signatures. These break the build.
                            && !name.contains("repackaged")
                            && !name.contains("shaded")
                            && !name.contains("glassfish")
                            && !name.startsWith("javax")
                            && !name.startsWith("jakarta")) {
                        String className = name.replace("/", ".").substring(0, name.length() - 6);
                        try {
                            registerClassForReflection(access.findClassByName(className));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private static void registerClassForReflection(Class<?> clazz) {
        if (clazz != null) {
            try {
                if (!isAnyElementGraalAnnotated(clazz)) {
                    RuntimeReflection.register(clazz);
                    RuntimeReflection.register(clazz.getDeclaredMethods());
                    RuntimeReflection.register(clazz.getDeclaredConstructors());
                    RuntimeReflection.register(clazz.getDeclaredFields());
                }
            } catch (NoClassDefFoundError e) {
                System.err.printf("NoClassDefFoundError: %s While inspecting %s\n", e.getMessage(), clazz.getName());
            } catch (Throwable t) {
                t.printStackTrace();
                // ignored
            }
        }
    }

    private static boolean isAnyElementGraalAnnotated(Class<?> clazz) {
        try {
            return isGraalAnnotated(clazz)
                    || isAnyGraalAnnotated(clazz.getDeclaredMethods())
                    || isAnyGraalAnnotated(clazz.getDeclaredConstructors())
                    || isAnyGraalAnnotated(clazz.getDeclaredFields());
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isAnyGraalAnnotated(AnnotatedElement[] elements) {
        for (AnnotatedElement element : elements) {
            if (isGraalAnnotated(element)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGraalAnnotated(AnnotatedElement element) {
        return element.isAnnotationPresent(Platforms.class);
    }
}
