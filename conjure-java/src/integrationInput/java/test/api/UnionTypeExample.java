package test.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Generated;

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

    public static UnionTypeExample of(StringExample stringExampleValue) {
        return new UnionTypeExample(new StringExampleWrapper(stringExampleValue));
    }

    public static UnionTypeExample of(Set<String> setStringValue) {
        return new UnionTypeExample(new SetStringWrapper(setStringValue));
    }

    public static UnionTypeExample of(int integerValue) {
        return new UnionTypeExample(new IntegerWrapper(integerValue));
    }

    public <T> T accept(Visitor<T> visitor) {
        if (value instanceof StringExampleWrapper) {
            return visitor.visit(((StringExampleWrapper) value).value);
        } else if (value instanceof SetStringWrapper) {
            return visitor.visit(((SetStringWrapper) value).value);
        } else if (value instanceof IntegerWrapper) {
            return visitor.visit(((IntegerWrapper) value).value);
        }
        return visitor.visitUnknown();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof UnionTypeExample && equalTo((UnionTypeExample) other))
                || (other instanceof StringExample && value instanceof StringExampleWrapper && Objects.equals(((StringExampleWrapper) value).value, (StringExample) other))
                || (other instanceof Set && value instanceof SetStringWrapper && Objects.equals(((SetStringWrapper) value).value, (Set) other))
                || (other instanceof Integer && value instanceof IntegerWrapper && Objects.equals(((IntegerWrapper) value).value, (Integer) other))
                || (other instanceof Map && value instanceof UnknownWrapper && Objects.equals(((UnknownWrapper) value).value, other));
    }

    private boolean equalTo(UnionTypeExample other) {
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UnionTypeExample{value: " + value + "}";
    }

    public interface Visitor<T> {
        T visit(StringExample stringExampleValue);

        T visit(Set<String> setStringValue);

        T visit(int integerValue);

        T visitUnknown();
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type",
            defaultImpl = UnknownWrapper.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(StringExampleWrapper.class),
            @JsonSubTypes.Type(SetStringWrapper.class),
            @JsonSubTypes.Type(IntegerWrapper.class)
    })
    private interface Base {
    }

    @JsonTypeName("StringExample")
    private static class StringExampleWrapper implements Base {
        private final StringExample value;

        @JsonCreator
        private StringExampleWrapper(@JsonProperty("StringExample") StringExample value) {
            Objects.requireNonNull(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof StringExampleWrapper && equalTo((StringExampleWrapper) other))
                    || (other instanceof StringExample && Objects.equals(value, ((StringExample) other)));
        }

        private boolean equalTo(StringExampleWrapper other) {
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "StringExampleWrapper{value: " + value + "}";
        }

        @JsonProperty("StringExample")
        private StringExample getValue() {
            return value;
        }
    }

    @JsonTypeName("set<string>")
    private static class SetStringWrapper implements Base {
        private final Set<String> value;

        @JsonCreator
        private SetStringWrapper(@JsonProperty("set<string>") Set<String> value) {
            Objects.requireNonNull(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof SetStringWrapper && equalTo((SetStringWrapper) other))
                    || (other instanceof Set && Objects.equals(value, ((Set) other)));
        }

        private boolean equalTo(SetStringWrapper other) {
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "SetStringWrapper{value: " + value + "}";
        }

        @JsonProperty("set<string>")
        private Set<String> getValue() {
            return value;
        }
    }

    @JsonTypeName("integer")
    private static class IntegerWrapper implements Base {
        private final int value;

        @JsonCreator
        private IntegerWrapper(@JsonProperty("integer") int value) {
            Objects.requireNonNull(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof IntegerWrapper && equalTo((IntegerWrapper) other))
                    || (other instanceof Integer && Objects.equals(value, ((Integer) other)));
        }

        private boolean equalTo(IntegerWrapper other) {
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "IntegerWrapper{value: " + value + "}";
        }

        @JsonProperty("integer")
        private int getValue() {
            return value;
        }
    }

    private static class UnknownWrapper implements Base {
        private final Map<String, Object> value;

        @JsonCreator
        private UnknownWrapper(Map<String, Object> value) {
            Objects.requireNonNull(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || (other instanceof UnknownWrapper && equalTo((UnknownWrapper) other))
                    || (other instanceof Map && Objects.equals(value, ((Map) other)));
        }

        private boolean equalTo(UnknownWrapper other) {
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "UnknownWrapper{value: " + value + "}";
        }
    }
}
