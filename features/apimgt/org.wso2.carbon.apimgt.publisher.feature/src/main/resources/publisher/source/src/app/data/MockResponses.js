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
        const response = '{\n   \'openapi\':\'3.0.0\',\n   \'info\':{\n      \'title\':\'Link Example\',\n      \'version\':\'1.0.0\'\n   },\n   \'paths\':{\n      \'/2.0/users/{username}\':{\n         \'get\':{\n            \'operationId\':\'getUserByName\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               }\n            ],\n            \'responses\':{\n               \'200\':{\n                  \'description\':\'The User\',\n                  \'content\':{\n                     \'application/json\':{\n                        \'schema\':{\n                           \'$ref\':\'#/components/schemas/user\'\n                        }\n                     }\n                  },\n                  \'links\':{\n                     \'userRepositories\':{\n                        \'$ref\':\'#/components/links/UserRepositories\'\n                     }\n                  }\n               }\n            }\n         }\n      },\n      \'/2.0/repositories/{username}\':{\n         \'get\':{\n            \'operationId\':\'getRepositoriesByOwner\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               }\n            ],\n            \'responses\':{\n               \'200\':{\n                  \'description\':\'repositories owned by the supplied user\',\n                  \'content\':{\n                     \'application/json\':{\n                        \'schema\':{\n                           \'type\':\'array\',\n                           \'items\':{\n                              \'$ref\':\'#/components/schemas/repository\'\n                           }\n                        }\n                     }\n                  },\n                  \'links\':{\n                     \'userRepository\':{\n                        \'$ref\':\'#/components/links/UserRepository\'\n                     }\n                  }\n               }\n            }\n         }\n      },\n      \'/2.0/repositories/{username}/{slug}\':{\n         \'get\':{\n            \'operationId\':\'getRepository\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'slug\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               }\n            ],\n            \'responses\':{\n               \'200\':{\n                  \'description\':\'The repository\',\n                  \'content\':{\n                     \'application/json\':{\n                        \'schema\':{\n                           \'$ref\':\'#/components/schemas/repository\'\n                        }\n                     }\n                  },\n                  \'links\':{\n                     \'repositoryPullRequests\':{\n                        \'$ref\':\'#/components/links/RepositoryPullRequests\'\n                     }\n                  }\n               }\n            }\n         }\n      },\n      \'/2.0/repositories/{username}/{slug}/pullrequests\':{\n         \'get\':{\n            \'operationId\':\'getPullRequestsByRepository\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'slug\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'state\',\n                  \'in\':\'query\',\n                  \'schema\':{\n                     \'type\':\'string\',\n                     \'enum\':[\n                        \'open\',\n                        \'merged\',\n                        \'declined\'\n                     ]\n                  }\n               }\n            ],\n            \'responses\':{\n               \'200\':{\n                  \'description\':\'an array of pull request objects\',\n                  \'content\':{\n                     \'application/json\':{\n                        \'schema\':{\n                           \'type\':\'array\',\n                           \'items\':{\n                              \'$ref\':\'#/components/schemas/pullrequest\'\n                           }\n                        }\n                     }\n                  }\n               }\n            }\n         }\n      },\n      \'/2.0/repositories/{username}/{slug}/pullrequests/{pid}\':{\n         \'get\':{\n            \'operationId\':\'getPullRequestsById\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'slug\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'pid\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               }\n            ],\n            \'responses\':{\n               \'200\':{\n                  \'description\':\'a pull request object\',\n                  \'content\':{\n                     \'application/json\':{\n                        \'schema\':{\n                           \'$ref\':\'#/components/schemas/pullrequest\'\n                        }\n                     }\n                  },\n                  \'links\':{\n                     \'pullRequestMerge\':{\n                        \'$ref\':\'#/components/links/PullRequestMerge\'\n                     }\n                  }\n               }\n            }\n         }\n      },\n      \'/2.0/repositories/{username}/{slug}/pullrequests/{pid}/merge\':{\n         \'post\':{\n            \'operationId\':\'mergePullRequest\',\n            \'parameters\':[\n               {\n                  \'name\':\'username\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'slug\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               },\n               {\n                  \'name\':\'pid\',\n                  \'in\':\'path\',\n                  \'required\':true,\n                  \'schema\':{\n                     \'type\':\'string\'\n                  }\n               }\n            ],\n            \'responses\':{\n               \'204\':{\n                  \'description\':\'the PR was successfully merged\'\n               }\n            }\n         }\n      }\n   },\n   \'components\':{\n      \'links\':{\n         \'UserRepositories\':{\n            \'operationId\':\'getRepositoriesByOwner\',\n            \'parameters\':{\n               \'username\':\'$response.body#/username\'\n            }\n         },\n         \'UserRepository\':{\n            \'operationId\':\'getRepository\',\n            \'parameters\':{\n               \'username\':\'$response.body#/owner/username\',\n               \'slug\':\'$response.body#/slug\'\n            }\n         },\n         \'RepositoryPullRequests\':{\n            \'operationId\':\'getPullRequestsByRepository\',\n            \'parameters\':{\n               \'username\':\'$response.body#/owner/username\',\n               \'slug\':\'$response.body#/slug\'\n            }\n         },\n         \'PullRequestMerge\':{\n            \'operationId\':\'mergePullRequest\',\n            \'parameters\':{\n               \'username\':\'$response.body#/author/username\',\n               \'slug\':\'$response.body#/repository/slug\',\n               \'pid\':\'$response.body#/id\'\n            }\n         }\n      },\n      \'schemas\':{\n         \'user\':{\n            \'type\':\'object\',\n            \'properties\':{\n               \'username\':{\n                  \'type\':\'string\'\n               },\n               \'uuid\':{\n                  \'type\':\'string\'\n               }\n            }\n         },\n         \'repository\':{\n            \'type\':\'object\',\n            \'properties\':{\n               \'slug\':{\n                  \'type\':\'string\'\n               },\n               \'owner\':{\n                  \'$ref\':\'#/components/schemas/user\'\n               }\n            }\n         },\n         \'pullrequest\':{\n            \'type\':\'object\',\n            \'properties\':{\n               \'id\':{\n                  \'type\':\'integer\'\n               },\n               \'title\':{\n                  \'type\':\'string\'\n               },\n               \'repository\':{\n                  \'$ref\':\'#/components/schemas/repository\'\n               },\n               \'author\':{\n                  \'$ref\':\'#/components/schemas/user\'\n               }\n            }\n         }\n      }\n   }\n}';
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
