package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * This enumerates the numbers 1:2.
 *
 * <p>This class is used instead of a native enum to support unknown values. Rather than throw an
 * exception, the {@link EnumExample#valueOf} method defaults to a new instantiation of {@link
 * EnumExample} where {@link EnumExample#get} will return {@link EnumExample.Value#UNKNOWN}.
 *
 * <p>For example, {@code EnumExample.valueOf("corrupted value").get()} will return {@link
 * EnumExample.Value#UNKNOWN}, but {@link EnumExample#toString} will return "corrupted value".
 *
 * <p>There is no method to access all instantiations of this class, since they cannot be known at
 * compile time.
 */
@Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
public final class EnumExample implements Serializable {
    public static final EnumExample ONE = new EnumExample(Value.ONE);

    public static final EnumExample TWO = new EnumExample(Value.TWO);

    private static final long serialVersionUID = 1L;

    private final Value value;

    private final String string;

    private EnumExample(Value value) {
        this.value = value;
        this.string = value.name();
    }

    private EnumExample(String string) {
        this.value = Value.UNKNOWN;
        this.string = string;
    }

    public Value get() {
        return this.value;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.string;
    }

    @Override
    public boolean equals(Object other) {
        return (this == other)
                || (other instanceof EnumExample
                        && this.string.equals(((EnumExample) other).string));
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @JsonCreator
    public static EnumExample valueOf(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        String upperCasedValue = value.toUpperCase(Locale.ROOT);
        switch (upperCasedValue) {
            case "ONE":
                return ONE;
            case "TWO":
                return TWO;
            default:
                return new EnumExample(upperCasedValue);
        }
    }

    private Object readResolve() {
        return valueOf(this.string);
    }

    @Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
    public enum Value {
        ONE,

        TWO,

        UNKNOWN
    }
}
