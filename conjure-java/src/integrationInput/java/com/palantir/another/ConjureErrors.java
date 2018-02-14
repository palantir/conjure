package com.palantir.another;

import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.ErrorGenerator")
public final class ConjureErrors {
    /** Different package. */
    public static final ErrorType DIFFERENT_PACKAGE =
            ErrorType.create(ErrorType.Code.INTERNAL, "Conjure:DifferentPackage");

    private ConjureErrors() {}

    public static ServiceException differentPackage() {
        return new ServiceException(DIFFERENT_PACKAGE);
    }

    public static ServiceException differentPackage(Throwable cause) {
        return new ServiceException(DIFFERENT_PACKAGE, cause);
    }

    /**
     * Throws a {@link ServiceException} of type DifferentPackage when {@code shouldThrow} is true.
     *
     * @param shouldThrow Cause the method to throw when true
     */
    public static void throwIfDifferentPackage(boolean shouldThrow) {
        if (shouldThrow) {
            throw differentPackage();
        }
    }
}
