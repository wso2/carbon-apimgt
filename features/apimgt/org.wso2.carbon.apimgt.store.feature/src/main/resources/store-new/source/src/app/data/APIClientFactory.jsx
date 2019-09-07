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
"use strict";
import APIClient from "./APIClient";
import Utils from "./Utils";

class APIClientFactory {
    constructor() {
        if (APIClientFactory._instance) {
            return APIClientFactory._instance;
        }

        this._APIClientMap = new Map();
        APIClientFactory._instance = this;
    }

    getAPIClient(environmentLabel) {
        let api_Client = this._APIClientMap.get(environmentLabel);

        if (api_Client) {
            return api_Client;
        }

        api_Client = new APIClient(Utils.getEnvironment().host);
        this._APIClientMap.set(environmentLabel, api_Client);
        return api_Client;
    }

    destroyAPIClient(environmentLabel) {
        this._APIClientMap.delete(environmentLabel);
    }
}

APIClientFactory._instance = null;

export default APIClientFactory;