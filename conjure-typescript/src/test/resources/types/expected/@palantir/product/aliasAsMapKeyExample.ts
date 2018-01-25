import { IManyFieldExample } from "./manyFieldExample";

export interface IAliasAsMapKeyExample {
    'bearertokens': { [key: string]: IManyFieldExample };
    'datetimes': { [key: string]: IManyFieldExample };
    'integers': { [key: number]: IManyFieldExample };
    'rids': { [key: string]: IManyFieldExample };
    'safelongs': { [key: number]: IManyFieldExample };
    'strings': { [key: string]: IManyFieldExample };
    'uuids': { [key: string]: IManyFieldExample };
}
