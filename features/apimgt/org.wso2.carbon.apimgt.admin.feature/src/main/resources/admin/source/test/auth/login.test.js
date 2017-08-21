/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import AuthManager from '../../src/app/data/AuthManager.js'
import {describe, it, before} from "mocha"
import {assert} from 'chai'
import TestUtils from "../utils";

describe('AuthManager',
    function () {
        before(function () {
            TestUtils.setupMockEnvironment();
        });
        describe('#authenticateUser()',
            function () {
                it('Should return HTTP 200 status code if user authenticate',
                    function () {
                        let authenticator = new AuthManager();
                        let promised_auth = authenticator.authenticateUser('admin', 'admin');
                        return promised_auth.then((response) => {
                            assert.equal(response.status, 200);
                        });
                    }
                );
            }
        );
    }
);


