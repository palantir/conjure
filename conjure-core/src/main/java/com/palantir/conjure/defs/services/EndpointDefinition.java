/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.parser.services.PathDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    RequestLineDefinition http();

    AuthDefinition auth();

    Map<ParameterName, ArgumentDefinition> args();

    Set<ConjureType> markers();

    Optional<ConjureType> returns();

    Optional<String> docs();

    Optional<String> deprecated();

    @Value.Check
    default void check() {
        for (EndpointDefinitionValidator validator : EndpointDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

    static EndpointDefinition parseFrom(
            com.palantir.conjure.parser.services.EndpointDefinition def,
            PathDefinition basePath,
            AuthDefinition defaultAuth,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        RequestLineDefinition requestDef = RequestLineDefinition.parseFrom(basePath, def.http());
        return builder()
                .http(requestDef)
                .auth(def.auth()
                        .map(AuthDefinition::parseFrom)
                        .orElse(defaultAuth))
                .args(parseArgs(def.args(), requestDef, typeResolver))
                .markers(parseMarkers(def.markers(), typeResolver))
                .returns(def.returns().map(t -> t.visit(new ConjureTypeParserVisitor(typeResolver))))
                .docs(def.docs())
                .deprecated(def.deprecated())
                .build();
    }

    // TODO(rfink): Move somewhere else and make private
    static Map<ParameterName, ArgumentDefinition> parseArgs(
            Map<com.palantir.conjure.parser.services.ParameterName,
                    com.palantir.conjure.parser.services.ArgumentDefinition> args,
            RequestLineDefinition requestDef,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        Map<ParameterName, ArgumentDefinition> result = new LinkedHashMap<>();
        for (Map.Entry<com.palantir.conjure.parser.services.ParameterName,
                com.palantir.conjure.parser.services.ArgumentDefinition> entry : args.entrySet()) {
            com.palantir.conjure.parser.services.ArgumentDefinition original = entry.getValue();
            ParameterName paramName = ParameterName.of(entry.getKey().name());
            ParameterName paramId =
                    ParameterName.of(original.paramId().map(id -> id.name()).orElse(entry.getKey().name()));
            ArgumentDefinition.Builder builder = ArgumentDefinition.builder()
                    .type(original.type().visit(new ConjureTypeParserVisitor(typeResolver)))
                    .paramId(paramId)
                    .docs(original.docs())
                    .markers(parseMarkers(original.markers(), typeResolver));

            if (original.paramType() != com.palantir.conjure.parser.services.ArgumentDefinition.ParamType.AUTO) {
                builder.paramType(ArgumentDefinition.ParamType.valueOf(original.paramType().name()));
            } else {
                // AUTO type
                if (requestDef.pathArgs().contains(paramId)) {
                    // argument exists in request line -- it is a path arg
                    builder.paramType(ArgumentDefinition.ParamType.PATH);
                } else {
                    // argument does not exist in request line -- it is a body arg
                    builder.paramType(ArgumentDefinition.ParamType.BODY);
                }
            }
            result.put(paramName, builder.build());
        }
        return result;
    }

    // TODO(rfink): Move somewhere else and make protected
    static Set<ConjureType> parseMarkers(
            Set<com.palantir.conjure.parser.types.ConjureType> markers,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        return markers.stream()
                .map(m -> m.visit(new ConjureTypeParserVisitor(typeResolver)))
                .collect(Collectors.toSet());
    }
}
