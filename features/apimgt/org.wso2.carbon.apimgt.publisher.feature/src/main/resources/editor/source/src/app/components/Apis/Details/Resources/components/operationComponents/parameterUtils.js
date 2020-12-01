/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Defining the supported data types by the OAS spec versions.
 *
 * 2.0 : https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#dataTypeFormat
 * 3.x :https://swagger.io/docs/specification/data-models/data-types/
 * */
const supportedDataTypes = {
    '2.0': {
        body: ['integer', 'number', 'string', 'boolean', 'object'],
        formData: ['integer', 'number', 'string', 'boolean', 'file'],
        query: ['integer', 'number', 'string', 'boolean'],
        path: ['integer', 'number', 'string', 'boolean'],
        header: ['integer', 'number', 'string', 'boolean'],
    },
    '3.x': {
        body: ['integer', 'number', 'string', 'boolean', 'object'],
        query: ['integer', 'number', 'string', 'boolean'],
        path: ['integer', 'number', 'string', 'boolean'],
        header: ['integer', 'number', 'string', 'boolean'],
        cookie: ['integer', 'number', 'string', 'boolean'],
    },
};

/**
 * Util method for checking conditions.
 *
 * @param {boolean} condition : The condition to be checked
 * @param {any} matched : The value to be returned if the condition is true
 * @param {any} otherwise : The value to be returned if the condition is false.
 * @return {object} If true -> matched, false -> otherwise
 * */
export function iff(condition, matched, otherwise) {
    return condition ? matched : otherwise;
}

/**
 * Method to get the supported data types for the given parameter type.
 *
 * @param {string} specVersion : The OAS version
 * @param {string} paramType : The parameter type.
 * @return {array} The list of supported parameters.
 * */
export function getSupportedDataTypes(specVersion, paramType) {
    return paramType ? supportedDataTypes[specVersion === '2.0' ? '2.0' : '3.x'][paramType] : [];
}

/**
 * Get the supported data formats by each data type.
 *
 * @param {string} dataType: THe data type.
 * @return {array} The list of supported data formats.
 * */
export function getDataFormats(dataType) {
    switch (dataType) {
        case 'integer':
            return ['int64', 'int32'];
        case 'number':
            return ['float', 'double'];
        case 'string':
            return ['date', 'date-time', 'byte', 'binary', 'password'];
        default:
            return [];
    }
}

/**
 * Method to get the parameter schema based on the spec version.
 * For more info about Data Models (Schemas) refer https://swagger.io/docs/specification/data-models/
 * @param {string} specVersion : The OAS version
 * @return {object} The default schema
 * */
export function getParameter(specVersion) {
    return specVersion === '2.0'
        ? {
            in: '', name: '', type: '', required: '',
        }
        : {
            in: '', name: '', schema: { type: '' }, required: '',
        };
}

/**
 * Get parameter types supported by spec version.
 *
 * @param {string} specVersion : The OAS version
 * @return {array} The list of supported parameter types.
 * */
export function getParameterTypes(specVersion) {
    return specVersion === '2.0' ? ['query', 'header', 'body', 'formData'] : ['query', 'header', 'cookie', 'body'];
}
