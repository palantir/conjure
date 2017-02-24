package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(
        builder = AnyMapExample.Builder.class
)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class AnyMapExample {
    private final Map<String, Object> items;

    private AnyMapExample(@JsonProperty("items") Map<String, Object> items) {
        validateFields(items);
        this.items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    @JsonProperty("items")
    public Map<String, Object> getItems() {
        return this.items;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof AnyMapExample && equalTo((AnyMapExample) other));
    }

    private boolean equalTo(AnyMapExample other) {
        return this.items.equals(other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return new StringBuilder("AnyMapExample").append("{")
                .append("items").append(": ").append(items)
            .append("}")
            .toString();
    }

    public static AnyMapExample of(Map<String, Object> items) {
        return builder()
            .items(items)
            .build();
    }

    private static void validateFields(Map<String, Object> items) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, items, "items");
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

    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    public static final class Builder {
        private Map<String, Object> items = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder from(AnyMapExample other) {
            items(other.getItems());
            return this;
        }

        @JsonSetter("items")
        public Builder items(Map<String, Object> items) {
            this.items.clear();
            this.items.putAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder putAllItems(Map<String, Object> items) {
            this.items.putAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder items(String key, Object value) {
            this.items.put(key, value);
            return this;
        }

        public AnyMapExample build() {
            return new AnyMapExample(items);
        }
    }
}
