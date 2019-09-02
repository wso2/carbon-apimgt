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
import Joi from '@hapi/joi';
import APIClientFactory from './APIClientFactory';
import Utils from './Utils';

const roleSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'systemRole',
    language: {
        role: 'needs to be a valid role',
    },
    rules: [
        {
            name: 'role',
            validate(params, value, state, options) {
                const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
                const promise = apiClient.then((client) => {
                    return client.apis.Roles.validateSystemRole({roleId:value}).then(resp => {
                        return resp.ok;
                    }).catch(err => {
                        return false;
                    });
                });
                return promise;
                // return APIClient.me.roles(value).then(response=>{
                //     return response.body.isValid
                // });

                // callee of `validate` method
                /* validate()..then(valid => {
                if(valid){
                    /..../
                }
            })
            */

            }
        }
    ]
}));

const userRoleSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'userRole',
    language: {
        role: 'needs to be a valid role',
    },
    rules: [
        {
            name: 'role',
            validate(params, value, state, options) {
                const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
                const promise = apiClient.then((client) => {
                    return client.apis.roles.validateUserRole({roleId:value}).then(resp => {
                        return resp.ok;
                    }).catch(err => {
                        return false;
                    });
                });
                return promise;
            }
        }
    ]
}));

const definition = {
    apiName: Joi.string()
        .regex(/^[a-zA-Z0-9]{1,30}$/),
    apiVersion: Joi.string()
        .regex(/^[a-zA-Z0-9]{1,30}$/),
    apiContext: Joi.string()
        .regex(/^[a-zA-Z0-9]{1,30}$/),
    url: Joi.string()
        .uri(),
    role: roleSchema.systemRole().role(),
    userRole: userRoleSchema.userRole().role(),
};


export default definition;
