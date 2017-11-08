import { IBackingFileSystem } from "@palantir/foundry-catalog-api-datasets";
import { IStringExample } from "@palantir/test-api";

export interface ITestService {
    testEndpoint(
        importedString: IStringExample
    ): Promise<IBackingFileSystem>;
}
