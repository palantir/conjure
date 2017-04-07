package test.api;

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
import java.util.Set;
import javax.annotation.Generated;

/** A type which can either be a StringExample, a set of strings, or an integer. */
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class UnionTypeExample {
    private final Base value;

    @JsonCreator
    private UnionTypeExample(Base value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @JsonValue
    private Base getValue() {
        return value;
    }

    /** Docs for when UnionTypeExample is of type StringExample. */
    public static UnionTypeExample of(StringExample value) {
        return new UnionTypeExample(new StringExampleWrapper(value));
    }

    public static UnionTypeExample of(Set<String> value) {
        return new UnionTypeExample(new SetWrapper(value));
    }

    public static UnionTypeExample of(int value) {
        return new UnionTypeExample(new NumberWrapper(value));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof NumberWrapper) {
            return visitor.visit(((NumberWrapper) value).value);
        } else if (value instanceof SetWrapper) {
            return visitor.visit(((SetWrapper) value).value);
        } else if (value instanceof StringExampleWrapper) {
            return visitor.visit(((StringExampleWrapper) value).value);
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
                || (other instanceof Integer
                        && value instanceof NumberWrapper
                        && Objects.equals(((NumberWrapper) value).value, other))
                || (other instanceof Set
                        && value instanceof SetWrapper
                        && Objects.equals(((SetWrapper) value).value, other))
                || (other instanceof StringExample
                        && value instanceof StringExampleWrapper
                        && Objects.equals(((StringExampleWrapper) value).value, other));
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
        T visit(int value);

        T visit(Set<String> value);

        T visit(StringExample value);

        T visitUnknown(String unknownType);
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true,
        defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(NumberWrapper.class),
        @JsonSubTypes.Type(SetWrapper.class),
        @JsonSubTypes.Type(StringExampleWrapper.class)
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface Base {}

    @JsonTypeName("stringExample")
    private static class StringExampleWrapper implements Base {
        private final StringExample value;

        @JsonCreator
        private StringExampleWrapper(@JsonProperty("stringExample") StringExample value) {
            Objects.requireNonNull(value);
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
            Objects.requireNonNull(value);
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

    @JsonTypeName("number")
    private static class NumberWrapper implements Base {
        private final int value;

        @JsonCreator
        private NumberWrapper(@JsonProperty("number") int value) {
            Objects.requireNonNull(value);
            this.value = value;
        }

        @JsonProperty("number")
        private int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof NumberWrapper && equalTo((NumberWrapper) other));
        }

        private boolean equalTo(NumberWrapper other) {
            return this.value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return new StringBuilder("NumberWrapper")
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
            Objects.requireNonNull(type);
            Objects.requireNonNull(value);
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
