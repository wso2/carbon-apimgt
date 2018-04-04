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

/* eslint-disable */
var path = require('path');

const config = {
    entry: {
        index: './source/index.jsx',
    },
    output: {
        path: path.resolve(__dirname, 'public/dist'),
        filename: '[name].js',
    },
    watch: false,
    devtool: 'source-map',
    plugins: [],
    resolve: {
        extensions: ['.js', '.jsx'],
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
        ],
    },
};

if (process.env.NODE_ENV === 'development') {
    config.watch = true;
} else if (process.env.NODE_ENV === 'production') {
    /* ESLint will only un in production build to increase the continues build(watch) time in the development mode */
    const esLintLoader = {
        enforce: 'pre',
        test: /\.(js|jsx)$/,
        /* exclude: /node_modules/, */
        include: [/.*\/source\/index.jsx/, /.*\/source\/src\/App.jsx/],
        loader: 'eslint-loader',
        options: {
            failOnError: true,
            quiet: true,
        },
    };
    config.module.rules.push(esLintLoader);
}

module.exports = config;
