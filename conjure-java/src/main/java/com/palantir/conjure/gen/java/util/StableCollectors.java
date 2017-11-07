/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StableCollectors {

    private StableCollectors() {}

    /**
     * Ensures the forEach order will be preserved, unlike the regular {@link Collectors#toMap}.
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {

        return Collectors.toMap(keyMapper, valueMapper,
                (first, second) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", first));
                },
                LinkedHashMap::new);
    }
}
