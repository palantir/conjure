/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum ConjureDefinitionValidator implements ConjureValidator<ConjureDefinition> {
    UNIQUE_SERVICE_NAMES(new UniqueServiceNamesValidator()),
    ILLEGAL_SUFFIXES(new IllegalSuffixesValidator());

    private final ConjureValidator<ConjureDefinition> validator;

    ConjureDefinitionValidator(ConjureValidator<ConjureDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ConjureDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueServiceNamesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            Set<String> seenNames = new HashSet<>();
            definition.services().forEach(service -> {
                boolean isNewName = seenNames.add(service.serviceName().name());
                Preconditions.checkState(isNewName,
                        "Service names must be unique: %s", service.serviceName().name());
            });
        }
    }

    /**
     * This ensures that ExperimentalFeatures.DisambiguateRetrofitServices won't cause collisions.
     */
    @com.google.errorprone.annotations.Immutable
    private static final class IllegalSuffixesValidator implements ConjureValidator<ConjureDefinition> {
        private static final String RETROFIT_SUFFIX = "Retrofit";

        @Override
        public void validate(ConjureDefinition definition) {
            List<String> violations = definition.services().stream()
                    .map(def -> def.serviceName().name())
                    .filter(name -> name.endsWith(RETROFIT_SUFFIX))
                    .collect(toList());

            Preconditions.checkState(violations.isEmpty(),
                    "Service names must not end in %s: %s", RETROFIT_SUFFIX, violations);
        }
    }
}
