/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureParserUtils;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import java.util.Optional;

public final class TypeDefinitionParserVisitor implements TypeDefinitionVisitor<TypeDefinition> {

    private final String name;
    private final Optional<String> defaultPackage;
    private final ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver;

    public TypeDefinitionParserVisitor(
            String typeName,
            Optional<String> defaultPackage,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        this.name = typeName;
        this.defaultPackage = defaultPackage;
        this.typeResolver = typeResolver;
    }

    @Override
    public TypeDefinition visit(AliasTypeDefinition def) {
        return ConjureParserUtils.parseAliasType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public TypeDefinition visit(EnumTypeDefinition def) {
        return ConjureParserUtils.parseEnumType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def);
    }

    @Override
    public TypeDefinition visit(ObjectTypeDefinition def) {
        return ConjureParserUtils.parseObjectType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public TypeDefinition visit(UnionTypeDefinition def) {
        return ConjureParserUtils.parseUnionType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }
}
