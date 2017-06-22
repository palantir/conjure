import { IStringExample } from "./stringExample";

export interface ITestServiceFoo {
    get(
        object: IStringExample
    ): Promise<IStringExample>;
}
