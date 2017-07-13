import { IHttpApiBridge } from "@foundry/conjure-fe-lib";
import { IStringExample } from "./stringExample";

export class TestServiceA {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public get(
        object: IStringExample
    ): Promise<IStringExample> {
        return this.bridge.callEndpoint<IStringExample>({
            data: object,
            endpointName: "get",
            endpointPath: "//get",
            method: "GET",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
            ],
            responseMediaType: "application/json",
        });
    }
}
