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
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { clientRoutingBypass, devServerBefore } = require('./source/dev/auth_login.js');

module.exports = function (env,args) {
    const isDevelopmentBuild = args.mode === 'development';
    const config = {
        entry: { index: './source/index.jsx' },
        output: {
            path: path.resolve(__dirname, 'site/public/dist'),
            filename: isDevelopmentBuild? '[name].bundle.js'  : '[name].[contenthash].bundle.js',
            chunkFilename: isDevelopmentBuild ? '[name].chunk.bundle.js' : '[name].[contenthash].bundle.js',
            publicPath: 'site/public/dist/',
        },
        node: {
            fs: 'empty',
            net: 'empty', // To fix joi issue: https://github.com/hapijs/joi/issues/665#issuecomment-113713020
        },
        watch: false,
        watchOptions: {
            aggregateTimeout: 200,
            poll: true,
            ignored: ['files/**/*.js', 'node_modules/**'],
        },
        devServer: {
            open: true,
            openPage: 'admin',
            inline: true,
            hotOnly: true,
            hot: true,
            publicPath: '/site/public/dist/',
            writeToDisk: false,
            overlay: true,
            before: devServerBefore,
            proxy: {
                '/services/': {
                    target: 'https://localhost:9443/admin',
                    secure: false,
                },
                '/api/am': {
                    target: 'https://localhost:9443',
                    secure: false,
                },
                '/admin/services': {
                    target: 'https://localhost:9443',
                    secure: false,
                },
                '/admin': {
                    bypass: clientRoutingBypass,
                },
            },
        },
        devtool: 'source-map', // todo: Commented out the source
        // mapping in case need to speed up the build time & reduce size
        resolve: {
            alias: {
                AppData: path.resolve(__dirname, 'source/src/app/data/'),
                AppComponents: path.resolve(__dirname, 'source/src/app/components/'),
                AppTests: path.resolve(__dirname, 'source/Tests/'),
            },
            extensions: ['.js', '.jsx'],
        },
        module: {
            rules: [
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
                // Until https://github.com/jantimon/html-webpack-plugin/issues/1483 ~tmkb
                // This was added to generate the index.jag from a hbs template file including the hashed bundle file
                {
                    test: /\.jag\.hbs$/,
                    loader: 'underscore-template-loader',
                    query: {
                        engine: 'lodash',
                        interpolate: '\\{\\[(.+?)\\]\\}',
                        evaluate: '\\{%([\\s\\S]+?)%\\}',
                        escape: '\\{\\{(.+?)\\}\\}',
                    },
                },
            ],
        },
        externals: {
            Themes: 'AppThemes', // Should use long names for preventing global scope JS variable conflicts
            MaterialIcons: 'MaterialIcons',
            Config: 'AppConfig',
        },
        plugins: [
            new MonacoWebpackPlugin({
                languages: ['xml', 'json', 'yaml', 'sql', 'mysql'],
                features: ['!gotoSymbol'],
            }),
            new HtmlWebpackPlugin({
                inject: false,
                template: path.resolve(__dirname, 'site/public/pages/index.jag.hbs'),
                filename: path.resolve(__dirname, 'site/public/pages/index.jag'),
                minify: false, // Make this true to get exploded, formatted index.jag file
            }),
        ],
    };
    
    // Note: for more info about monaco plugin: https://github.com/Microsoft/monaco-editor-webpack-plugin
    if (process.env.NODE_ENV === 'development') {
        config.watch = true;
    } else if (process.env.NODE_ENV === 'production') {
        /* ESLint will only run in production build to increase the continues build(watch) time in the development mode */
        const esLintLoader = {
            enforce: 'pre',
            test: /\.(js|jsx)$/,
            loader: 'eslint-loader',
            options: {
                failOnError: true,
                quiet: true,
            },
        };
        config.module.rules.push(esLintLoader);
    }

    if (env && env.analysis) {
        const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
        config.plugins.push(new BundleAnalyzerPlugin());
    }
    if (env && env.unused) {
        const { UnusedFilesWebpackPlugin } = require('unused-files-webpack-plugin');

        config.plugins.push(new UnusedFilesWebpackPlugin({
            failOnUnused: process.env.NODE_ENV !== 'development',
            patterns: ['source/src/**/*.jsx', 'source/src/**/*.js'],
            ignore: ['babel.config.js', '**/*.txt', 'source/src/index.js'],
        }));
    }
    return config;
};
