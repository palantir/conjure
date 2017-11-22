package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Generated;

/** A type which can either be a StringExample, a set of strings, or an integer. */
@Generated("com.palantir.conjure.gen.java.types.UnionGenerator")
public final class UnionTypeExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Base value;

    @JsonCreator
    private UnionTypeExample(Base value) {
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    /** Docs for when UnionTypeExample is of type StringExample. */
    public static UnionTypeExample stringExample(StringExample value) {
        return new UnionTypeExample(new StringExampleWrapper(value));
    }

    public static UnionTypeExample set(Set<String> value) {
        return new UnionTypeExample(new SetWrapper(value));
    }

    public static UnionTypeExample thisFieldIsAnInteger(int value) {
        return new UnionTypeExample(new ThisFieldIsAnIntegerWrapper(value));
    }

    public static UnionTypeExample alsoAnInteger(int value) {
        return new UnionTypeExample(new AlsoAnIntegerWrapper(value));
    }

    public static UnionTypeExample if_(int value) {
        return new UnionTypeExample(new IfWrapper(value));
    }

    public static UnionTypeExample new_(int value) {
        return new UnionTypeExample(new NewWrapper(value));
    }

    public static UnionTypeExample interface_(int value) {
        return new UnionTypeExample(new InterfaceWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof StringExampleWrapper) {
            return visitor.visitStringExample(((StringExampleWrapper) value).value);
        } else if (value instanceof SetWrapper) {
            return visitor.visitSet(((SetWrapper) value).value);
        } else if (value instanceof ThisFieldIsAnIntegerWrapper) {
            return visitor.visitThisFieldIsAnInteger(((ThisFieldIsAnIntegerWrapper) value).value);
        } else if (value instanceof AlsoAnIntegerWrapper) {
            return visitor.visitAlsoAnInteger(((AlsoAnIntegerWrapper) value).value);
        } else if (value instanceof IfWrapper) {
            return visitor.visitIf(((IfWrapper) value).value);
        } else if (value instanceof NewWrapper) {
            return visitor.visitNew(((NewWrapper) value).value);
        } else if (value instanceof InterfaceWrapper) {
            return visitor.visitInterface(((InterfaceWrapper) value).value);
        } else if (value instanceof UnknownWrapper) {
            return visitor.visitUnknown(((UnknownWrapper) value).getType());
        }
        throw new IllegalStateException(
                String.format("Could not identify type %s", value.getClass()));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof UnionTypeExample && equalTo((UnionTypeExample) other))
                || (other instanceof StringExample
                        && value instanceof StringExampleWrapper
                        && Objects.equals(((StringExampleWrapper) value).value, other))
                || (other instanceof Set
                        && value instanceof SetWrapper
                        && Objects.equals(((SetWrapper) value).value, other))
                || (other instanceof Integer
                        && value instanceof ThisFieldIsAnIntegerWrapper
                        && Objects.equals(((ThisFieldIsAnIntegerWrapper) value).value, other))
                || (other instanceof Integer
                        && value instanceof AlsoAnIntegerWrapper
                        && Objects.equals(((AlsoAnIntegerWrapper) value).value, other))
                || (other instanceof Integer
                        && value instanceof IfWrapper
                        && Objects.equals(((IfWrapper) value).value, other))
                || (other instanceof Integer
                        && value instanceof NewWrapper
                        && Objects.equals(((NewWrapper) value).value, other))
                || (other instanceof Integer
                        && value instanceof InterfaceWrapper
                        && Objects.equals(((InterfaceWrapper) value).value, other));
    }

    private boolean equalTo(UnionTypeExample other) {
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringBuilder("UnionTypeExample")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append("}")
                .toString();
    }

    public interface Visitor<T> {
        T visitStringExample(StringExample value);

        T visitSet(Set<String> value);

        T visitThisFieldIsAnInteger(int value);

        T visitAlsoAnInteger(int value);

        T visitIf(int value);

        T visitNew(int value);

        T visitInterface(int value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(StringExampleWrapper.class),
        @JsonSubTypes.Type(SetWrapper.class),
        @JsonSubTypes.Type(ThisFieldIsAnIntegerWrapper.class),
        @JsonSubTypes.Type(AlsoAnIntegerWrapper.class),
        @JsonSubTypes.Type(IfWrapper.class),
        @JsonSubTypes.Type(NewWrapper.class),
        @JsonSubTypes.Type(InterfaceWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("stringExample")
    private static class StringExampleWrapper implements Base {
        private final StringExample value;

        @JsonCreator
        private StringExampleWrapper(@JsonProperty("stringExample") StringExample value) {
            Objects.requireNonNull(value, "stringExample cannot be null");
            this.value = value;
        }

        @JsonProperty("stringExample")
        private StringExample getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof StringExampleWrapper
                            && equalTo((StringExampleWrapper) other));
        }

        private boolean equalTo(StringExampleWrapper other) {
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("StringExampleWrapper")
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
        private final Set<String> value;

        @JsonCreator
        private SetWrapper(@JsonProperty("set") Set<String> value) {
            Objects.requireNonNull(value, "set cannot be null");
            this.value = value;
        }

        @JsonProperty("set")
        private Set<String> getValue() {
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

    @JsonTypeName("thisFieldIsAnInteger")
    private static class ThisFieldIsAnIntegerWrapper implements Base {
        private final int value;

        @JsonCreator
        private ThisFieldIsAnIntegerWrapper(@JsonProperty("thisFieldIsAnInteger") int value) {
            Objects.requireNonNull(value, "thisFieldIsAnInteger cannot be null");
            this.value = value;
        }

        @JsonProperty("thisFieldIsAnInteger")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof ThisFieldIsAnIntegerWrapper
                            && equalTo((ThisFieldIsAnIntegerWrapper) other));
        }

        private boolean equalTo(ThisFieldIsAnIntegerWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("ThisFieldIsAnIntegerWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("alsoAnInteger")
    private static class AlsoAnIntegerWrapper implements Base {
        private final int value;

        @JsonCreator
        private AlsoAnIntegerWrapper(@JsonProperty("alsoAnInteger") int value) {
            Objects.requireNonNull(value, "alsoAnInteger cannot be null");
            this.value = value;
        }

        @JsonProperty("alsoAnInteger")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof AlsoAnIntegerWrapper
                            && equalTo((AlsoAnIntegerWrapper) other));
        }

        private boolean equalTo(AlsoAnIntegerWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("AlsoAnIntegerWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("if")
    private static class IfWrapper implements Base {
        private final int value;

        @JsonCreator
        private IfWrapper(@JsonProperty("if") int value) {
            Objects.requireNonNull(value, "if cannot be null");
            this.value = value;
        }

        @JsonProperty("if")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof IfWrapper && equalTo((IfWrapper) other));
        }

        private boolean equalTo(IfWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("IfWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("new")
    private static class NewWrapper implements Base {
        private final int value;

        @JsonCreator
        private NewWrapper(@JsonProperty("new") int value) {
            Objects.requireNonNull(value, "new cannot be null");
            this.value = value;
        }

        @JsonProperty("new")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof NewWrapper && equalTo((NewWrapper) other));
        }

        private boolean equalTo(NewWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("NewWrapper")
                    .append("{")
                    .append("value")
                    .append(": ")
                    .append(value)
                    .append("}")
                    .toString();
        }
    }

    @JsonTypeName("interface")
    private static class InterfaceWrapper implements Base {
        private final int value;

        @JsonCreator
        private InterfaceWrapper(@JsonProperty("interface") int value) {
            Objects.requireNonNull(value, "interface cannot be null");
            this.value = value;
        }

        @JsonProperty("interface")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof InterfaceWrapper && equalTo((InterfaceWrapper) other));
        }

        private boolean equalTo(InterfaceWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("InterfaceWrapper")
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
