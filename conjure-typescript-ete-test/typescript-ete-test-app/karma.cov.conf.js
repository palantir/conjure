/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

"use strict";

const path = require("path");

const baseWebpackConfig = require("./webpack.config");

// Add code coverage to the webpack build
const webpackConfig = Object.assign({}, baseWebpackConfig, {
    devtool: "inline-source-map",
    module: Object.assign({}, baseWebpackConfig.module, {
        postLoaders: [{
            test: /\.js$/,
            include: path.resolve("build/tmp/src/"),
            loader: "istanbul-instrumenter",
        }].concat(baseWebpackConfig.module.postLoaders || []),
    }),
});

delete webpackConfig.entry;
delete webpackConfig.output;

module.exports = (config) => {
    config.set({
        basePath: process.cwd(),
        browserDisconnectTimeout: 10000,
        browserNoActivityTimeout: 1000000,
        browsers: ["PhantomJSNoSecurity"],
        client: {
            useIframe: false,
        },
        coverageReporter: {
            check: {
                each: {
                    statements: 0,
                    lines: 0,
                },
            },
            reporters: [
                {type: "html", dir: "build/coverage"},
                {type: "text"},
            ],
            includeAllSources: true,
            phantomjsLauncher: {
                exitOnResourceError: true,
            },
            watermarks: {
                statements: [80, 90],
                lines: [80, 90],
            },
        },
        customLaunchers: {
            PhantomJSNoSecurity: {
                base: "PhantomJS",
                flags: ["--ssl-protocol=any"],
            },
        },
        files: [
            // All test files must have "Tests" as a suffix to be included by the index file.
            "build/tmp/test/src/index.js",
        ],
        frameworks: ["mocha", "chai", "sinon", "phantomjs-shim"],
        preprocessors: {
            "build/tmp/src/**/*": ["coverage"],
            "build/tmp/test/src/index.js": ["webpack", "sourcemap"],
        },
        reporters: ["mocha", "coverage"],
        singleRun: true,
        webpack: webpackConfig,
        webpackMiddleware: {
            noInfo: true,
            stats: webpackConfig.stats,
        },
    });
};
