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
        return new Builder()
                .baseType(PrimitiveType.STRING)
                .putExternal("java", external)
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableExternalTypeDefinition.Builder {}

}
