/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Inspired by {@link org.junit.runners.BlockJUnit4ClassRunner}, except instead of a test 'method' being the unit of
 * work, it's a 'directory'.
 *
 * Note, this doesn't support @Rule or @ClassRule.
 */
public final class ConjureSubfolderRunner extends ParentRunner<Path> {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ParentFolder {
        String value();
    }

    private final Path parent;
    private final FrameworkMethod onlyMethod;

    public ConjureSubfolderRunner(Class<?> klass) throws InitializationError {
        super(klass);
        parent = getParentFolder(getTestClass().getAnnotation(ParentFolder.class));
        onlyMethod = validateMethod(getTestClass().getAnnotatedMethods(Test.class));
    }

    @Override
    protected List<Path> getChildren() {
        return Arrays.stream(parent.toFile().listFiles())
                .filter(File::isDirectory)
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    @Override
    protected Description describeChild(Path child) {
        return Description.createTestDescription(getName(), child.toString());
    }

    @Override
    protected void runChild(Path child, RunNotifier notifier) {
        Description description = describeChild(child);
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            onlyMethod.invokeExplosively(createTestClassInstance(), child);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    private Object createTestClassInstance()
            throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        return getTestClass().getOnlyConstructor().newInstance();
    }

    private Path getParentFolder(ParentFolder annotated) {
        if (annotated == null) {
            throw new RuntimeException("The class must be annotated with @ParentFolder");
        }

        return Paths.get(annotated.value());
    }

    private FrameworkMethod validateMethod(List<FrameworkMethod> annotated) {
        if (annotated.size() != 1) {
            throw new RuntimeException("There must be exactly one @Test method");
        }

        FrameworkMethod method = annotated.get(0);

        if (!method.isPublic()) {
            throw new RuntimeException("@Test method must be public: " + method);
        }

        Class<?>[] parameters = method.getMethod().getParameterTypes();
        if (parameters.length != 1 || parameters[0] != Path.class) {
            throw new RuntimeException("@Test method must have exactly one parameter (java.nio.file.Path): " + method);
        }

        return method;
    }
}
