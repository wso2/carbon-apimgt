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
/* eslint-disable no-underscore-dangle */
// Because swagerJS generated method contains trailing underscore methods,
// hence disabling eslint no-underscore-dangle rule

import Resource from './Resource';
import APIClientFactory from './APIClientFactory';
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
     * @param {Object} kwargs rest
     * @memberof Endpoint
     */
    constructor(name, type, maxTps, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
        } else {
            this.name = name;
            this.type = type;
            this.maxTps = maxTps;
        }
        this.endpointSecurity = {};
        this.endpointConfig = {};
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                this[key] = properties[key];
            }
        }
    }

    /**
     * Return the service URL of the Endpoint
     * @returns {String} HTTP/HTTPS Service URL
     * @memberof Endpoint
     */
    getServiceUrl() {
        return this.endpointConfig.service_url;
    }

    /**
     * Persist the local endpoint object changes via Endpoint REST API
     * @returns {Promise} Promise resolve with newly created Endpoint object
     * @memberof Endpoint
     */
    save() {
        const promisedEndpoint = this.client.then((client) => {
            return this._serialize().then((serializedData) => {
                const payload = { body: serializedData, 'Content-Type': 'application/json' };
                return client.apis['Endpoint (Collection)'].post_endpoints(payload, Resource._requestMetaData());
            });
        });
        return promisedEndpoint.then((response) => {
            return new Endpoint(response.body);
        });
    }

    /**
     * Serialize the object to send over the wire
     * @returns {Object} serialized JSON object
     * @memberof Endpoint
     */
    _serialize() {
        return this.client.then((client) => {
            const { properties } = client.spec.definitions.EndPoint;
            const data = {};
            for (const property in this) {
                if (property in properties) {
                    data[property] = this[property];
                }
            }
            return data;
        });
    }

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

    /**
     * Get all Global endpoints
     * @static
     * @returns {Array} Array of global Endpoint objects
     * @memberof Endpoint
     */
    static all() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedEndpoints = apiClient.then((client) => {
            return client.apis['Endpoint (Collection)'].get_endpoints({}, this._requestMetaData());
        });
        return promisedEndpoints.then((response) => {
            return response.body.list.map((endpointJSON) => new Endpoint(endpointJSON));
        });
    }
}
/* eslint-enable no-underscore-dangle */
