package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * This class is used instead of a native enum to support unknown values. Rather than throw an
 * exception, the {@link HttpMethod#valueOf} method defaults to a new instantiation of {@link
 * HttpMethod} where {@link HttpMethod#get} will return {@link HttpMethod.Value#UNKNOWN}.
 *
 * <p>For example, {@code HttpMethod.valueOf("corrupted value").get()} will return {@link
 * HttpMethod.Value#UNKNOWN}, but {@link HttpMethod#toString} will return "corrupted value".
 *
 * <p>There is no method to access all instantiations of this class, since they cannot be known at
 * compile time.
 */
@Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
public final class HttpMethod {
    public static final HttpMethod GET = new HttpMethod(Value.GET, "GET");

    public static final HttpMethod POST = new HttpMethod(Value.POST, "POST");

    public static final HttpMethod PUT = new HttpMethod(Value.PUT, "PUT");

    public static final HttpMethod DELETE = new HttpMethod(Value.DELETE, "DELETE");

    private final Value value;

    private final String string;

    private HttpMethod(Value value, String string) {
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
                || (other instanceof HttpMethod && this.string.equals(((HttpMethod) other).string));
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @JsonCreator
    public static HttpMethod valueOf(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        String upperCasedValue = value.toUpperCase(Locale.ROOT);
        switch (upperCasedValue) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "DELETE":
                return DELETE;
            default:
                return new HttpMethod(Value.UNKNOWN, upperCasedValue);
        }
    }

    @Generated("com.palantir.conjure.gen.java.types.EnumGenerator")
    public enum Value {
        GET,

        POST,

        PUT,

        DELETE,

        UNKNOWN
    }
}
