/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspired by {@link org.junit.runners.BlockJUnit4ClassRunner}, except instead of a test 'method' being the unit of
 * work, it's a 'directory'.
 *
 * Note, this doesn't support @Rule or @ClassRule.
 */
public final class ConjureSubfolderRunner extends ParentRunner<Path> {
    private static final Logger log = LoggerFactory.getLogger(ConjureSubfolderRunner.class);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ParentFolder {
        /** Parent folder to list. */
        String value();
        /** Whether tests should be executed indepenently on a CachedThreadPool. */
        boolean parallel() default false;
    }

    /**
     * Use this annotation to tell {@link ConjureSubfolderRunner} to execute your test method for every subfolder
     * it finds inside the {@link ParentFolder} you specified.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Test {}

    private final Path parent;
    private final FrameworkMethod onlyMethod;

    public ConjureSubfolderRunner(Class<?> klass) throws InitializationError {
        super(klass);
        ParentFolder annotation = getParentFolder(getTestClass().getAnnotation(ParentFolder.class));
        parent = Paths.get(annotation.value());
        onlyMethod = validateMethod(getTestClass().getAnnotatedMethods(ConjureSubfolderRunner.Test.class));
        maybeParallelScheduler(annotation).ifPresent(this::setScheduler);
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

    private ParentFolder getParentFolder(ParentFolder annotation) {
        if (annotation == null) {
            throw new RuntimeException("The class must be annotated with @ParentFolder");
        }

        return annotation;
    }

    private FrameworkMethod validateMethod(List<FrameworkMethod> annotated) {
        if (annotated.size() != 1) {
            throw new RuntimeException("There must be exactly one @ConjureSubfolderRunner.Test method");
        }

        FrameworkMethod method = annotated.get(0);

        if (!method.isPublic()) {
            throw new RuntimeException("@ConjureSubfolderRunner.Test method must be public: " + method);
        }

        Class<?>[] parameters = method.getMethod().getParameterTypes();
        if (parameters.length != 1 || parameters[0] != Path.class) {
            throw new RuntimeException(
                    "@ConjureSubfolderRunner.Test method must have exactly one parameter (java.nio.file.Path): "
                            + method);
        }

        return method;
    }

    private Optional<RunnerScheduler> maybeParallelScheduler(ParentFolder annotation) {
        if (!annotation.parallel()) {
            return Optional.empty();
        }

        return Optional.of(new RunnerScheduler() {
            private final ExecutorService executor = Executors.newCachedThreadPool();

            @Override
            @SuppressWarnings("FutureReturnValueIgnored")
            public void schedule(Runnable childStatement) {
                executor.submit(childStatement);
            }

            @Override
            public void finished() {
                try {
                    executor.shutdown();
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("Parallel executor interrupted during shutdown", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
