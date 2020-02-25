/**
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

/* eslint-disable */
import APIClientFactory from './APIClientFactory';
import Utils from './Utils';
import Resource from './Resource';
import cloneDeep from 'lodash.clonedeep';

/**
 * An abstract representation of an API
 */
class API extends Resource {
    constructor(name, version, context, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
            Utils.deepFreeze(properties);
        } else {
            this.name = name;
            this.version = version;
            this.context = context;
            this.isDefaultVersion = false;
            this.gatewayEnvironments = ['Production and Sandbox']; //todo: load the environments from settings API
            this.transport = ['http', 'https'];
            this.visibility = 'PUBLIC';
            this.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: '',
                },
                production_endpoints: {
                    url: '',
                },
            };
        }
        this.apiType = API.CONSTS.API;
        this._data = properties;
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                this[key] = properties[key];
            }
        }
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData() {
        Resource._requestMetaData();
    }

    /**
     *
     * Instance method of the API class to provide raw JSON object
     * which is API body friendly to use with REST api requests
     * Use this method instead of accessing the private _data object for
     * converting to a JSON representation of an API object.
     * Note: This is deep coping, Use sparingly, Else will have a bad impact on performance
     * Basically this is the revers operation in constructor.
     * This method simply iterate through all the object properties (excluding the properties in `excludes` list)
     * and copy their values to new object.
     * So use this method with care!!
     * @memberof API
     * @param {Array} [userExcludes=[]] List of properties that are need to be excluded from the generated JSON object
     * @returns {JSON} JSON representation of the API
     */
    toJSON(userExcludes = []) {
        var copy = {},
            excludes = ['_data', 'client', 'apiType', ...userExcludes];
        for (var prop in this) {
            if (!excludes.includes(prop)) {
                copy[prop] = cloneDeep(this[prop]);
            }
        }
        return copy;
    }

    importOpenAPIByFile(openAPIData, callback = null) {
        let payload, promisedCreate;
        promisedCreate = this.client.then(client => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                file: openAPIData,
                additionalProperties: JSON.stringify(apiData),
            };

            const promisedResponse = client.apis['APIs'].importOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
            return promisedResponse.then(response => new API(response.body));
        });
        return promisedCreate;
    }

    importOpenAPIByUrl(openAPIUrl) {
        let payload, promise_create;

        promise_create = this.client.then(client => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                url: openAPIUrl,
                additionalProperties: JSON.stringify(apiData),
            };

            const promisedResponse = client.apis['APIs'].importOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
            return promisedResponse.then(response => new API(response.body));
        });
        return promise_create;
    }

    static validateOpenAPIByFile(openAPIData) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        let payload, promisedValidate;
        payload = {
            file: openAPIData,
            'Content-Type': 'multipart/form-data',
        };
        promisedValidate = apiClient.then(client => {
            return client.apis.Validation.validateOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promisedValidate;
    }

    static validateOpenAPIByUrl(url, params = { returnContent: false }) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const payload = {
            url: url,
            'Content-Type': 'multipart/form-data',
            ...params
        };
        return apiClient.then(client => {
            return client.apis['Validation'].validateOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });

    }

    /**
     * Get API Security Audit Report
     */
    getSecurityAuditReport(apiId) {
        const promiseGetAuditReport = this.client.then((client) => {
            return client.apis['API Audit'].get_apis__apiId__auditapi({
                apiId: apiId
            }, this._requestMetaData());
        });
        return promiseGetAuditReport;
    }

    /**
     * export an API Directory as A Zpi file
     * @returns {promise} Promise Containing the ZPI file of the selected API 
     */
    exportApi(apiId) {
        const apiZip = this.client.then((client) => {
            return client.apis['Import Export'].get_apis_export({
                apiId: apiId
            },  this._requestMetaData({ 
                    'accept': 'application/zip'
                })
            );
        });
        return apiZip;
    }

    /**
     * Get detailed policy information of the API
     * @returns {Promise} Promise containing policy detail request calls for all the available policies
     * @memberof API
     */
    getPolicies() {
        const promisedPolicies = this.policies.map(policy => {
            return this.client.then(client =>
                client.apis['Throttling Policies'].getThrottlingPolicyByName(
                    {
                        policyLevel: 'subscription',
                        policyName: policy,
                    },
                    this._requestMetaData(),
                ),
            );
        });
        return Promise.all(promisedPolicies).then(policies => policies.map(response => response.body));
    }

    getResourcePolicies(sequenceType = 'in') {
        return this.client.then(client => {
            return client.apis['API Resource Policies'].get_apis__apiId__resource_policies({
                apiId: this.id,
                sequenceType,
            });
        });
    }

    updateResourcePolicy(resourcePolicy) {
        return this.client.then(client => {
            return client.apis['API Resource Policies'].put_apis__apiId__resource_policies__resourcePolicyId_({
                apiId: this.id,
                resourcePolicyId: resourcePolicy.id,
                body: resourcePolicy,
            });
        });
    }

    setInlineProductionEndpoint(serviceURL) {
        this.endpointConfig.production_endpoints.url = serviceURL;
        return this.endpointConfig;
    }

    getProductionEndpoint() {
        if (!this.endpointConfig) {
            return null;
        }
        if (!this.endpointConfig.production_endpoints) {
            return '';
        }
        if (Array.isArray(this.endpointConfig.production_endpoints)) {
            return this.endpointConfig.production_endpoints[0].url;
        } else {
            return this.endpointConfig.production_endpoints.url;
        }
    }

    getSandboxEndpoint() {
        if (!this.endpointConfig.sandbox_endpoints) {
            return '';
        }
        if (Array.isArray(this.endpointConfig.sandbox_endpoints)) {
            return this.endpointConfig.sandbox_endpoints[0].url;
        } else {
            return this.endpointConfig.sandbox_endpoints.url;
        }
    }

    /**
     * Tests the endpoints
     */
    testEndpoint(endpointUrl, apiId) {
        return this.client.then(client => {
            return client.apis['Validation'].validateEndpoint({ endpointUrl: endpointUrl, apiId: apiId });
        });
    }

    save(openAPIVersion = 'v3') {
        const promisedAPIResponse = this.client.then(client => {
            const properties = client.spec.definitions.API.properties;
            const data = {};
            Object.keys(this).forEach(apiAttribute => {
                if (apiAttribute in properties) {
                    data[apiAttribute] = this[apiAttribute];
                }
            });
            const payload = {
                body: data,
                'Content-Type': 'application/json',
                openAPIVersion,
            };
            return client.apis['APIs'].post_apis(payload, this._requestMetaData());
        });
        return promisedAPIResponse.then(response => {
            return new API(response.body);
        });
    }

    saveProduct() {
        const promisedAPIResponse = this.client.then(client => {
            const properties = client.spec.definitions.APIProduct.properties;
            const data = {};

            Object.keys(this).forEach(apiAttribute => {
                if (apiAttribute in properties) {
                    data[apiAttribute] = this[apiAttribute];
                }
            });
            const payload = {
                body: data,
                'Content-Type': 'application/json',
            };
            return client.apis['API Products'].post_api_products(payload, this._requestMetaData());
        });
        return promisedAPIResponse.then(response => {
            return new API(response.body);
        });
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated use static API.get() method instead
     */
    get(id, callback = null) {
        const promise_get = this.client.then(client => {
            return client.apis['APIs'].get_apis__apiId_(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated use static API.get() method instead
     */
    getProduct(id, callback = null) {
        const promise_get = this.client.then(client => {
            return client.apis['API Products'].get_api_products__apiProductId_(
                {
                    apiProductId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Validate the api parameters for existence. (api name, context)
     * @param {string} query The parameters that should be validated.
     * @return {promise}
     * */
    validateAPIParameter(query) {
        return this.client.then(client => {
            return client.apis.Validation.validateAPI({ query: query })
                .then(resp => {
                    return resp.ok;
                })
                .catch(err => {
                    console.log(err);
                    return false;
                });
        });
    }

    /**
     * Validate the given document name exists
     * @param {string} id The api id.
     * @param {string} name The document name
     * @return {promise}
     * */
    validateDocumentExists(id, name) {
        return this.client.then(client => {
            return client.apis['API Documents']
                .validateDocument({ apiId: id, name: name })
                .then(resp => {
                    return resp.ok;
                })
                .catch(err => {
                    console.log(err);
                    return false;
                });
        });
    }

    /**
     * Create a new version of a given API
     * @param version {string} new API version.
     * @param isDefaultVersion specifies whether new API version is set as default version
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createNewAPIVersion(version, isDefaultVersion, callback = null) {
        const promise_copy_api = this.client.then(client => {
            return client.apis['APIs'].post_apis_copy_api(
                {
                    apiId: this.id,
                    newVersion: version,
                    defaultVersion: isDefaultVersion,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_copy_api.then(callback);
        } else {
            return promise_copy_api;
        }
    }

    /**
     * Get the swagger of an API
     * @param id {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwagger(id = this.id, callback = null) {
        const promise_get = this.client.then(client => {
            return client.apis['APIs'].get_apis__apiId__swagger(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promise_get;
    }

    /**
     * Mock sample responses for Inline Prototyping
     * of a swagger OAS defintion
     * 
     * @param id {String} The api id.
     */
    generateMockResponses(id=this.id) {
        const promise_get = this.client.then(client => { 
            return client.apis['APIs'].generateMockResponses(
                { 
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promise_get;
    }


    /**
     * Get the graphQL schema of an API
     * @param id {String} UUID of the API in which the schema is needed
     * @param callback {function} Function which needs to be called upon success of the retrieving schema
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSchema(id, callback = null) {
        const promise_get = this.client.then(client => {
            return client.apis['GraphQL Schema (Individual)'].get_apis__apiId__graphql_schema(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get the scopes of an API
     * @param id {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getScopes(id = this.id, callback = null) {
        const promise_get = this.client.then(client => {
            return client.apis['API Scopes'].get_apis__apiId__scopes(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get settings of an API
     */
    getSettings() {
        const promisedSettings = this.client.then(client => {
            return client.apis['Settings'].get_settings();
        });
        return promisedSettings.then(response => response.body);
    }

    /**
     * Get Subscription Policies of an API
     * @param id {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSubscriptionPolicies(id, callback = null) {
        const promisePolicies = this.client.then(client => {
            return client.apis['APIs'].get_apis__apiId__subscription_policies(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisePolicies.then(response => response.body);
    }

    /**
     * Get monettization status of an API
     * @param id {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of get Monetization status
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getMonetization(id, callback = null) {
        const promiseMonetization = this.client.then(client => {
            return client.apis['API Monetization'].get_apis__apiId__monetization(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promiseMonetization.then(response => response.body);
    }

    /**
     * Get monettization Invoice
     * @param id {String} UUID of the subscription
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getMonetizationInvoice(id, callback = null) {
        const promiseInvoice = this.client.then(client => {
            return client.apis['API Monetization'].get_subscriptions__subscriptionId__usage(
                {
                    subscriptionId: id,
                },
                this._requestMetaData(),
            );
        });
        return promiseInvoice.then(response => response.body);
    }

    /**
     * configure monetization to an API
     * @param apiId APIID
     * @param body details of tiers
     */
    configureMonetizationToApi(apiId, body) {
        const promised_status = this.client.then(client => {
            return client.apis['API Monetization'].post_apis__apiId__monetize({
                apiId,
                body,
            });
        });
        return promised_status;
    }

    /**
     * Get the detail of scope of an API
     * @param {String} api_id - UUID of the API in which the scopes is needed
     * @param {String} scopeName - Name of the scope
     * @param {function} callback - Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getScopeDetail(api_id, scopeName, callback = null) {
        const promise_get_Scope_detail = this.client.then(client => {
            return client.apis['API Scopes'].get_apis__apiId__scopes__name_(
                {
                    apiId: api_id,
                    name: scopeName,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get_Scope_detail.then(callback);
        } else {
            return promise_get_Scope_detail;
        }
    }

    /**
     * Update a scope of an API
     * @param {String} api_id - UUID of the API in which the scopes is needed
     * @param {String} scopeName - Name of the scope
     * @param {Object} body - Scope details
     */
    updateScope(api_id, scopeName, body) {
        const promised_updateScope = this.client.then(client => {
            const payload = {
                apiId: api_id,
                body,
                name: scopeName,
                'Content-Type': 'application/json',
            };
            return client.apis['API Scopes'].put_apis__apiId__scopes__name_(payload, this._requestMetaData());
        });
        return promised_updateScope;
    }

    addScope(api_id, body) {
        const promised_addScope = this.client.then(client => {
            const payload = {
                apiId: api_id,
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['API Scopes'].post_apis__apiId__scopes(payload, this._requestMetaData());
        });
        return promised_addScope;
    }

    deleteScope(api_id, scope_name) {
        const promise_deleteScope = this.client.then(client => {
            return client.apis['API Scopes'].delete_apis__apiId__scopes__name_(
                {
                    apiId: api_id,
                    name: scope_name,
                },
                this._requestMetaData(),
            );
        });
        return promise_deleteScope;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     * @deprecated
     */
    updateSwagger(id, swagger) {
        const promised_update = this.client.then(client => {
            const payload = {
                apiId: id,
                endpointId: JSON.stringify(swagger),
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['APIs'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_update;
    }

    /**
     * Update API definition of a given API by URL content
     * @param apiId         API Identifier
     * @param openAPIUrl    OpenAPI definition content URL
     * @returns {boolean|*}
     */
    updateAPIDefinitionByUrl(apiId, openAPIUrl) {
        let payload, promise_updated;

        promise_updated = this.client.then(client => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                apiId: apiId,
                url: openAPIUrl,
            };

            const promisedResponse = client.apis['APIs'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
            return promisedResponse.then(response => new API(response.body));
        });
        return promise_updated;
    }

    /**
     * Update API definition of a given API by file content
     * @param apiId         API Identifier
     * @param openAPIFile   OpenAPI definition file content
     * @returns {boolean|*}
     */
    updateAPIDefinitionByFile(apiId, openAPIFile) {
        let payload, promise_updated;

        promise_updated = this.client.then(client => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                apiId: apiId,
                file: openAPIFile,
            };

            const promisedResponse = client.apis['APIs'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
            return promisedResponse.then(response => new API(response.body));
        });
        return promise_updated;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param apiId {Object} Updated graphQL schema which needs to be updated
     * @param graphQLSchema
     */
    updateGraphQLAPIDefinition(apiId, graphQLSchema) {
        const promised_updateSchema = this.client.then(client => {
            const payload = {
                apiId: apiId,
                schemaDefinition: graphQLSchema,
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['GraphQL Schema'].put_apis__apiId__graphql_schema(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_updateSchema;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     *
     */
    updateSwagger(swagger) {
        const promised_update = this.client.then(client => {
            const payload = {
                apiId: this.id,
                apiDefinition: JSON.stringify(swagger),
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['APIs'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_update;
    }

    /**
     * Delete the current api instance
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    delete() {
        return this.client.then(client => {
            return client.apis['APIs'].delete_apis__apiId_(
                {
                    apiId: this.id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Delete the current api product instance
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    deleteProduct() {
        return this.client.then(client => {
            return client.apis['API Products'].delete_api_products__apiProductId_(
                {
                    apiProductId: this.id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    getLcState(id, callback = null) {
        const promise_lc_get = this.client.then(client => {
            return client.apis['API Lifecycle'].get_apis__apiId__lifecycle_state(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_lc_get.then(callback);
        } else {
            return promise_lc_get;
        }
    }

    /**
     * Get the life cycle history data of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    getLcHistory(id, callback = null) {
        const promise_lc_history_get = this.client.then(client => {
            return client.apis['API Lifecycle'].get_apis__apiId__lifecycle_history(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_lc_history_get.then(callback);
        } else {
            return promise_lc_history_get;
        }
    }

    /**
     *
     * Shortcut method to publish `this` API instance
     *
     * @param {Object} checkedItems State change checklist items
     * @returns {Promise}
     * @memberof API
     */
    publish(checkedItems) {
        const payload = {
            action: 'Publish',
            apiId: this.id,
            lifecycleChecklist: checkedItems,
            'Content-Type': 'application/json',
        };
        return this.client.then(client => {
            return client.apis['API Lifecycle'].post_apis_change_lifecycle(payload, this._requestMetaData());
        });
    }

    /**
     * Update the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param state {string} Target state which need to be transferred
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    updateLcState(id, state, checkedItems, callback = null) {
        const payload = {
            action: state,
            apiId: id,
            lifecycleChecklist: checkedItems,
            'Content-Type': 'application/json',
        };
        const promise_lc_update = this.client.then(client => {
            return client.apis['API Lifecycle'].post_apis_change_lifecycle(payload, this._requestMetaData());
        });
        if (callback) {
            return promise_lc_update.then(callback);
        } else {
            return promise_lc_update;
        }
    }

    /**
     * Cleanup pending workflow state change task for API given its id (UUID)
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    cleanupPendingTask(id, callback = null) {
        const promise_deletePendingTask = this.client.then(client => {
            return client.apis['API Lifecycle'].delete_apis__apiId__lifecycle_state_pending_tasks(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promise_deletePendingTask;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    update(updatedProperties) {
        const updatedAPI = { ...this.toJSON(), ...updatedProperties };
        const promisedUpdate = this.client.then(client => {
            const payload = {
                apiId: updatedAPI.id,
                body: updatedAPI,
            };
            return client.apis['APIs'].put_apis__apiId_(payload);
        });
        return promisedUpdate.then(response => {
            return new API(response.body);
        });
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    updateProduct(api) {
        const promised_update = this.client.then(client => {
            const payload = {
                apiProductId: api.id,
                body: api,
            };
            return client.apis['API Products'].put_api_products__apiProductId_(payload);
        });
        return promised_update;
    }
    /**
     * Get the available subscriptions for a given API
     * @param {String} apiId API UUID
     * @returns {Promise} With given callback attached to the success chain else API invoke promise.
     */
    subscriptions(apiId, offset = null, limit = null, query = null, callback = null) {
        const promise_subscription = this.client.then(client => {
            return client.apis['Subscriptions'].get_subscriptions(
                { apiId, limit, offset, query },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_subscription.then(callback);
        } else {
            return promise_subscription;
        }
    }

    /**
     * Block subscriptions for given subscriptionId
     * @param {String} id Subscription UUID
     * @param {String} state Subscription status
     * @returns {Promise} With given callback attached to the success chain else API invoke promise.
     */
    blockSubscriptions(id, state, callback = null) {
        const promise_subscription = this.client.then(client => {
            return client.apis['Subscriptions'].post_subscriptions_block_subscription(
                {
                    subscriptionId: id,
                    blockState: state,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_subscription.then(callback);
        } else {
            return promise_subscription;
        }
    }

    /**
     * Unblock subscriptions for given subscriptionId
     * @param {String} id Subscription UUID
     * @returns {Promise} With given callback attached to the success chain else API invoke promise.
     */
    unblockSubscriptions(id, callback = null) {
        const promise_subscription = this.client.then(client => {
            return client.apis['Subscriptions'].post_subscriptions_unblock_subscription(
                {
                    subscriptionId: id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_subscription.then(callback);
        } else {
            return promise_subscription;
        }
    }

    /**
     * Discovered Service Endpoints.
     * @returns {Promise} Promised list of discovered services
     *
     * TODO: remove
     */
    discoverServices() {
        return this.client.then(client => {
            return client.apis['External Resources (Collection)'].get_external_resources_services(
                {},
                this._requestMetaData(),
            );
        });
    }

    addDocument(api_id, body) {
        const promised_addDocument = this.client.then(client => {
            const payload = {
                apiId: api_id,
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['API Documents'].post_apis__apiId__documents(payload, this._requestMetaData());
        });
        return promised_addDocument;
    }

    /*
     Add a File resource to a document
     */
    addFileToDocument(api_id, docId, fileToDocument) {
        const promised_addFileToDocument = this.client.then(client => {
            const payload = {
                apiId: api_id,
                documentId: docId,
                file: fileToDocument,
                'Content-Type': 'application/json',
            };
            return client.apis['API Documents'].post_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });

        return promised_addFileToDocument;
    }

    /*
     Add inline content to a INLINE type document
     */
    addInlineContentToDocument(apiId, documentId, sourceType, inlineContent) {
        const promised_addInlineContentToDocument = this.client.then(client => {
            const payload = {
                apiId,
                documentId,
                sourceType,
                inlineContent,
                'Content-Type': 'application/json',
            };
            return client.apis['API Documents'].post_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_addInlineContentToDocument;
    }

    getFileForDocument(api_id, docId) {
        const promised_getDocContent = this.client.then(client => {
            const payload = {
                apiId: api_id,
                documentId: docId,
                Accept: 'application/octet-stream',
            };
            return client.apis['API Documents'].get_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_getDocContent;
    }

    /*
     Get the inline content of a given document
     */
    getInlineContentOfDocument(api_id, docId) {
        const promised_getDocContent = this.client.then(client => {
            const payload = {
                apiId: api_id,
                documentId: docId,
            };
            return client.apis['API Documents'].get_apis__apiId__documents__documentId__content(payload);
        });
        return promised_getDocContent;
    }

    getDocuments(api_id, callback) {
        const promise_get_all = this.client.then(client => {
            return client.apis['API Documents'].get_apis__apiId__documents(
                {
                    apiId: api_id,
                },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promise_get_all.then(callback);
        } else {
            return promise_get_all;
        }
    }

    updateDocument(api_id, docId, body) {
        const promised_updateDocument = this.client.then(client => {
            const payload = {
                apiId: api_id,
                body,
                documentId: docId,
                'Content-Type': 'application/json',
            };
            return client.apis['API Documents'].put_apis__apiId__documents__documentId_(
                payload,
                this._requestMetaData(),
            );
        });
        return promised_updateDocument;
    }

    getDocument(api_id, docId, callback) {
        const promise_get = this.client.then(client => {
            return client.apis['API Documents'].get_apis__apiId__documents__documentId_(
                {
                    apiId: api_id,
                    documentId: docId,
                },
                this._requestMetaData(),
            );
        });
        return promise_get;
    }

    deleteDocument(api_id, document_id) {
        const promise_deleteDocument = this.client.then(client => {
            return client.apis['API Documents'].delete_apis__apiId__documents__documentId_(
                {
                    apiId: api_id,
                    documentId: document_id,
                },
                this._requestMetaData(),
            );
        });
        return promise_deleteDocument;
    }

    /**
     * Get the available labels.
     * @returns {Promise.<TResult>}
     * TODO: remove
     */
    labels() {
        const promise_labels = this.client.then(client => {
            return client.apis['Label (Collection)'].get_labels({}, this._requestMetaData());
        });
        return promise_labels;
    }

    /**
     * Create an API from GraphQL with the given parameters and call the callback method given optional.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     * @param {function} callback - An optional callback method
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
    importGraphQL(api_data, callback = null) {
        let payload;
        let promise_create;
        payload = {
            type: 'GraphQL',
            additionalProperties: api_data.additionalProperties,
            file: api_data.file,
            'Content-Type': 'multipart/form-data',
        };

        promise_create = this.client.then(client => {
            return client.apis['APIs'].post_apis_import_graphql_schema(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    static validateGraphQLFile(file) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promised_validationResponse = apiClient.then(client => {
            return client.apis['Validation'].post_apis_validate_graphql_schema(
                {
                    type: 'GraphQL',
                    file,
                    'Content-Type': 'multipart/form-data',
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_validationResponse;
    }

    /**
     * Downloads the WSDL of an API
     * 
     * @param {string} apiId Id (UUID) of the API
     */
    getWSDL(apiId) {
        const promised_wsdlResponse = this.client.then(client => {
            return client.apis['APIs'].getWSDLOfAPI(
                {
                    apiId,
                },
                this._requestMetaData(),
            );
        });
        return promised_wsdlResponse;
    }

    /**
     * Get WSDL meta info of an API - indicates whether the WSDL is a ZIP or a single file.
     * 
     * @param {string} apiId Id (UUID) of the API
     */
    getWSDLInfo(apiId) {
        const promised_wsdlResponse = this.client.then(client => {
            return client.apis['APIs'].getWSDLInfoOfAPI(
                {
                    apiId,
                },
                this._requestMetaData(),
            );
        });
        return promised_wsdlResponse;
    }

    /**
     * Updates the API's WSDL with the WSDL of the provided URL
     * 
     * @param {string} apiId Id (UUID) of the API 
     * @param {string} url WSDL url
     */
    updateWSDLByUrl(apiId, url) {
        const promised_wsdlResponse = this.client.then(client => {
            return client.apis['APIs'].updateWSDLOfAPI(
                {
                    apiId,
                    url,
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_wsdlResponse;
    }

    /**
     * Updates the API's WSDL with the WSDL of the provided file (zip or .wsdl)
     * 
     * @param {string} apiId Id (UUID) of the API 
     * @param {*} file WSDL file (zip or .wsdl)
     */
    updateWSDLByFileOrArchive(apiId, file) {
        const promised_wsdlResponse = this.client.then(client => {
            return client.apis['APIs'].updateWSDLOfAPI(
                {
                    apiId,
                    file,
                },
                this._requestMetaData(),
            );
        });
        return promised_wsdlResponse;
    }

    /**
     * Get all threat protection policies
     * TODO: remove
     */
    getThreatProtectionPolicies() {
        const promisedPolicies = this.client.then(client => {
            return client.apis['Threat Protection Policies'].get_threat_protection_policies();
        });
        return promisedPolicies;
    }

    /**
     * Retrieve a single threat protection policy
     * @param id Threat protection policy id
     * TODO: remove
     */
    getThreatProtectionPolicy(id) {
        const promisedPolicies = this.client.then(client => {
            return client.apis['Threat Protection Policy'].get_threat_protection_policies__policyId_({
                policyId: id,
            });
        });
        return promisedPolicies;
    }

    /**
     * Add threat protection policy to an API
     * @param apiId APIID
     * @param policyId Threat protection policy id
     * TODO: remove
     */
    addThreatProtectionPolicyToApi(apiId, policyId) {
        const promisedPolicies = this.client.then(client => {
            return client.apis['API (Individual)'].post_apis__apiId__threat_protection_policies({
                apiId,
                policyId,
            });
        });
        return promisedPolicies;
    }

    /**
     * Delete threat protection policy from an API
     * @param apiId APIID
     * @param policyId Threat protection policy id
     * TODO: remove
     */
    deleteThreatProtectionPolicyFromApi(apiId, policyId) {
        console.log(apiId);
        const promisedDelete = this.client.then(client => {
            console.log(client.apis);
            return client.apis['API (Individual)'].delete_apis__apiId__threat_protection_policies({
                apiId,
                policyId,
            });
        });
        return promisedDelete;
    }

    /**
     * Update HasOwnGateway property of an API
     * @param apiId APIId
     * @param body  body which contains update details
     * TODO: remove
     */
    updateHasOwnGateway(api_id, body) {
        const promised_updateDedicatedGateway = this.client.then(client => {
            const payload = {
                apiId: api_id,
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['DedicatedGateway (Individual)'].put_apis__apiId__dedicated_gateway(
                payload,
                this._requestMetaData(),
            );
        });
        return promised_updateDedicatedGateway;
    }

    /**
     * Get the HasOwnGateway property of an API
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     * TODO: remove
     */
    getHasOwnGateway(id) {
        const promised_getDedicatedGateway = this.client.then(client => {
            return client.apis['DedicatedGateway (Individual)'].get_apis__apiId__dedicated_gateway(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promised_getDedicatedGateway;
    }

    /**
     * Get the thumnail of an API
     *
     * @param id {string} UUID of the api
     */
    getAPIThumbnail(id) {
        const promised_getAPIThumbnail = this.client.then(client => {
            return client.apis['APIs'].get_apis__apiId__thumbnail(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });

        return promised_getAPIThumbnail;
    }

    validateSystemRole(role) {
        const promise = this.client.then(client => {
            return client.apis.Roles.validateSystemRole({ roleId: role });
        });
        return promise;
    }

    validateUSerRole(role) {
        const promise = this.client.then(client => {
            return client.apis.Roles.validateUserRole({ roleId: role });
        });
        return promise;
    }

    validateScopeName(name) {
        const promise = this.client.then(client => {
            return client.apis.scope.validateScope({ name: name });
        });
        return promise;
    }

    /**
     * Add new thumbnail image to an API
     *
     * @param {String} api_id id of the API
     * @param {File} imageFile thumbnail image to be uploaded
     */
    addAPIThumbnail(api_id, imageFile) {
        const promised_addAPIThumbnail = this.client.then(client => {
            const payload = {
                apiId: api_id,
                file: imageFile,
                'Content-Type': imageFile.type,
            };
            return client.apis['APIs'].updateAPIThumbnail(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });

        return promised_addAPIThumbnail;
    }

    /**
     * Add new comment to an existing API
     * @param apiId apiId of the api to which the comment is added
     * @param commentInfo comment text
     * * TODO: remove
     */
    addComment(apiId, commentInfo, callback = null) {
        let promise = this.client
            .then(client => {
                return client.apis['Comment (Individual)'].post_apis__apiId__comments(
                    { apiId: apiId, body: commentInfo },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

    /**
     * Get all comments for a particular API
     * @param apiId api id of the api to which the comment is added
     * * TODO: remove
     */
    getAllComments(apiId, callback = null) {
        let promise_get = this.client
            .then(client => {
                return client.apis['Comment (Collection)'].get_apis__apiId__comments(
                    { apiId: apiId },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Delete a comment belongs to a particular API
     * @param apiId api id of the api to which the comment belongs to
     * @param commentId comment id of the comment which has to be deleted
     * * TODO: remove
     */
    deleteComment(apiId, commentId, callback = null) {
        let promise = this.client
            .then(client => {
                return client.apis['Comment (Individual)'].delete_apis__apiId__comments__commentId_(
                    { apiId: apiId, commentId: commentId },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

    /**
     * Update a comment belongs to a particular API
     * @param apiId apiId of the api to which the comment is added
     * @param commentId comment id of the comment which has to be updated
     * @param commentInfo comment text
     * TODO: remove
     */
    updateComment(apiId, commentId, commentInfo, callback = null) {
        let promise = this.client
            .then(client => {
                return client.apis['Comment (Individual)'].put_apis__apiId__comments__commentId_(
                    { apiId: apiId, commentId: commentId, body: commentInfo },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

    /**
     *
     * To get API object with the fields filled as per the definition
     * @param {Object} client Client object after resolving this.client.then()
     * @returns API Object corresponding to spec fields
     * @memberof API
     */
    getDataFromSpecFields(client) {
        const properties = client.spec.definitions.API.properties;
        const data = {};
        Object.keys(this).forEach(apiAttribute => {
            if (apiAttribute in properties) {
                data[apiAttribute] = this[apiAttribute];
            }
        });
        return data;
    }

    /**
     * Get all active Tenants
     * @param state state of the tenant
     */
    getTenantsByState(state) {
        return this.client.then(client => {
            return client.apis['Tenants'].getTenantsByState({ state });
        });
    }

    /**
     * Get list of microgateway labels
     */
    microgatewayLabelsGet() {
        return this.client.then(client => {
            return client.apis['Label Collection'].get_labels();
        });
    }

    /**
     *
     * Static method for get all APIs for current environment user.
     * @static
     * @param {Object} params APIs filtering parameters i:e { "name": "MyBank API"}
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    static all(params) {
        let query = '';
        if (params && 'query' in params) {
            Object.entries(params.query).forEach(([key, value], index) => {
                query = `${key}:${value}`;
                if (Object.entries(params.query).length !== index + 1) {
                    query += ',';
                }
            });
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedAPIs = apiClient.then(client => {
            return client.apis['APIs'].get_apis(params, Resource._requestMetaData());
        });

        return promisedAPIs.then(response => {
            response.obj.apiType = API.CONSTS.API;
            return response;
        });
    }

    /**
     *
     * Static method for get all API products for current environment user.
     * @static
     * @param {Object} params APIs filtering parameters i:e { "name": "MyBank API"}
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    static allProducts(params) {
        let query = '';
        if (params && 'query' in params) {
            for (const [key, value] of Object.entries(params.query)) {
                query += `${key}:${value},`;
            }
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['API Products'].get_api_products(params, Resource._requestMetaData());
        });
    }

    /**
     *
     * Static method to search apis and documents based on content
     * @static
     * @param {Object} params APIs, Documents filtering parameters i:e { "name": "MyBank API"}
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    static search(params) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Unified Search'].get_search(params, Resource._requestMetaData());
        });
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedAPI = apiClient.then(client => {
            return client.apis['APIs'].get_apis__apiId_(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisedAPI.then(response => {
            return new API(response.body);
        });
    }

    /**
     * Get details of a given API Product
     * @param id {string} UUID of the api product.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    static getProduct(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedAPI = apiClient.then(client => {
            return client.apis['API Products'].get_api_products__apiProductId_(
                {
                    apiProductId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisedAPI.then(response => {
            return new API(response.body);
        });
    }

    /**
     *
     * Delete an API given its UUID
     * @static
     * @param {String} id API UUID
     * @returns {Promise} Swagger-Js promise object resolve to NT response object
     * @memberof API
     */
    static delete(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['APIs'].delete_apis__apiId_(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     *
     * Delete an API Product given its UUID
     * @static
     * @param {String} id API Product UUID
     * @returns {Promise} Swagger-Js promise object resolve to NT response object
     * @memberof API
     */
    static deleteProduct(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['API Products'].delete_api_products__apiProductId_(
                {
                    apiProductId: id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get the available policies information by tier level.
     * @param {String} policyLevel List API or Application or Resource type policies.parameter should be one
     * of api, application, subscription and resource
     * @returns {Promise}
     *
     */
    static policies(policyLevel) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Throttling Policies'].getAllThrottlingPolicies(
                {
                    policyLevel: policyLevel,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get all the endpoint certificates.
     * */
    static getEndpointCertificates() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Endpoint Certificates'].get_endpoint_certificates();
        });
    }

    /**
     * Upload endpoint certificate.
     *
     * @param {any} certificateFile The certificate file to be uploaded.
     * @param {string} endpoint The certificate endpoint.
     * @param {string} alias The certificate alias.
     * */
    static addCertificate(certificateFile, endpoint, alias) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(
            client => {
                return client.apis['Endpoint Certificates'].post_endpoint_certificates({
                    certificate: certificateFile,
                    endpoint,
                    alias,
                });
            },
            this._requestMetaData({
                'Content-Type': 'multipart/form-data',
            }),
        );
    }

    /**
     * Upload endpoint certificate.
     *
     * @param {string} apiId API UUID
     * @param {any} certificateFile The certificate file to be uploaded.
     * @param {string} tier The tier the certificate needs to be associated.
     * @param {string} alias The certificate alias.
     * */
    static addClientCertificate(apiId, certificateFile, tier, alias) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(
            client => {
                return client.apis['Client Certificates'].post_apis__apiId__client_certificates({
                    certificate: certificateFile,
                    alias,
                    apiId,
                    tier,
                });
            },
            this._requestMetaData({
                'Content-Type': 'multipart/form-data',
            }),
        );
    }

    /**
     * Get all certificates for a particular API.
     *
     * @param apiId api id of the api to which the certificate is added
     */
    static getAllClientCertificates(apiId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(
            client => {
                return client.apis['Client Certificates'].get_apis__apiId__client_certificates(
                    { apiId: apiId },
                    this._requestMetaData(),
                );
            },
            this._requestMetaData({
                'Content-Type': 'multipart/form-data',
            }),
        );
    }

    /**
     * Get the status of the client certificate which matches the given alias.
     *
     * @param {string} alias The alias of the certificate which the information required.
     * @param apiId api id of the api of which the certificate is retrieved.
     * */
    static getClientCertificateStatus(alias, apiId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Client Certificates'].get_apis__apiId__client_certificates__alias_({
                alias,
                apiId,
            });
        }, this._requestMetaData());
    }

    /**
     * Delete the endpoint certificate which represented by the given alias.
     *
     * @param {string} alias The alias of the certificate.
     * @param apiId api id of the api of which the certificate is deleted.
     * */
    static deleteClientCertificate(alias, apiId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Client Certificates'].delete_apis__apiId__client_certificates__alias_({
                alias,
                apiId,
            });
        }, this._requestMetaData());
    }

    /**
     * Get the status of the endpoint certificate which matches the given alias.
     *
     * @param {string} alias The alias of the certificate which the information required.
     * */
    static getCertificateStatus(alias) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Endpoint Certificates'].get_endpoint_certificates__alias_({
                alias: alias,
            });
        }, this._requestMetaData());
    }

    /**
     * Delete the endpoint certificate which represented by the given alias.
     *
     * @param {string} alias The alias of the certificate
     * */
    static deleteEndpointCertificate(alias) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Endpoint Certificates'].delete_endpoint_certificates__alias_({
                alias,
            });
        }, this._requestMetaData());
    }

    /**
     * Get the available mediation policies by the api uuid.
     * @param {String} apiId uuid
     * @returns {Promise}
     *
     */
    static getMediationPolicies(apiId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policies'].apisApiIdMediationPoliciesGet(
                {
                    apiId: apiId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Add a mediation policy to a given api.
     * @param {String} apiId uuid
     * @returns {Promise}
     *
     */
    static addMediationPolicy(policyFile, apiId, type) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policies'].apisApiIdMediationPoliciesPost(
                {
                    apiId: apiId,
                    type: type.toLowerCase(),
                    mediationPolicyFile: policyFile,
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
    }

    /**
     * Get the available mediation policies by the mediation policy uuid and api.
     * @param {String} seqId mediation policy uuid
     * @param {String} apiId uuid
     * @returns {Promise}
     *
     */
    static getMediationPolicy(seqId, apiId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policy'].apisApiIdMediationPoliciesMediationPolicyIdGet(
                {
                    mediationPolicyId: seqId,
                    apiId: apiId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Delete the available mediation policies by the mediation policy uuid and api.
     * @param {String} seqId mediation policy uuid
     * @param {String} apiId uuid
     * @returns {Promise}
     *
     */
    static deleteMediationPolicy(seqId, apiId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policy'].apisApiIdMediationPoliciesMediationPolicyIdDelete(
                {
                    mediationPolicyId: seqId,
                    apiId: apiId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Update the available mediation policies by the mediation policy uuid and api.
     * @param {String} seqId mediation policy uuid
     * @param {String} apiId uuid
     * @returns {Promise}
     *
     */
    static updateMediationPolicyContent(seqId, apiId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policy'].apisApiIdMediationPoliciesMediationPolicyIdContentPut(
                {
                    mediationPolicyId: seqId,
                    apiId: apiId,
                    type: 'in',
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
    }

    /**
     * Get the content of a mediation policy.
     * @param {String} mediationPolicyId mediation policy uuid
     * @param {String} apiId uuid of the api
     * @returns {Promise}
     *
     */
    static getMediationPolicyContent(mediationPolicyId, apiId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['API Mediation Policy'].apisApiIdMediationPoliciesMediationPolicyIdContentGet(
                {
                    mediationPolicyId: mediationPolicyId,
                    apiId: apiId,
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
    }

    /**
     * Get global mediation policies.
     * @returns {Promise}
     *
     */
    static getGlobalMediationPolicies() {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['Global Mediation Policies'].getAllGlobalMediationPolicies({}, this._requestMetaData());
        });
    }

    /**
     * Get the content of a mediation policy.
     * @param {String} mediationPolicyId mediation policy uuid
     * @param {String} apiId uuid of the api
     * @returns {Promise}
     *
     */
    static getGlobalMediationPolicyContent(mediationPolicyId) {
        const restApiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return restApiClient.then(client => {
            return client.apis['Global Mediation Policy'].getGlobalMediationPolicyContent(
                {
                    mediationPolicyId: mediationPolicyId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * @static
     * Get all the external stores configured for the current environment
     * @returns {Promise}
     */
    static getAllExternalStores() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['External Stores'].getAllExternalStores(this._requestMetaData());
        });
    }

    /**
     * @static
     * Get published external stores for the given API
     * @param {String} apiId uuid
     * @returns {Promise}
     */
    static getPublishedExternalStores(apiId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['External Stores'].getAllPublishedExternalStoresByAPI(
                { apiId: apiId },
                this._requestMetaData,
            );
        });
    }

    /**
     * @static
     * Publish the given API to given set of external stores and remove from others which are not specified
     * @param {String} apiId uuid
     * @param {Array} externalStoreIds
     */
    static publishAPIToExternalStores(apiId, externalStoreIds) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['External Stores'].publishAPIToExternalStores(
                {
                    apiId: apiId,
                    externalStoreIds: externalStoreIds,
                },
                this._requestMetaData,
            );
        });
    }

    /**
     * @static
     * Get the supported alert types by the publisher.
     * @return {Promise}
     * */
    static getSupportedAlertTypes() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alerts'].getPublisherAlertTypes(this._requestMetaData());
        });
    }

    /**
     * @static
     * Get the subscribed alert types by the current user.
     * @returns {Promise}
     * */
    static getSubscribedAlertTypesByUser() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Subscriptions'].getSubscribedAlertTypes(this._requestMetaData());
        });
    }

    /**
     * @static
     * Subscribe to the provided set of alerts.
     * @return {Promise}
     * */
    static subscribeAlerts(alerts) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Subscriptions'].subscribeToAlerts({ body: alerts }, this._requestMetaData());
        });
    }

    /**
     * @static
     * Unsubscribe from all the alerts.
     * @return {Promise}
     * */
    static unsubscribeAlerts() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Subscriptions'].unsubscribeAllAlerts(this._requestMetaData());
        });
    }

    /**
     * @static
     * Get the configuration for the given alert type.
     * @param {string} alertType The alert type name.
     * @return {Promise}
     * */
    static getAlertConfigurations(alertType) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Configuration'].getAllAlertConfigs(
                {
                    alertType: alertType,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * @static
     * Add configuration for the given alert type.
     * @param {string} alertType The alert type name.
     * @param {object} alertConfig Alert configurations.
     * @param {string} configId The alert configuration id.
     * @return {Promise}
     * */
    static putAlertConfiguration(alertType, alertConfig, configId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Configuration'].addAlertConfig(
                {
                    alertType: alertType,
                    body: alertConfig,
                    configurationId: configId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * @static
     * Delete configuration.
     * @param {string} alertType The alert type name.
     * @param {string} configId The alert configuration id.
     * @return {Promise}
     * */
    static deleteAlertConfiguration(alertType, configId) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['Alert Configuration'].deleteAlertConfig(
                {
                    alertType: alertType,
                    configurationId: configId,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get ARNs of an user role
     * @param id {string} UUID of the api product.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    static getAmznResourceNames(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis['AWS Lambda (Individual)'].get_apis__apiId__amznResourceNames(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * @static
     * Get all API Categories of the given tenant
     * @return {Promise}
     * */
    static apiCategories() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then(client => {
            return client.apis["API Category (Collection)"].get_api_categories(
                this._requestMetaData(),
            );
        });
    }
}


API.CONSTS = {
    API: 'API',
    APIProduct: 'APIProduct',
};

Object.freeze(API.CONSTS);

export default API;
