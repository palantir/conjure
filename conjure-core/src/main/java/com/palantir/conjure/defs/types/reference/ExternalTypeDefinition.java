/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ExternalTypeDefinition {

    TypeName typeName();

    Map<String, String> external();

    PrimitiveType baseType();

    static ExternalTypeDefinition javaType(String external) {
        return ImmutableExternalTypeDefinition.builder()
                .baseType(PrimitiveType.STRING)
                .putExternal("java", external)
                .build();
    }

    static ExternalTypeDefinition fromParse(
            TypeName name, com.palantir.conjure.parser.types.reference.ExternalTypeDefinition def) {
        return ImmutableExternalTypeDefinition.builder()
                .typeName(name)
                .baseType(PrimitiveType.parseFrom(def.baseType()))
                .putAllExternal(def.external())
                .build();
    }
}
