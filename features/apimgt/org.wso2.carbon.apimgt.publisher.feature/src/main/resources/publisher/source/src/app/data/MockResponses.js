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
        // eslint-disable-next-line max-len
        const response = '{"openapi":"3.0.0","info":{"title":"Link Example","version":"1.0.0"},"paths":{"/2.0/users/{username}":{"get":{"operationId":"getUserByName","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}}],"responses":{"200":{"description":"The User","content":{"application/json":{"schema":{"$ref":"#/components/schemas/user"}}},"links":{"userRepositories":{"$ref":"#/components/links/UserRepositories"}}}}}},"/2.0/repositories/{username}":{"get":{"operationId":"getRepositoriesByOwner","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}}],"responses":{"200":{"description":"repositories owned by the supplied user","content":{"application/json":{"schema":{"type":"array","items":{"$ref":"#/components/schemas/repository"}}}},"links":{"userRepository":{"$ref":"#/components/links/UserRepository"}}}}}},"/2.0/repositories/{username}/{slug}":{"get":{"operationId":"getRepository","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}},{"name":"slug","in":"path","required":true,"schema":{"type":"string"}}],"responses":{"200":{"description":"The repository","content":{"application/json":{"schema":{"$ref":"#/components/schemas/repository"}}},"links":{"repositoryPullRequests":{"$ref":"#/components/links/RepositoryPullRequests"}}}}}},"/2.0/repositories/{username}/{slug}/pullrequests":{"get":{"operationId":"getPullRequestsByRepository","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}},{"name":"slug","in":"path","required":true,"schema":{"type":"string"}},{"name":"state","in":"query","schema":{"type":"string","enum":["open","merged","declined"]}}],"responses":{"200":{"description":"an array of pull request objects","content":{"application/json":{"schema":{"type":"array","items":{"$ref":"#/components/schemas/pullrequest"}}}}}}}},"/2.0/repositories/{username}/{slug}/pullrequests/{pid}":{"get":{"operationId":"getPullRequestsById","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}},{"name":"slug","in":"path","required":true,"schema":{"type":"string"}},{"name":"pid","in":"path","required":true,"schema":{"type":"string"}}],"responses":{"200":{"description":"a pull request object","content":{"application/json":{"schema":{"$ref":"#/components/schemas/pullrequest"}}},"links":{"pullRequestMerge":{"$ref":"#/components/links/PullRequestMerge"}}}}}},"/2.0/repositories/{username}/{slug}/pullrequests/{pid}/merge":{"post":{"operationId":"mergePullRequest","parameters":[{"name":"username","in":"path","required":true,"schema":{"type":"string"}},{"name":"slug","in":"path","required":true,"schema":{"type":"string"}},{"name":"pid","in":"path","required":true,"schema":{"type":"string"}}],"responses":{"204":{"description":"the PR was successfully merged"}}}}},"components":{"links":{"UserRepositories":{"operationId":"getRepositoriesByOwner","parameters":{"username":"$response.body#/username"}},"UserRepository":{"operationId":"getRepository","parameters":{"username":"$response.body#/owner/username","slug":"$response.body#/slug"}},"RepositoryPullRequests":{"operationId":"getPullRequestsByRepository","parameters":{"username":"$response.body#/owner/username","slug":"$response.body#/slug"}},"PullRequestMerge":{"operationId":"mergePullRequest","parameters":{"username":"$response.body#/author/username","slug":"$response.body#/repository/slug","pid":"$response.body#/id"}}},"schemas":{"user":{"type":"object","properties":{"username":{"type":"string"},"uuid":{"type":"string"}}},"repository":{"type":"object","properties":{"slug":{"type":"string"},"owner":{"$ref":"#/components/schemas/user"}}},"pullrequest":{"type":"object","properties":{"id":{"type":"integer"},"title":{"type":"string"},"repository":{"$ref":"#/components/schemas/repository"},"author":{"$ref":"#/components/schemas/user"}}}}}}';
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
