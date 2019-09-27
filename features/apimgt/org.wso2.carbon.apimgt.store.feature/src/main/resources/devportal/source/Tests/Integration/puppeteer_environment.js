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
const NodeEnvironment = require('./node_modules/jest-environment-node');
const puppeteer = require('./node_modules/puppeteer');
const fs = require('fs');
const os = require('os');
const path = require('path');

const DIR = path.join(os.tmpdir(), 'jest_puppeteer_global_setup');

/**
 *
 *
 * @class PuppeteerEnvironment
 * @extends {NodeEnvironment}
 */
class PuppeteerEnvironment extends NodeEnvironment {
    /**
     *
     *
     * @memberof PuppeteerEnvironment
     */
    async setup() {
        console.log('Setup Test Environment.');
        await super.setup();
        const wsEndpoint = fs.readFileSync(path.join(DIR, 'wsEndpoint'), 'utf8');
        if (!wsEndpoint) {
            throw new Error('wsEndpoint not found');
        }
        // eslint-disable-next-line no-underscore-dangle
        this.global.__BROWSER__ = await puppeteer.connect({
            browserWSEndpoint: wsEndpoint,
            ignoreHTTPSErrors: true,
        });
    }


    /**
     *
     *
     * @memberof PuppeteerEnvironment
     */
    async teardown() {
        console.log('Teardown Test Environment.');
        await super.teardown();
    }


    /**
     *
     *
     * @param {*} script
     * @returns
     * @memberof PuppeteerEnvironment
     */
    runScript(script) {
        return super.runScript(script);
    }
}

module.exports = PuppeteerEnvironment;
