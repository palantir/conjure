/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;

public enum ExperimentalFeatures {

    /** Enables conjure Union types in the {@link BeanGenerator}. */
    UnionTypes,

    /**
     * Instructs the {@link Retrofit2ServiceGenerator} to generate service
     * endpoints returning {@link java.util.concurrent.CompletableFuture} instead of {@code Call<>} objects.
     */
    RetrofitCompletableFutures
}
