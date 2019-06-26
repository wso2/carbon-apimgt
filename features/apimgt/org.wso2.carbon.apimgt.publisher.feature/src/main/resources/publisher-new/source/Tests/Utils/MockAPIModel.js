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
import { generateStatic } from '@stoplight/prism-http/lib/mocker/generator/JSONSchema';
import SwaggerParser from 'swagger-parser';
import fs from 'fs';
import path from 'path';

const CARBON_APIMGT_ROOT = path.join(__dirname, '../../../../../../../../../../');
const SWAGGER_RELATIVE_PATH =
    'components/apimgt/org.wso2.carbon.apimgt.rest.api.publisher.v1/src/main/resources/publisher-api.yaml';
const swaggerFilePath = path.join(CARBON_APIMGT_ROOT, SWAGGER_RELATIVE_PATH);

/**
 *
 * Return sample mocked model data for given definition name
 * @export
 * @param {string} modelName model name
 * @returns {*} Mocked API model
 */
export default async function getMockedModel(modelName) {
    const swaggerFile = fs.readFileSync(swaggerFilePath, 'utf8');
    const { YAML } = SwaggerParser;
    const swagger = await SwaggerParser.dereference(YAML.parse(swaggerFile));
    return generateStatic(swagger.definitions[modelName]);
}
