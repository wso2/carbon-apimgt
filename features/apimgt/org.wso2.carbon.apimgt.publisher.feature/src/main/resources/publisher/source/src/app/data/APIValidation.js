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

import Joi from '@hapi/joi';
import API from 'AppData/api';
import queryString from 'query-string';

/**
 * Get the base error message for error types.
 * This error overrides the default error messages of joi and adds simple error messages.
 *
 * @param {string} errorType The joi error type
 * @return {string} simplified error message.
 * */
function getMessage(errorType, maxLength) {
    switch (errorType) {
        case 'any.empty':
            return 'should not be empty';
        case 'string.regex.base':
            return 'should not contain spaces or special characters';
        case 'string.max':
            return 'has exceeded the maximum number of ' + maxLength + ' characters';
        default:
            return 'should not be empty';
    }
}

/*
* eslint validation rule for no-unused-vars has been disabled in this component.
* According to the Joi extension, it is required to provide required four input
* arguments to custom Joi schema validate function.
* Ref: https://hapi.dev/family/joi/?v=15.1.1#extendextension
*/
const roleSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'systemRole',
    rules: [
        {
            name: 'role',
            validate(params, value, state, options) { // eslint-disable-line no-unused-vars
                const api = new API();
                return api.validateSystemRole(value);
            },
        },
    ],
}));

const scopeSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'scopes',
    rules: [
        {
            name: 'scope',
            validate(params, value, state, options) { // eslint-disable-line no-unused-vars
                const api = new API();
                return api.validateScopeName(value);
            },
        },
    ],
}));

const userRoleSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'userRole',
    rules: [
        {
            name: 'role',
            validate(params, value, state, options) { // eslint-disable-line no-unused-vars
                const api = new API();
                return api.validateUSerRole(value);
            },
        },
    ],
}));

const apiSchema = Joi.extend((joi) => ({
    base: joi.string(),
    name: 'api',
    rules: [
        {
            name: 'isAPIParameterExist',
            validate(params, value, state, options) { // eslint-disable-line no-unused-vars
                const inputValue = value.trim().toLowerCase();
                const composeQuery = '?query=' + inputValue;
                const composeQueryJSON = queryString.parse(composeQuery);
                composeQueryJSON.limit = 1;
                composeQueryJSON.offset = 0;
                return API.search(composeQueryJSON);
            },
        },
    ],
}));

const documentSchema = Joi.extend((joi) => ({
    base: joi.object(),
    name: 'document',
    rules: [
        {
            name: 'isDocumentPresent',
            validate(params, value, state, options) { // eslint-disable-line no-unused-vars
                const api = new API();
                return api.validateDocumentExists(value.id, value.name);
            },
        },
    ],
}));

const definition = {
    apiName: Joi.string().max(50).regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+[\]/]*$/).required()
        .error((errors) => {
            return errors.map((error) => ({ ...error, message: 'Name ' + getMessage(error.type, 50) }));
        }),
    apiVersion: Joi.string().regex(/^[^~!@#;:%^*()+={}|\\<>"',&/$[\]\s]+$/).required().error((errors) => {
        const tmpErrors = [...errors];
        errors.forEach((err, index) => {
            const tmpError = { ...err };
            tmpError.message = 'API Version ' + getMessage(err.type);
            tmpErrors[index] = tmpError;
        });
        return tmpErrors;
    }),
    apiContext: Joi.string().max(60).regex(/(?!.*\/t\/.*|.*\/t$)^[^~!@#:%^&*+=|\\<>"',&\s[\]]*$/).required()
        .error((errors) => {
            return errors.map((error) => ({ ...error, message: 'Context ' + getMessage(error.type, 60) }));
        }),
    documentName: Joi.string().max(50).regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+[\]/]*$/).required()
        .error((errors) => {
            return errors.map((error) => ({ ...error, message: 'Document name ' + getMessage(error.type, 50) }));
        }),
    authorizationHeader: Joi.string().regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+]*$/).required()
        .error((errors) => {
            return errors.map((error) => ({ ...error, message: 'Authorization Header ' + getMessage(error.type) }));
        }),
    role: roleSchema.systemRole().role(),
    scope: scopeSchema.scopes().scope(),
    url: Joi.string().uri({ scheme: ['http', 'https'] }).error((errors) => {
        const tmpErrors = [...errors];
        errors.forEach((err, index) => {
            const tmpError = { ...err };
            tmpError.message = 'URL ' + getMessage(err.type);
            tmpErrors[index] = tmpError;
        });
        return tmpErrors;
    }),
    wsUrl: Joi.string().uri({ scheme: ['ws', 'wss'] }).error((errors) => {
        const tmpErrors = [...errors];
        errors.forEach((err, index) => {
            const tmpError = { ...err };
            const errType = err.type;
            tmpError.message = errType === 'string.uriCustomScheme' ? 'Invalid WebSocket URL'
                : 'WebSocket URL ' + getMessage(errType);
            tmpErrors[index] = tmpError;
        });
        return tmpErrors;
    }),
    alias: Joi.string().max(30).regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+[\]/]*$/).required()
        .error((errors) => {
            return errors.map((error) => ({ ...error, message: 'Alias ' + getMessage(error.type, 30) }));
        }),
    userRole: userRoleSchema.userRole().role(),
    apiParameter: apiSchema.api().isAPIParameterExist(),
    apiDocument: documentSchema.document().isDocumentPresent(),
    operationVerbs: Joi.array().items(Joi.string()).min(1).unique(),
    operationTarget: Joi.string().required(),
    websubOperationTarget: Joi.string().regex(/^[^{}]*$/).required(),
    name: Joi.string().min(1).max(255),
    email: Joi.string().email({ tlds: true }).required(),
};

export default definition;
