import { IStringExample } from "./stringExample";

export interface IUnionTypeExample {
    [key: string]: any;
    type: string;
    "integer"?: number;
    "map<string, string>"?: { [key: string]: string };
    "set<string>"?: string[];
    "stringExample"?: IStringExample;
}
export function integer(
    obj: IUnionTypeExample
) {
    if (obj.type === "integer") {
        return obj["integer"];
    }
    return undefined;
}
export function mapStringString(
    obj: IUnionTypeExample
) {
    if (obj.type === "map<string, string>") {
        return obj["map<string, string>"];
    }
    return undefined;
}
export function setString(
    obj: IUnionTypeExample
) {
    if (obj.type === "set<string>") {
        return obj["set<string>"];
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
