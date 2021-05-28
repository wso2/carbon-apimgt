/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
const suggestions = {
    common: [
        {
            label: 'Overview',
            route: 'overview',
        },
        {
            label: 'MetaData',
            route: 'overview',
        },
        {
            label: 'Runtime Configurations',
            route: 'runtime-configuration',
        },
        {
            label: 'Transport Level Security',
            route: 'runtime-configuration',
        },
        {
            label: 'Mutual SSL',
            route: 'runtime-configuration',
        },
        {
            label: 'Design Configurations',
            route: 'configuration',
        },
        {
            label: 'Manage SSL Certificates',
            route: 'configuration',
        },
        {
            label: 'HTTP HTTPS enable disable',
            route: 'runtime-configuration',
        },
        {
            label: 'Application Level Security',
            route: 'runtime-configuration',
        },
        {
            label: 'Enable/Disable OAuth2',
            route: 'runtime-configuration',
        },
        {
            label: 'Enable/Disable Basic Auth',
            route: 'runtime-configuration',
        },
        {
            label: 'Enable/Disable Api Key',
            route: 'runtime-configuration',
        },
        {
            label: 'Authorization Headers',
            route: 'runtime-configuration',
        },
        {
            label: 'CORS Configuration',
            route: 'runtime-configuration',
        },
        {
            label: 'Backend Throughput',
            route: 'runtime-configuration',
        },
        {
            label: 'Message Mediation',
            route: 'runtime-configuration',
        },
        {
            label: 'Response',
            route: 'runtime-configuration',
        },
        {
            label: 'Request',
            route: 'runtime-configuration',
        },
        {
            label: 'Backend',
            route: 'runtime-configuration',
        },
        {
            label: 'Access Control Allow Origins',
            route: 'configuration',
        },
        {
            label: 'Access Control Allow Headers',
            route: 'configuration',
        },
        {
            label: 'Access Control Allow Credentials',
            route: 'configuration',
        },
        {
            label: 'Response caching',
            route: 'runtime-configuration',
        },
        {
            label: 'Publisher Access Control',
            route: 'configuration',
        },
        {
            label: 'Developer Portal Visibility',
            route: 'configuration',
        },
        {
            label: 'Tags',
            route: 'configuration',
        },
        {
            label: 'Default Version',
            route: 'configuration',
        },
        {
            label: 'Image',
            route: 'configuration',
        },
        {
            label: 'Thumbnail',
            route: 'configuration',
        },
        {
            label: 'Description',
            route: 'configuration',
        },
        {
            label: 'Documents',
            route: 'documents',
        },
        {
            label: 'Add New Documents',
            route: 'documents',
        },
        {
            label: 'API Help',
            route: 'documents',
        },
        {
            label: 'Markdown Documents',
            route: 'documents',
        },
        {
            label: 'Business Information',
            route: 'business info',
        },
        {
            label: 'Business Owner',
            route: 'business info',
        },
        {
            label: 'Technical Owner',
            route: 'business info',
        },
        {
            label: 'Properties',
            route: 'business info',
        },
        {
            label: 'Subscriptions',
            route: 'subscriptions',
        },
        {
            label: 'Subscription Policies',
            route: 'subscriptions',
        },
        {
            label: 'Subscription Availability',
            route: 'subscriptions',
        },
        {
            label: 'Revisions',
            route: 'deployments',
        },
        {
            label: 'VHosts',
            route: 'deployments',
        },
    ],
    apiOnly: [
        {
            label: 'API Gateways',
            route: 'deployments',
        },
        {
            label: 'Environments',
            route: 'deployments',
        },
        {
            label: 'Deployments',
            route: 'deployments',
        },
        {
            label: 'Lifecycle',
            route: 'lifecycle',
        },
        {
            label: 'Publish API',
            route: 'lifecycle',
        },
        {
            label: 'Block API',
            route: 'lifecycle',
        },
        {
            label: 'Depricate API',
            route: 'lifecycle',
        },
        {
            label: 'Change Lifecycle',
            route: 'lifecycle',
        },
        {
            label: 'Deploy as a Prototype API',
            route: 'lifecycle',
        },
        {
            label: 'Requirements',
            route: 'lifecycle',
        },
        {
            label: 'Mediation Policies',
            route: 'runtime-configuration',
        },
        {
            label: 'Monetization',
            route: 'monetization',
        },
        {
            label: 'Monetization Properties',
            route: 'monetization',
        },
        {
            label: 'Commercial Policies',
            route: 'monetization',
        },
        {
            label: 'Create New Version',
            route: 'new_version',
        },
        {
            label: 'Prototyped API',
            route: 'endpoints',
        },
        {
            label: 'AWS Lambda Endpoints',
            route: 'endpoints',
        },
        {
            label: 'HTTP/HTTPS Endpoints',
            route: 'endpoints',
        },
        {
            label: 'HTTP/SOAP Endpoints',
            route: 'endpoints',
        },
        {
            label: 'Dynamic Endpoints',
            route: 'endpoints',
        },
        {
            label: 'Endpoint Security',
            route: 'endpoints',
        },
        {
            label: 'Certificates',
            route: 'endpoints',
        },
        {
            label: 'Production Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Sandbox Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Failover Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Load Balance Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Endpoints',
            route: 'endpoints',
        },
        {
            label: 'API Definition',
            route: 'api definition',
        },
        {
            label: 'Import API Definition',
            route: 'api definition',
        },
        {
            label: 'Download API Definition',
            route: 'api definition',
        },
        {
            label: 'Swagger',
            route: 'api definition',
        },
        {
            label: 'Import Swagger',
            route: 'api definition',
        },
        {
            label: 'Download Swagger',
            route: 'api definition',
        },
        {
            label: 'OAS',
            route: 'api definition',
        },
        {
            label: 'Import OAS',
            route: 'api definition',
        },
        {
            label: 'Download OAS',
            route: 'api definition',
        },
        {
            label: 'OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Import OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Download OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Resources',
            route: 'resources',
        },
        {
            label: 'Resources Configuration',
            route: 'resources',
        },
        {
            label: 'Rate Limiting',
            route: 'resources',
        },
        {
            label: 'Operation Governance',
            route: 'resources',
        },
        {
            label: 'Scopes',
            route: 'scopes',
        },
        {
            label: 'Add New Scope',
            route: 'scopes',
        },
        {
            label: 'Schema validation',
            route: 'runtime-configuration',
        },
        {
            label: 'Fault',
            route: 'runtime-configuration',
        },
        {
            label: 'Endpoints',
            route: 'runtime-configuration',
        },
        {
            label: 'Properties',
            route: 'properties',
        },
    ],
    productOnly: [
        {
            label: 'Product only feature',
            route: 'configuration',
        },
        {
            label: 'API Definition',
            route: 'api definition',
        },
        {
            label: 'Import API Definition',
            route: 'api definition',
        },
        {
            label: 'Download API Definition',
            route: 'api definition',
        },
        {
            label: 'Swagger',
            route: 'api definition',
        },
        {
            label: 'Import Swagger',
            route: 'api definition',
        },
        {
            label: 'Download Swagger',
            route: 'api definition',
        },
        {
            label: 'OAS',
            route: 'api definition',
        },
        {
            label: 'Import OAS',
            route: 'api definition',
        },
        {
            label: 'Download OAS',
            route: 'api definition',
        },
        {
            label: 'OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Import OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Download OpenAPI',
            route: 'api definition',
        },
        {
            label: 'Resources',
            route: 'resources',
        },
        {
            label: 'Resources Configuration',
            route: 'resources',
        },
        {
            label: 'Rate Limiting',
            route: 'resources',
        },
        {
            label: 'Operation Governance',
            route: 'resources',
        },
        {
            label: 'Schema validation',
            route: 'runtime-configuration',
        },
    ],
    graphqlOnly: [
        {
            label: 'Operations',
            route: 'operations',
        },
        {
            label: 'Operation Type',
            route: 'operations',
        },
        {
            label: 'Operations Configuration',
            route: 'operations',
        },
        {
            label: 'Rate Limiting',
            route: 'operations',
        },
        {
            label: 'Schema Definition',
            route: 'schema definition',
        },
        {
            label: 'Scopes',
            route: 'scopes',
        },
        {
            label: 'Add New Scope',
            route: 'scopes',
        },
        {
            label: 'Fault',
            route: 'runtime-configuration',
        },
        {
            label: 'Endpoints',
            route: 'runtime-configuration',
        },
        {
            label: 'Environments',
            route: 'deployments',
        },
        {
            label: 'Deployments',
            route: 'deployments',
        },
        {
            label: 'API Gateways',
            route: 'deployments',
        },
        {
            label: 'Lifecycle',
            route: 'lifecycle',
        },
        {
            label: 'Publish API',
            route: 'lifecycle',
        },
        {
            label: 'Block API',
            route: 'lifecycle',
        },
        {
            label: 'Depricate API',
            route: 'lifecycle',
        },
        {
            label: 'Change Lifecycle',
            route: 'lifecycle',
        },
        {
            label: 'Deploy as a Prototype API',
            route: 'lifecycle',
        },
        {
            label: 'Requirements',
            route: 'lifecycle',
        },
        {
            label: 'Monetization',
            route: 'monetization',
        },
        {
            label: 'Monetization Properties',
            route: 'monetization',
        },
        {
            label: 'Commercial Policies',
            route: 'monetization',
        },
        {
            label: 'Create New Version',
            route: 'new_version',
        },
        {
            label: 'Prototyped API',
            route: 'endpoints',
        },
        {
            label: 'AWS Lambda Endpoints',
            route: 'endpoints',
        },
        {
            label: 'HTTP/HTTPS Endpoints',
            route: 'endpoints',
        },
        {
            label: 'HTTP/SOAP Endpoints',
            route: 'endpoints',
        },
        {
            label: 'Dynamic Endpoints',
            route: 'endpoints',
        },
        {
            label: 'Endpoint Security',
            route: 'endpoints',
        },
        {
            label: 'Certificates',
            route: 'endpoints',
        },
        {
            label: 'Production Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Sandbox Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Failover Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Load Balance Endpoint',
            route: 'endpoints',
        },
        {
            label: 'Endpoints',
            route: 'endpoints',
        },
        {
            label: 'Fault',
            route: 'runtime-configuration',
        },
        {
            label: 'Endpoints',
            route: 'runtime-configuration',
        },
        {
            label: 'Properties',
            route: 'properties',
        },
    ],
};
export default suggestions;
