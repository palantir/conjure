import { IHttpApiBridge } from "@foundry/conjure-fe-lib";

export class DuplicateService {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public copy1(): Promise<void> {
        return this.bridge.callEndpoint<void>({
            data: undefined,
            endpointName: "copy1",
            endpointPath: "/copy1",
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
