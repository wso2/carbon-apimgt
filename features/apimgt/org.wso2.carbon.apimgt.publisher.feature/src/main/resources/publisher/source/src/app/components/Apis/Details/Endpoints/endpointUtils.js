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

import cloneDeep from 'lodash.clonedeep';

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
        tmpEndpointConfig.production_failovers = currentEndpointConfig.production_failovers
            ? currentEndpointConfig.production_failovers : [];
        tmpEndpointConfig.sandbox_failovers = currentEndpointConfig.sandbox_failovers
            ? currentEndpointConfig.sandbox_failovers : [];
        tmpEndpointConfig.production_endpoints = Array.isArray(currentEndpointConfig.production_endpoints)
            ? currentEndpointConfig.production_endpoints[0] : currentEndpointConfig.production_endpoints;
        tmpEndpointConfig.sandbox_endpoints = Array.isArray(currentEndpointConfig.sandbox_endpoints)
            ? currentEndpointConfig.sandbox_endpoints[0] : currentEndpointConfig.sandbox_endpoints;
        tmpEndpointConfig.failOver = true;
    } else if (endpointType === 'load_balance') {
        tmpEndpointConfig.endpoint_type = endpointType;
        tmpEndpointConfig.algoClassName = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
        tmpEndpointConfig.algoCombo = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
        tmpEndpointConfig.sessionManagement = '';
        tmpEndpointConfig.sessionTimeOut = '';
        if (currentEndpointConfig.production_endpoints) {
            tmpEndpointConfig.production_endpoints = Array.isArray(currentEndpointConfig.production_endpoints)
                ? currentEndpointConfig.production_endpoints : [currentEndpointConfig.production_endpoints];
        }
        if (currentEndpointConfig.sandbox_endpoints) {
            tmpEndpointConfig.sandbox_endpoints = Array.isArray(currentEndpointConfig.sandbox_endpoints)
                ? currentEndpointConfig.sandbox_endpoints : [currentEndpointConfig.sandbox_endpoints];
        }
        tmpEndpointConfig.failOver = false;
    } else {
        tmpEndpointConfig.endpoint_type = isAddressEndpoint === true ? 'address' : 'http';
        tmpEndpointConfig.production_endpoints = Array.isArray(currentEndpointConfig.production_endpoints)
            ? currentEndpointConfig.production_endpoints[0] : currentEndpointConfig.production_endpoints;
        tmpEndpointConfig.sandbox_endpoints = Array.isArray(currentEndpointConfig.sandbox_endpoints)
            ? currentEndpointConfig.sandbox_endpoints[0] : currentEndpointConfig.sandbox_endpoints;
        tmpEndpointConfig.failOver = false;
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
    const config = cloneDeep(endpointConfig);
    const endpoints = [];
    if (Array.isArray(config.production_endpoints)) {
        endpoints.push(...config.production_endpoints);
    } else {
        endpoints.push(config.production_endpoints);
    }

    if (Array.isArray(config.sandbox_endpoints)) {
        endpoints.push(...config.sandbox_endpoints);
    } else {
        endpoints.push(config.sandbox_endpoints);
    }

    if (config.endpoint_type === 'failover') {
        if (config.sandbox_failovers) {
            endpoints.push(...config.sandbox_failovers);
        }
        if (config.production_failovers) {
            endpoints.push(...config.production_failovers);
        }
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
    if (implementationType === 'PROTOTYPED') {
        tmpEndpointConfig.endpoint_type = 'http';
        tmpEndpointConfig.implementation_status = 'prototyped';
        tmpEndpointConfig.production_endpoints = { config: null, url: 'http://localhost' };
        tmpEndpointConfig.sandbox_endpoints = { config: null, url: 'http://localhost' };
    } else {
        tmpEndpointConfig.endpoint_type = 'http';
        tmpEndpointConfig.production_endpoints = { url: '' };
        tmpEndpointConfig.sandbox_endpoints = { url: '' };
        tmpEndpointConfig.failOver = false;
    }
    return tmpEndpointConfig;
}

/**
 * Get the endpoint config based on the selected endpoint type.
 * Supported endpoint types:
 * 1. http
 * 2. address
 * 3. prototyped
 * 4. awslambda
 * 5. default (Dynamic)
 *
 * @param {string} endpointType The selected endpoint type.
 * @return {endpointConfig} Endpoint config object.
 * */
function createEndpointConfig(endpointType) {
    const tmpEndpointConfig = {};
    switch (endpointType) {
        case 'http':
            tmpEndpointConfig.endpoint_type = 'http';
            tmpEndpointConfig.failOver = false;
            break;
        case 'address':
            tmpEndpointConfig.endpoint_type = 'address';
            tmpEndpointConfig.failOver = false;
            break;
        case 'prototyped':
            tmpEndpointConfig.implementation_status = 'prototyped';
            tmpEndpointConfig.endpoint_type = 'http';
            tmpEndpointConfig.production_endpoints = { config: null, url: 'http://localhost' };
            tmpEndpointConfig.sandbox_endpoints = { config: null, url: 'http://localhost' };
            break;
        case 'awslambda':
            tmpEndpointConfig.endpoint_type = 'awslambda';
            tmpEndpointConfig.access_method = 'role-supplied';
            tmpEndpointConfig.amznAccessKey = '';
            tmpEndpointConfig.amznSecretKey = '';
            tmpEndpointConfig.amznRegion = '';
            break;
        default:
            tmpEndpointConfig.endpoint_type = 'default';
            tmpEndpointConfig.production_endpoints = { url: 'default' };
            tmpEndpointConfig.sandbox_endpoints = { url: 'default' };
            tmpEndpointConfig.failOver = false;
            break;
    }
    return tmpEndpointConfig;
}

/**
 * Get the endpoint template based on endpoint type.
 *
 * @param {string} type: Endpoint type (HTTP/ Address)
 * @return {object} Endpoint Template
 * */
function getEndpointTemplate(type) {
    if (type === 'address') {
        return { url: '', endpoint_type: 'address', template_not_supported: false };
    }
    return { url: '', template_not_supported: false };
}

export {
    getEndpointTypeProperty,
    mergeEndpoints,
    getEndpointTemplateByType,
    endpointsToList,
    getEndpointConfigByImpl,
    createEndpointConfig,
    getEndpointTemplate,
};
