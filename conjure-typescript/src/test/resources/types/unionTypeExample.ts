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
    if (obj.type === "map<string, string>") {
        return obj["map"];
    }
    return undefined;
}
export function number(
    obj: IUnionTypeExample
) {
    if (obj.type === "integer") {
        return obj["number"];
    }
    return undefined;
}
export function set(
    obj: IUnionTypeExample
) {
    if (obj.type === "set<string>") {
        return obj["set"];
    }
    return undefined;
}
export function stringExample(
    obj: IUnionTypeExample
) {
    if (obj.type === "StringExample") {
        return obj["stringExample"];
    }
    return undefined;
}
