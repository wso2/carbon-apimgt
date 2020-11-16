/**
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import APIClientFactory from './APIClientFactory';
import Utils from './Utils';

/**
 * This class contains tenant related api requests
 */
class Tenants {
    /**
     * @inheritdoc
     */
    constructor() {
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
    }

    /**
     * Gets tenants by state. If no state is passed it returns tenants who are active
     * @param {String} state tenant state either active or inactive
     * @memberof Tenants
     * @returns {promise} tenants
     */
    getTenantsByState = (state = 'active') => {
        return this.client.then((client) => {
            return client.apis.Tenants.get_tenants({ state });
        });
    }
}

export default Tenants;
