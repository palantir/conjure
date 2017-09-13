package test.api.errors;

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.ErrorGenerator")
public final class ConjureErrors {
    /** Invalid Conjure service definition. */
    public static final ErrorType INVALID_SERVICE_DEFINITION =
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, "Conjure:InvalidServiceDefinition");

    /** Invalid Conjure type definition. */
    public static final ErrorType INVALID_TYPE_DEFINITION =
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, "Conjure:InvalidTypeDefinition");

    private ConjureErrors() {}

    public static ServiceException invalidTypeDefinition(String typeName, Object typeDef) {
        return new ServiceException(
                INVALID_TYPE_DEFINITION,
                SafeArg.of("typeName", typeName),
                UnsafeArg.of("typeDef", typeDef));
    }

    /**
     * @serviceName: Name of the invalid service definition.
     * @serviceDef: Details of the invalid service definition.
     */
    public static ServiceException invalidServiceDefinition(String serviceName, Object serviceDef) {
        return new ServiceException(
                INVALID_SERVICE_DEFINITION,
                SafeArg.of("serviceName", serviceName),
                UnsafeArg.of("serviceDef", serviceDef));
    }
}
