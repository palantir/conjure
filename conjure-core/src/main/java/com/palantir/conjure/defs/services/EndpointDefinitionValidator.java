/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;

@com.google.errorprone.annotations.Immutable
public enum EndpointDefinitionValidator implements ConjureValidator<EndpointDefinition> {
    ARGUMENT_TYPE(new NonBodyArgumentTypeValidator()),
    SINGLE_BODY_PARAM(new SingleBodyParamValidator()),
    PATH_PARAM(new PathParamValidator()),
    NO_BEARER_TOKEN_PATH_PARAMS(new NoBearerTokenPathParams()),
    NO_COMPLEX_PATH_PARAMS(new NoComplexPathParamValidator()),
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

    @com.google.errorprone.annotations.Immutable
    private static final class NonBodyArgumentTypeValidator implements ConjureValidator<EndpointDefinition> {
        private static final ImmutableSet<Class<? extends Type>> ILLEGAL_NON_BODY_ARG_TYPES
                = ImmutableSet.of(BinaryType.class);

        private static boolean isIllegal(Type type) {
            return ILLEGAL_NON_BODY_ARG_TYPES.stream()
                    .filter(illegalClass -> illegalClass.isAssignableFrom(type.getClass()))
                    .count() > 0;
        }

        @Override
        public void validate(EndpointDefinition definition) {
            definition.args()
                    .stream()
                    .filter(arg -> !arg.paramType().equals(ArgumentDefinition.ParamType.BODY))
                    .map(ArgumentDefinition::type)
                    .forEach(type -> checkArgument(
                            !isIllegal(type),
                            "Endpoint cannot have non-body argument with type '%s'",
                            type));
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class SingleBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            List<ArgumentDefinition> bodyParams = definition.args()
                    .stream()
                    .filter(entry -> entry.paramType().equals(ArgumentDefinition.ParamType.BODY))
                    .collect(Collectors.toList());

            Preconditions.checkState(bodyParams.size() <= 1,
                    "Endpoint cannot have multiple body parameters: %s",
                    bodyParams.stream().map(e -> e.argName()).collect(Collectors.toList()));
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoGetBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            String method = definition.http().method();
            if (method.equals(HttpMethod.GET)) {
                boolean hasBody = definition.args()
                        .stream()
                        .anyMatch(entry -> entry.paramType().equals(ArgumentDefinition.ParamType.BODY));

                Preconditions.checkState(!hasBody, "Endpoint cannot be a GET and contain a body: %s",
                        definition.http());
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class PathParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            Set<ArgumentName> pathParamIds = new HashSet<>();
            definition.args().stream()
                    .filter(entry -> entry.paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(entry -> {
                        boolean added = pathParamIds.add(entry.argName());
                        Preconditions.checkState(added,
                                "Path parameter with identifier \"%s\" is defined multiple times for endpoint",
                                entry.argName().name());
                    });

            Set<ArgumentName> extraParams = Sets.difference(pathParamIds, definition.http().pathArgs());
            Preconditions.checkState(extraParams.isEmpty(),
                    "Path parameters defined in endpoint but not present in path template: %s", extraParams);

            Set<ArgumentName> missingParams = Sets.difference(definition.http().pathArgs(), pathParamIds);
            Preconditions.checkState(missingParams.isEmpty(),
                    "Path parameters defined path template but not present in endpoint: %s", missingParams);
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexPathParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().stream()
                    .filter(entry -> entry.paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(entry -> {
                        Type conjureType = entry.type();

                        Boolean isValid = conjureType.visit(IsPrimitiveOrReferenceType.INSTANCE);
                        Preconditions.checkState(isValid,
                                "Path parameters must be primitives or aliases: \"%s\" is not allowed",
                                entry.argName());
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoBearerTokenPathParams implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().stream()
                    .filter(entry -> entry.paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(entry -> {
                        Type conjureType = entry.type();

                        Preconditions.checkState(!conjureType.equals(PrimitiveType.BEARERTOKEN),
                                "Path parameters of type 'bearertoken' are not allowed as this "
                                        + "would introduce a security vulnerability: \"%s\"",
                                entry.argName());
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParameterNameValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().forEach(arg -> {
                Matcher matcher = ArgumentName.ANCHORED_PATTERN.matcher(arg.argName().name());
                Preconditions.checkState(matcher.matches(),
                        "Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                        ArgumentName.ANCHORED_PATTERN,
                        arg.argName().name());
            });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParamIdValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.args().forEach(arg -> {
                final Pattern pattern;
                switch (arg.paramType()) {
                    case BODY:
                    case PATH:
                    case QUERY:
                        pattern = ArgumentName.ANCHORED_PATTERN;
                        break;
                    case HEADER:
                        pattern = ParameterId.HEADER_PATTERN;
                        break;
                    default:
                        throw new IllegalStateException("Validation for paramType does not exist: " + arg.paramType());
                }

                if (arg.paramId().isPresent()) {
                    Preconditions.checkState(pattern.matcher(arg.paramId().get().name()).matches(),
                            "Parameter ids with type %s must match pattern %s: %s",
                            arg.paramType(), pattern, arg.paramId().get().name());
                }
            });
        }
    }
}
