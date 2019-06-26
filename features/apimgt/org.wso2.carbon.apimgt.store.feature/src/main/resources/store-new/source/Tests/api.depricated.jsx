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

import Api from '../src/app/data/api'
import {before, describe, it} from "mocha"
import {assert} from 'chai'
import TestUtils from './utils'

TestUtils.setupMockEnvironment();

describe('Api',
    function () {
        before("Make any REST API calls ,Login a user and get access token", function (done) {
            TestUtils.userLogin().then((response) => {
                done();
            })
        });
        describe('#create()',
            function () {
                it('Should return HTTP 201 status code with newly created API UUID',
                    function () {
                        let api = new Api();
                        let c_time = Date.now() / 1000 | 0;
                        let data = {
                            "name": "test_api_" + c_time,
                            "context": "/testing_" + c_time,
                            "version": "1.0.0"
                        };
                        let promised_create = api.create(data);
                        return promised_create.then((response) => {
                            assert.equal(response.status, 201, 'API creation failed!');
                        });
                    }
                );
            }
        );
        describe('#createWithInlineEndpoint()',
            function () {
                it('Should return HTTP 201 status code with newly created API UUID',
                    function () {
                        let api = new Api();
                        let c_time = Date.now();
                        let data = {
                            "name": "test_api_" + c_time,
                            "context": "/testing_" + c_time,
                            "version": "1.0.0",
                            "endpoint": [{
                                type: "production",
                                inline: {
                                    endpointConfig: JSON.stringify({serviceUrl: 'http://test.wso2.org/api/endpoint'}),
                                    endpointSecurity: {enabled: false},
                                    type: "http",
                                    name: "testing_endpoint" + c_time,
                                    maxTps: 1000
                                }
                            }]
                        };
                        let promised_create = api.create(data);
                        return promised_create.then((response) => {
                            assert.isAtLeast(response.obj.endpoint.length, 1, 'No endpoint configurations were found');
                            assert.equal(response.status, 201, 'API creation failed!');
                        });
                    }
                );
            }
        );

        describe('#getAll',
            function () {
                it('Should return all the APIs available to the store',
                    function () {
                        let api = new Api();
                        let promised_all_apis = api.getAll();
                        return promised_all_apis.then((response) => {
                            let apis = response.obj;
                            assert.isTrue((apis.count === apis.list.length) && (response.status === 200), 'APIs count miss match or wrong response code');
                            assert.isAtLeast(apis.count, 1, "APIs count should be at least one");
                        });
                    }
                );
            }
        );

        describe('#delete',
            function () {
                it('Should delete the previously created API using it`s returned UUID',
                    function () {
                        let api = new Api();
                        let c_time = Date.now();
                        let data = {
                            "name": "test_api_" + c_time,
                            "context": "/testing_" + c_time,
                            "version": "1.0.0",
                            "endpoint": []
                        };
                        let promised_create = api.create(data);
                        return promised_create.then((response) => {
                            assert.equal(response.status, 201, 'API creation failed');
                            let new_api_uuid = response.obj.id;
                            let promised_delete = api.deleteAPI(new_api_uuid);
                            return promised_delete.then(response => {
                                return api.get(new_api_uuid).catch(error => {
                                    assert.equal(error.status, 404, 'API should be not found if it was deleted');
                                }).then(response => {
                                    assert.isUndefined(response, 'API can`t be found if it`s deleted correctly');
                                });
                            });
                        });
                    });
            });

        describe('#update',
            function () {
                it('Should update an API with inline endpoints',
                    function () {
                        let api = new Api();
                        let c_time = Date.now();
                        let data = {
                            "name": "test_api_update" + c_time,
                            "context": "/testing_update" + c_time,
                            "version": "1.0.0",
                            "endpoint": []
                        };
                        let promised_create = api.create(data);
                        return promised_create.then((response) => {
                            assert.equal(response.status, 201, 'API creation failed');
                            let new_api_uuid = response.obj.id;
                            let get_api = api.get(new_api_uuid);
                            return get_api.then((response) => {
                                assert.equal(response.obj.id, new_api_uuid, 'API get operation failed');
                                let endpoint_created_time = Date.now();
                                let endpointData = [{
                                    inline: {
                                        endpointConfig: JSON.stringify({serviceUrl: 'http://test.wso2.org/api/endpoint'}),
                                        endpointSecurity: {enabled: false},
                                        type: "http",
                                        name: "testing_endpoint" + endpoint_created_time,
                                        maxTps: 1000
                                    }
                                }];
                                let update_data = response.obj;
                                // Update the payload by setting the endpoint
                                update_data.endpoint = endpointData;
                                let promised_api_update = api.update(update_data);
                                return promised_api_update.then(response => {
                                    assert.equal("testing_endpoint" + endpoint_created_time, response.obj.endpoint[0].inline.name, 'API update has failed');
                                });
                            });

                        });

                    });
            });
    }
);
