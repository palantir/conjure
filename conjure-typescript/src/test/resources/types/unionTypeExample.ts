import { IStringExample } from "./stringExample";

export interface IUnionTypeExample_StringExample {
    'stringExample': IStringExample;
    'type': "stringExample";
}
export interface IUnionTypeExample_Set {
    'set': string[];
    'type': "set";
}
export interface IUnionTypeExample_Number {
    'number': number;
    'type': "number";
}
export interface IUnionTypeExample_Map {
    'map': { [key: string]: string };
    'type': "map";
}
export interface IUnionTypeExample_Return {
    'return': number;
    'type': "return";
}
export interface IUnionTypeExample_If {
    'if': number;
    'type': "if";
}
export interface IUnionTypeExample_Any {
    'any': number;
    'type': "any";
}
export type IUnionTypeExample = (IUnionTypeExample_StringExample | IUnionTypeExample_Set | IUnionTypeExample_Number | IUnionTypeExample_Map | IUnionTypeExample_Return | IUnionTypeExample_If | IUnionTypeExample_Any);
function isStringExample(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_StringExample {
    return (obj.type === "stringExample");
}
function isSet(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_Set {
    return (obj.type === "set");
}
function isNumber(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_Number {
    return (obj.type === "number");
}
function isMap(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_Map {
    return (obj.type === "map");
}
function isReturn(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_Return {
    return (obj.type === "return");
}
function isIf(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_If {
    return (obj.type === "if");
}
function isAny(
    obj: IUnionTypeExample
): obj is IUnionTypeExample_Any {
    return (obj.type === "any");
}
function stringExample(
    stringExample: IStringExample
): IUnionTypeExample_StringExample {
    return {
        stringExample: stringExample,
        type: "stringExample",
    };
}
function set_(
    set_: string[]
): IUnionTypeExample_Set {
    return {
        set: set_,
        type: "set",
    };
}
function number_(
    number_: number
): IUnionTypeExample_Number {
    return {
        number: number_,
        type: "number",
    };
}
function map(
    map: { [key: string]: string }
): IUnionTypeExample_Map {
    return {
        map: map,
        type: "map",
    };
}
function return_(
    return_: number
): IUnionTypeExample_Return {
    return {
        return: return_,
        type: "return",
    };
}
function if_(
    if_: number
): IUnionTypeExample_If {
    return {
        if: if_,
        type: "if",
    };
}
function any_(
    any_: number
): IUnionTypeExample_Any {
    return {
        any: any_,
        type: "any",
    };
}
export const IUnionTypeExample = {
    any: any_,
    if: if_,
    isAny: isAny,
    isIf: isIf,
    isMap: isMap,
    isNumber: isNumber,
    isReturn: isReturn,
    isSet: isSet,
    isStringExample: isStringExample,
    map: map,
    number: number_,
    return: return_,
    set: set_,
    stringExample: stringExample,
};
