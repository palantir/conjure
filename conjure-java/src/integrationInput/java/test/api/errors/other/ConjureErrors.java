package test.api.errors.other;

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
}
