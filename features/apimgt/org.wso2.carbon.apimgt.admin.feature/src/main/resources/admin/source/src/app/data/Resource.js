/**
 * Copyright (c), WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

/** *
 * Abstract resource representation, Host for generic resource related methods
 */
export default class Resource {
    /**
     *Creates an instance of Resource.
     * @memberof Resource
     */
    constructor() {
        this.client = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
    }

    /**
     * @param data
     * @returns {object} Metadata for API request
     */
    static _requestMetaData(data = {}) {
        /* TODO: This should be moved to an interceptor ~tmkb */
        return {
            requestContentType: data['Content-Type'] || 'application/json',
        };
    }


    /**
     * Check whether current resource is of type APIProduct
     *
     * @returns {boolean} condition
     * @memberof Resource
     */
    isAPIProduct() {
        return this.apiType === 'APIProduct';
    }

    /**
     * Check whether current api is of type WebSocket
     *
     * @returns {boolean} condition
     * @memberof Resource
     */
    isWebSocket() {
        return this.type === 'WS';
    }

    isGraphql() {
        return this.type === 'GRAPHQL';
    }

    isSOAPToREST() {
        return this.type === 'SOAPTOREST';
    }

    isSOAP() {
        return this.type === 'SOAP';
    }
}
