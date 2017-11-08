import { IHttpApiBridge } from "@foundry/conjure-fe-lib";

export class DuplicateService {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public copy2(): Promise<void> {
        return this.bridge.callEndpoint<void>({
            data: undefined,
            endpointName: "copy2",
            endpointPath: "/copy2",
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
