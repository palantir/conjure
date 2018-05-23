package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = QueryParameterType.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class QueryParameterType {
    private final ParameterId paramId;

    private volatile int memoizedHashCode;

    private QueryParameterType(ParameterId paramId) {
        validateFields(paramId);
        this.paramId = paramId;
    }

    @JsonProperty("paramId")
    public ParameterId getParamId() {
        return this.paramId;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof QueryParameterType && equalTo((QueryParameterType) other));
    }

    private boolean equalTo(QueryParameterType other) {
        return this.paramId.equals(other.paramId);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(paramId);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("QueryParameterType")
                .append("{")
                .append("paramId")
                .append(": ")
                .append(paramId)
                .append("}")
                .toString();
    }

    public static QueryParameterType of(ParameterId paramId) {
        return builder().paramId(paramId).build();
    }

    private static void validateFields(ParameterId paramId) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, paramId, "paramId");
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
        private ParameterId paramId;

        private Builder() {}

        public Builder from(QueryParameterType other) {
            paramId(other.getParamId());
            return this;
        }

        @JsonSetter("paramId")
        public Builder paramId(ParameterId paramId) {
            this.paramId = Objects.requireNonNull(paramId, "paramId cannot be null");
            return this;
        }

        public QueryParameterType build() {
            return new QueryParameterType(paramId);
        }
    }
}
