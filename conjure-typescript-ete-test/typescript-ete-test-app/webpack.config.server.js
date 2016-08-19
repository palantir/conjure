/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

"use strict";

const fs = require("fs");
const path = require("path");
const url = require("url");

const Handlebars = require("handlebars");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const yaml = require("js-yaml");
const webpack = require("webpack");

const baseWebpackConfig = require("./webpack.config");

// Get the baseUrl from the Dropwizard server config and set it in index.html.
const serverConfig = yaml.safeLoad(fs.readFileSync("../typescript-ete-test-server/dev/conf/server.yml", "utf8"));
const baseUrl = serverConfig.server.applicationContextPath + "/";
const indexHtml = fs.readFileSync("./src/index.html", "utf8");
const indexHtmlContent = Handlebars.compile(indexHtml)({baseUrl});

const webpackDevServerPort = "8543";

module.exports = Object.assign({}, baseWebpackConfig, {
    entry: [
        ...baseWebpackConfig.entry.app,
        "webpack/hot/dev-server",
        `${require.resolve("webpack-dev-server/client/")}?https://localhost:${webpackDevServerPort}`,
    ],
    output: Object.assign({}, baseWebpackConfig.output, {
        publicPath: `https://localhost:${webpackDevServerPort}${baseUrl}`,
    }),
    plugins: [
        new webpack.HotModuleReplacementPlugin(),
        new HtmlWebpackPlugin({
            favicon: path.join(__dirname, "src", "favicon.ico"),
            templateContent: indexHtmlContent,
            title: "TypescriptEteTest",
        }),
        ...baseWebpackConfig.plugins.filter((plugin) => !(plugin instanceof HtmlWebpackPlugin)),
    ],
    devServer: {
        contentBase: path.join(__dirname, "build", "src"),
        historyApiFallback: {
            index: baseUrl,
        },
        https: true,
        hot: true,
        port: webpackDevServerPort,
        proxy: {
            "*": {
                target: "https://localhost:8443",
                bypass: (req) => {
                    // Bypass the proxy if a browser is making the request (as opposed to Fetch/XMLHttpsRequest)
                    // *unless* it's to /redirect or /logout. We want those endpoints to be handled by the actual
                    // server so that Multipass works.
                    if (!url.parse(req.url).pathname.endsWith("/redirect")
                     && !req.url.endsWith("/logout")
                     && req.headers.accept.indexOf("html") !== -1) {
                        return baseUrl;
                    } else {
                        return false;
                    }
                },
            },
        },
        publicPath: `https://localhost:${webpackDevServerPort}${baseUrl}`,
        stats: baseWebpackConfig.stats,
    },
});
