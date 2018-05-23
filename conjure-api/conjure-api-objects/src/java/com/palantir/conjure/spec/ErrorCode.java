package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * This class is used instead of a native enum to support unknown values. Rather than throw an
 * exception, the {@link ErrorCode#valueOf} method defaults to a new instantiation of {@link
 * ErrorCode} where {@link ErrorCode#get} will return {@link ErrorCode.Value#UNKNOWN}.
 *
 * <p>For example, {@code ErrorCode.valueOf("corrupted value").get()} will return {@link
 * ErrorCode.Value#UNKNOWN}, but {@link ErrorCode#toString} will return "corrupted value".
 *
 * <p>There is no method to access all instantiations of this class, since they cannot be known at
 * compile time.
 */
@Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
public final class ErrorCode {
    public static final ErrorCode PERMISSION_DENIED =
            new ErrorCode(Value.PERMISSION_DENIED, "PERMISSION_DENIED");

    public static final ErrorCode INVALID_ARGUMENT =
            new ErrorCode(Value.INVALID_ARGUMENT, "INVALID_ARGUMENT");

    public static final ErrorCode NOT_FOUND = new ErrorCode(Value.NOT_FOUND, "NOT_FOUND");

    public static final ErrorCode CONFLICT = new ErrorCode(Value.CONFLICT, "CONFLICT");

    public static final ErrorCode REQUEST_ENTITY_TOO_LARGE =
            new ErrorCode(Value.REQUEST_ENTITY_TOO_LARGE, "REQUEST_ENTITY_TOO_LARGE");

    public static final ErrorCode FAILED_PRECONDITION =
            new ErrorCode(Value.FAILED_PRECONDITION, "FAILED_PRECONDITION");

    public static final ErrorCode INTERNAL = new ErrorCode(Value.INTERNAL, "INTERNAL");

    public static final ErrorCode TIMEOUT = new ErrorCode(Value.TIMEOUT, "TIMEOUT");

    public static final ErrorCode CUSTOM_CLIENT =
            new ErrorCode(Value.CUSTOM_CLIENT, "CUSTOM_CLIENT");

    public static final ErrorCode CUSTOM_SERVER =
            new ErrorCode(Value.CUSTOM_SERVER, "CUSTOM_SERVER");

    private final Value value;

    private final String string;

    private ErrorCode(Value value, String string) {
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
                || (other instanceof ErrorCode && this.string.equals(((ErrorCode) other).string));
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @JsonCreator
    public static ErrorCode valueOf(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        String upperCasedValue = value.toUpperCase(Locale.ROOT);
        switch (upperCasedValue) {
            case "PERMISSION_DENIED":
                return PERMISSION_DENIED;
            case "INVALID_ARGUMENT":
                return INVALID_ARGUMENT;
            case "NOT_FOUND":
                return NOT_FOUND;
            case "CONFLICT":
                return CONFLICT;
            case "REQUEST_ENTITY_TOO_LARGE":
                return REQUEST_ENTITY_TOO_LARGE;
            case "FAILED_PRECONDITION":
                return FAILED_PRECONDITION;
            case "INTERNAL":
                return INTERNAL;
            case "TIMEOUT":
                return TIMEOUT;
            case "CUSTOM_CLIENT":
                return CUSTOM_CLIENT;
            case "CUSTOM_SERVER":
                return CUSTOM_SERVER;
            default:
                return new ErrorCode(Value.UNKNOWN, upperCasedValue);
        }
    }

    @Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
    public enum Value {
        PERMISSION_DENIED,

        INVALID_ARGUMENT,

        NOT_FOUND,

        CONFLICT,

        REQUEST_ENTITY_TOO_LARGE,

        FAILED_PRECONDITION,

        INTERNAL,

        TIMEOUT,

        CUSTOM_CLIENT,

        CUSTOM_SERVER,

        UNKNOWN
    }
}
