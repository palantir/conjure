import { IAnyExample } from "./anyExample";

export interface IManyFieldExample {
    alias: string;
    doubleValue: number;
    integer: number;
    items: string[];
    kebabCase: string;
    map: { [key: string]: string };
    mapAlias: { [key: string]: any };
    reference: IAnyExample;
    referenceAlias: IAnyExample;
    set: string[];
    string: string;
    optionalItem?: string;
}
