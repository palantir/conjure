import { ICreateDatasetRequest } from "@palantir/product";
import { IBackingFileSystem } from "@palantir/product-datasets";
import { IDataset } from "@palantir/product-datasets";

export interface ITestService {
    createDataset(
        request: ICreateDatasetRequest,
        testHeaderArg: string
    ): Promise<IDataset>;

    getBranches(
        datasetRid: string
    ): Promise<string[]>;

    /** Gets all branches of this dataset. */
    getBranchesDeprecated(
        datasetRid: string
    ): Promise<string[]>;

    getDataset(
        datasetRid: string
    ): Promise<IDataset | undefined>;

    /** Returns a mapping from file system id to backing file system configuration. */
    getFileSystems(): Promise<{ [key: string]: IBackingFileSystem }>;

    getRawData(
        datasetRid: string
    ): Promise<any>;

    maybeGetRawData(
        datasetRid: string
    ): Promise<any | undefined>;

    resolveBranch(
        datasetRid: string,
        branch: string
    ): Promise<string | undefined>;

    testBoolean(): Promise<boolean>;

    testDouble(): Promise<number>;

    testInteger(): Promise<number>;

    testParam(
        datasetRid: string
    ): Promise<string | undefined>;

    uploadRawData(
        input: any
    ): Promise<void>;
}
