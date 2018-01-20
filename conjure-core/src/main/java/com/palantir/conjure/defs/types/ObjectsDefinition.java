/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectsDefinition {

    // TODO(rfink): Rename to "types".
    List<BaseObjectTypeDefinition> objects();

    List<ErrorTypeDefinition> errors();

    @Value.Check
    default void check() {
        for (ObjectsDefinitionValidator validator : ObjectsDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectsDefinition.Builder {}

    static ObjectsDefinition fromParse(
            com.palantir.conjure.parser.types.ObjectsDefinition defs,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        Optional<ConjurePackage> defaultPackage = defs.defaultConjurePackage().map(p -> ConjurePackage.of(p.name()));

        List<BaseObjectTypeDefinition> objects = new ArrayList<>();
        defs.objects().forEach((name, def) ->
                objects.add(def.visit(new ObjectTypeDefParserVisitor(name.name(), defaultPackage, typeResolver))));
        List<ErrorTypeDefinition> errors = new ArrayList<>();
        defs.errors().forEach((name, def) -> {
            TypeName typeName = TypeName.of(
                    name.name(), ObjectTypeDefParserVisitor.orElseOrThrow(def.conjurePackage(), defaultPackage));
            errors.add(ErrorTypeDefinition.parseFrom(typeName, def, typeResolver));
        });
        return builder()
                .objects(objects)
                .errors(errors)
                .build();
    }
}
