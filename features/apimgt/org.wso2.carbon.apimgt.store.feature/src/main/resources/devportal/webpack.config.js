/* eslint-disable */
/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var path = require('path');
const fs = require('fs');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const ManifestPlugin = require('webpack-manifest-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');
const { clientRoutingBypass, devServerBefore } = require('./source/dev/webpack/auth_login.js');

module.exports = function (env, argv) {
    const isDevelopmentBuild = argv.mode === 'development';

    if (env && env.analysis) {
        var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

        config.plugins.push(new BundleAnalyzerPlugin());

    }
    if (env && env.unused) {
        var UnusedFilesWebpackPlugin = require("unused-files-webpack-plugin").UnusedFilesWebpackPlugin;

        config.plugins.push(new UnusedFilesWebpackPlugin({
            failOnUnused: process.env.NODE_ENV !== 'development',
            patterns: ['source/src/**/*.jsx', 'source/src/**/*.js'],
            ignore: ['babel.config.js', '**/*.txt', 'source/src/index.js'],
        }));

    }
    const config = {
        entry: {
            index: './source/index.jsx',
        },
        output: {
            path: path.resolve(__dirname, 'site/public/dist'),
            filename: isDevelopmentBuild ? '[name].bundle.js' : '[name].[contenthash].bundle.js',
            chunkFilename: isDevelopmentBuild ? '[name].chunk.bundle.js' : '[name].[contenthash].bundle.js',
            publicPath: 'site/public/dist/',
            globalObject: 'this',
        },
        watch: false,
        watchOptions: {
            poll: 1000,
            ignored: ['files/**/*.js', 'node_modules'],
        },
        devtool: 'source-map',
        resolve: {
            alias: {
                OverrideData: path.resolve(__dirname, 'override/src/app/data/'),
                OverrideComponents: path.resolve(__dirname, 'override/src/app/components/'),
                AppData: path.resolve(__dirname, 'source/src/app/data/'),
                AppComponents: path.resolve(__dirname, 'source/src/app/components/'),
                AppTests: path.resolve(__dirname, 'source/Tests/'),
                react: fs.existsSync('../../../../../node_modules/react')
                    ? path.resolve('../../../../../node_modules/react') : path.resolve('../node_modules/react'),
                reactDom: fs.existsSync('../../../../../node_modules/react-dom')
                    ? path.resolve('../../../../../node_modules/react-dom') : path.resolve('../node_modules/react-dom'),
            },
            extensions: ['.mjs', '.js', '.jsx'],
        },
        node: { fs: 'empty' },
        devServer: {
            open: true,
            openPage: 'devportal',
            inline: true,
            hotOnly: true,
            hot: true,
            publicPath: '/site/public/dist/',
            writeToDisk: false,
            overlay: true,
            before: devServerBefore,
            proxy: {
                '/services/': {
                    target: 'https://localhost:9443/devportal',
                    secure: false,
                },
                '/api/am': {
                    target: 'https://localhost:9443',
                    secure: false,
                },
                '/devportal/services': {
                    target: 'https://localhost:9443',
                    secure: false,
                },
                '/devportal': {
                    bypass: clientRoutingBypass,
                },
            },
        },
        module: {
            rules: [
                {
                    test: /\.(js|jsx)$/,
                    exclude: /node_modules/,
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
                    test: /\.css$/,
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
                    test: /\.(woff|woff2|eot|ttf|svg)$/,
                    loader: 'url-loader?limit=100000',
                },
            ],
        },
        externals: {
            Config: 'Configurations',
            Settings: 'Settings',
            MaterialIcons: 'MaterialIcons',
        },
        plugins: [
            new CleanWebpackPlugin(),
            new ManifestPlugin(),
            new ESLintPlugin({
                extensions: ['js', 'ts', 'jsx'],
                failOnError: true,
                quiet: true,
                exclude: ['node_modules'],
            }),
        ],
    };
    return config;
};

