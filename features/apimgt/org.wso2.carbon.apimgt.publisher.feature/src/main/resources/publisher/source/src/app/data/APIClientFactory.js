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
import ServiceCatalogClient from './ServiceCatalogClient';
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
    getAPIClient(environment, clientType) {
        const {
            label,
        } = environment;
        if (label === undefined) {
            throw new Error('Environment label is undefined, Please provide'
                + 'a valid environment object with keys (host,label & loginTokenPath)');
        }
        const apiClientEnvLabel = environment.label + Utils.CONST.API_CLIENT;
        const catalogClientEnvLabel = environment.label + Utils.CONST.SERVICE_CATALOG_CLIENT;
        let apiClient;
        if (clientType === Utils.CONST.API_CLIENT) {
            apiClient = this._APIClientMap.get(apiClientEnvLabel);
            if (apiClient) {
                return apiClient;
            } else {
                apiClient = new APIClient(environment);
                this._APIClientMap.set(apiClientEnvLabel);
            }
        } else if (clientType === Utils.CONST.SERVICE_CATALOG_CLIENT) {
            apiClient = this._APIClientMap.get(catalogClientEnvLabel);
            if (apiClient) {
                return apiClient;
            } else {
                apiClient = new ServiceCatalogClient(environment);
                this._APIClientMap.set(catalogClientEnvLabel);
            }
        }
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
