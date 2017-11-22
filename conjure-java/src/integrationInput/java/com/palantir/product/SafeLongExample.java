package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.lib.SafeLong;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = SafeLongExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class SafeLongExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SafeLong safeLongValue;

    private SafeLongExample(SafeLong safeLongValue) {
        validateFields(safeLongValue);
        this.safeLongValue = safeLongValue;
    }

    @JsonProperty("safeLongValue")
    public SafeLong getSafeLongValue() {
        return this.safeLongValue;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SafeLongExample && equalTo((SafeLongExample) other));
    }

    private boolean equalTo(SafeLongExample other) {
        return this.safeLongValue.equals(other.safeLongValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(safeLongValue);
    }

    @Override
    public String toString() {
        return new StringBuilder("SafeLongExample")
                .append("{")
                .append("safeLongValue")
                .append(": ")
                .append(safeLongValue)
                .append("}")
                .toString();
    }

    public static SafeLongExample of(SafeLong safeLongValue) {
        return builder().safeLongValue(safeLongValue).build();
    }

    private static void validateFields(SafeLong safeLongValue) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, safeLongValue, "safeLongValue");
        if (missingFields != null) {
            throw new IllegalArgumentException(
                    "Some required fields have not been set: " + missingFields);
        }
    }

    private static List<String> addFieldIfMissing(
            List<String> prev, Object fieldValue, String fieldName) {
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

    @Generated("com.palantir.conjure.gen.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private SafeLong safeLongValue;

        private Builder() {}

        public Builder from(SafeLongExample other) {
            safeLongValue(other.getSafeLongValue());
            return this;
        }

        @JsonSetter("safeLongValue")
        public Builder safeLongValue(SafeLong safeLongValue) {
            this.safeLongValue =
                    Objects.requireNonNull(safeLongValue, "safeLongValue cannot be null");
            return this;
        }

        public SafeLongExample build() {
            return new SafeLongExample(safeLongValue);
        }
    }
}
