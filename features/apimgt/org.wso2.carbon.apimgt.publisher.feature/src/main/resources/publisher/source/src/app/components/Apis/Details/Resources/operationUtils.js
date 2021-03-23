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

const VERSIONS = {
    V3: ['3.0.0', '3.0.1', '3.0.2'],
    V2: ['2.0'],
};
/**
 *
 *
 * @param {*} apiProduct
 */
function getAPIProductTaggedOperations(apiProduct, openAPI) {
    const taggedOperations = { Default: [] };
    apiProduct.apis.map((apiOperations) => {
        return apiOperations.operations.map((operation) => {
            const { target, verb } = operation;
            // TODO: WARN! `toLowerCase` could cause a case sensitive issues
            // in `verb` if definition has mixed case verbs
            const openAPIOperation = openAPI.paths[target] && openAPI.paths[target][verb.toLowerCase()];
            if (!openAPIOperation) {
                console.warn(`Could not find target = ${target} `
                        + `verb (lower cased) = ${verb.toLowerCase()} operation in OpenAPI definition`);
                // Skipping not found operations
                return null;
            }
            const operationInfo = { spec: openAPIOperation, ...operation };

            if (!taggedOperations[apiOperations.name]) {
                taggedOperations[apiOperations.name] = [];
            }
            taggedOperations[apiOperations.name].push(operationInfo);
            return operationInfo;
        });
    });
    return taggedOperations;
}

/**
 *
 *
 */
function getTaggedOperations(api, openAPI) {
    const taggedOperations = { Default: [] };
    if (api.isAPIProduct()) {
        return getAPIProductTaggedOperations(api, openAPI);
    } else {
        api.operations.map((apiOperation) => {
            const { target, verb } = apiOperation;
            // TODO: WARN! `toLowerCase` could cause a case sensitive
            // issues in `verb` if definition has mixed case verbs
            const openAPIOperation = openAPI.paths[target] && openAPI.paths[target][verb.toLowerCase()];
            if (!openAPIOperation) {
                console.warn(`Could not find target = ${target} `
                        + `verb (lower cased) = ${verb.toLowerCase()} operation in OpenAPI definition`);
                // Skipping not found operations
                return null;
            }
            const operationInfo = { spec: openAPIOperation, ...apiOperation };
            if (openAPIOperation.tags) {
                openAPIOperation.tags.map((tag) => {
                    if (!taggedOperations[tag]) {
                        taggedOperations[tag] = [];
                    }
                    taggedOperations[tag].push(operationInfo);
                    return operationInfo; // Just to satisfy an es-lint rule or could use `for ... of ...`
                });
            } else {
                taggedOperations.Default.push(operationInfo);
            }
            return operationInfo; // Just to satisfy an es-lint rule or could use `for ... of ...`
        });
        return taggedOperations;
    }
}

/**
 * Return the definition version given the parsed spec object
 *
 * @param {SwaggerObject} spec
 * @returns {String} version
 */
function getVersion(spec) {
    return spec.openapi || spec.swagger;
}
/**
 *Extract the path parameters from URI template. User has to give the Open API spec version as well
 * https://github.com/OAI/OpenAPI-Specification/tree/master/versions
 * @param {String} target URI template
 * @param {String} openAPIVersion Should be a valid Open API specification version (i:e "2.0", "3.0.0")
 * @returns {Array} List of parameter objects according to the given spec version
 */
function extractPathParameters(target, spec) {
    const regEx = /[^{}]+(?=})/g;
    const params = target.match(regEx) || [];
    let parameters = [];
    const openAPIVersion = getVersion(spec);
    if (VERSIONS.V3.includes(openAPIVersion)) {
        parameters = params.map((para) => {
            const paraObj = {};
            paraObj.name = para;
            paraObj.in = 'path';
            paraObj.required = true;
            paraObj.schema = {
                type: 'string',
                format: 'string',
            };
            return paraObj;
        });
    } else if (VERSIONS.V2.includes(openAPIVersion)) {
        parameters = params.map((para) => {
            const paraObj = {};
            paraObj.name = para;
            paraObj.in = 'path';
            paraObj.required = true;
            paraObj.type = 'string';
            paraObj.format = 'string';
            return paraObj;
        });
    }

    return parameters;
}

/**
 * Extract AsyncAPI path parameters from the channel name.
 * @param {*} target
 */
function extractAsyncAPIPathParameters(target) {
    const regEx = /[^{}]+(?=})/g;
    const params = target.match(regEx) || [];
    const parameters = { };
    params.forEach((param) => {
        parameters[param] = {
            description: '',
            schema: {
                type: 'string',
            },
        };
    });
    return parameters;
}

/**
 *
 * Return the WSO2 specific scopes array (currently only use the first element of the array)
 * @param {*} operation
 * @param {*} openAPIVersion
 * @returns {Array} Scopes of the `default` security scheme
 */
function getOperationScopes(operation, spec) {
    const openAPIVersion = getVersion(spec);
    let scopes = [];
    if (VERSIONS.V3.includes(openAPIVersion)) {
        if (Array.isArray(operation.security) && operation.security.find((item) => item.default)) {
            scopes = operation.security.find((item) => item.default).default;
        } else if (operation['x-scope']) {
            scopes = [operation['x-scope']];
        }
    } else if (VERSIONS.V2.includes(openAPIVersion)) {
        if (Array.isArray(operation.security) && operation.security.find((item) => item.default)) {
            scopes = operation.security.find((item) => item.default).default;
        } else if (operation['x-scope']) {
            scopes = [operation['x-scope']];
        }
    }
    return scopes;
}

/**
 * Return AsyncAPI operation scopes array.
 * @param {*} operation
 * @param {*} spec
 */
function getAsyncAPIOperationScopes(operation) {
    const scopes = [];
    if (operation['x-scopes']) {
        // eslint-disable-next-line no-unused-vars
        Object.entries(operation['x-scopes']).forEach(([k, v]) => {
            scopes.push(v);
        });
    }
    return scopes;
}

/**
 * Map the api.operations array to swagger paths like object
 * @param {Array} operations Operations in API DTO
 * @returns {Object} Mapped operations object
 */
function mapAPIOperations(operations) {
    const temp = {};
    for (const operation of operations) {
        const { target, verb, ...rest } = operation;
        if (!temp[target]) {
            temp[target] = {
                [verb]: rest,
            };
        } else {
            temp[target][verb] = rest;
        }
    }
    return temp;
}

/**
 *
 *
 * @param {*} selectedOperations
 * @param {*} operations
 * @returns
 */
function isSelectAll(selectedOperations, operations) {
    for (const path in operations) {
        if (Object.prototype.hasOwnProperty.call(operations, path)) {
            const verbs = operations[path];
            if (
                !selectedOperations[path]
                || Object.keys(selectedOperations[path]).length !== Object.keys(verbs).length
            ) {
                return false;
            }
        }
    }
    return true;
}

/**
 * Check whether the provided object is a Ref object.
 *
 * @param {object} content : The object that needs to be checked.
 * @return {boolean} true if Ref, false otherwise.
 * */
function isRef(content) {
    let isReference = false;
    if (typeof content === 'object') {
        Object.keys(content).map((name) => {
            isReference = name === '$ref';
            return name;
        });
    }
    return isReference;
}

export {
    mapAPIOperations,
    getTaggedOperations,
    getAPIProductTaggedOperations,
    extractPathParameters,
    extractAsyncAPIPathParameters,
    getOperationScopes,
    getAsyncAPIOperationScopes,
    isSelectAll,
    getVersion,
    VERSIONS,
    isRef,
};
