/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface TypesDefinition {

    List<ExternalTypeDefinition> externalImports();

    /**
     * The object and error definitions imported from a particular ConjureDefinition. Compilers should typically not
     * generate these types, but only use them to resolve types referenced in {@link #definitions()}.
     */
    ObjectsDefinition imports();

    /** The object and error definitions local to a particular ConjureDefinition. */
    ObjectsDefinition definitions();

    /** The unions of {@link #definitions() locally defined} and {@link #imports() imported} objects and errors . */
    @Value.Derived
    default ObjectsDefinition definitionsAndImports() {
        return ObjectsDefinition.builder()
                .addAllObjects(imports().objects())
                .addAllObjects(definitions().objects())
                .addAllErrors(imports().errors())
                .addAllErrors(definitions().errors())
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}
}
