package test.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.ri.ResourceIdentifier;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class RidAliasExample {
    private final ResourceIdentifier value;

    private RidAliasExample(ResourceIdentifier value) {
        Objects.requireNonNull(value, "value cannot be null");
        this.value = value;
    }

    @JsonValue
    public ResourceIdentifier get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof RidAliasExample
                        && this.value.equals(((RidAliasExample) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static RidAliasExample valueOf(String value) {
        return new RidAliasExample(ResourceIdentifier.valueOf(value));
    }

    @JsonCreator
    public static RidAliasExample of(ResourceIdentifier value) {
        return new RidAliasExample(value);
    }
}
