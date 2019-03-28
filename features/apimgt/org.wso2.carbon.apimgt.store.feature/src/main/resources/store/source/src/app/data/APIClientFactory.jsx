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

import APIClient from './APIClient';
import Utils from './Utils';
/**
 *
 *
 * @class APIClientFactory
 */
class APIClientFactory {
    constructor() {
        if (APIClientFactory.instance) {
            return APIClientFactory.instance;
        }

        this.APIClientMap = new Map();
        APIClientFactory.instance = this;
    }

    /**
     *
     *
     * @param {*} environmentLabel
     * @returns
     * @memberof APIClientFactory
     */
    getAPIClient(environmentLabel) {
        let apiClient = this.APIClientMap.get(environmentLabel);

        if (apiClient) {
            return apiClient;
        }

        apiClient = new APIClient(Utils.getEnvironment().host);
        this.APIClientMap.set(environmentLabel, apiClient);
        return apiClient;
    }

    /**
     *
     *
     * @param {*} environmentLabel
     * @memberof APIClientFactory
     */
    destroyAPIClient(environmentLabel) {
        this.APIClientMap.delete(environmentLabel);
    }
}

APIClientFactory.instance = null;

export default APIClientFactory;
