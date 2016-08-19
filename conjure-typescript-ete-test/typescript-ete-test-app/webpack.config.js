/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
/*
 * Used for development builds. Production builds share a lot of the
 * configuration. See webpack.config.prod.js for the differences.
 *
 * Production: `webpack --config webpack.config.prod.js`
 * Development (with watch): `webpack -d --progress --watch`
 */

"use strict";

const path = require("path");

const autoprefixer = require("autoprefixer");
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");

const staticFileRegex = /\.(woff|svg|ttf|eot|gif|jpeg|jpg|png)([\?]?.*)$/;

module.exports = {
    entry: {
        app: [
            path.resolve(__dirname, "build/tmp/app.js"),
            path.resolve(__dirname, "src/app.less"),
        ],
    },
    output: {
        filename: "[name].js",
        path: path.join(__dirname, "build", "src"),
    },
    devtool: "eval-cheap-module-source-map",
    // Configure logging. See https://webpack.github.io/docs/node.js-api.html for all options.
    stats: {
        assets: false,
        cached: false,
        cachedAssets: false,
        children: false,
        chunks: false,
        chunkModes: false,
        chunkOrigins: false,
        colors: true,
        errorDetails: true,
        hash: false,
        modules: false,
        reasons: false,
        source: false,
        timings: false,
    },
    module: {
        preLoaders: [
            {
                test: /\.js$/,
                loader: "source-map",
            },
        ],
        loaders: [
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract(
                    "style",
                    [
                        "css?sourceMap",
                        "postcss",
                    ]
                ),
            },
            {
                test: /\.less$/,
                loader: ExtractTextPlugin.extract(
                    "style",
                    [
                        "css?sourceMap",
                        "postcss",
                        "less?sourceMap",
                    ]
                ),
            },
            {
                test: staticFileRegex,
                include: [
                    path.resolve(__dirname, "node_modules"),
                ],
                loader: "file-loader",
                query: {
                    name: "[path][name].[ext]",
                },
            },
            {
                test: staticFileRegex,
                include: path.resolve(__dirname, "src"),
                loader: "file-loader",
                query: {
                    name: "[name]-[hash].[ext]",
                },
            },
        ],
    },
    postcss: () => {
        return [
            autoprefixer({
                browsers: [
                    "> 1%",
                    "last 2 versions",
                    "Firefox ESR",
                    "Opera 12.1",
                ],
            }),
        ];
    },
    plugins: [
        new ExtractTextPlugin("[name].css"),
        new HtmlWebpackPlugin({
            favicon: path.resolve(__dirname, "src/favicon.ico"),
            minify: {
                collapseWhitespace: true,
            },
            template: path.resolve(__dirname, "src/index.html"),
            title: "TypescriptEteTest",
        }),
    ],
};
