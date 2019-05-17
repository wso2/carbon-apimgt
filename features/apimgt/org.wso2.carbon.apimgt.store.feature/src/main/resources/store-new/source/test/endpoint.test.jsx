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
import API from '../src/app/data/api'

TestUtils.setupMockEnvironment();

describe("Endpoint", function () {
    before("Make any REST API calls ,Login a user and get access token", function (done) {
        TestUtils.userLogin().then((response) => {
            done();
        })
    });
    describe("#create()", function () {
        it("Should create a global endpoint", function () {
            const api = new API();
            const c_time = Date.now();
            const endpointDefinition = {
                endpointConfig: JSON.stringify({serviceUrl: 'http://test.wso2.org/api/endpoint'}),
                endpointSecurity: {enabled: true, username: "admin", password: "admin", type: "basic"}, // type: digest
                type: "http",
                name: "testing_endpoint_" + c_time,
                maxTps: 1000
            };
            const promisedEndpoint = api.addEndpoint(endpointDefinition);
            return promisedEndpoint.then(
                response => {
                    assert.equal(response.status, 201, "Endpoint creation failed!");
                    assert.containsAllKeys(response.obj,endpointDefinition,
                        "Response endpoint object doesn't match with created endpoint!");
                }
            )
        });
    });
    describe("#update()", function () {
        it.skip("Should update existing endpoint resource", function () {
            const api = new API();
            const c_time = Date.now();
            const endpointDefinition = api.getEndpoints();
            return endpointDefinition.then(
                response => {
                    console.log(response);
                }
            );
        });
    });
    describe("#delete()", function () { /* TODO: not implemented ~tmkb*/
        it("Should delete an endpoint resource by its ID", function () {
            const api = new API();
            const c_time = Date.now();
            const endpointDefinition = api.getEndpoints();
            return endpointDefinition.then(
                response => {
                    const endpoint = response.obj.list.pop(); // Poping the endpoint created in #create() case
                    const promisedDelete = api.deleteEndpoint(endpoint.id);
                    return promisedDelete.then(
                        response => {
                            assert.equal(response.status, 200, "Can't delete API!")
                        }
                    );
                }
            );
        });
    });
});