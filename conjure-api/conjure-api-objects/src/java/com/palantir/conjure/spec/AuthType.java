package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.UnionGenerator")
public final class AuthType {
    private final Base value;

    @JsonCreator
    private AuthType(Base value) {
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    public static AuthType header(HeaderAuthType value) {
        return new AuthType(new HeaderWrapper(value));
    }

    public static AuthType cookie(CookieAuthType value) {
        return new AuthType(new CookieWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof HeaderWrapper) {
            return visitor.visitHeader(((HeaderWrapper) value).value);
        } else if (value instanceof CookieWrapper) {
            return visitor.visitCookie(((CookieWrapper) value).value);
        } else if (value instanceof UnknownWrapper) {
            return visitor.visitUnknown(((UnknownWrapper) value).getType());
        }
        throw new IllegalStateException(
                String.format("Could not identify type %s", value.getClass()));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof AuthType && equalTo((AuthType) other))
                || (other instanceof HeaderAuthType
                        && value instanceof HeaderWrapper
                        && Objects.equals(((HeaderWrapper) value).value, other))
                || (other instanceof CookieAuthType
                        && value instanceof CookieWrapper
                        && Objects.equals(((CookieWrapper) value).value, other));
    }

    private boolean equalTo(AuthType other) {
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringBuilder("AuthType")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append("}")
                .toString();
    }

    public interface Visitor<T> {
        T visitHeader(HeaderAuthType value);

        T visitCookie(CookieAuthType value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(HeaderWrapper.class),
        @JsonSubTypes.Type(CookieWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("header")
    private static class HeaderWrapper implements Base {
        private final HeaderAuthType value;

        @JsonCreator
        private HeaderWrapper(@JsonProperty("header") HeaderAuthType value) {
            Objects.requireNonNull(value, "header cannot be null");
            this.value = value;
        }

        @JsonProperty("header")
        private HeaderAuthType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof HeaderWrapper && equalTo((HeaderWrapper) other));
        }

        private boolean equalTo(HeaderWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("HeaderWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("cookie")
    private static class CookieWrapper implements Base {
        private final CookieAuthType value;

        @JsonCreator
        private CookieWrapper(@JsonProperty("cookie") CookieAuthType value) {
            Objects.requireNonNull(value, "cookie cannot be null");
            this.value = value;
        }

        @JsonProperty("cookie")
        private CookieAuthType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof CookieWrapper && equalTo((CookieWrapper) other));
        }

        private boolean equalTo(CookieWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("CookieWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    private static class UnknownWrapper implements Base {
        private final String type;

        private final Map<String, Object> value;

        @JsonCreator
        private UnknownWrapper(@JsonProperty("type") String type) {
            this(type, new HashMap<String, Object>());
        }

        private UnknownWrapper(String type, Map<String, Object> value) {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            this.type = type;
            this.value = value;
        }

        @JsonProperty
        private String getType() {
            return type;
        }

        @JsonAnyGetter
        private Map<String, Object> getValue() {
            return value;
        }

        @JsonAnySetter
        private void put(String key, Object val) {
            value.put(key, val);
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof UnknownWrapper && equalTo((UnknownWrapper) other));
        }

        private boolean equalTo(UnknownWrapper other) {
            return this.type.equals(other.type) && this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }

        @Override
        public String toString() {
            return new StringBuilder("UnknownWrapper")
                    .append("{")
                    .append("type")
                    .append(": ")
                    .append(type)
                    .append(", ")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }
}
