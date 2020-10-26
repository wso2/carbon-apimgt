/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ServiceCatalogClient from './ServiceCatalogClient';

/**
 * Class representing a Factory of ServiceCatalogClients
 */
class ServiceCatalogClientFactory {
    /**
     * Initialize a single instance of ServiceCatalogClientFactory
     * @returns {ServiceCatalogClientFactory}
     */
    constructor() {
        /* eslint-disable no-underscore-dangle */
        // indicate “private” members of ServiceCatalogClientFactory that is why underscore has used here
        if (ServiceCatalogClientFactory._instance) {
            return ServiceCatalogClientFactory._instance;
        }

        this._ServiceCatalogClientMap = new Map();
        ServiceCatalogClientFactory._instance = this;
        /* eslint-enable no-underscore-dangle */
    }

    /**
     *
     * @param {Object} environment
     * @returns {ServiceCatalogClient} ServiceCatalogClient object for the environment
     */
    getServiceCatalogClient(environment) {
        const {
            label,
        } = environment;
        if (label === undefined) {
            throw new Error('Environment label is undefined, Please provide'
                + 'a valid environment object with keys (host,label & loginTokenPath)');
        }
        let serviceCatalogClient = this._ServiceCatalogClientMap.get(environment.label);

        if (serviceCatalogClient) {
            return serviceCatalogClient;
        }

        serviceCatalogClient = new ServiceCatalogClient(environment);
        this._ServiceCatalogClientMap.set(environment.label, serviceCatalogClient);
        return serviceCatalogClient;
    }

    /**
     * Remove an ServiceCatalogClient object from the environment
     * @param {String} environmentLabel name of the environment
     */
    destroyServiceCatalogClient(environmentLabel) {
        this._ServiceCatalogClientMap.delete(environmentLabel);
    }
}

/**
 * Single instance of ServiceCatalogClientFactory indicate “private” members of objects
 * @type {ServiceCatalogClientFactory}
 * @private
 */
// eslint-disable-next-line no-underscore-dangle
ServiceCatalogClientFactory._instance = null;

export default ServiceCatalogClientFactory;
