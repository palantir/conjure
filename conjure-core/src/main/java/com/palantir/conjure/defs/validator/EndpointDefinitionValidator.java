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
import com.palantir.conjure.either.Either;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import com.palantir.conjure.visitor.ParameterTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@com.google.errorprone.annotations.Immutable
public enum EndpointDefinitionValidator implements ConjureContextualValidator<EndpointDefinition> {
    ARGUMENT_TYPE(new NonBodyArgumentTypeValidator()),
    SINGLE_BODY_PARAM(new SingleBodyParamValidator()),
    PATH_PARAM(new PathParamValidator()),
    NO_BEARER_TOKEN_PATH_OR_QUERY_PARAMS(new NoBearerTokenPathOrQueryParams()),
    NO_COMPLEX_PATH_PARAMS(new NoComplexPathParamValidator()),
    NO_COMPLEX_HEADER_PARAMS(new NoComplexHeaderParamValidator()),
    NO_COMPLEX_QUERY_PARAMS(new NoComplexQueryParamValidator()),
    NO_GET_BODY_VALIDATOR(new NoGetBodyParamValidator()),
    PARAMETER_NAME(new ParameterNameValidator()),
    PARAM_ID(new ParamIdValidator());

    private static final Logger log = LoggerFactory.getLogger(EndpointDefinitionValidator.class);

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
                        Either<TypeDefinition, Type> resolvedType = dealiasingTypeVisitor.dealias(entry.getType());

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
                    .forEach(headerArgDefinition -> {
                        boolean isValid = recursivelyValidate(headerArgDefinition.getType(), dealiasingTypeVisitor);
                        Preconditions.checkState(isValid,
                                "Header parameters must be enums, primitives, aliases or optional primitive:"
                                        + " \"%s\" is not allowed",
                                headerArgDefinition.getArgName());
                    });
        }

        private static Boolean recursivelyValidate(Type type, DealiasingTypeVisitor visitor) {
            return visitor.dealias(type).fold(
                    typeDefinition -> typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                    subType -> {
                        boolean definedPrimitive =
                                subType.accept(TypeVisitor.IS_PRIMITIVE) && !subType.accept(TypeVisitor.IS_ANY);

                        boolean optionalPrimitive = subType.accept(TypeVisitor.IS_OPTIONAL)
                                && recursivelyValidate(subType.accept(TypeVisitor.OPTIONAL).getItemType(), visitor);

                        return definedPrimitive || optionalPrimitive;
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexQueryParams implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH))
                    .forEach(entry -> {
                        Either<TypeDefinition, Type> resolvedType = dealiasingTypeVisitor.dealias(entry.getType());
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
    private static final class NoComplexQueryParamValidator implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_QUERY))
                    .forEach(headerArgDefinition -> {
                        boolean isValid = recursivelyValidate(headerArgDefinition.getType(), dealiasingTypeVisitor);
                        Preconditions.checkState(isValid,
                                "Query parameters must be enums, primitives, aliases, list, sets "
                                        + "or optional of primitive: \"%s\" is not allowed",
                                headerArgDefinition.getArgName());
                    });
        }

        private static Boolean recursivelyValidate(Type type, DealiasingTypeVisitor visitor) {
            return visitor.dealias(type).fold(
                    typeDefinition -> typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                    subType -> subType.accept(new Type.Visitor<Boolean>() {
                        @Override
                        public Boolean visitPrimitive(PrimitiveType value) {
                            return value.get() != PrimitiveType.Value.ANY;
                        }

                        @Override
                        public Boolean visitOptional(OptionalType value) {
                            return recursivelyValidate(value.getItemType(), visitor);
                        }

                        @Override
                        public Boolean visitList(ListType value) {
                            log.warn("Collections as query parameters are deprecated and will "
                                    + "be removed in a future release");
                            return recursivelyValidate(value.getItemType(), visitor);
                        }

                        @Override
                        public Boolean visitSet(SetType value) {
                            log.warn("Collections as query parameters are deprecated and will "
                                    + "be removed in a future release");
                            return recursivelyValidate(value.getItemType(), visitor);
                        }

                        @Override
                        public Boolean visitMap(MapType value) {
                            return false;
                        }

                        // The cases below should not be handled here, since they implicitly handled by the
                        // DealiasingTypeVisitor above
                        @Override
                        public Boolean visitReference(TypeName value) {
                            throw new RuntimeException("Unexpected type when validating query parameters");
                        }

                        @Override
                        public Boolean visitExternal(ExternalReference value) {
                            throw new RuntimeException("Unexpected type when validating query parameters");
                        }

                        @Override
                        public Boolean visitUnknown(String unknownType) {
                            throw new RuntimeException("Unexpected type when validating query parameters");
                        }
                    }));
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoBearerTokenPathOrQueryParams implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH)
                            || entry.getParamType().accept(ParameterTypeVisitor.IS_QUERY))
                    .forEach(entry -> {
                        Type conjureType = entry.getType();

                        Preconditions.checkState(!conjureType.accept(TypeVisitor.IS_PRIMITIVE)
                                || conjureType.accept(TypeVisitor.PRIMITIVE).get() != PrimitiveType.Value.BEARERTOKEN,
                                "Path or query parameters of type 'bearertoken' are not allowed as this "
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
