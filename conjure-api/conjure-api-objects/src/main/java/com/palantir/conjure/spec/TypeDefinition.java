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
public final class TypeDefinition {
    private final Base value;

    @JsonCreator
    private TypeDefinition(Base value) {
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    public static TypeDefinition alias(AliasDefinition value) {
        return new TypeDefinition(new AliasWrapper(value));
    }

    public static TypeDefinition enum_(EnumDefinition value) {
        return new TypeDefinition(new EnumWrapper(value));
    }

    public static TypeDefinition object(ObjectDefinition value) {
        return new TypeDefinition(new ObjectWrapper(value));
    }

    public static TypeDefinition union(UnionDefinition value) {
        return new TypeDefinition(new UnionWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof AliasWrapper) {
            return visitor.visitAlias(((AliasWrapper) value).value);
        } else if (value instanceof EnumWrapper) {
            return visitor.visitEnum(((EnumWrapper) value).value);
        } else if (value instanceof ObjectWrapper) {
            return visitor.visitObject(((ObjectWrapper) value).value);
        } else if (value instanceof UnionWrapper) {
            return visitor.visitUnion(((UnionWrapper) value).value);
        } else if (value instanceof UnknownWrapper) {
            return visitor.visitUnknown(((UnknownWrapper) value).getType());
        }
        throw new IllegalStateException(
                String.format("Could not identify type %s", value.getClass()));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof TypeDefinition && equalTo((TypeDefinition) other))
                || (other instanceof AliasDefinition
                        && value instanceof AliasWrapper
                        && Objects.equals(((AliasWrapper) value).value, other))
                || (other instanceof EnumDefinition
                        && value instanceof EnumWrapper
                        && Objects.equals(((EnumWrapper) value).value, other))
                || (other instanceof ObjectDefinition
                        && value instanceof ObjectWrapper
                        && Objects.equals(((ObjectWrapper) value).value, other))
                || (other instanceof UnionDefinition
                        && value instanceof UnionWrapper
                        && Objects.equals(((UnionWrapper) value).value, other));
    }

    private boolean equalTo(TypeDefinition other) {
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringBuilder("TypeDefinition")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append("}")
                .toString();
    }

    public interface Visitor<T> {
        T visitAlias(AliasDefinition value);

        T visitEnum(EnumDefinition value);

        T visitObject(ObjectDefinition value);

        T visitUnion(UnionDefinition value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(AliasWrapper.class),
        @JsonSubTypes.Type(EnumWrapper.class),
        @JsonSubTypes.Type(ObjectWrapper.class),
        @JsonSubTypes.Type(UnionWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("alias")
    private static class AliasWrapper implements Base {
        private final AliasDefinition value;

        @JsonCreator
        private AliasWrapper(@JsonProperty("alias") AliasDefinition value) {
            Objects.requireNonNull(value, "alias cannot be null");
            this.value = value;
        }

        @JsonProperty("alias")
        private AliasDefinition getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof AliasWrapper && equalTo((AliasWrapper) other));
        }

        private boolean equalTo(AliasWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("AliasWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("enum")
    private static class EnumWrapper implements Base {
        private final EnumDefinition value;

        @JsonCreator
        private EnumWrapper(@JsonProperty("enum") EnumDefinition value) {
            Objects.requireNonNull(value, "enum cannot be null");
            this.value = value;
        }

        @JsonProperty("enum")
        private EnumDefinition getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof EnumWrapper && equalTo((EnumWrapper) other));
        }

        private boolean equalTo(EnumWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("EnumWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("object")
    private static class ObjectWrapper implements Base {
        private final ObjectDefinition value;

        @JsonCreator
        private ObjectWrapper(@JsonProperty("object") ObjectDefinition value) {
            Objects.requireNonNull(value, "object cannot be null");
            this.value = value;
        }

        @JsonProperty("object")
        private ObjectDefinition getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof ObjectWrapper && equalTo((ObjectWrapper) other));
        }

        private boolean equalTo(ObjectWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("ObjectWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("union")
    private static class UnionWrapper implements Base {
        private final UnionDefinition value;

        @JsonCreator
        private UnionWrapper(@JsonProperty("union") UnionDefinition value) {
            Objects.requireNonNull(value, "union cannot be null");
            this.value = value;
        }

        @JsonProperty("union")
        private UnionDefinition getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof UnionWrapper && equalTo((UnionWrapper) other));
        }

        private boolean equalTo(UnionWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("UnionWrapper")
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
