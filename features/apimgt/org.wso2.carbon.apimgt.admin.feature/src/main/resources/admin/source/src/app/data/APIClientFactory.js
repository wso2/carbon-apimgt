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

import APIClient from './APIClient';
import Utils from './Utils';


/**
 * Class representing a Factory of APIClients
 */
class APIClientFactory {
    /**
     * Initialize a single instance of APIClientFactory
     * @returns {APIClientFactory}
     */
    constructor() {
        /* eslint-disable no-underscore-dangle */
        // indicate “private” members of APIClientFactory that is why underscore has used here
        if (APIClientFactory._instance) {
            return APIClientFactory._instance;
        }

        this._APIClientMap = new Map();
        APIClientFactory._instance = this;
        /* eslint-enable no-underscore-dangle */
    }

    /**
     *
     * @param {Object} environment
     * @returns {APIClient} APIClient object for the environment
     */
    getAPIClient(environment = Utils.getDefaultEnvironment()) {
        let apiClient = this._APIClientMap.get(environment.label);

        if (apiClient) {
            return apiClient;
        }

        apiClient = new APIClient(environment);
        this._APIClientMap.set(environment.label, apiClient);
        return apiClient;
    }

    /**
     * Remove an APIClient object from the environment
     * @param {String} environmentLabel name of the environment
     */
    destroyAPIClient(environmentLabel) {
        this._APIClientMap.delete(environmentLabel);
    }
}

/**
 * Single instance of APIClientFactory indicate “private” members of objects
 * @type {APIClientFactory}
 * @private
 */
// eslint-disable-next-line no-underscore-dangle
APIClientFactory._instance = null;

export default APIClientFactory;
