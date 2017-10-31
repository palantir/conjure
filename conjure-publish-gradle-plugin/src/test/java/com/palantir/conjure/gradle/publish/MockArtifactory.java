/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public final class MockArtifactory {

    public static void enqueueNpmrcBody(MockWebServer server, String npmrc) {
        server.enqueue(new MockResponse().setBody(npmrc));
    }

    public static void enqueueNoopPublish(MockWebServer server) {
        // npm publish makes two requests to the registry
        server.enqueue(new MockResponse());
        server.enqueue(new MockResponse());
    }

    private MockArtifactory() {}

}
