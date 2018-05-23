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
public final class Type {
    private final Base value;

    @JsonCreator
    private Type(Base value) {
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    public static Type primitive(PrimitiveType value) {
        return new Type(new PrimitiveWrapper(value));
    }

    public static Type optional(OptionalType value) {
        return new Type(new OptionalWrapper(value));
    }

    public static Type list(ListType value) {
        return new Type(new ListWrapper(value));
    }

    public static Type set(SetType value) {
        return new Type(new SetWrapper(value));
    }

    public static Type map(MapType value) {
        return new Type(new MapWrapper(value));
    }

    /**
     * The name and package of a custom Conjure type. The custom type must be defined in the "types"
     * section.
     */
    public static Type reference(TypeName value) {
        return new Type(new ReferenceWrapper(value));
    }

    public static Type external(ExternalReference value) {
        return new Type(new ExternalWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof PrimitiveWrapper) {
            return visitor.visitPrimitive(((PrimitiveWrapper) value).value);
        } else if (value instanceof OptionalWrapper) {
            return visitor.visitOptional(((OptionalWrapper) value).value);
        } else if (value instanceof ListWrapper) {
            return visitor.visitList(((ListWrapper) value).value);
        } else if (value instanceof SetWrapper) {
            return visitor.visitSet(((SetWrapper) value).value);
        } else if (value instanceof MapWrapper) {
            return visitor.visitMap(((MapWrapper) value).value);
        } else if (value instanceof ReferenceWrapper) {
            return visitor.visitReference(((ReferenceWrapper) value).value);
        } else if (value instanceof ExternalWrapper) {
            return visitor.visitExternal(((ExternalWrapper) value).value);
        } else if (value instanceof UnknownWrapper) {
            return visitor.visitUnknown(((UnknownWrapper) value).getType());
        }
        throw new IllegalStateException(
                String.format("Could not identify type %s", value.getClass()));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof Type && equalTo((Type) other))
                || (other instanceof PrimitiveType
                        && value instanceof PrimitiveWrapper
                        && Objects.equals(((PrimitiveWrapper) value).value, other))
                || (other instanceof OptionalType
                        && value instanceof OptionalWrapper
                        && Objects.equals(((OptionalWrapper) value).value, other))
                || (other instanceof ListType
                        && value instanceof ListWrapper
                        && Objects.equals(((ListWrapper) value).value, other))
                || (other instanceof SetType
                        && value instanceof SetWrapper
                        && Objects.equals(((SetWrapper) value).value, other))
                || (other instanceof MapType
                        && value instanceof MapWrapper
                        && Objects.equals(((MapWrapper) value).value, other))
                || (other instanceof TypeName
                        && value instanceof ReferenceWrapper
                        && Objects.equals(((ReferenceWrapper) value).value, other))
                || (other instanceof ExternalReference
                        && value instanceof ExternalWrapper
                        && Objects.equals(((ExternalWrapper) value).value, other));
    }

    private boolean equalTo(Type other) {
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringBuilder("Type")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append("}")
                .toString();
    }

    public interface Visitor<T> {
        T visitPrimitive(PrimitiveType value);

        T visitOptional(OptionalType value);

        T visitList(ListType value);

        T visitSet(SetType value);

        T visitMap(MapType value);

        T visitReference(TypeName value);

        T visitExternal(ExternalReference value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(PrimitiveWrapper.class),
        @JsonSubTypes.Type(OptionalWrapper.class),
        @JsonSubTypes.Type(ListWrapper.class),
        @JsonSubTypes.Type(SetWrapper.class),
        @JsonSubTypes.Type(MapWrapper.class),
        @JsonSubTypes.Type(ReferenceWrapper.class),
        @JsonSubTypes.Type(ExternalWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("primitive")
    private static class PrimitiveWrapper implements Base {
        private final PrimitiveType value;

        @JsonCreator
        private PrimitiveWrapper(@JsonProperty("primitive") PrimitiveType value) {
            Objects.requireNonNull(value, "primitive cannot be null");
            this.value = value;
        }

        @JsonProperty("primitive")
        private PrimitiveType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof PrimitiveWrapper && equalTo((PrimitiveWrapper) other));
        }

        private boolean equalTo(PrimitiveWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("PrimitiveWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("optional")
    private static class OptionalWrapper implements Base {
        private final OptionalType value;

        @JsonCreator
        private OptionalWrapper(@JsonProperty("optional") OptionalType value) {
            Objects.requireNonNull(value, "optional cannot be null");
            this.value = value;
        }

        @JsonProperty("optional")
        private OptionalType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof OptionalWrapper && equalTo((OptionalWrapper) other));
        }

        private boolean equalTo(OptionalWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("OptionalWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("list")
    private static class ListWrapper implements Base {
        private final ListType value;

        @JsonCreator
        private ListWrapper(@JsonProperty("list") ListType value) {
            Objects.requireNonNull(value, "list cannot be null");
            this.value = value;
        }

        @JsonProperty("list")
        private ListType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof ListWrapper && equalTo((ListWrapper) other));
        }

        private boolean equalTo(ListWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("ListWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("set")
    private static class SetWrapper implements Base {
        private final SetType value;

        @JsonCreator
        private SetWrapper(@JsonProperty("set") SetType value) {
            Objects.requireNonNull(value, "set cannot be null");
            this.value = value;
        }

        @JsonProperty("set")
        private SetType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof SetWrapper && equalTo((SetWrapper) other));
        }

        private boolean equalTo(SetWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("SetWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("map")
    private static class MapWrapper implements Base {
        private final MapType value;

        @JsonCreator
        private MapWrapper(@JsonProperty("map") MapType value) {
            Objects.requireNonNull(value, "map cannot be null");
            this.value = value;
        }

        @JsonProperty("map")
        private MapType getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof MapWrapper && equalTo((MapWrapper) other));
        }

        private boolean equalTo(MapWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("MapWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("reference")
    private static class ReferenceWrapper implements Base {
        private final TypeName value;

        @JsonCreator
        private ReferenceWrapper(@JsonProperty("reference") TypeName value) {
            Objects.requireNonNull(value, "reference cannot be null");
            this.value = value;
        }

        @JsonProperty("reference")
        private TypeName getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof ReferenceWrapper && equalTo((ReferenceWrapper) other));
        }

        private boolean equalTo(ReferenceWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("ReferenceWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("external")
    private static class ExternalWrapper implements Base {
        private final ExternalReference value;

        @JsonCreator
        private ExternalWrapper(@JsonProperty("external") ExternalReference value) {
            Objects.requireNonNull(value, "external cannot be null");
            this.value = value;
        }

        @JsonProperty("external")
        private ExternalReference getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof ExternalWrapper && equalTo((ExternalWrapper) other));
        }

        private boolean equalTo(ExternalWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("ExternalWrapper")
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
