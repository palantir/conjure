/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

"use strict";

const baseWebpackConfig = require("./webpack.config");

// Add code coverage to the webpack build
const webpackConfig = Object.assign({}, baseWebpackConfig, {
    devtool: "inline-source-map",
});

delete webpackConfig.entry;
delete webpackConfig.output;

module.exports = (config) => {
    config.set({
        basePath: process.cwd(),
        browserDisconnectTimeout: 10000,
        browserNoActivityTimeout: 1000000,
        browsers: ["Chrome"],
        client: {
            useIframe: true,
            mocha: {
                reporter: "html",
                ui: "bdd",
            },
        },
        files: [
            // All test files must have "Tests" as a suffix to be included by the index file.
            "build/tmp/test/src/index.js",
        ],
        frameworks: ["mocha", "chai", "sinon"],
        preprocessors: {
            "build/tmp/test/src/index.js": ["webpack", "sourcemap"],
        },
        reporters: ["mocha"],
        webpack: webpackConfig,
        webpackMiddleware: {
            noInfo: true,
            stats: webpackConfig.stats,
        },
    });
};
