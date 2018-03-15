/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureValidator;
import java.util.Collection;
import java.util.regex.Pattern;

@com.google.errorprone.annotations.Immutable
public enum ServiceDefinitionValidator implements ConjureValidator<ServiceDefinition> {
    UNIQUE_PATH_METHODS(new UniquePathMethodsValidator());

    private final ConjureValidator<ServiceDefinition> validator;

    ServiceDefinitionValidator(ConjureValidator<ServiceDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ServiceDefinition definition) {
        validator.validate(definition);
    }

    // The ? is for reluctant matching, i.e. matching as few characters as possible.
    private static final Pattern PATHVAR_PATTERN = Pattern.compile(Pattern.quote("{") + ".+?" + Pattern.quote("}"));

    @com.google.errorprone.annotations.Immutable
    private static final class UniquePathMethodsValidator implements ConjureValidator<ServiceDefinition> {
        @Override
        public void validate(ServiceDefinition definition) {
            Multimap<String, String> pathToEndpoints = ArrayListMultimap.create();
            definition.endpoints().stream().forEach(entry -> {
                String methodPath = entry.httpMethod() + " " + entry.httpPath().toString();
                // normalize all path parameter variables and regular expressions because all path args are treated
                // as identical for comparisons (paths cannot differ only in the name/regular expression of a path
                // variable)
                methodPath = PATHVAR_PATTERN.matcher(methodPath).replaceAll("{arg}");
                pathToEndpoints.put(methodPath, entry.endpointName().name());
            });

            pathToEndpoints.keySet().stream().sorted().forEachOrdered(key -> {
                Collection<String> endpoints = pathToEndpoints.get(key);
                Preconditions.checkState(endpoints.size() <= 1,
                        "Endpoint \"%s\" is defined by multiple endpoints: %s",
                        key, endpoints.toString());
            });
        }
    }
}
