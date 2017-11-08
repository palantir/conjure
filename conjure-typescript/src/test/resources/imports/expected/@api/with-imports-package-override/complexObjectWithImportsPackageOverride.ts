import { IStringExample } from "@palantir/test-api";

export interface IComplexObjectWithImportsPackageOverride {
    'aliased': string;
    'complexAliased': IStringExample;
    'imported': IStringExample;
    'string': string;
}
