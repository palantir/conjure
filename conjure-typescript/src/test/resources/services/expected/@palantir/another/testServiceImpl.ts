import { IHttpApiBridge } from "@foundry/conjure-fe-lib";
import { ICreateDatasetRequest } from "@palantir/product";
import { IBackingFileSystem } from "@palantir/product-datasets";
import { IDataset } from "@palantir/product-datasets";

export class TestService {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

    public createDataset(
        request: ICreateDatasetRequest
    ): Promise<IDataset> {
        return this.bridge.callEndpoint<IDataset>({
            data: request,
            endpointName: "createDataset",
            endpointPath: "/catalog/datasets",
            method: "POST",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public getBranches(
        datasetRid: string
    ): Promise<string[]> {
        return this.bridge.callEndpoint<string[]>({
            data: undefined,
            endpointName: "getBranches",
            endpointPath: "/catalog/datasets/{datasetRid}/branches",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public getBranchesDeprecated(
        datasetRid: string
    ): Promise<string[]> {
        return this.bridge.callEndpoint<string[]>({
            data: undefined,
            endpointName: "getBranchesDeprecated",
            endpointPath: "/catalog/datasets/{datasetRid}/branchesDeprecated",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public getDataset(
        datasetRid: string
    ): Promise<IDataset | undefined> {
        return this.bridge.callEndpoint<IDataset | undefined>({
            data: undefined,
            endpointName: "getDataset",
            endpointPath: "/catalog/datasets/{datasetRid}",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public getFileSystems(): Promise<{ [key: string]: IBackingFileSystem }> {
        return this.bridge.callEndpoint<{ [key: string]: IBackingFileSystem }>({
            data: undefined,
            endpointName: "getFileSystems",
            endpointPath: "/catalog/fileSystems",
            method: "GET",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public getRawData(
        datasetRid: string
    ): Promise<any> {
        return this.bridge.callEndpoint<any>({
            data: undefined,
            endpointName: "getRawData",
            endpointPath: "/catalog/datasets/{datasetRid}/raw",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/octet-stream",
        });
    }

    public maybeGetRawData(
        datasetRid: string
    ): Promise<any | undefined> {
        return this.bridge.callEndpoint<any | undefined>({
            data: undefined,
            endpointName: "maybeGetRawData",
            endpointPath: "/catalog/datasets/{datasetRid}/raw-maybe",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public resolveBranch(
        datasetRid: string,
        branch: string
    ): Promise<string | undefined> {
        return this.bridge.callEndpoint<string | undefined>({
            data: undefined,
            endpointName: "resolveBranch",
            endpointPath: "/catalog/datasets/{datasetRid}/branches/{branch:.+}/resolve",
            method: "GET",
            pathArguments: [
                datasetRid,
                branch,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public testBoolean(): Promise<boolean> {
        return this.bridge.callEndpoint<boolean>({
            data: undefined,
            endpointName: "testBoolean",
            endpointPath: "/catalog/boolean",
            method: "GET",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public testDouble(): Promise<number> {
        return this.bridge.callEndpoint<number>({
            data: undefined,
            endpointName: "testDouble",
            endpointPath: "/catalog/double",
            method: "GET",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public testInteger(): Promise<number> {
        return this.bridge.callEndpoint<number>({
            data: undefined,
            endpointName: "testInteger",
            endpointPath: "/catalog/integer",
            method: "GET",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public testParam(
        datasetRid: string
    ): Promise<string | undefined> {
        return this.bridge.callEndpoint<string | undefined>({
            data: undefined,
            endpointName: "testParam",
            endpointPath: "/catalog/datasets/{datasetRid}/testParam",
            method: "GET",
            pathArguments: [
                datasetRid,
            ],
            queryArguments: {
            },
            requestMediaType: "application/json",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }

    public uploadRawData(
        input: any
    ): Promise<void> {
        return this.bridge.callEndpoint<void>({
            data: input,
            endpointName: "uploadRawData",
            endpointPath: "/catalog/datasets/upload-raw",
            method: "POST",
            pathArguments: [
            ],
            queryArguments: {
            },
            requestMediaType: "application/octet-stream",
            requiredHeaders: [
                "Authorization",
            ],
            responseMediaType: "application/json",
        });
    }
}
