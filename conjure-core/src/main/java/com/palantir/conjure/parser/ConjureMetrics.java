/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "types"))
                    .inc(definition.types().definitions().objects().size());

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "empty-default-conjure-package"))
                    .inc(definition.types().definitions().defaultConjurePackage().isPresent() ? 0 : 1);

            metrics.counter(MetricRegistry.name(ObjectsDefinition.class, "errors"))
                    .inc(definition.types().definitions().errors().size());
        });
    }
}
