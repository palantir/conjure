/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.palantir.conjure.parser.types.ObjectsDefinition;
import com.palantir.conjure.parser.types.TypesDefinition;
import java.util.Optional;

public final class ConjureMetrics {

    private ConjureMetrics() {}

    public static void incrementCounter(Class<?> clazz, String... names) {
        Optional.ofNullable(SharedMetricRegistries.tryGetDefault()).ifPresent(metrics ->
                metrics.counter(MetricRegistry.name(clazz, names)).inc());
    }

    public static void histogram(int value, Class<?> clazz, String... names) {
        Optional.ofNullable(SharedMetricRegistries.tryGetDefault()).ifPresent(metrics ->
                metrics.histogram(MetricRegistry.name(clazz, names)).update(value));
    }

    public static void recordMetrics(ConjureDefinition definition) {
        Optional.ofNullable(SharedMetricRegistries.tryGetDefault()).ifPresent(metrics -> {
            metrics.counter(MetricRegistry.name(TypesDefinition.class, "conjure-imports"))
                    .inc(definition.types().conjureImports().size());

            metrics.counter(MetricRegistry.name(TypesDefinition.class, "imports"))
                    .inc(definition.types().imports().size());

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "services"))
                    .inc(definition.services().size());

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "objects"))
                    .inc(definition.types().definitions().objects().size());

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "empty-default-conjure-package"))
                    .inc(definition.types().definitions().defaultConjurePackage().isPresent() ? 0 : 1);

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "errors"))
                    .inc(definition.types().definitions().errors().size());
        });
    }
}
