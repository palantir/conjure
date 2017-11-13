import { IHttpApiBridge } from "@foundry/conjure-fe-lib";

export class MyService {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public getFoo(): Promise<void> {
        return this.bridge.callEndpoint<void>({
            data: undefined,
            endpointName: "getFoo",
            endpointPath: "/foo",
            headers: {
            },
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
