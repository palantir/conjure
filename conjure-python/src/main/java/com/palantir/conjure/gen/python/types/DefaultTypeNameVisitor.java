/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.palantir.conjure.defs.types.TypeDefinitionVisitor;
import com.palantir.conjure.defs.types.TypeVisitor;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public final class DefaultTypeNameVisitor implements Type.Visitor<String> {

    private final Set<TypeName> typesByName;

    public DefaultTypeNameVisitor(List<TypeDefinition> types) {
        this.typesByName = types.stream().map(type ->
                type.accept(TypeDefinitionVisitor.TYPE_NAME)).collect(Collectors.toSet());
    }

    @Override
    public String visitList(ListType type) {
        return "ListType(" + type.getItemType().accept(this) + ")";
    }

    @Override
    public String visitMap(MapType type) {
        return "DictType(" + type.getKeyType().accept(this) + ", " + type.getValueType().accept(this) + ")";
    }

    @Override
    public String visitOptional(OptionalType type) {
        return "OptionalType(" + type.getItemType().accept(this) + ")";
    }

    @Override
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public String visitPrimitive(PrimitiveType type) {
        switch (type.get()) {
            case STRING:
            case RID:
            case BEARERTOKEN:
            case DATETIME:
            case UUID:
                return "str";
            case BINARY:
                return "BinaryType()";
            case BOOLEAN:
                return "bool";
            case DOUBLE:
                return "float";
            case INTEGER:
            case SAFELONG:
                return "int";
            case ANY:
                return "object";
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    @Override
    public String visitReference(TypeName type) {
        if (typesByName.contains(type)) {
            return type.getName();
        } else {
            throw new IllegalStateException("unknown type: " + type);
        }
    }

    @Override
    public String visitExternal(ExternalReference externalType) {
        if (externalType.getFallback().accept(TypeVisitor.IS_PRIMITIVE)) {
            return visitPrimitive(externalType.getFallback().accept(TypeVisitor.PRIMITIVE));
        } else {
            throw new IllegalStateException("unknown type: " + externalType);
        }
    }

    @Override
    public String visitSet(SetType type) {
        // TODO (bduffield): real sets
        return Type.list(ListType.of(type.getItemType())).accept(this);
    }

    @Override
    public String visitUnknown(String unknownType) {
        throw new IllegalStateException("unknown type: " + unknownType);
    }
}
