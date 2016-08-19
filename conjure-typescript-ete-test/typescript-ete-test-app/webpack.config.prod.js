/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
/*
 * Used for production builds.
 */

"use strict";

const path = require("path");

const ExtractTextPlugin = require("extract-text-webpack-plugin");
const webpack = require("webpack");

const baseConfig = require("./webpack.config");

module.exports = Object.assign({}, baseConfig, {
    // Outputs to build/min rather than build/src
    output: {
        filename: "[name]-[hash].js",
        path: path.join(__dirname, "build", "min"),
    },
    devtool: null,
    plugins: [
        new ExtractTextPlugin("[name]-[hash].css"),
        ...baseConfig.plugins.filter((plugin) => !(plugin instanceof ExtractTextPlugin)),
        // Reduces React lib size and disables redux logger
        new webpack.DefinePlugin({
            "process.env": {
                "NODE_ENV": "\"production\"",
            },
        }),
        // See http://webpack.github.io/docs/optimization.html for details on what these plugins do
        new webpack.optimize.DedupePlugin(),
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                warnings: false,
            },
            preserveComments: "license",
        }),
        new webpack.optimize.OccurenceOrderPlugin(),
    ],
});
