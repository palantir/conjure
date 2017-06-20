package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = ListExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ListExample {
    private final List<String> items;

    private final List<Integer> primitiveItems;

    private ListExample(List<String> items, List<Integer> primitiveItems) {
        validateFields(items, primitiveItems);
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.primitiveItems = Collections.unmodifiableList(new ArrayList<>(primitiveItems));
    }

    @JsonProperty("items")
    public List<String> getItems() {
        return this.items;
    }

    @JsonProperty("primitiveItems")
    public List<Integer> getPrimitiveItems() {
        return this.primitiveItems;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ListExample && equalTo((ListExample) other));
    }

    private boolean equalTo(ListExample other) {
        return this.items.equals(other.items) && this.primitiveItems.equals(other.primitiveItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, primitiveItems);
    }

    @Override
    public String toString() {
        return new StringBuilder("ListExample")
                .append("{")
                .append("items")
                .append(": ")
                .append(items)
                .append(", ")
                .append("primitiveItems")
                .append(": ")
                .append(primitiveItems)
                .append("}")
                .toString();
    }

    public static ListExample of(List<String> items, List<Integer> primitiveItems) {
        return builder().items(items).primitiveItems(primitiveItems).build();
    }

    private static void validateFields(List<String> items, List<Integer> primitiveItems) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, items, "items");
        missingFields = addFieldIfMissing(missingFields, primitiveItems, "primitiveItems");
        if (missingFields != null) {
            throw new IllegalStateException(
                    "Some required fields have not been set: " + missingFields);
        }
    }

    private static List<String> addFieldIfMissing(
            List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(2);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private List<String> items = new ArrayList<>();

        private List<Integer> primitiveItems = new ArrayList<>();

        private Builder() {}

        public Builder from(ListExample other) {
            items(other.getItems());
            primitiveItems(other.getPrimitiveItems());
            return this;
        }

        @JsonSetter("items")
        public Builder items(Collection<String> items) {
            this.items.clear();
            this.items.addAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder addAllItems(Collection<String> items) {
            this.items.addAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder items(String items) {
            this.items.add(items);
            return this;
        }

        @JsonSetter("primitiveItems")
        public Builder primitiveItems(Collection<Integer> primitiveItems) {
            this.primitiveItems.clear();
            this.primitiveItems.addAll(
                    Objects.requireNonNull(primitiveItems, "primitiveItems cannot be null"));
            return this;
        }

        public Builder addAllPrimitiveItems(Collection<Integer> primitiveItems) {
            this.primitiveItems.addAll(
                    Objects.requireNonNull(primitiveItems, "primitiveItems cannot be null"));
            return this;
        }

        public Builder primitiveItems(int primitiveItems) {
            this.primitiveItems.add(primitiveItems);
            return this;
        }

        public ListExample build() {
            return new ListExample(items, primitiveItems);
        }
    }
}
