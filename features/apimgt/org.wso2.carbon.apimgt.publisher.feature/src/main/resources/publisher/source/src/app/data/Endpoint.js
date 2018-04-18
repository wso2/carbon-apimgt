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
import APIClientFactory from './APIClientFactory';
import Resource from './Resource';
import Utils from './Utils';

/**
 * Global and inline API endpoint representation
 * @class Endpoint
 */
export default class Endpoint extends Resource {
    /**
     * Creates an instance of Endpoint.
     * @param {String} name Endpoint name
     * @param {String} type Endpoint Type
     * @param {Number} maxTps Endpoint max TPS
     * @param {String} endpointConfig Endpoint config
     * @param {Object} kwargs rest
     * @memberof Endpoint
     */
    constructor(name, type, maxTps, endpointConfig, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
        }
        this.client = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment().label).client;
        this.endpointSecurity = {};
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                if (key === 'endpointConfig') {
                    this[key] = JSON.parse(properties[key]);
                } else {
                    this[key] = properties[key];
                }
            }
        }
    }

    /* eslint-disable no-underscore-dangle */

    // Because swagerJS generated method contains trailing underscore methods,
    // hence disabling eslint no-underscore-dangle rule
    /**
     * Get a global endpoint by giving its UUID
     * @static
     * @param {String} id UUID of the endpoint
     * @returns {Promise} promise resolving to endpoint object
     * @memberof Endpoint
     */
    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedGet = apiClient.then((client) => {
            return client.apis['Endpoint (individual)'].get_endpoints__endpointId_(
                {
                    endpointId: id,
                    'Content-Type': 'application/json',
                },
                this._requestMetaData(),
            );
        });
        return promisedGet.then((response) => {
            const endpointJSON = response.body;
            return new Endpoint(endpointJSON);
        });
    }

    /* eslint-enable no-underscore-dangle */
}
