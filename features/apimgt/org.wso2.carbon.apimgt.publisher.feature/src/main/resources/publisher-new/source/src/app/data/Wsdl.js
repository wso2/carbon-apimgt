/**
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import Utils from './Utils';
import Resource from './Resource';

/**
 * An abstract representation of a Scopes
 */
class Wsdl extends Resource {
    /**
     *
     *
     * @static
     * @param {*} scope
     * @memberof Scopes
     */
    static validate(input) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then((client) => {
            return client.apis.Validation.validateWSDLDefinition({ url: input, returnContent: true });
        });
    }

    /**
     *
     *
     * @static
     * @param {*} resource
     * @param {*} additionalProperties
     * @param {*} implementationType SOAPTOREST
     * @memberof Wsdl
     */
    static import(input, additionalProperties, implementationType = 'SOAP') {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then((client) => {
            return client.apis.APIs.importWSDLDefinition({
                url: input,
                additionalProperties: JSON.stringify(additionalProperties),
                implementationType,
            });
        });
    }
}

export default Wsdl;
