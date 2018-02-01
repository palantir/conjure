/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureParserUtils;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.ObjectTypeDefVisitor;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import java.util.Optional;

public final class ObjectTypeDefParserVisitor implements ObjectTypeDefVisitor<BaseObjectTypeDefinition> {

    private final String name;
    private final Optional<ConjurePackage> defaultPackage;
    private final ConjureTypeParserVisitor.TypeNameResolver typeResolver;

    public ObjectTypeDefParserVisitor(
            String typeName,
            Optional<ConjurePackage> defaultPackage,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        this.name = typeName;
        this.defaultPackage = defaultPackage;
        this.typeResolver = typeResolver;
    }

    @Override
    public BaseObjectTypeDefinition visit(AliasTypeDefinition def) {
        return ConjureParserUtils.parseAliasType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(EnumTypeDefinition def) {
        return ConjureParserUtils.parseEnumType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def);
    }

    @Override
    public BaseObjectTypeDefinition visit(ErrorTypeDefinition def) {
        return ConjureParserUtils.parseErrorType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(ObjectTypeDefinition def) {
        return ConjureParserUtils.parseObjectType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public BaseObjectTypeDefinition visit(UnionTypeDefinition def) {
        return ConjureParserUtils.parseUnionType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

}
