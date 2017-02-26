package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(
        builder = BinaryExample.Builder.class
)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class BinaryExample {
    private final byte[] binary;

    private BinaryExample(@JsonProperty("binary") byte[] binary) {
        validateFields(binary);
        this.binary = binary;
    }

    @JsonProperty("binary")
    public byte[] getBinary() {
        return this.binary;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof BinaryExample && equalTo((BinaryExample) other));
    }

    private boolean equalTo(BinaryExample other) {
        return Arrays.equals(this.binary, other.binary);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{binary});
    }

    @Override
    public String toString() {
        return new StringBuilder("BinaryExample").append("{")
                .append("binary").append(": ").append(Base64.getEncoder().encodeToString(binary))
            .append("}")
            .toString();
    }

    public static BinaryExample of(byte[] binary) {
        return builder()
            .binary(binary)
            .build();
    }

    private static void validateFields(byte[] binary) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, binary, "binary");
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
        private byte[] binary;

        private Builder() {
        }

        public Builder from(BinaryExample other) {
            binary(other.getBinary());
            return this;
        }

        @JsonSetter("binary")
        public Builder binary(byte[] binary) {
            this.binary = Objects.requireNonNull(binary, "binary cannot be null");
            return this;
        }

        public BinaryExample build() {
            return new BinaryExample(binary);
        }
    }
}
