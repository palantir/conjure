/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.core.HttpHeaders;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.immutables.value.Value;

/**
 * Looks something like the following.
 *
 * @palantir:registry=https://artifactory.palantir.build:443/artifactory/api/npm/all-npm/
 * //artifactory.palantir.build:443/artifactory/api/npm/all-npm/:_password=REDACTED_PASSWORD
 * //artifactory.palantir.build:443/artifactory/api/npm/all-npm/:username=REDACTED_USERNAME
 * //artifactory.palantir.build:443/artifactory/api/npm/all-npm/:email=EMAIL
 * //artifactory.palantir.build:443/artifactory/api/npm/all-npm/:always-auth=true
 */
@Value.Immutable
public abstract class Npmrc {
    public static final String FILENAME = ".npmrc";

    @Value.Parameter
    public abstract String get();

    @SuppressWarnings("checkstyle:designforextension")
    public static Npmrc of(String string) {
        return ImmutableNpmrc.of(string);
    }

    @Override
    public final String toString() {
        return get();
    }

    public static final Npmrc fromArtifactoryCreds(
            String registryUri, Scope scope, String username, String password) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder().build();
        String userPass = String.format("%s:%s", username, password);
        String base64UserPass = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
        String npmRegistryUriWithScope = String.format("%s/auth/%s", registryUri, scope);

        Request request = new Request.Builder()
                .url(npmRegistryUriWithScope)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + base64UserPass)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Call to artifactory failed:" + response.code());
        }
        return Npmrc.of(response.body().string());
    }
}
