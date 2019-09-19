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
                console.warn(`Could not find target = ${target} ` +
                        `verb (lower cased) = ${verb.toLowerCase()} operation in OpenAPI definition`);
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
                console.warn(`Could not find target = ${target} ` +
                        `verb (lower cased) = ${verb.toLowerCase()} operation in OpenAPI definition`);
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

export { getTaggedOperations, getAPIProductTaggedOperations };
