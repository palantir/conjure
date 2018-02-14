package com.palantir.product;

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.ErrorGenerator")
public final class ConjureErrors {
    /** Invalid Conjure type definition. */
    public static final ErrorType INVALID_TYPE_DEFINITION =
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, "Conjure:InvalidTypeDefinition");

    /** Invalid Conjure service definition. */
    public static final ErrorType INVALID_SERVICE_DEFINITION =
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, "Conjure:InvalidServiceDefinition");

    private ConjureErrors() {}

    public static ServiceException invalidTypeDefinition(String typeName, Object typeDef) {
        return new ServiceException(
                INVALID_TYPE_DEFINITION,
                SafeArg.of("typeName", typeName),
                UnsafeArg.of("typeDef", typeDef));
    }

    public static ServiceException invalidTypeDefinition(
            Throwable cause, String typeName, Object typeDef) {
        return new ServiceException(
                INVALID_TYPE_DEFINITION,
                cause,
                SafeArg.of("typeName", typeName),
                UnsafeArg.of("typeDef", typeDef));
    }

    /**
     * @param serviceName Name of the invalid service definition.
     * @param serviceDef Details of the invalid service definition.
     */
    public static ServiceException invalidServiceDefinition(String serviceName, Object serviceDef) {
        return new ServiceException(
                INVALID_SERVICE_DEFINITION,
                SafeArg.of("serviceName", serviceName),
                UnsafeArg.of("serviceDef", serviceDef));
    }

    /**
     * @param serviceName Name of the invalid service definition.
     * @param serviceDef Details of the invalid service definition.
     */
    public static ServiceException invalidServiceDefinition(
            Throwable cause, String serviceName, Object serviceDef) {
        return new ServiceException(
                INVALID_SERVICE_DEFINITION,
                cause,
                SafeArg.of("serviceName", serviceName),
                UnsafeArg.of("serviceDef", serviceDef));
    }

    /**
     * Throws a {@link ServiceException} of type InvalidTypeDefinition when {@code shouldThrow} is
     * true.
     *
     * @param shouldThrow Cause the method to throw when true
     * @param typeName
     * @param typeDef
     */
    public static void throwIfInvalidTypeDefinition(
            boolean shouldThrow, String typeName, Object typeDef) {
        if (shouldThrow) {
            throw invalidTypeDefinition(typeName, typeDef);
        }
    }

    /**
     * Throws a {@link ServiceException} of type InvalidServiceDefinition when {@code shouldThrow}
     * is true.
     *
     * @param shouldThrow Cause the method to throw when true
     * @param serviceName Name of the invalid service definition.
     * @param serviceDef Details of the invalid service definition.
     */
    public static void throwIfInvalidServiceDefinition(
            boolean shouldThrow, String serviceName, Object serviceDef) {
        if (shouldThrow) {
            throw invalidServiceDefinition(serviceName, serviceDef);
        }
    }
}
