/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;


import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;

/**
 * Returns the given {@link #returnValue} for all {@code Type} subtypes. Override individual methods to customize
 * behavior.
 */
@SuppressWarnings("checkstyle:designforextension")  // explicitly designed for extension
public abstract class FixedDefaultConjureTypeVisitor<T> implements ConjureTypeVisitor<T> {

    private final T returnValue;

    public FixedDefaultConjureTypeVisitor(T returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public T visitAny(AnyType type) {
        return returnValue;
    }

    @Override
    public T visitList(ListType type) {
        return returnValue;
    }

    @Override
    public T visitMap(MapType type) {
        return returnValue;
    }

    @Override
    public T visitOptional(OptionalType type) {
        return returnValue;
    }

    @Override
    public T visitPrimitive(PrimitiveType type) {
        return returnValue;
    }

    @Override
    public T visitLocalReference(LocalReferenceType type) {
        return returnValue;
    }

    @Override
    public T visitExternal(ExternalType type) {
        return returnValue;
    }

    @Override
    public T visitSet(SetType type) {
        return returnValue;
    }

    @Override
    public T visitBinary(BinaryType type) {
        return returnValue;
    }

    @Override
    public T visitDateTime(DateTimeType type) {
        return returnValue;
    }
}
