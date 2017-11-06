/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;

public enum ExperimentalFeatures {

    /**
     * Instructs the {@link Retrofit2ServiceGenerator} to generate service
     * endpoints returning {@link java.util.concurrent.CompletableFuture} instead of {@code Call<>} objects.
     */
    RetrofitCompletableFutures,

    /**
     * Enables conjure error types in the {@link BeanGenerator}.
     * YML files defining {@code types.definition.errors} will generate
     * static methods to throw remoting-api compatible ServiceExceptions. */
    ErrorTypes
}
