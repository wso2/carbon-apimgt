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
const unitTestsConfigs = require('../../../jest.config');

module.exports = {
    rootDir: '../../../',
    ...unitTestsConfigs,
    testPathIgnorePatterns: ['<rootDir>/node_modules/'],
    testMatch: ['<rootDir>/source/Tests/Integration/*/*.test.js'],
    globalSetup: '<rootDir>/source/Tests/Integration/setup.js',
    globalTeardown: '<rootDir>/source/Tests/Integration/teardown.js',
    testEnvironment: '<rootDir>/source/Tests/Integration/puppeteer_environment.js',
};
