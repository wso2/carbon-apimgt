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
    * Return API created from service
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static createApiFromService() {
        const response = {
            id: '87624513-0123-6274-9013-012345678901',
            name: 'New-API',
            context: '/newapi',
            description: 'This is a new API created from a service',
            version: '1.0.0',
        };
        return Promise.resolve({ body: response });
    }

    /**
    *
    * Return sample mocked model data for addSampleService
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static addSampleService() {
        const response = {
            id: '87624513-0123-6274-9013-012345678901',
            name: 'Sample-Endpoint',
            displayName: 'Sample-Endpoint',
            description: 'A sample Catalog Entry that exposes a REST endpoint',
            version: '1.0.0',
            serviceUrl: 'http://localhost/sample',
            definitionType: 'OAS3',
            securityType: 'BASIC',
            mutualSSLEnabled: false,
            usage: 1,
            createdTime: '2020-02-20T13:57:16.229Z',
            lastUpdatedTime: '2020-02-20T13:57:16.229Z',
            etag: '32c890312cfyuc94a7c1153f65a4f100',
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
            total: 2,
            list: [
                {
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
                },
                {
                    id: '43214567-0123-2345-1298-012345678901',
                    name: 'New-Endpoint',
                    displayName: 'New-Endpoint',
                    description: 'A Catalog Entry that exposes a REST endpoint',
                    version: 'v1',
                    serviceUrl: 'http://localhost/new',
                    definitionType: 'ASYNC_API',
                    securityType: 'OAUTH2',
                    mutualSSLEnabled: false,
                    usage: 1,
                    createdTime: '2020-02-20T13:57:16.229Z',
                    lastUpdatedTime: '2020-02-20T13:57:16.229Z',
                    etag: '32c890312cfadc94a7c1153f65a4f100',
                },
            ],
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
        const response = {
            id: '7abe3fc3-1abe-47de-894a-696a7f4bb052',
            name: 'PizzaShackAPI',
            description: 'This is a simple API for Pizza Shack online pizza delivery store.',
            context: '/pizzashack',
            version: '1.0.0',
            provider: 'admin',
            lifeCycleStatus: 'CREATED',
            type: 'HTTP',
            createdTime: 1609821955163,
            lastUpdatedTime: 1609821972343,
        };
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
    * Return sample mocked model data for getAPIUsages
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static getAPIUsages() {
        const response = {
            limit: 0,
            offset: 0,
            total: 1,
            list: [
                {
                    id: '7abe3fc3-1abe-47de-894a-696a7f4bb052',
                    name: 'PizzaShackAPI',
                    description: 'This is a simple API for Pizza Shack online pizza delivery store.',
                    context: '/pizzashack',
                    version: '1.0.0',
                    provider: 'admin',
                    lifeCycleStatus: 'CREATED',
                    type: 'HTTP',
                    createdTime: 1609821955163,
                    lastUpdatedTime: 1609821972343,
                },
            ],
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
    * Return sample mocked model data for getServiceDefinition
    * @export
    * @returns {*} Mocked Service Catalog model
    */
    static getServiceDefinition() {
        const response = {
            openapi: '3.0.0',
            info: {
                title: 'Callback Example',
                version: '1.0.0',
            },
            paths: {
                '/streams': {
                    post: {
                        description: 'subscribes a client to receive out-of-band data',
                        parameters: [
                            {
                                name: 'callbackUrl',
                                in: 'query',
                                required: true,
                                description: 'the location where data will be sent.'
                                + ' Must be network accessible\nby the source server\n',
                                schema: {
                                    type: 'string',
                                    format: 'uri',
                                    example: 'https://tonys-server.com',
                                },
                            },
                        ],
                        responses: {
                            201: {
                                description: 'subscription successfully created',
                                content: {
                                    'application/json': {
                                        schema: {
                                            description: 'subscription information',
                                            required: [
                                                'subscriptionId',
                                            ],
                                            properties: {
                                                subscriptionId: {
                                                    description: 'this unique identifier allows'
                                                    + ' management of the subscription',
                                                    type: 'string',
                                                    example: '2531329f-fb09-4ef7-887e-84e648214436',
                                                },
                                            },
                                        },
                                    },
                                },
                            },
                        },
                        callbacks: {
                            onData: {
                                '{$request.query.callbackUrl}/data': {
                                    post: {
                                        requestBody: {
                                            description: 'subscription payload',
                                            content: {
                                                'application/json': {
                                                    schema: {
                                                        type: 'object',
                                                        properties: {
                                                            timestamp: {
                                                                type: 'string',
                                                                format: 'date-time',
                                                            },
                                                            userData: {
                                                                type: 'string',
                                                            },
                                                        },
                                                    },
                                                },
                                            },
                                        },
                                        responses: {
                                            202: {
                                                description: 'Your server implementation should return'
                                                + ' this HTTP status code\nif the data was received successfully\n',
                                            },
                                            204: {
                                                description: 'Your server should return this HTTP status'
                                                + ' code if no longer interested\nin further updates\n',
                                            },
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        };
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
