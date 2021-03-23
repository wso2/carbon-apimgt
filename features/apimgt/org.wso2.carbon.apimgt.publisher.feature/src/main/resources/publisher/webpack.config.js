/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const path = require('path');
const fs = require('fs');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const { WebpackManifestPlugin } = require('webpack-manifest-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');

const config = {
    entry: {
        index: './source/index.jsx',
        swaggerWorkerInit: './source/src/app/webWorkers/swaggerWorkerInit.js',
    },
    output: {
        path: path.resolve(__dirname, 'site/public/dist'),
        filename: '[name].[contenthash].bundle.js',
        chunkFilename: '[name].[contenthash].bundle.js',
        publicPath: 'site/public/dist/',
        globalObject: 'this',
    },
    node: {
        fs: 'empty',
        net: 'empty', // To fix joi issue: https://github.com/hapijs/joi/issues/665#issuecomment-113713020
    },
    watch: false,
    watchOptions: {
        poll: 1000,
        ignored: ['files/**/*.js', 'node_modules'],
    },
    devtool: 'source-map', // todo: Commented out the source
    // mapping in case need to speed up the build time & reduce size
    resolve: {
        alias: {
            AppData: path.resolve(__dirname, 'source/src/app/data/'),
            AppComponents: path.resolve(__dirname, 'source/src/app/components/'),
            OverrideData: path.resolve(__dirname, 'override/src/app/data/'),
            OverrideComponents: path.resolve(__dirname, 'override/src/app/components/'),
            AppTests: path.resolve(__dirname, 'source/Tests/'),
            react: fs.existsSync('../../../../../node_modules/react')
                ? path.resolve('../../../../../node_modules/react') : path.resolve('../node_modules/react'),
            reactDom: fs.existsSync('../../../../../node_modules/react-dom')
                ? path.resolve('../../../../../node_modules/react-dom') : path.resolve('../node_modules/react-dom'),
        },
        extensions: ['.js', '.jsx'],
    },
    module: {
        rules: [
            {
                test: /\.worker\.js$/,
                use: { loader: 'worker-loader' },
            },
            {
                test: /\.(js|jsx)$/,
                exclude: [/node_modules\/(?!(@hapi)\/).*/, /coverage/],
                use: [
                    {
                        loader: 'babel-loader',
                    },
                    {
                        loader: path.resolve('loader.js'),
                    },
                ],
            },
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.less$/,
                use: [
                    {
                        loader: 'style-loader', // creates style nodes from JS strings
                    },
                    {
                        loader: 'css-loader', // translates CSS into CommonJS
                    },
                    {
                        loader: 'less-loader', // compiles Less to CSS
                    },
                ],
            },
            {
                test: /\.(png|jpe?g|gif|svg|eot|ttf|woff|woff2)$/i,
                loader: 'url-loader',
                options: {
                    limit: 8192,
                },
            },
        ],
    },
    externals: {
        userCustomThemes: 'userThemes', // Should use long names for preventing global scope JS variable conflicts
        MaterialIcons: 'MaterialIcons',
        Config: 'AppConfig',
        Settings: 'Settings',
    },
    plugins: [
        new MonacoWebpackPlugin({ languages: ['xml', 'json', 'yaml'], features: ['!gotoSymbol'] }),
        new CleanWebpackPlugin(),
        new WebpackManifestPlugin(),
        new ESLintPlugin({
            extensions: ['js', 'ts', 'jsx'],
            failOnError: true,
            quiet: true,
            exclude: ['/devportal/', 'node_modules'],
        }),
    ],
};

// Note: for more info about monaco plugin: https://github.com/Microsoft/monaco-editor-webpack-plugin
if (process.env.NODE_ENV === 'development') {
    config.watch = true;
}

module.exports = function (env) {
    if (env && env.analysis) {
        config.plugins.push(new BundleAnalyzerPlugin());
    }
    return config;
};
