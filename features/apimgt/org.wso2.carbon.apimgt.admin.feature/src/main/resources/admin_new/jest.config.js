/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Refer https://jestjs.io/docs/en/configuration for more information about jest configs
 * Added monaco-editor mapping because of this issue
 *      https://github.com/react-monaco-editor/react-monaco-editor/issues/133#issuecomment-403960502
 */
module.exports = {
    setupFiles: ['<rootDir>/source/Tests/setupTests.js'],
    moduleNameMapper: {
        'AppComponents(.*)$': '<rootDir>/source/src/app/components/$1',
        'AppData(.*)$': '<rootDir>/source/src/app/data/$1',
        'AppTests(.*)$': '<rootDir>/source/Tests/$1',
        '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
            '<rootDir>/source/Tests/Unit/__mocks__/fileMock.js',
        '\\.(css|less)$': '<rootDir>/source/Tests/Unit/__mocks__/styleMock.js',
        Themes: '<rootDir>/site/public/conf/defaultTheme.js',
        Config: '<rootDir>/site/public/conf/settings.js',
        '^MaterialIcons$': '<rootDir>/site/public/fonts/iconfont/MaterialIcons.js',
        'monaco-editor': '<rootDir>/node_modules/react-monaco-editor',
    },
    transform: {
        '^.+\\.jsx$': 'babel-jest',
        '^.+\\.js$': 'babel-jest',
    },
    testPathIgnorePatterns: ['<rootDir>/node_modules/', '<rootDir>/source/Tests/Integration/'],
    transformIgnorePatterns: ['<rootDir>/node_modules/'],

    // Automatically clear mock calls and instances between every test
    clearMocks: true,

    // Indicates whether the coverage information should be collected while executing the test
    collectCoverage: false,

    // An array of glob patterns indicating a set of files for which coverage information should be collected
    collectCoverageFrom: ['<rootDir>/source/src/**/*.{js,jsx}'],

    // The directory where Jest should output its coverage files
    coverageDirectory: 'coverage',

    // A list of reporter names that Jest uses when writing coverage reports
    coverageReporters: ['json', 'text', 'lcov', 'clover', 'html'],

    // The test environment that will be used for testing,
    // Default is JSDOM https://github.com/jest-community/vscode-jest/issues/165
    // testEnvironment: 'node',
};
