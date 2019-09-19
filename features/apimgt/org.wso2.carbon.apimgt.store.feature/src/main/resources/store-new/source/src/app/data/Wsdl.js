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
 * API client for WSDL related operations
 */
class Wsdl extends Resource {
    /**
     * Constructor of the WSDL API client
     * @param {*} client SwaggerClient object
     */
    constructor(client = null) {
        super();
        if (client == null) {
            this.apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        } else {
            this.apiClient = client;
        }
    }

    /**
     * Download the WSDL of an API for the given gateway environment
     *
     * @static
     * @param {string} apiId API UUID
     * @param {string} environmentName name of the gateway environment
     * @returns {*} WSDL validation response
     * @memberof Wsdl
     */
    downloadWSDLForEnvironment(apiId, environmentName = null) {
        return this.apiClient.then((client) => {
            return client.apis.APIs.getWSDLOfAPI({ apiId, environmentName });
        });
    }
}

export default Wsdl;
