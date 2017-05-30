package test.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EmptyObjectExample {
    private static final EmptyObjectExample INSTANCE = new EmptyObjectExample();

    private EmptyObjectExample() {}

    @Override
    public String toString() {
        return new StringBuilder("EmptyObjectExample").append("{").append("}").toString();
    }

    @JsonCreator
    public static EmptyObjectExample of() {
        return INSTANCE;
    }
}
