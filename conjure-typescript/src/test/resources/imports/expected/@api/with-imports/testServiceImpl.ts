import { IHttpApiBridge } from "@foundry/conjure-fe-lib";
import { IBackingFileSystem } from "@palantir/foundry-catalog-api-datasets";
import { IStringExample } from "@palantir/test-api";

export class TestService {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public testEndpoint(
        importedString: IStringExample
    ): Promise<IBackingFileSystem> {
        return this.bridge.callEndpoint<IBackingFileSystem>({
            data: importedString,
            endpointName: "testEndpoint",
            endpointPath: "/catalog/testEndpoint",
            method: "POST",
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
