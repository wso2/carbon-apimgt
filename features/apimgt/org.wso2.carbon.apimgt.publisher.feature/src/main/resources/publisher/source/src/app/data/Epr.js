/**
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* eslint-disable */
import APIClientFactory from './APIClientFactory';
import Utils from './Utils';
import ResourceEpr from './ResourceEpr';
import cloneDeep from 'lodash.clonedeep';

/**
 * An abstract representation of an API
 */
class Epr extends ResourceEpr {
    constructor() {
        super();
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for Epr request
     * @private
     */
    _requestMetaData() {
        ResourceEpr._requestMetaData();
    }

    /**
     *
     * Instance method of the API class to provide raw JSON object
     * which is API body friendly to use with REST api requests
     * Use this method instead of accessing the private _data object for
     * converting to a JSON representation of an API object.
     * Note: This is deep coping, Use sparingly, Else will have a bad impact on performance
     * Basically this is the revers operation in constructor.
     * This method simply iterate through all the object properties (excluding the properties in `excludes` list)
     * and copy their values to new object.
     * So use this method with care!!
     * @memberof Epr
     * @param {Array} [userExcludes=[]] List of properties that are need to be excluded from the generated JSON object
     * @returns {JSON} JSON representation of the API
     */
    toJSON(userExcludes = []) {
        var copy = {},
            excludes = ['_data', 'client', 'apiType', ...userExcludes];
        for (var prop in this) {
            if (!excludes.includes(prop)) {
                copy[prop] = cloneDeep(this[prop]);
            }
        }
        return copy;
    }

    getRegistries(params = {}) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEprEnvironment(),true).client;

        // const promiseGetAll = apiClient.then((client) => {
        //     return client.apis.APIs.get_apis(params, this._requestMetaData());
        // });
        const promiseGetAll = new Promise(((resolve) => {
            setTimeout(() => {
                resolve(
                    [
                        {
                          "name": "WSO2 Dev Registry1",
                          "id": "f0584a57-acb2-40b5-966f-d6e950fc16ad",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry2",
                          "id": "60f00978-9309-4be7-b8bb-c6bb9ddae20a",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry3",
                          "id": "375f29d3-9b5f-40b9-a09a-65ee921eea1c",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry4",
                          "id": "6dd6abbb-940a-4e79-8ed0-7edc91572ff9",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry5",
                          "id": "7fd01c7e-99f6-4acf-9182-4c3e3f690e8f",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry6",
                          "id": "26181e9c-09a1-49f4-babe-33238fa8900f",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry7",
                          "id": "d0395fbf-c1ce-43fb-934d-8bf3db0c987f",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry8",
                          "id": "54ba4458-e1c3-4579-9c73-ebb00adb7dfb",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry9",
                          "id": "18ce4af7-0a5f-41c9-8c7e-8e5da0048280",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        },
                        {
                          "name": "WSO2 Dev Registry10",
                          "id": "fb86be76-1a8d-4d8f-bf34-d26d749440bb",
                          "type": "WSO2",
                          "mode": "READONLY",
                          "owner": "admin"
                        }
                      ]
                );
            }, 1000);
        }));
        return promiseGetAll;
    }


}


Epr.CONSTS = {
    Epr: 'Epr',
};

Object.freeze(Epr.CONSTS);

export default Epr;
