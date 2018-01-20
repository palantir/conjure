/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ServiceDefinition {

    TypeName serviceName();

    Optional<String> docs();

    Map<String, EndpointDefinition> endpoints();

    @Value.Check
    default void check() {
        for (ServiceDefinitionValidator validator : ServiceDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static ServiceDefinition fromParse(
            com.palantir.conjure.parser.services.ServiceDefinition parsed,
            TypeName serviceName,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        Map<String, EndpointDefinition> endpoints = new LinkedHashMap<>();
        parsed.endpoints().forEach((name, def) -> endpoints.put(
                name, EndpointDefinition.parseFrom(
                        def, parsed.basePath(), AuthDefinition.parseFrom(parsed.defaultAuth()), typeResolver)));
        return builder()
                .serviceName(serviceName)
                .docs(parsed.docs())
                .putAllEndpoints(endpoints)
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableServiceDefinition.Builder {}

}
