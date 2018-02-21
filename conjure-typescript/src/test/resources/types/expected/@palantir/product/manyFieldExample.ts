export interface IManyFieldExample {
    /** docs for alias field */
    'alias': string;
    /** docs for doubleValue field */
    'doubleValue': number | "NaN";
    /** docs for integer field */
    'integer': number;
    /** docs for items field */
    'items': Array<string>;
    /** docs for map field */
    'map': { [key: string]: string };
    /** docs for set field */
    'set': Array<string>;
    /** docs for string field */
    'string': string;
    /** docs for optionalItem field */
    'optionalItem'?: string | null;
}
