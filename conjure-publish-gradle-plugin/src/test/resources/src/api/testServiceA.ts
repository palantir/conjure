import { IStringExample } from "./stringExample";

export interface ITestServiceA {
    get(
        object: IStringExample
    ): Promise<IStringExample>;
}
