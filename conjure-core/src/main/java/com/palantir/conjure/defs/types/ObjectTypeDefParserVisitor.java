/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.parser.types.ObjectTypeDefVisitor;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.complex.FieldDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.FieldName;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ObjectTypeDefParserVisitor implements ObjectTypeDefVisitor<BaseObjectTypeDefinition> {

    private final String name;
    private final Optional<ConjurePackage> defaultPackage;
    private final ConjureTypeParserVisitor.TypeNameResolver typeResolver;

    ObjectTypeDefParserVisitor(
            String typeName,
            Optional<ConjurePackage> defaultPackage,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        this.name = typeName;
        this.defaultPackage = defaultPackage;
        this.typeResolver = typeResolver;
    }

    @Override
    public BaseObjectTypeDefinition visit(AliasTypeDefinition def) {
        return com.palantir.conjure.defs.types.reference.AliasTypeDefinition.parseFrom(
                createTypeName(def), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(EnumTypeDefinition def) {
        return com.palantir.conjure.defs.types.complex.EnumTypeDefinition.parseFrom(createTypeName(def), def);
    }

    @Override
    public BaseObjectTypeDefinition visit(ErrorTypeDefinition def) {
        return com.palantir.conjure.defs.types.complex.ErrorTypeDefinition.parseFrom(
                createTypeName(def), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(ObjectTypeDefinition def) {
        return com.palantir.conjure.defs.types.complex.ObjectTypeDefinition.parseFrom(
                createTypeName(def), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(UnionTypeDefinition def) {
        return com.palantir.conjure.defs.types.complex.UnionTypeDefinition.parseFrom(
                createTypeName(def), def, typeResolver);
    }

    public static Map<com.palantir.conjure.defs.types.names.FieldName,
            com.palantir.conjure.defs.types.complex.FieldDefinition> parseFieldDef(
            Map<FieldName, FieldDefinition> def, ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        Map<com.palantir.conjure.defs.types.names.FieldName, com.palantir.conjure.defs.types.complex.FieldDefinition>
                result = new LinkedHashMap<>();
        def.forEach((name, field) -> result.put(
                com.palantir.conjure.defs.types.names.FieldName.of(name.name()),
                com.palantir.conjure.defs.types.complex.FieldDefinition.of(
                        field.type().visit(new ConjureTypeParserVisitor(typeResolver)), field.docs())));
        return result;
    }

    static ConjurePackage orElseOrThrow(
            Optional<com.palantir.conjure.parser.types.names.ConjurePackage> conjurePackage,
            Optional<ConjurePackage> defaultPackage) {
        return conjurePackage
                .map(p -> ConjurePackage.of(p.name()))
                .orElseGet(() -> defaultPackage.orElseThrow(() -> new IllegalArgumentException(
                        // TODO(rfink): Better errors: Can we provide context on where exactly no package was provided?
                        "Must provide default conjure package or "
                                + "explicit conjure package for every object and service")));
    }

    private TypeName createTypeName(com.palantir.conjure.parser.types.BaseObjectTypeDefinition def) {
        return TypeName.of(name, ObjectTypeDefParserVisitor.orElseOrThrow(def.conjurePackage(), defaultPackage));
    }
}
