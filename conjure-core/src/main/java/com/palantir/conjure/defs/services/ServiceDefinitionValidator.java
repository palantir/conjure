/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureValidator;
import java.util.Collection;

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

    private static final class UniquePathMethodsValidator implements ConjureValidator<ServiceDefinition> {
        @Override
        public void validate(ServiceDefinition definition) {
            Multimap<String, String> pathToEndpoints = ArrayListMultimap.create();
            definition.endpoints().entrySet().stream().forEach(entry -> {
                String methodPath = entry.getValue().http().method() + " " + entry.getValue().http().path().toString();
                pathToEndpoints.put(methodPath, entry.getKey());
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
