/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.ArrayList;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureDefinition {

    TypesDefinition types();

    List<ServiceDefinition> services();

    @Value.Check
    default void check() {
        for (ConjureDefinitionValidator validator : ConjureDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static ConjureDefinition fromParse(com.palantir.conjure.parser.ConjureDefinition parsed) {
        List<ServiceDefinition> services = new ArrayList<>();
        ConjureTypeParserVisitor.TypeNameResolver typeResolver =
                new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(parsed.types());
        parsed.services().forEach((serviceName, service) -> {
            services.add(ServiceDefinition.fromParse(
                    service,
                    TypeName.of(serviceName.name(), ConjurePackage.parseFrom(service.conjurePackage())),
                    typeResolver));
        });

        return builder()
                .types(TypesDefinition.fromParse(parsed.types(), typeResolver))
                .services(services)
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureDefinition.Builder {}

}
