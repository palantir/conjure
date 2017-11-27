/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.types.ObjectGenerator;

public enum ExperimentalFeatures {

    /**
     * Instructs the {@link Retrofit2ServiceGenerator} to generate service
     * endpoints returning {@link java.util.concurrent.CompletableFuture} instead of {@code Call<>} objects.
     */
    RetrofitCompletableFutures,

    /**
     * Enables conjure error types in the {@link ObjectGenerator}.
     * YML files defining {@code types.definition.errors} will generate
     * static methods to throw remoting-api compatible ServiceExceptions. */
    ErrorTypes,

    /**
     * Allow markers on service methods. This feature exists for gotham to migrate to conjure and will be removed once
     * migration is complete.
     *
     * @see <a href="https://github.palantir.build/foundry/conjure/issues/708">708</a>
     */
    DangerousGothamMethodMarkers,

    /**
     * Feature for gotham support.
     * @see <a href="https://github.palantir.build/foundry/conjure/issues/708">708</a>
     */
    DangerousGothamSerializableBeans,

    /**
     * Feature for gotham support.
     * Allows jax-rs interface methods to return <pre>InputStream</pre> objects
     * as opposed to <pre>StreamingOutput</pre>.
     * @see <a href="https://github.palantir.build/foundry/conjure/issues/708">708</a>
     */
    DangerousGothamJerseyBinaryReturnInputStream,

}
