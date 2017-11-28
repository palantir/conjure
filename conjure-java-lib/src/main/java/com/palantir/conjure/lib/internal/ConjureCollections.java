/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.lib.internal;

import java.util.Collection;

/**
 * Utility functions for conjure. Consumers should prefer to use something like
 * guava instead of using these functions directly.
 */
public final class ConjureCollections {

    private ConjureCollections() {
        // cannot instantiate
    }

    @SuppressWarnings("unchecked")
    public static <T> void addAll(Collection<T> addTo, Iterable<? extends T> elementsToAdd) {
        if (elementsToAdd instanceof Collection) {
            addTo.addAll((Collection<T>) elementsToAdd);
        } else {
            for (T element : elementsToAdd) {
                addTo.add(element);
            }
        }
    }
}
