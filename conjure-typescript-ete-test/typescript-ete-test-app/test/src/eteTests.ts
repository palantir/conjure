/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

import { assert } from "chai";

import { DefaultHttpApiBridge } from "@elements/conjure-fe-lib";

import { ICalculatorService } from "../../src/conjure";
import { CalculatorService } from "../../src/conjure";

describe.only("End to end calculator service tests", () => {
    let service: ICalculatorService;

    before(() => {
        const token = "token";
        service = new CalculatorService(
            new DefaultHttpApiBridge("http://localhost:8554/typescript-ete-test/api", token));
    });

    it("Test identities", (done) => {
        service.getIdentities().then((response) => {
            assert.strictEqual(response.additive, 0);
            assert.strictEqual(response.multiplicative, 1);
            done();
        }).catch((err) => {
            done(err);
        });
    });
});
