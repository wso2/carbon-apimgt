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
"use strict";

import {describe, it, before} from 'mocha'
import {assert} from 'chai'

import TestUtils from './utils'
import ConfigManager from '../src/app/data/ConfigManager'

TestUtils.setupMockEnvironment();

describe("ConfigManager", function () {
    before(function () {
        TestUtils.setupMockEnvironment();
    });
    describe("#readEnvironmentConfigs()", function () {
        it("Should return HTTP 200 status code if configurations are read", function () {
            const promisedEnvironments = ConfigManager.getConfigs().environments;
            return promisedEnvironments.then(
                response => {
                    assert.equal(response.status, 200, "Configuration read failed!");
                    assert.isNotNull(response.data.environmentName, "Environment-name is NULL!");

                    let environments = response.data.environments;
                    assert.isAtLeast(environments.length, 1, "There should be AT LEAST one environment!");
                    for (let environment of environments) {
                        assert.isNotEmpty(environment.loginTokenPath, "Login Token Path of environment "
                            + environment.label + " is EMPTY!");
                    }
                }
            )
        });
    });
});
