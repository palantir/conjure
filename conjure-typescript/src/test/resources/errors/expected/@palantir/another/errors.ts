export interface IDifferentPackage {
    'errorCode': "INTERNAL";
    'errorInstanceId': string;
    'errorName': "Conjure:DifferentPackage";
    'parameters': {
    }
;
}
export function isDifferentPackage(
    arg: any
): arg is IDifferentPackage {
    return arg && arg.errorName === 'Conjure:DifferentPackage';
}
