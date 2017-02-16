package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(
        builder = AnyExample.Builder.class
)
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class AnyExample {
    private final Object any;

    private AnyExample(@JsonProperty("any") Object any) {
        validateFields(any);
        this.any = any;
    }

    @JsonProperty("any")
    public Object getAny() {
        return this.any;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof AnyExample && equalTo((AnyExample) other));
    }

    private boolean equalTo(AnyExample other) {
        return this.any.equals(other.any);
    }

    @Override
    public int hashCode() {
        return Objects.hash(any);
    }

    @Override
    public String toString() {
        return new StringBuilder("AnyExample").append("{")
                .append("any").append(": ").append(any)
            .append("}")
            .toString();
    }

    public static AnyExample of(Object any) {
        return builder()
            .any(any)
            .build();
    }

    private static void validateFields(Object any) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, any, "any");
        if (missingFields != null) {
            throw new IllegalStateException("Some required fields have not been set: " + missingFields);
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(1);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Object any;

        private Builder() {
        }

        public Builder from(AnyExample other) {
            any(other.getAny());
            return this;
        }

        public Builder any(Object any) {
            this.any = Objects.requireNonNull(any, "any cannot be null");
            return this;
        }

        public AnyExample build() {
            return new AnyExample(any);
        }
    }
}
