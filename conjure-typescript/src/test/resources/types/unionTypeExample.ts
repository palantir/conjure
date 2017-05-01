import { IStringExample } from "./stringExample";

export interface IUnionTypeExample {
    [key: string]: any;
    type: string;
    "map"?: { [key: string]: string };
    "number"?: number;
    "set"?: string[];
    "stringExample"?: IStringExample;
}
export function map(
    obj: IUnionTypeExample
) {
    if (obj.type === "map") {
        return obj["map"];
    }
    return undefined;
}
export function number(
    obj: IUnionTypeExample
) {
    if (obj.type === "number") {
        return obj["number"];
    }
    return undefined;
}
export function set(
    obj: IUnionTypeExample
) {
    if (obj.type === "set") {
        return obj["set"];
    }
    return undefined;
}
export function stringExample(
    obj: IUnionTypeExample
) {
    if (obj.type === "stringExample") {
        return obj["stringExample"];
    }
    return undefined;
}
