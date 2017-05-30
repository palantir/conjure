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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;

public enum EndpointDefinitionValidator implements ConjureValidator<EndpointDefinition> {
    ARGUMENT_TYPE(new NonBodyArgumentTypeValidator()),
    SINGLE_BODY_PARAM(new SingleBodyParamValidator()),
    PATH_PARAM(new PathParamValidator()),
    NO_GET_BODY_VALIDATOR(new NoGetBodyParamValidator()),
    PARAMETER_NAME(new ParameterNameValidator()),
    PARAM_ID(new ParamIdValidator());

    private final ConjureValidator<EndpointDefinition> validator;

    EndpointDefinitionValidator(ConjureValidator<EndpointDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EndpointDefinition definition) {
        validator.validate(definition);
    }

    private static final class NonBodyArgumentTypeValidator implements ConjureValidator<EndpointDefinition> {
        private static final ImmutableSet<Class<? extends ConjureType>> ILLEGAL_NON_BODY_ARG_TYPES
                = ImmutableSet.of(BinaryType.class);

        private static boolean isIllegal(ConjureType type) {
            return ILLEGAL_NON_BODY_ARG_TYPES.stream()
                    .filter(illegalClass -> illegalClass.isAssignableFrom(type.getClass()))
                    .count() > 0;
        }

        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().orElse(ImmutableMap.of())
                    .values()
                    .stream()
                    .filter(arg -> !arg.paramType().equals(ArgumentDefinition.ParamType.BODY))
                    .map(ArgumentDefinition::type)
                    .forEach(type -> checkArgument(
                            !isIllegal(type),
                            "Endpoint cannot have non-body argument with type '%s'",
                            type));
        }
    }

    private static final class SingleBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            List<Map.Entry<ParameterName, ArgumentDefinition>> bodyParams = definition.argsWithAutoDefined()
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
            Set<ParameterName> pathParamIds = new HashSet<>();
            definition.argsWithAutoDefined().orElse(ImmutableMap.of()).entrySet().stream()
                    .filter(entry -> entry.getValue().paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(entry -> {
                        ParameterName paramId = entry.getValue().paramId().orElse(entry.getKey());
                        boolean added = pathParamIds.add(paramId);
                        Preconditions.checkState(added,
                                "Path parameter with identifier \"%s\" is defined multiple times for endpoint",
                                paramId.name());
                    });

            Set<ParameterName> extraParams = Sets.difference(pathParamIds, definition.http().pathArgs());
            Preconditions.checkState(extraParams.isEmpty(),
                    "Path parameters defined in endpoint but not present in path template: " + extraParams);

            Set<ParameterName> missingParams = Sets.difference(definition.http().pathArgs(), pathParamIds);
            Preconditions.checkState(missingParams.isEmpty(),
                    "Path parameters defined path template but not present in endpoint: " + missingParams);
        }
    }

    private static final class ParameterNameValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.argsWithAutoDefined().orElse(ImmutableMap.of()).keySet().forEach(name -> {
                Matcher matcher = ParameterName.ANCHORED_PATTERN.matcher(name.name());
                Preconditions.checkState(matcher.matches(),
                        "Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                        ParameterName.ANCHORED_PATTERN,
                        name);
            });
        }
    }

    private static final class ParamIdValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.argsWithAutoDefined().orElse(ImmutableMap.of()).forEach((name, arg) -> {
                final Pattern pattern;
                switch (arg.paramType()) {
                    case AUTO:
                    case BODY:
                    case PATH:
                    case QUERY:
                        pattern = ParameterName.ANCHORED_PATTERN;
                        break;
                    case HEADER:
                        pattern = ParameterName.HEADER_PATTERN;
                        break;
                    default:
                        throw new IllegalStateException("Validation for paramType does not exist: " + arg.paramType());
                }
                ParameterName paramId = arg.paramId().orElse(name);
                Preconditions.checkState(pattern.matcher(paramId.name()).matches(),
                        "Parameter ids with type %s must match pattern %s: %s", arg.paramType(), pattern, paramId);
            });
        }
    }
}
