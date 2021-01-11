/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  * Swagger-parser library(https://apidevtools.org/swagger-parser/) is used for parsing the swagger YAML file
  * Prism-HTTP module is used for generating the mock data from the swagger definition
  */
import { generateStatic } from '@stoplight/prism-http/dist/mocker/generator/JSONSchema';

/**
 *
 * Return sample mocked model data for given definition name
 * @export
 * @param {string} modelName model name
 * @returns {*} Mocked API model
 */
export default async function getMockedModel(modelName) {
    const swagger = await apiDef;
    return generateStatic(swagger.components.schemas[modelName]);
}

/**
 *
 * Return all the available scopes under securityDefinitions in publisher-api.yaml
 * @export
 * @returns {Array} All the scopes available in publisher-api swagger
 */
export async function getAllScopes() {
    const swagger = await apiDef;
    return Object.keys(swagger.components.securitySchemes.OAuth2Security.flows.password.scopes);
}

/**
 *
 * Returns the example identified by the 'id' from the swagger extension 'x-examples' for the particular path and verb.
 * The example will contain both request and response information.
 * @param {string} resource resource path
 * @param {string} verb http verb
 * @param {string} id id of the example
 * @returns {Promise<*>} the example for the given operation and id
 */
export async function getExampleById(resource, verb, id) {
    const swagger = await apiDef;
    return swagger.paths[resource][verb]['x-examples'].find((x) => x.id === id);
}

/**
 *
 * Returns the example identified by the 'id' from the swagger extension 'x-examples' for the particular path and verb.
 * The example will only contain response information (status and body).
 * @param {string} resource resource path
 * @param {string} verb http verb
 * @param {string} id id of the example
 * @returns {Promise<*>} the example response for the given operation and id
 */
export async function getExampleResponseById(resource, verb, id) {
    const example = await getExampleById(resource, verb, id);
    return example.response;
}

/**
 *
 * Returns the example identified by the 'id' from the swagger extension 'x-examples' for the particular path and verb.
 * The example will only contain response body.
 * @param {string} resource resource path
 * @param {string} verb http verb
 * @param {string} id id of the example
 * @returns {Promise<*>} the example response body for the given operation and id
 */
export async function getExampleBodyById(resource, verb, id) {
    const example = await getExampleResponseById(resource, verb, id);
    return example.body;
}
