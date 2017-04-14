/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;

public enum EndpointDefinitionValidator implements ConjureValidator<EndpointDefinition> {
    ARGUMENT_TYPE(new ArgumentTypeValidator()),
    SINGLE_BODY_PARAM(new SingleBodyParamValidator()),
    PATH_PARAM(new PathParamValidator()),
    NO_GET_BODY_VALIDATOR(new NoGetBodyParamValidator()),
    ;

    private final ConjureValidator<EndpointDefinition> validator;

    EndpointDefinitionValidator(ConjureValidator<EndpointDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EndpointDefinition definition) {
        validator.validate(definition);
    }


    private static final class ArgumentTypeValidator implements ConjureValidator<EndpointDefinition> {
        private static final Set<Class<? extends ConjureType>> ILLEGAL_ARG_TYPES = ImmutableSet.of(BinaryType.class);

        private static boolean isIllegal(ConjureType type) {
            return ILLEGAL_ARG_TYPES.stream()
                    .filter(illegalClass -> illegalClass.isAssignableFrom(type.getClass()))
                    .count() > 0;
        }

        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().orElse(ImmutableMap.of())
                    .values()
                    .stream()
                    .map(ArgumentDefinition::type)
                    .forEach(type -> checkArgument(
                            !isIllegal(type),
                            "Endpoint cannot have argument with type '%s'",
                            type));
        }
    }

    private static final class SingleBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            List<Map.Entry<String, ArgumentDefinition>> bodyParams = definition.argsWithAutoDefined()
                    .orElse(ImmutableMap.of()).entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().paramType().equals(ArgumentDefinition.ParamType.BODY))
                    .collect(Collectors.toList());

            Preconditions.checkState(bodyParams.size() <= 1,
                    "Endpoint cannot have multiple body parameters: " + bodyParams.stream().map(
                            e -> e.getKey()).collect(
                            Collectors.toList()));
        }
    }

    private static final class NoGetBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            String method = definition.http().method();
            if (method.equals(HttpMethod.GET)) {
                boolean hasBody = definition.argsWithAutoDefined()
                        .orElse(ImmutableMap.of()).entrySet()
                        .stream()
                        .anyMatch(entry -> entry.getValue().paramType().equals(ArgumentDefinition.ParamType.BODY));

                Preconditions.checkState(!hasBody, "Endpoint cannot be a GET and contain a body: %s",
                        definition.http());
            }
        }
    }

    private static final class PathParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            Set<String> pathParamIds = new HashSet<>();
            definition.argsWithAutoDefined().orElse(ImmutableMap.of()).entrySet().stream()
                    .filter(entry -> entry.getValue().paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(entry -> {
                        String paramId = entry.getValue().paramId().orElse(entry.getKey());
                        boolean added = pathParamIds.add(paramId);
                        Preconditions.checkState(added,
                                "Path parameter with identifier \"" + paramId
                                        + "\" is defined multiple times for endpoint");

                    });

            Set<String> extraParams = Sets.difference(pathParamIds, definition.http().pathArgs());
            Preconditions.checkState(extraParams.isEmpty(),
                    "Path parameters defined in endpoint but not present in path template: " + extraParams);

            Set<String> missingParams = Sets.difference(definition.http().pathArgs(), pathParamIds);
            Preconditions.checkState(missingParams.isEmpty(),
                    "Path parameters defined path template but not present in endpoint: " + missingParams);
        }
    }

}
