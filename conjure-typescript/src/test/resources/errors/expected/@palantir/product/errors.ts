export interface IInvalidTypeDefinition {
    'errorCode': "INVALID_ARGUMENT";
    'errorInstanceId': string;
    'errorName': "Conjure:InvalidTypeDefinition";
    'parameters': {
        'typeDef': any;
        'typeName': any;
    }
;
}
export function isInvalidTypeDefinition(
    arg: any
): arg is IInvalidTypeDefinition {
    return arg && arg.errorName === 'Conjure:InvalidTypeDefinition';
}
export interface IInvalidServiceDefinition {
    'errorCode': "INVALID_ARGUMENT";
    'errorInstanceId': string;
    'errorName': "Conjure:InvalidServiceDefinition";
    'parameters': {
        'serviceDef': any;
        'serviceName': any;
    }
;
}
export function isInvalidServiceDefinition(
    arg: any
): arg is IInvalidServiceDefinition {
    return arg && arg.errorName === 'Conjure:InvalidServiceDefinition';
}
export interface IJavaCompilationFailed {
    'errorCode': "INTERNAL";
    'errorInstanceId': string;
    'errorName': "ConjureJava:JavaCompilationFailed";
    'parameters': {
    }
;
}
export function isJavaCompilationFailed(
    arg: any
): arg is IJavaCompilationFailed {
    return arg && arg.errorName === 'ConjureJava:JavaCompilationFailed';
}
