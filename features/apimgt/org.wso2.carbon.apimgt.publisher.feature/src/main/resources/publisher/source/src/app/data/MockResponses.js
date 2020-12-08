/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Class representing Mock responses for Service Catalog rest api functions
 */
class MockResponses {
    /**
    *
    * Return sample mocked model data for getSettings
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static getSettings() {
        const response = {
            scopes: [
                'string',
            ],
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for searchServices
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static searchServices() {
        const response = {
            limit: 0,
            offset: 0,
            total: 0,
            list: [],
            pagination: {
                offset: 0,
                limit: 1,
                total: 10,
                next: 'string',
                previous: 'string',
            },
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for checkServiceExistence
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static checkServiceExistence() {
        const response = {};
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for createService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static createService() {
        const response = {
            id: '01234567-0123-0123-0123-012345678901',
            name: 'Pizzashack-Endpoint',
            displayName: 'Pizzashack-Endpoint',
            description: 'A Catalog Entry that exposes a REST endpoint',
            version: 'v1',
            serviceUrl: 'http://localhost/pizzashack',
            definitionType: 'OAS3',
            securityType: 'BASIC',
            mutualSSLEnabled: false,
            usage: 1,
            createdTime: '2020-02-20T13:57:16.229Z',
            lastUpdatedTime: '2020-02-20T13:57:16.229Z',
            etag: '32c890312cfadc94a7c1153f65a4f100',
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for createService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static listRevision() {
        const response = {
            count: 1,
            list: [
                {
                    id: 1,
                    uuid: 'c26b2b9b-4632-4ca4-b6f3-521c8863990c',
                    description: 'removed a post resource',
                    apiInfo: {
                        id: '01234567-0123-0123-0123-012345678901',
                        name: 'CalculatorAPI',
                        description: 'A calculator API that supports basic operations',
                        context: 'CalculatorAPI',
                        version: '1.0.0',
                        provider: 'admin',
                        type: 'HTTP',
                        lifeCycleStatus: 'CREATED',
                    },
                },
                {
                    id: 2,
                    uuid: 'c26b2b9b-4632-4ca4-b6f3-521c8863990d',
                    description: 'removed a post resource and removed a get resource removed a post resource',
                    apiInfo: {
                        id: '01234567-0123-0123-0123-012345678901',
                        name: 'CalculatorAPI',
                        description: 'A calculator API that supports basic operations',
                        context: 'CalculatorAPI',
                        version: '1.0.0',
                        provider: 'admin',
                        type: 'HTTP',
                        lifeCycleStatus: 'CREATED',
                    },
                },
            ],
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for deleteService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static deleteService() {
        const response = {};
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for exportService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static exportService() {
        const response = 'string';
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for importService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static importService() {
        const response = {};
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for getServiceById
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static getServiceById() {
        const response = {
            id: '01234567-0123-0123-0123-012345678901',
            name: 'Pizzashack-Endpoint',
            displayName: 'Pizzashack-Endpoint',
            description: 'A Catalog Entry that exposes a REST endpoint',
            version: 'v1',
            serviceUrl: 'http://localhost/pizzashack',
            definitionType: 'OAS3',
            securityType: 'BASIC',
            mutualSSLEnabled: false,
            usage: 1,
            createdTime: '2020-02-20T13:57:16.229Z',
            lastUpdatedTime: '2020-02-20T13:57:16.229Z',
            etag: '32c890312cfadc94a7c1153f65a4f100',
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for getServiceDefinition
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static getServiceDefinition() {
        const response = 'string';
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for getServiceById
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static updateService() {
        const response = {
            id: '01234567-0123-0123-0123-012345678901',
            name: 'Pizzashack-Endpoint',
            displayName: 'Pizzashack-Endpoint',
            description: 'A Catalog Entry that exposes a REST endpoint',
            version: 'v1',
            serviceUrl: 'http://localhost/pizzashack',
            definitionType: 'OAS3',
            securityType: 'BASIC',
            mutualSSLEnabled: false,
            usage: 1,
            createdTime: '2020-02-20T13:57:16.229Z',
            lastUpdatedTime: '2020-02-20T13:57:16.229Z',
            etag: '32c890312cfadc94a7c1153f65a4f100',
        };
        return Promise.resolve({ body: response });
    }
}

export default MockResponses;
