package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = DateTimeExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class DateTimeExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ZonedDateTime datetime;

    private DateTimeExample(ZonedDateTime datetime) {
        validateFields(datetime);
        this.datetime = datetime;
    }

    @JsonProperty("datetime")
    public ZonedDateTime getDatetime() {
        return this.datetime;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof DateTimeExample && equalTo((DateTimeExample) other));
    }

    private boolean equalTo(DateTimeExample other) {
        return this.datetime.isEqual(other.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime.toInstant());
    }

    @Override
    public String toString() {
        return new StringBuilder("DateTimeExample")
                .append("{")
                .append("datetime")
                .append(": ")
                .append(datetime)
                .append("}")
                .toString();
    }

    public static DateTimeExample of(ZonedDateTime datetime) {
        return builder().datetime(datetime).build();
    }

    private static void validateFields(ZonedDateTime datetime) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, datetime, "datetime");
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
        private ZonedDateTime datetime;

        private Builder() {}

        public Builder from(DateTimeExample other) {
            datetime(other.getDatetime());
            return this;
        }

        @JsonSetter("datetime")
        public Builder datetime(ZonedDateTime datetime) {
            this.datetime = Objects.requireNonNull(datetime, "datetime cannot be null");
            return this;
        }

        public DateTimeExample build() {
            return new DateTimeExample(datetime);
        }
    }
}
