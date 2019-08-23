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
import com.google.common.collect.Sets;
import com.palantir.conjure.CaseConverter;
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
import com.palantir.logsafe.exceptions.SafeRuntimeException;
import java.util.Arrays;
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
    NO_OPTIONAL_BINARY_BODY_PARAM_VALIDATOR(new NoOptionalBinaryBodyParamValidator()),
    PARAMETER_NAME(new ParameterNameValidator()),
    PARAM_ID(new ParamIdValidator());

    private static final Logger log = LoggerFactory.getLogger(EndpointDefinitionValidator.class);

    public static void validateAll(EndpointDefinition definition, DealiasingTypeVisitor dealiasingVisitor) {
        for (EndpointDefinitionValidator validator : values()) {
            validator.validate(definition, dealiasingVisitor);
        }
    }

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
    private static final class NonBodyArgumentTypeValidator implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs()
                    .stream()
                    .filter(arg -> !arg.getParamType().accept(ParameterTypeVisitor.IS_BODY))
                    .forEach(arg -> {
                        boolean isValid = dealiasingTypeVisitor.dealias(arg.getType())
                                .fold(
                                        typeDefinition -> true,
                                        type -> !type.accept(TypeVisitor.IS_BINARY)
                                                && !type.accept(TypeVisitor.IS_ANY)
                                );
                        Preconditions.checkArgument(
                                isValid, "Non body parameters cannot be of the 'binary' type: '%s' is not allowed",
                                arg.getArgName());
                    });
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

            Preconditions.checkState(
                    bodyParams.size() <= 1,
                    "Endpoint '%s' cannot have multiple body parameters: %s",
                    describe(definition),
                    bodyParams.stream().map(ArgumentDefinition::getArgName).collect(Collectors.toList()));
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

                Preconditions.checkState(
                        !hasBody,
                        "Endpoint '%s' cannot be a GET and contain a body",
                        describe(definition));
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoOptionalBinaryBodyParamValidator
            implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs()
                    .stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_BODY))
                    .forEach(entry -> {
                        boolean isOptionalBinary = dealiasingTypeVisitor.dealias(entry.getType())
                                .fold(
                                        typeDef -> false, // typeDef cannot resolve to optional<binary>
                                        type -> isOptionalBinary(type));
                        Preconditions.checkState(
                                !isOptionalBinary,
                                "Endpoint BODY argument must not be optional<binary> or alias thereof: %s",
                                describe(definition));
                    });
        }

        private static boolean isOptionalBinary(Type type) {
            return type.accept(TypeVisitor.IS_OPTIONAL)
                    ? type.accept(TypeVisitor.OPTIONAL).getItemType().accept(TypeVisitor.IS_BINARY)
                    : false;
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class PathParamValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            String description = describe(definition);
            Set<ArgumentName> pathParamIds = new HashSet<>();
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH))
                    .forEach(entry -> {
                        boolean added = pathParamIds.add(entry.getArgName());
                        Preconditions.checkState(
                                added,
                                "Path parameter with identifier \"%s\" is defined multiple times for endpoint %s",
                                entry.getArgName().get(), description);
                    });

            Set<ArgumentName> pathArgs = HttpPathValidator.pathArgs(definition.getHttpPath().get());
            Set<ArgumentName> extraParams = Sets.difference(pathParamIds, pathArgs);
            Preconditions.checkState(extraParams.isEmpty(),
                    "Path parameters defined in endpoint but not present in path template: %s. "
                            + "Note that the `param-id` is no longer supported and the path template name is always "
                            + "used instead. So make sure the path template name matches the path parameter defined "

                            + "in endpoint %s.", extraParams, description);
            Set<ArgumentName> missingParams = Sets.difference(pathArgs, pathParamIds);
            Preconditions.checkState(missingParams.isEmpty(),
                    "Path parameters %s defined path template but not present in endpoint: %s",
                    missingParams, description);
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
                                type -> type.accept(TypeVisitor.IS_PRIMITIVE));
                        Preconditions.checkState(
                                isValid,
                                "Path parameters must be primitives or aliases: \"%s\" is not allowed on endpoint %s",
                                entry.getArgName(),
                                describe(definition));
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
                        Preconditions.checkState(
                                isValid,
                                "Header parameters must be enums, primitives, aliases or optional primitive:"
                                        + " \"%s\" is not allowed on endpoint %s",
                                headerArgDefinition.getArgName(), describe(definition));
                    });
        }

        private static Boolean recursivelyValidate(Type type, DealiasingTypeVisitor visitor) {
            return visitor.dealias(type).fold(
                    typeDefinition -> typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                    subType -> {
                        boolean definedPrimitive = subType.accept(TypeVisitor.IS_PRIMITIVE);

                        boolean optionalPrimitive = subType.accept(TypeVisitor.IS_OPTIONAL)
                                && recursivelyValidate(subType.accept(TypeVisitor.OPTIONAL).getItemType(), visitor);

                        return definedPrimitive || optionalPrimitive;
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexQueryParamValidator implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_QUERY))
                    .forEach(argDefinition -> {
                        boolean isValid = recursivelyValidate(argDefinition.getType(), dealiasingTypeVisitor);
                        Preconditions.checkState(
                                isValid,
                                "Query parameters must be enums, primitives, aliases, list, sets "
                                        + "or optional of primitive: \"%s\" is not allowed on endpoint %s",
                                argDefinition.getArgName(), describe(definition));
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
                            return recursivelyValidate(value.getItemType(), visitor);
                        }

                        @Override
                        public Boolean visitSet(SetType value) {
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
                            throw new SafeRuntimeException("Unexpected type when validating query parameters");
                        }

                        @Override
                        public Boolean visitExternal(ExternalReference value) {
                            throw new SafeRuntimeException("Unexpected type when validating query parameters");
                        }

                        @Override
                        public Boolean visitUnknown(String unknownType) {
                            throw new SafeRuntimeException("Unexpected type when validating query parameters");
                        }
                    }));
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoBearerTokenPathOrQueryParams
            implements ConjureContextualValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition, DealiasingTypeVisitor dealiasingTypeVisitor) {
            definition.getArgs().stream()
                    .filter(entry -> entry.getParamType().accept(ParameterTypeVisitor.IS_PATH)
                            || entry.getParamType().accept(ParameterTypeVisitor.IS_QUERY))
                    .forEach(entry -> {
                        Either<TypeDefinition, Type> conjureType = dealiasingTypeVisitor.dealias(entry.getType());
                        boolean isValid = conjureType.fold(
                                typeDefinition -> true,
                                type -> !type.accept(TypeVisitor.IS_PRIMITIVE)
                                        || type.accept(TypeVisitor.PRIMITIVE).get() != PrimitiveType.Value.BEARERTOKEN
                        );

                        Preconditions.checkState(
                                isValid,
                                "Path or query parameters of type 'bearertoken' are not allowed as this "
                                        + "would introduce a security vulnerability: \"%s\" endpoint \"%s\"",
                                entry.getArgName(), describe(definition));
                    });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParameterNameValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        public void validate(EndpointDefinition definition) {
            definition.getArgs().forEach(arg -> {
                Matcher matcher = CaseConverter.CAMEL_CASE_PATTERN.matcher(arg.getArgName().get());
                Preconditions.checkState(
                        matcher.matches(),
                        "Parameter names in endpoint paths and service definitions "
                                + "must match pattern %s: %s on endpoint %s",
                        CaseConverter.CAMEL_CASE_PATTERN,
                        arg.getArgName().get(),
                        describe(definition));
            });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ParamIdValidator implements ConjureValidator<EndpointDefinition> {
        @Override
        @SuppressWarnings("Slf4jLogsafeArgs")
        public void validate(EndpointDefinition definition) {
            definition.getArgs().forEach(arg -> {
                ParameterType paramType = arg.getParamType();
                if (paramType.accept(ParameterTypeVisitor.IS_BODY) || paramType.accept(ParameterTypeVisitor.IS_PATH)) {
                    // No validation for param-id of body and path parameters, as it is never (de)serialized.
                } else if (paramType.accept(ParameterTypeVisitor.IS_HEADER)) {
                    ParameterId paramId = paramType.accept(ParameterTypeVisitor.HEADER).getParamId();
                    Preconditions.checkState(HEADER_PATTERN.matcher(paramId.get()).matches(),
                            "Header parameter id %s on endpoint %s must match pattern %s",
                            paramId.get(), describe(definition), HEADER_PATTERN);

                } else if (paramType.accept(ParameterTypeVisitor.IS_QUERY)) {
                    ParameterId paramId = paramType.accept(ParameterTypeVisitor.QUERY).getParamId();
                    Preconditions.checkState(
                            CaseConverter.CAMEL_CASE_PATTERN.matcher(paramId.get()).matches()
                                    || CaseConverter.KEBAB_CASE_PATTERN.matcher(paramId.get()).matches()
                                    || CaseConverter.SNAKE_CASE_PATTERN.matcher(paramId.get()).matches(),
                            "Query param id %s on endpoint %s must match one of the following patterns: %s",
                                    paramId.get(), describe(definition), Arrays.toString(CaseConverter.Case.values()));

                    if (!CaseConverter.CAMEL_CASE_PATTERN.matcher(paramId.get()).matches()) {
                        log.warn("Query param ids should be camelCase. kebab-case and snake_case are supported for "
                                + "legacy endpoints only: {} on endpoint {}", paramId.get(), describe(definition));
                    }
                } else {
                    throw new IllegalStateException("Validation for paramType does not exist: " + arg.getParamType());
                }
            });
        }
    }

    private static String describe(EndpointDefinition endpoint) {
        return String.format("%s{http: %s %s}",
                endpoint.getEndpointName(), endpoint.getHttpMethod(), endpoint.getHttpPath());
    }
}
