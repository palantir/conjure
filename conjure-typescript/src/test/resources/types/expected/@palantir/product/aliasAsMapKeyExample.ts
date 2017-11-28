import { IManyFieldExample } from "./manyFieldExample";

export interface IAliasAsMapKeyExample {
    'bearertokens': { [key: string]: IManyFieldExample };
    'datetimes': { [key: string]: IManyFieldExample };
    'doubles': { [key: number]: IManyFieldExample };
    'integers': { [key: number]: IManyFieldExample };
    'rids': { [key: string]: IManyFieldExample };
    'safelongs': { [key: number]: IManyFieldExample };
    'strings': { [key: string]: IManyFieldExample };
}
