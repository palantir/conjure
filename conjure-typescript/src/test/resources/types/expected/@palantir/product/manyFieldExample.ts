import { StringAliasExample } from "./stringAliasExample";

export interface IManyFieldExample {
    'alias': StringAliasExample;
    'doubleValue': number;
    'integer': number;
    'items': string[];
    'map': { [key: string]: string };
    'set': string[];
    'string': string;
    'optionalItem'?: string;
}
