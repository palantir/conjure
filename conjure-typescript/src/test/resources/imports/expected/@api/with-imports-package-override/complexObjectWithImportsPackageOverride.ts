import { AliasExample } from "@palantir/test-api";
import { ComplexAliasExample } from "@palantir/test-api";
import { IStringExample } from "@palantir/test-api";

export interface IComplexObjectWithImportsPackageOverride {
    'aliased': AliasExample;
    'complexAliased': ComplexAliasExample;
    'imported': IStringExample;
    'string': string;
}
