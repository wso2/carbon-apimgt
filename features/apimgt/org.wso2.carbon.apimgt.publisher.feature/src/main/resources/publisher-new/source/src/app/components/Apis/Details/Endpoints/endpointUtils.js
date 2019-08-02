/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Utility method to get the endpoint property name based on the given endpoint type and category.
 *
 * @param {string} type The type of the endpoint (load_balance/ failover)
 * @param {string} category The endpoint category (production/ sandbox)
 * @return {string} The property name of the endpoints.
 */
function getEndpointTypeProperty(type, category) {
    if (type !== 'failover') {
        return category;
    } else {
        return category === 'sandbox_endpoints' ? 'sandbox_failovers' : 'production_failovers';
    }
}

/**
 * Merge the loadbalance/ failover endpoints to single object.
 *
 * @param {object} endpointConfig The endpoint config object
 * @return {object} {production: [], sandbox: []}
 * */
function mergeEndpoints(endpointConfig) {
    const type = endpointConfig.endpoint_type;
    if (type === 'load_balance') {
        return { production: endpointConfig.production_endpoints, sandbox: endpointConfig.sandbox_endpoints };
    } else if (type === 'failover') {
        const prodEps = [endpointConfig.production_endpoints].concat(endpointConfig.production_failovers);
        const sandboxEps = [endpointConfig.sandbox_endpoints].concat(endpointConfig.sandbox_failovers);
        return { production: prodEps, sandbox: sandboxEps };
    }
    return { production: [endpointConfig.production_endpoints], sandbox: [endpointConfig.sandbox_endpoints] };
}

/**
 * Method to get the endpoints templates based on the selected endpoint type (loadbalance/ failover) and whether is
 * http or address endpoint.
 *
 * @param {string} endpointType The endpoint type
 * @param {bool} isAddressEndpoint Whether the endpoint is soap or not.
 * @param {object} currentEndpointConfig The existing endpoint information.
 * @return {object} A endpoint template object.
 * */
function getEndpointTemplateByType(endpointType, isAddressEndpoint, currentEndpointConfig) {
    const tmpEndpointConfig = {};
    if (endpointType === 'failover') {
        tmpEndpointConfig.endpoint_type = endpointType;
        tmpEndpointConfig.production_failovers =
            currentEndpointConfig.production_failovers ? currentEndpointConfig.production_failovers : [];
        tmpEndpointConfig.sandbox_failovers =
            currentEndpointConfig.sandbox_failovers ? currentEndpointConfig.sandbox_failovers : [];
        tmpEndpointConfig.production_endpoints =
            Array.isArray(currentEndpointConfig.production_endpoints) ?
                currentEndpointConfig.production_endpoints[0] : currentEndpointConfig.production_endpoints;
        tmpEndpointConfig.sandbox_endpoints =
            Array.isArray(currentEndpointConfig.sandbox_endpoints) ?
                currentEndpointConfig.sandbox_endpoints[0] : currentEndpointConfig.sandbox_endpoints;
        tmpEndpointConfig.failOver = 'True';
    } else if (endpointType === 'load_balance') {
        tmpEndpointConfig.endpoint_type = endpointType;
        tmpEndpointConfig.algoClassName = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
        tmpEndpointConfig.algoCombo = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
        tmpEndpointConfig.sessionManagement = '';
        tmpEndpointConfig.sessionTimeOut = '';
        tmpEndpointConfig.production_endpoints = Array.isArray(currentEndpointConfig.production_endpoints) ?
            currentEndpointConfig.production_endpoints : [currentEndpointConfig.production_endpoints];
        tmpEndpointConfig.sandbox_endpoints =
            Array.isArray(currentEndpointConfig.sandbox_endpoints) ?
                currentEndpointConfig.sandbox_endpoints : [currentEndpointConfig.sandbox_endpoints];
        tmpEndpointConfig.failOver = 'False';
    } else {
        tmpEndpointConfig.endpoint_type = isAddressEndpoint === true ? 'address' : endpointType;
        tmpEndpointConfig.production_endpoints = Array.isArray(currentEndpointConfig.production_endpoints) ?
            currentEndpointConfig.production_endpoints[0] : currentEndpointConfig.production_endpoints;
        tmpEndpointConfig.sandbox_endpoints =
            Array.isArray(currentEndpointConfig.sandbox_endpoints) ?
                currentEndpointConfig.sandbox_endpoints[0] : currentEndpointConfig.sandbox_endpoints;
        tmpEndpointConfig.failOver = 'False';
    }
    return tmpEndpointConfig;
}

/**
 * Returns all the endpoints as a list.
 *
 * @param {object} endpointConfig The endpoint config object from the api.
 * @return {array} The list of endpoints.
 * */
function endpointsToList(endpointConfig) {
    const config = JSON.parse(JSON.stringify(endpointConfig));
    const endpoints = [];

    if (Array.isArray(config.production_endpoints)) {
        endpoints.concat(config.production_endpoints);
    } else {
        endpoints.push(config.production_endpoints);
    }

    if (Array.isArray(config.sandbox_endpoints)) {
        endpoints.concat(config.sandbox_endpoints);
    } else {
        endpoints.push(config.sandbox_endpoints);
    }

    if (config.endpoint_type === 'failover') {
        endpoints.push(...config.sandbox_failovers);
        endpoints.push(...config.production_failovers);
    }
    return endpoints;
}

/**
 * Returns an endpoint config object template based on the implementation method.
 * Eg: Managed, Prototyped.
 *
 * @param {string} implementationType The endpoint implementation type.
 * @return {object} The endpoint template.
 * */
function getEndpointConfigByImpl(implementationType) {
    const tmpEndpointConfig = {};
    if (implementationType === 'prototyped') {
        tmpEndpointConfig.endpoint_type = 'http';
        tmpEndpointConfig.implementation_status = 'prototyped';
        tmpEndpointConfig.production_endpoints = { config: null, url: 'http://localhost' };
        tmpEndpointConfig.sandbox_endpoints = { config: null, url: 'http://localhost' };
    } else {
        tmpEndpointConfig.endpoint_type = 'http';
        tmpEndpointConfig.production_endpoints = { url: 'http://myservice/resource' };
        tmpEndpointConfig.sandbox_endpoints = { url: 'http://myservice/resource' };
        tmpEndpointConfig.failOver = 'False';
    }
    return tmpEndpointConfig;
}

export {
    getEndpointTypeProperty,
    mergeEndpoints,
    getEndpointTemplateByType,
    endpointsToList,
    getEndpointConfigByImpl,
};
