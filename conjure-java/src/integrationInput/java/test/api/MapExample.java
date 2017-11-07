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

@JsonDeserialize(builder = MapExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class MapExample {
    private final Map<String, String> items;

    private MapExample(Map<String, String> items) {
        validateFields(items);
        this.items = Collections.unmodifiableMap(items);
    }

    @JsonProperty("items")
    public Map<String, String> getItems() {
        return this.items;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof MapExample && equalTo((MapExample) other));
    }

    private boolean equalTo(MapExample other) {
        return this.items.equals(other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return new StringBuilder("MapExample")
                .append("{")
                .append("items")
                .append(": ")
                .append(items)
                .append("}")
                .toString();
    }

    public static MapExample of(Map<String, String> items) {
        return builder().items(items).build();
    }

    private static void validateFields(Map<String, String> items) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, items, "items");
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
        private Map<String, String> items = new LinkedHashMap<>();

        private Builder() {}

        public Builder from(MapExample other) {
            items(other.getItems());
            return this;
        }

        @JsonSetter("items")
        public Builder items(Map<String, String> items) {
            this.items.clear();
            this.items.putAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder putAllItems(Map<String, String> items) {
            this.items.putAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder items(String key, String value) {
            this.items.put(key, value);
            return this;
        }

        public MapExample build() {
            return new MapExample(items);
        }
    }
}
