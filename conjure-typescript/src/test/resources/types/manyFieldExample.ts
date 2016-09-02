export interface IManyFieldExample {
    alias: string;
    doubleValue: number;
    integer: number;
    items: string[];
    map: { [key: string]: string };
    set: string[];
    string: string;
    optionalItem?: string;
}
