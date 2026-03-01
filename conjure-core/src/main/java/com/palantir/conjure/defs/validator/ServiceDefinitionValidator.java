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

package com.palantir.conjure.defs.validator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.spec.ServiceDefinition;
import java.util.Collection;
import java.util.regex.Pattern;

@com.google.errorprone.annotations.Immutable
public enum ServiceDefinitionValidator implements ConjureValidator<ServiceDefinition> {
    UNIQUE_PATH_METHODS(new UniquePathMethodsValidator()),
    ILLEGAL_SUFFIXES(new IllegalSuffixesValidator());

    private final ConjureValidator<ServiceDefinition> validator;

    public static void validateAll(ServiceDefinition definition) {
        for (ServiceDefinitionValidator validator : ServiceDefinitionValidator.values()) {
            validator.validate(definition);
        }
    }

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
            definition.getEndpoints().forEach(entry -> {
                String methodPath =
                        entry.getHttpMethod().get() + " " + entry.getHttpPath().get();
                // normalize all path parameter variables and regular expressions because all path args are treated
                // as identical for comparisons (paths cannot differ only in the name/regular expression of a path
                // variable)
                methodPath = PATHVAR_PATTERN.matcher(methodPath).replaceAll("{arg}");
                pathToEndpoints.put(methodPath, entry.getEndpointName().get());
            });

            pathToEndpoints.keySet().stream().sorted().forEachOrdered(key -> {
                Collection<String> endpoints = pathToEndpoints.get(key);
                Preconditions.checkState(
                        endpoints.size() <= 1,
                        "Endpoint \"%s\" is defined by multiple endpoints: %s",
                        key,
                        endpoints.toString());
            });
        }
    }

    /**
     * This ensures that ExperimentalFeatures.DisambiguateRetrofitServices won't cause collisions.
     */
    @com.google.errorprone.annotations.Immutable
    private static final class IllegalSuffixesValidator implements ConjureValidator<ServiceDefinition> {
        private static final String RETROFIT_SUFFIX = "Retrofit";

        @Override
        public void validate(ServiceDefinition definition) {
            Preconditions.checkState(
                    !definition.getServiceName().getName().endsWith(RETROFIT_SUFFIX),
                    "Service name must not end in %s: %s",
                    RETROFIT_SUFFIX,
                    definition.getServiceName().getName());
        }
    }
}
