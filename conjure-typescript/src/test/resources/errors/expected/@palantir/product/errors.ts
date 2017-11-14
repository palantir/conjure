export interface IInvalidTypeDefinition {
    'errorCode': "INVALID_ARGUMENT";
    'errorInstanceId': string;
    'errorName': "Conjure:InvalidTypeDefinition";
    'parameters':     {
        'typeName': any;
    }
;
}
export interface IInvalidServiceDefinition {
    'errorCode': "INVALID_ARGUMENT";
    'errorInstanceId': string;
    'errorName': "Conjure:InvalidServiceDefinition";
    'parameters':     {
        'serviceName': any;
    }
;
}
export interface IJavaCompilationFailed {
    'errorCode': "INTERNAL";
    'errorInstanceId': string;
    'errorName': "ConjureJava:JavaCompilationFailed";
    'parameters':     {
    }
;
}
