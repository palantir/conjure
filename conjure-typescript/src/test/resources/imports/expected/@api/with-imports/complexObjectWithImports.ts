import { IStringExample } from "@palantir/test-api";

export interface IComplexObjectWithImports {
    'aliased': string;
    'complexAliased': IStringExample;
    'imported': IStringExample;
    'string': string;
}
