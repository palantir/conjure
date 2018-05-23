package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * This class is used instead of a native enum to support unknown values. Rather than throw an
 * exception, the {@link PrimitiveType#valueOf} method defaults to a new instantiation of {@link
 * PrimitiveType} where {@link PrimitiveType#get} will return {@link PrimitiveType.Value#UNKNOWN}.
 *
 * <p>For example, {@code PrimitiveType.valueOf("corrupted value").get()} will return {@link
 * PrimitiveType.Value#UNKNOWN}, but {@link PrimitiveType#toString} will return "corrupted value".
 *
 * <p>There is no method to access all instantiations of this class, since they cannot be known at
 * compile time.
 */
@Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
public final class PrimitiveType {
    public static final PrimitiveType STRING = new PrimitiveType(Value.STRING, "STRING");

    public static final PrimitiveType DATETIME = new PrimitiveType(Value.DATETIME, "DATETIME");

    public static final PrimitiveType INTEGER = new PrimitiveType(Value.INTEGER, "INTEGER");

    public static final PrimitiveType DOUBLE = new PrimitiveType(Value.DOUBLE, "DOUBLE");

    public static final PrimitiveType SAFELONG = new PrimitiveType(Value.SAFELONG, "SAFELONG");

    public static final PrimitiveType BINARY = new PrimitiveType(Value.BINARY, "BINARY");

    public static final PrimitiveType ANY = new PrimitiveType(Value.ANY, "ANY");

    public static final PrimitiveType BOOLEAN = new PrimitiveType(Value.BOOLEAN, "BOOLEAN");

    public static final PrimitiveType UUID = new PrimitiveType(Value.UUID, "UUID");

    public static final PrimitiveType RID = new PrimitiveType(Value.RID, "RID");

    public static final PrimitiveType BEARERTOKEN =
            new PrimitiveType(Value.BEARERTOKEN, "BEARERTOKEN");

    private final Value value;

    private final String string;

    private PrimitiveType(Value value, String string) {
        this.value = value;
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
                || (other instanceof PrimitiveType
                        && this.string.equals(((PrimitiveType) other).string));
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @JsonCreator
    public static PrimitiveType valueOf(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        String upperCasedValue = value.toUpperCase(Locale.ROOT);
        switch (upperCasedValue) {
            case "STRING":
                return STRING;
            case "DATETIME":
                return DATETIME;
            case "INTEGER":
                return INTEGER;
            case "DOUBLE":
                return DOUBLE;
            case "SAFELONG":
                return SAFELONG;
            case "BINARY":
                return BINARY;
            case "ANY":
                return ANY;
            case "BOOLEAN":
                return BOOLEAN;
            case "UUID":
                return UUID;
            case "RID":
                return RID;
            case "BEARERTOKEN":
                return BEARERTOKEN;
            default:
                return new PrimitiveType(Value.UNKNOWN, upperCasedValue);
        }
    }

    @Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
    public enum Value {
        STRING,

        DATETIME,

        INTEGER,

        DOUBLE,

        SAFELONG,

        BINARY,

        ANY,

        BOOLEAN,

        UUID,

        RID,

        BEARERTOKEN,

        UNKNOWN
    }
}
