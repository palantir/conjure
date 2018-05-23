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
public final class ParameterType {
    private final Base value;

    @JsonCreator
    private ParameterType(Base value) {
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    public static ParameterType body(BodyParameterType value) {
        return new ParameterType(new BodyWrapper(value));
    }

    public static ParameterType header(HeaderParameterType value) {
        return new ParameterType(new HeaderWrapper(value));
    }

    public static ParameterType path(PathParameterType value) {
        return new ParameterType(new PathWrapper(value));
    }

    public static ParameterType query(QueryParameterType value) {
        return new ParameterType(new QueryWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof BodyWrapper) {
            return visitor.visitBody(((BodyWrapper) value).value);
        } else if (value instanceof HeaderWrapper) {
            return visitor.visitHeader(((HeaderWrapper) value).value);
        } else if (value instanceof PathWrapper) {
            return visitor.visitPath(((PathWrapper) value).value);
        } else if (value instanceof QueryWrapper) {
            return visitor.visitQuery(((QueryWrapper) value).value);
        } else if (value instanceof UnknownWrapper) {
            return visitor.visitUnknown(((UnknownWrapper) value).getType());
        }
        throw new IllegalStateException(
                String.format("Could not identify type %s", value.getClass()));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ParameterType && equalTo((ParameterType) other))
                || (other instanceof BodyParameterType
                        && value instanceof BodyWrapper
                        && Objects.equals(((BodyWrapper) value).value, other))
                || (other instanceof HeaderParameterType
                        && value instanceof HeaderWrapper
                        && Objects.equals(((HeaderWrapper) value).value, other))
                || (other instanceof PathParameterType
                        && value instanceof PathWrapper
                        && Objects.equals(((PathWrapper) value).value, other))
                || (other instanceof QueryParameterType
                        && value instanceof QueryWrapper
                        && Objects.equals(((QueryWrapper) value).value, other));
    }

    private boolean equalTo(ParameterType other) {
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringBuilder("ParameterType")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append("}")
                .toString();
    }

    public interface Visitor<T> {
        T visitBody(BodyParameterType value);

        T visitHeader(HeaderParameterType value);

        T visitPath(PathParameterType value);

        T visitQuery(QueryParameterType value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(BodyWrapper.class),
        @JsonSubTypes.Type(HeaderWrapper.class),
        @JsonSubTypes.Type(PathWrapper.class),
        @JsonSubTypes.Type(QueryWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("body")
    private static class BodyWrapper implements Base {
        private final BodyParameterType value;

        @JsonCreator
        private BodyWrapper(@JsonProperty("body") BodyParameterType value) {
            Objects.requireNonNull(value, "body cannot be null");
            this.value = value;
        }

        @JsonProperty("body")
        private BodyParameterType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof BodyWrapper && equalTo((BodyWrapper) other));
        }

        private boolean equalTo(BodyWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("BodyWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("header")
    private static class HeaderWrapper implements Base {
        private final HeaderParameterType value;

        @JsonCreator
        private HeaderWrapper(@JsonProperty("header") HeaderParameterType value) {
            Objects.requireNonNull(value, "header cannot be null");
            this.value = value;
        }

        @JsonProperty("header")
        private HeaderParameterType getValue() {
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

    @JsonTypeName("path")
    private static class PathWrapper implements Base {
        private final PathParameterType value;

        @JsonCreator
        private PathWrapper(@JsonProperty("path") PathParameterType value) {
            Objects.requireNonNull(value, "path cannot be null");
            this.value = value;
        }

        @JsonProperty("path")
        private PathParameterType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof PathWrapper && equalTo((PathWrapper) other));
        }

        private boolean equalTo(PathWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("PathWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("query")
    private static class QueryWrapper implements Base {
        private final QueryParameterType value;

        @JsonCreator
        private QueryWrapper(@JsonProperty("query") QueryParameterType value) {
            Objects.requireNonNull(value, "query cannot be null");
            this.value = value;
        }

        @JsonProperty("query")
        private QueryParameterType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof QueryWrapper && equalTo((QueryWrapper) other));
        }

        private boolean equalTo(QueryWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("QueryWrapper")
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
