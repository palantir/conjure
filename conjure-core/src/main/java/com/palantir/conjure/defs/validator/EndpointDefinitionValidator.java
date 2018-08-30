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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.DealiasingTypeVisitor;
import com.palantir.conjure.either.Either;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.visitor.ParameterTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@com.google.errorprone.annotations.Immutable
public enum EndpointDefinitionValidator implements ConjureContextualValidator<EndpointDefinition> {
    ARGUMENT_TYPE(new NonBodyArgumentTypeValidator()),
    SINGLE_BODY_PARAM(new SingleBodyParamValidator()),
    PATH_PARAM(new PathParamValidator()),
    NO_BEARER_TOKEN_PATH_PARAMS(new NoBearerTokenPathParams()),
    NO_COMPLEX_PATH_PARAMS(new NoComplexPathParamValidator()),
    NO_COMPLEX_HEADER_PARAMS(new NoComplexHeaderParamValidator()),
    NO_GET_BODY_VALIDATOR(new NoGetBodyParamValidator()),
    PARAMETER_NAME(new ParameterNameValidator()),
    PARAM_ID(new ParamIdValidator());

    public static void validateAll(EndpointDefinition definition, DealiasingTypeVisitor dealiasingVisitor) {
        for (EndpointDefinitionValidator validator : values()) {
            validator.validate(definition, dealiasingVisitor);
        }
    }

    public static final String PATTERN = "[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*";
    public static final Pattern ANCHORED_PATTERN = Pattern.compile("^" + PATTERN + "$");
    public static final Pattern HEADER_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*(-[A-Z][a-zA-Z0-9]*)*$");

    private final ConjureContextualValidator<EndpointDefinition> validator;

    /**
     * Simplified constructor for validators that don't need to look at the context.
     */
    EndpointDefinitionValidator(ConjureValidator<EndpointDefinition> validator) {
        this.validator = (definition, dealiasingTypeVisitor) -> validator.validate(definition);
    }

    EndpointDefinitionValidator(ConjureContextualValidator<EndpointDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
        validator.validate(definition, dealiasingTypeVisitor);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NonBodyArgumentTypeValidator implements ConjureValidator<EndpointDefinition> {
        private static final ImmutableSet<PrimitiveType.Value> ILLEGAL_NON_BODY_ARG_TYPES
                = ImmutableSet.of(PrimitiveType.Value.BINARY);

        private static boolean isIllegal(Type type) {
            return ILLEGAL_NON_BODY_ARG_TYPES.stream()
                    .filter(illegalClass -> type.accept(TypeVisitor.IS_BINARY)).count() > 0;
        }

        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs()
                    .stream()
                    .filter(arg -> !(arg.getParamType().accept(ParameterTypeVisitor.IS_BODY)))
                    .map(ArgumentDefinition::getType)
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
            List<ArgumentDefinition> bodyParams = definition.getArgs()
                    .stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_BODY))
                    .collect(Collectors.toList());

            Preconditions.checkState(bodyParams.size() <= 1,
                    "Endpoint cannot have multiple body parameters: %s",
                    bodyParams.stream().map(e -> e.getArgName()).collect(Collectors.toList()));
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoGetBodyParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            HttpMethod method = definition.getHttpMethod();
            if (method.equals(HttpMethod.GET)) {
                boolean hasBody = definition.getArgs()
                        .stream()
                        .anyMatch(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_BODY));

                Preconditions.checkState(!hasBody,
                        "Endpoint cannot be a GET and contain a body: method: %s, path: %s",
                        definition.getHttpMethod(),
                        definition.getHttpPath());
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class PathParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            Set<ArgumentName> pathParamIds = new HashSet<>();
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH))
                    .forEach(entry -> {
                        boolean added = pathParamIds.add(entry.getArgName());
                        Preconditions.checkState(added,
                                "Path parameter with identifier \"%s\" is defined multiple times for endpoint",
                                entry.getArgName().get());
                    });

            Set<ArgumentName> pathArgs = HttpPathValidator.pathArgs(definition.getHttpPath().get());
            Set<ArgumentName> extraParams = Sets.difference(pathParamIds, pathArgs);
            Preconditions.checkState(extraParams.isEmpty(),
                    "Path parameters defined in endpoint but not present in path template: %s. "
                            + "Note that the `param-id` is no longer supported and the path template name is always "
                            + "used instead. So make sure the path template name matches the path parameter defined "
                            + "in endpoint.", extraParams);

            Set<ArgumentName> missingParams = Sets.difference(pathArgs, pathParamIds);
            Preconditions.checkState(missingParams.isEmpty(),
                    "Path parameters defined path template but not present in endpoint: %s", missingParams);
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexPathParamValidator implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH))
                    .forEach(entry -> {
                        Either<TypeDefinition, Type> resolvedType = entry.getType().accept(dealiasingTypeVisitor);

                        Boolean isValid = resolvedType.fold(
                                typeDefinition -> typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                                type -> type.accept(TypeVisitor.IS_PRIMITIVE) && !type.accept(TypeVisitor.IS_ANY));
                        Preconditions.checkState(isValid,
                                "Path parameters must be primitives or aliases: \"%s\" is not allowed",
                                entry.getArgName());
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexHeaderParamValidator implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_HEADER))
                    .forEach(entry -> {
                        Either<TypeDefinition, Type> resolvedType = entry.getType().accept(dealiasingTypeVisitor);

                        boolean isValid = resolvedType.fold(
                                typeDefinition -> typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                                type -> {
                                    boolean isDefinedPrimitive = type.accept(TypeVisitor.IS_PRIMITIVE)
                                            && !type.accept(TypeVisitor.IS_ANY);
                                    boolean isOptionalPrimitive = type.accept(TypeVisitor.IS_OPTIONAL)
                                            && type.accept(TypeVisitor.OPTIONAL)
                                            .getItemType()
                                            .accept(TypeVisitor.IS_PRIMITIVE);
                                    return isDefinedPrimitive || isOptionalPrimitive;
                                }
                        );
                        Preconditions.checkState(isValid,
                                "Header parameters must be primitives, aliases or optional primitive:"
                                        + " \"%s\" is not allowed",
                                entry.getArgName());
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoBearerTokenPathParams implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH))
                    .forEach(entry -> {
                        Type conjureType = entry.getType();

                        Preconditions.checkState(!conjureType.accept(TypeVisitor.IS_PRIMITIVE)
                                || conjureType.accept(TypeVisitor.PRIMITIVE).get() != PrimitiveType.Value.BEARERTOKEN,
                                "Path parameters of type 'bearertoken' are not allowed as this "
                                        + "would introduce a security vulnerability: \"%s\"",
                                entry.getArgName());
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParameterNameValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs().forEach(arg -> {
                Matcher matcher = ANCHORED_PATTERN.matcher(arg.getArgName().get());
                Preconditions.checkState(matcher.matches(),
                        "Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                        ANCHORED_PATTERN,
                        arg.getArgName().get());
            });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParamIdValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs().forEach(arg -> {
                final Pattern pattern;
                ParameterType paramType = arg.getParamType();
                if (paramType.accept(ParameterTypeVisitor.IS_BODY)
                        || paramType.accept(ParameterTypeVisitor.IS_PATH)
                        || paramType.accept(ParameterTypeVisitor.IS_QUERY)) {
                    pattern = ANCHORED_PATTERN;
                } else if (paramType.accept(ParameterTypeVisitor.IS_HEADER)) {
                    pattern = HEADER_PATTERN;
                } else {
                    throw new IllegalStateException("Validation for paramType does not exist: " + arg.getParamType());
                }

                if (paramType.accept(ParameterTypeVisitor.IS_QUERY)) {
                    ParameterId paramId = paramType.accept(ParameterTypeVisitor.QUERY).getParamId();
                    Preconditions.checkState(pattern.matcher(paramId.get()).matches(),
                            "Parameter ids with type %s must match pattern %s: %s",
                            arg.getParamType(), pattern, paramId.get());
                } else if (paramType.accept(ParameterTypeVisitor.IS_HEADER)) {
                    ParameterId paramId = paramType.accept(ParameterTypeVisitor.HEADER).getParamId();
                    Preconditions.checkState(pattern.matcher(paramId.get()).matches(),
                            "Parameter ids with type %s must match pattern %s: %s",
                            arg.getParamType(), pattern, paramId.get());
                }
            });
        }
    }
}
