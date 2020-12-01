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

import API from 'AppData/api';

import APIClientFactory from './APIClientFactory';
import Utils from './Utils';
import Resource from './Resource';

/**
 * An abstract representation of a Scopes
 */
class Wsdl extends Resource {
    /**
     * Validate a WSDL file or an archive
     *
     * @static
     * @param {*} file WSDL file or archive
     * @returns {*} WSDL validation response
     * @memberof Wsdl
     */
    static validateFileOrArchive(file) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT)
            .client;
        const requestBody = {
            requestBody: {
                file,
            },
        };
        return apiClient.then((client) => {
            return client.apis.Validation.validateWSDLDefinition(null, requestBody);
        });
    }

    /**
     * Validate a WSDL URL
     *
     * @static
     * @param {*} url WSDL URL
     * @returns {*} WSDL validation response
     * @memberof Wsdl
     */
    static validateUrl(url) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT)
            .client;
        return apiClient.then((client) => {
            return client.apis.Validation.validateWSDLDefinition(
                {},
                {
                    requestBody: { url },
                },
            );
        });
    }

    /**
     * Importing a WSDL and creating an API by a .wsdl file or a WSDL archive zip file
     *
     * @static
     * @param {*} url WSDL url
     * @param {*} additionalProperties additional properties of the API eg: name, version, context
     * @param {*} implementationType SOAPTOREST or SOAP
     * @returns {API} API object which was created
     * @memberof Wsdl
     */
    static importByUrl(url, additionalProperties, implementationType = 'SOAP') {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT)
            .client;
        return apiClient.then((client) => {
            const promisedResponse = client.apis.APIs.importWSDLDefinition(
                {},
                {
                    requestBody: {
                        url,
                        additionalProperties: JSON.stringify(additionalProperties),
                        implementationType,
                    },
                },
            );
            return promisedResponse.then((response) => new API(response.body));
        });
    }

    /**
     * Importing a WSDL and creating an API by a .wsdl file or a WSDL archive zip file
     *
     * @static
     * @param {*} file WSDL file or archive
     * @param {*} additionalProperties additional properties of the API eg: name, version, context
     * @param {*} implementationType SOAPTOREST or SOAP
     * @returns {API} API object which was created
     * @memberof Wsdl
     */
    static importByFileOrArchive(file, additionalProperties, implementationType = 'SOAP') {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT)
            .client;
        return apiClient.then((client) => {
            const promisedResponse = client.apis.APIs.importWSDLDefinition(
                null,
                {
                    requestBody: {
                        file,
                        additionalProperties: JSON.stringify(additionalProperties),
                        implementationType,
                    },
                },
            );

            return promisedResponse.then((response) => new API(response.body));
        });
    }
}

export default Wsdl;
