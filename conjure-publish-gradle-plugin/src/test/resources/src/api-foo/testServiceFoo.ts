import { IStringExample } from "@palantir/api";

export interface ITestServiceFoo {
    get(
        object: IStringExample
    ): Promise<IStringExample>;
}
