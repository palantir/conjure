package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.lib.SafeLong;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.BearerToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.annotation.Generated;

@JsonDeserialize(builder = PrimitiveOptionalsExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class PrimitiveOptionalsExample {
    private final OptionalDouble num;

    private final Optional<Boolean> bool;

    private final OptionalInt integer;

    private final Optional<SafeLong> safelong;

    private final Optional<ResourceIdentifier> rid;

    private final Optional<BearerToken> bearertoken;

    private PrimitiveOptionalsExample(
            OptionalDouble num,
            Optional<Boolean> bool,
            OptionalInt integer,
            Optional<SafeLong> safelong,
            Optional<ResourceIdentifier> rid,
            Optional<BearerToken> bearertoken) {
        validateFields(num, bool, integer, safelong, rid, bearertoken);
        this.num = num;
        this.bool = bool;
        this.integer = integer;
        this.safelong = safelong;
        this.rid = rid;
        this.bearertoken = bearertoken;
    }

    @JsonProperty("num")
    public OptionalDouble getNum() {
        return this.num;
    }

    @JsonProperty("bool")
    public Optional<Boolean> getBool() {
        return this.bool;
    }

    @JsonProperty("integer")
    public OptionalInt getInteger() {
        return this.integer;
    }

    @JsonProperty("safelong")
    public Optional<SafeLong> getSafelong() {
        return this.safelong;
    }

    @JsonProperty("rid")
    public Optional<ResourceIdentifier> getRid() {
        return this.rid;
    }

    @JsonProperty("bearertoken")
    public Optional<BearerToken> getBearertoken() {
        return this.bearertoken;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof PrimitiveOptionalsExample
                        && equalTo((PrimitiveOptionalsExample) other));
    }

    private boolean equalTo(PrimitiveOptionalsExample other) {
        return this.num.equals(other.num)
                && this.bool.equals(other.bool)
                && this.integer.equals(other.integer)
                && this.safelong.equals(other.safelong)
                && this.rid.equals(other.rid)
                && this.bearertoken.equals(other.bearertoken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, bool, integer, safelong, rid, bearertoken);
    }

    @Override
    public String toString() {
        return new StringBuilder("PrimitiveOptionalsExample")
                .append("{")
                .append("num")
                .append(": ")
                .append(num)
                .append(", ")
                .append("bool")
                .append(": ")
                .append(bool)
                .append(", ")
                .append("integer")
                .append(": ")
                .append(integer)
                .append(", ")
                .append("safelong")
                .append(": ")
                .append(safelong)
                .append(", ")
                .append("rid")
                .append(": ")
                .append(rid)
                .append(", ")
                .append("bearertoken")
                .append(": ")
                .append(bearertoken)
                .append("}")
                .toString();
    }

    private static void validateFields(
            OptionalDouble num,
            Optional<Boolean> bool,
            OptionalInt integer,
            Optional<SafeLong> safelong,
            Optional<ResourceIdentifier> rid,
            Optional<BearerToken> bearertoken) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, num, "num");
        missingFields = addFieldIfMissing(missingFields, bool, "bool");
        missingFields = addFieldIfMissing(missingFields, integer, "integer");
        missingFields = addFieldIfMissing(missingFields, safelong, "safelong");
        missingFields = addFieldIfMissing(missingFields, rid, "rid");
        missingFields = addFieldIfMissing(missingFields, bearertoken, "bearertoken");
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
                missingFields = new ArrayList<>(6);
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
        private OptionalDouble num = OptionalDouble.empty();

        private Optional<Boolean> bool = Optional.empty();

        private OptionalInt integer = OptionalInt.empty();

        private Optional<SafeLong> safelong = Optional.empty();

        private Optional<ResourceIdentifier> rid = Optional.empty();

        private Optional<BearerToken> bearertoken = Optional.empty();

        private Builder() {}

        public Builder from(PrimitiveOptionalsExample other) {
            num(other.getNum());
            bool(other.getBool());
            integer(other.getInteger());
            safelong(other.getSafelong());
            rid(other.getRid());
            bearertoken(other.getBearertoken());
            return this;
        }

        @JsonSetter("num")
        public Builder num(OptionalDouble num) {
            this.num = Objects.requireNonNull(num, "num cannot be null");
            return this;
        }

        public Builder num(double num) {
            this.num = OptionalDouble.of(num);
            return this;
        }

        @JsonSetter("bool")
        public Builder bool(Optional<Boolean> bool) {
            this.bool = Objects.requireNonNull(bool, "bool cannot be null");
            return this;
        }

        public Builder bool(boolean bool) {
            this.bool = Optional.of(bool);
            return this;
        }

        @JsonSetter("integer")
        public Builder integer(OptionalInt integer) {
            this.integer = Objects.requireNonNull(integer, "integer cannot be null");
            return this;
        }

        public Builder integer(int integer) {
            this.integer = OptionalInt.of(integer);
            return this;
        }

        @JsonSetter("safelong")
        public Builder safelong(Optional<SafeLong> safelong) {
            this.safelong = Objects.requireNonNull(safelong, "safelong cannot be null");
            return this;
        }

        public Builder safelong(SafeLong safelong) {
            this.safelong =
                    Optional.of(Objects.requireNonNull(safelong, "safelong cannot be null"));
            return this;
        }

        @JsonSetter("rid")
        public Builder rid(Optional<ResourceIdentifier> rid) {
            this.rid = Objects.requireNonNull(rid, "rid cannot be null");
            return this;
        }

        public Builder rid(ResourceIdentifier rid) {
            this.rid = Optional.of(Objects.requireNonNull(rid, "rid cannot be null"));
            return this;
        }

        @JsonSetter("bearertoken")
        public Builder bearertoken(Optional<BearerToken> bearertoken) {
            this.bearertoken = Objects.requireNonNull(bearertoken, "bearertoken cannot be null");
            return this;
        }

        public Builder bearertoken(BearerToken bearertoken) {
            this.bearertoken =
                    Optional.of(Objects.requireNonNull(bearertoken, "bearertoken cannot be null"));
            return this;
        }

        public PrimitiveOptionalsExample build() {
            return new PrimitiveOptionalsExample(num, bool, integer, safelong, rid, bearertoken);
        }
    }
}
