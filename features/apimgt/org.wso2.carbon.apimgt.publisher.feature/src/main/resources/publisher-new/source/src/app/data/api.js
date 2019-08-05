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

/**
 * An abstract representation of an API
 */
class API extends Resource {

    constructor(name, version, context, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
        } else {
            this.name = name;
            this.version = version;
            this.context = context;
            this.isDefaultVersion = false;
            this.gatewayEnvironments = ["Production and Sandbox"]; //todo: load the environments from settings API
            this.transport = [
                "http",
                "https"
            ];
            this.visibility = "PUBLIC";
            this.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: '',
                },
                production_endpoints : {
                    url: ''
                }
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
     * Create an API with the given parameters in template and call the callback method given optional.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     * @param {function} callback - An optional callback method
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
    create(api_data, callback = null) {
        let payload;
        let promise_create;
        if (api_data.constructor.name === 'Blob' || api_data.constructor.name === 'File') {
            payload = {
                file: api_data,
                'Content-Type': 'multipart/form-data'
            };
            promise_create = this.client.then((client) => {
                return client.apis['API (Individual)'].post_apis_import_definition(
                    payload,
                    this._requestMetaData({
                        'Content-Type': 'multipart/form-data'
                    }),
                );
            });
        } else if (api_data.type === 'swagger-url') {
            payload = {
                url: api_data.url,
                'Content-Type': 'multipart/form-data'
            };
            promise_create = this.client.then((client) => {
                return client.apis['API (Individual)'].post_apis_import_definition(
                    payload,
                    this._requestMetaData({
                        'Content-Type': 'multipart/form-data'
                    }),
                );
            });
        } else {
            payload = {
                body: api_data,
                'Content-Type': 'application/json'
            };
            promise_create = this.client.then((client) => {
                return client.apis['API (Individual)'].post_apis(payload, this._requestMetaData());
            });
        }
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    importOpenAPIByFile(openAPIData, callback = null) {
        let payload, promise_create;

        promise_create = this.client.then((client) => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                file: openAPIData,
                additionalProperties: JSON.stringify(apiData),
            };

            return client.apis['APIs'].importOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    importOpenAPIByUrl(openAPIUrl, callback = null) {
        let payload, promise_create;

        promise_create = this.client.then((client) => {
            const apiData = this.getDataFromSpecFields(client);

            payload = {
                url: openAPIUrl,
                additionalProperties: JSON.stringify(apiData),
            };

            return client.apis['APIs'].importOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    validateOpenAPIByFile(openAPIData, callback = null) {
        let payload, promise_validate;
        payload = {
            file: openAPIData,
            'Content-Type': 'multipart/form-data'
        };
        promise_validate = this.client.then((client) => {
            return client.apis['Validation'].validateOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        if (callback) {
            return promise_validate.then(callback);
        } else {
            return promise_validate;
        }
    }

    validateOpenAPIByUrl(url, callback = null) {
        let payload, promise_validate;
        payload = {
            url: url,
            'Content-Type': 'multipart/form-data'
        };
        promise_validate = this.client.then((client) => {
            return client.apis['Validation'].validateOpenAPIDefinition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        if (callback) {
            return promise_validate.then(callback);
        } else {
            return promise_validate;
        }
    }

    /**
     * Get detailed policy information of the API
     * @returns {Promise} Promise containing policy detail request calls for all the available policies
     * @memberof API
     */
    getPolicies() {
        const promisedPolicies = this.policies.map(policy => {
            return this.client.then(
                client => client.apis["Throttling Policies"].getThrottlingPolicyByName({
                        policyLevel: 'subscription',
                        policyName: policy
                    },
                    this._requestMetaData(),
                )
            )
        })
        return Promise.all(promisedPolicies).then(policies => policies.map(response => response.body));
    }

    setInlineProductionEndpoint(serviceURL) {
        this.endpointConfig.production_endpoints.url = serviceURL;
        return this.endpointConfig;
    }

    getProductionEndpoint() {
        if (!this.endpointConfig.production_endpoints) {
            return "";
        }
        if (Array.isArray(this.endpointConfig.production_endpoints)) {
            return this.endpointConfig.production_endpoints[0].url;
        } else {
            return this.endpointConfig.production_endpoints.url;
        }
    }

    getSandboxEndpoint() {
        if (!this.endpointConfig.sandbox_endpoints) {
            return "";
        }
        if (Array.isArray(this.endpointConfig.sandbox_endpoints)) {
            return this.endpointConfig.sandbox_endpoints[0].url;
        } else {
            return this.endpointConfig.sandbox_endpoints.url;
        }
    }

    save() {
        const promisedAPIResponse = this.client.then((client) => {
            const properties = client.spec.definitions.API.properties;
            const data = {};
            Object.keys(this).forEach(apiAttribute => {
                if (apiAttribute in properties) {
                    data[apiAttribute] = this[apiAttribute];
                }
            });
            const payload = {
                body: data,
                'Content-Type': 'application/json'
            };
            return client.apis['API (Individual)'].post_apis(payload, this._requestMetaData());
        });
        return promisedAPIResponse.then(response => {
            return new API(response.body);
        });

    }

    saveProduct() {
        const promisedAPIResponse = this.client.then((client) => {
            const properties = client.spec.definitions.APIProduct.properties;
            const data = {};

            Object.keys(this).forEach(apiAttribute => {
                if (apiAttribute in properties) {
                    data[apiAttribute] = this[apiAttribute];
                }
            });
            const payload = {
                body: data,
                'Content-Type': 'application/json'
            };
            return client.apis['API Product (Individual)'].post_api_products(payload, this._requestMetaData());
        });
        return promisedAPIResponse.then(response => {
            return new API(response.body);
        });

    }
    /**
     * Create an API from WSDL with the given parameters and call the callback method given optional.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     * @param {function} callback - An optional callback method
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
    importWSDL(api_data, callback = null) {
        let payload;
        let promise_create;
        payload = {
            type: 'WSDL',
            additionalProperties: api_data.additionalProperties,
            implementationType: api_data.implementationType,
            'Content-Type': 'multipart/form-data',
        };
        if (api_data.url) {
            payload.url = api_data.url;
        } else {
            payload.file = api_data.file;
        }
        promise_create = this.client.then((client) => {
            return client.apis['API (Collection)'].post_apis_import_definition(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated use static API.get() method instead
     */
    get(id, callback = null) {
        const promise_get = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId_({
                apiId: id
            }, this._requestMetaData());
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
        const promise_get = this.client.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId_({
                apiProductId: id
            }, this._requestMetaData());
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Create a new version of a given API
     * @param version {string} new API version.
     * @param isDefaultVersion specifies whether new API version is set as default version
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createNewAPIVersion(version, isDefaultVersion, callback = null) {
        const promise_copy_api = this.client.then((client) => {
            return client.apis['API (Individual)'].post_apis_copy_api({
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
    getSwagger(id, callback = null) {
        const promise_get = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId__swagger({
                apiId: id
            }, this._requestMetaData());
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
    getScopes(id, callback = null) {
        const promise_get = this.client.then((client) => {
            return client.apis['Scope (Collection)'].get_apis__apiId__scopes({
                apiId: id
            }, this._requestMetaData());
        });
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get the detail of scope of an API
     * @param {String} api_id - UUID of the API in which the scopes is needed
     * @param {String} scopeName - Name of the scope
     * @param {function} callback - Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getScopeDetail(api_id, scopeName, callback = null) {
        const promise_get_Scope_detail = this.client.then((client) => {
            return client.apis['Scope (Individual)'].get_apis__apiId__scopes__name_({
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
        const promised_updateScope = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                body,
                name: scopeName,
                'Content-Type': 'application/json',
            };
            return client.apis['Scope (Individual)'].put_apis__apiId__scopes__name_(payload, this._requestMetaData());
        });
        return promised_updateScope;
    }

    addScope(api_id, body) {
        const promised_addScope = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['Scope (Collection)'].post_apis__apiId__scopes(payload, this._requestMetaData());
        });
        return promised_addScope;
    }

    deleteScope(api_id, scope_name) {
        const promise_deleteScope = this.client.then((client) => {
            return client.apis['Scope (Individual)'].delete_apis__apiId__scopes__name_({
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
        const promised_update = this.client.then((client) => {
            const payload = {
                apiId: id,
                endpointId: JSON.stringify(swagger),
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['API (Individual)'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_update;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     *
     */
    updateSwagger(swagger) {
        const promised_update = this.client.then((client) => {
            const payload = {
                apiId: this.id,
                apiDefinition: JSON.stringify(swagger),
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['API (Individual)'].put_apis__apiId__swagger(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_update.then(response => {
            return this;
        });
    }

    /**
     * Delete the current api instance
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    delete() {
        return this.client.then((client) => {
            return client.apis['API (Individual)'].delete_apis__apiId_({
                apiId: this.id
            }, this._requestMetaData());
        });
    }

    /**
     * Delete the current api product instance
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    deleteProduct() {
        return this.client.then((client) => {
            return client.apis['API Product (Individual)'].delete_api_products__apiProductId_({
                apiProductId: this.id
            }, this._requestMetaData());
        });
    }

    /**
     * Get the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    getLcState(id, callback = null) {
        const promise_lc_get = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId__lifecycle_state({
                apiId: id
            }, this._requestMetaData());
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
        const promise_lc_history_get = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId__lifecycle_history({
                    apiId: id
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
        return this.client.then((client) => {
            return client.apis['API (Individual)'].post_apis_change_lifecycle(payload, this._requestMetaData());
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
        const promise_lc_update = this.client.then((client) => {
            return client.apis['API (Individual)'].post_apis_change_lifecycle(payload, this._requestMetaData());
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
        const promise_deletePendingTask = this.client.then((client) => {
            return client.apis['API (Individual)'].delete_apis_apiId_lifecycle_lifecycle_pending_task({
                    apiId: id
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
    update(api) {
        const promised_update = this.client.then((client) => {
            const payload = {
                apiId: api.id,
                body: api
            };
            return client.apis['API (Individual)'].put_apis__apiId_(payload);
        });
        return promised_update;
    }

        /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    updateProduct(api) {
        const promised_update = this.client.then((client) => {
            const payload = {
                apiProductId: api.id,
                body: api
            };
            return client.apis['API Product (Individual)'].put_api_products__apiProductId_(payload);
        });
        return promised_update;
    }
    /**
     * Get the available subscriptions for a given API
     * @param {String} apiId API UUID
     * @returns {Promise} With given callback attached to the success chain else API invoke promise.
     */
    subscriptions(id, callback = null) {
        const promise_subscription = this.client.then((client) => {
            return client.apis['Subscription (Collection)'].get_subscriptions({
                apiId: id
            }, this._requestMetaData());
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
        const promise_subscription = this.client.then((client) => {
            return client.apis['Subscription (Individual)'].post_subscriptions_block_subscription({
                    subscriptionId: id,
                    blockState: state
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
        const promise_subscription = this.client.then((client) => {
            return client.apis['Subscription (Individual)'].post_subscriptions_unblock_subscription({
                    subscriptionId: id
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
     */
    discoverServices() {
        return this.client.then((client) => {
            return client.apis['External Resources (Collection)'].get_external_resources_services({},
                this._requestMetaData(),
            );
        });
    }

    addDocument(api_id, body) {
        const promised_addDocument = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                body,
                'Content-Type': 'application/json'
            };
            return client.apis['Document (Collection)'].post_apis__apiId__documents(payload, this._requestMetaData());
        });
        return promised_addDocument;
    }

    /*
     Add a File resource to a document
     */
    addFileToDocument(api_id, docId, fileToDocument) {
        const promised_addFileToDocument = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                documentId: docId,
                file: fileToDocument,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].post_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });

        return promised_addFileToDocument;
    }

    /*
     Add inline content to a INLINE type document
     */
    addInlineContentToDocument(apiId, documentId, sourceType, inlineContent) {
        const promised_addInlineContentToDocument = this.client.then((client) => {
            const payload = {
                apiId,
                documentId,
                sourceType,
                inlineContent,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].post_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_addInlineContentToDocument;
    }

    getFileForDocument(api_id, docId) {
        const promised_getDocContent = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                documentId: docId,
                Accept: 'application/octet-stream'
            };
            return client.apis['Document (Individual)'].get_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_getDocContent;
    }

    /*
     Get the inline content of a given document
     */
    getInlineContentOfDocument(api_id, docId) {
        const promised_getDocContent = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                documentId: docId
            };
            return client.apis['Document (Individual)'].get_apis__apiId__documents__documentId__content(payload);
        });
        return promised_getDocContent;
    }

    getDocuments(api_id, callback) {
        const promise_get_all = this.client.then((client) => {
            return client.apis['Document (Collection)'].get_apis__apiId__documents({
                    apiId: api_id
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
        const promised_updateDocument = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                body,
                documentId: docId,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].put_apis__apiId__documents__documentId_(
                payload,
                this._requestMetaData(),
            );
        });
        return promised_updateDocument;
    }

    getDocument(api_id, docId, callback) {
        const promise_get = this.client.then((client) => {
            return client.apis['Document (Individual)'].get_apis__apiId__documents__documentId_({
                    apiId: api_id,
                    documentId: docId,
                },
                this._requestMetaData(),
            );
        });
        return promise_get;
    }

    deleteDocument(api_id, document_id) {
        const promise_deleteDocument = this.client.then((client) => {
            return client.apis['Document (Individual)'].delete_apis__apiId__documents__documentId_({
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
     */
    labels() {
        const promise_labels = this.client.then((client) => {
            return client.apis['Label (Collection)'].get_labels({}, this._requestMetaData());
        });
        return promise_labels;
    }

    validateWSDLUrl(wsdlUrl) {
        const promised_validationResponse = this.client.then((client) => {
            return client.apis['API (Collection)'].post_apis_validate_definition({
                    type: 'WSDL',
                    url: wsdlUrl,
                    'Content-Type': 'multipart/form-data',
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_validationResponse;
    }

    validateWSDLFile(file) {
        const promised_validationResponse = this.client.then((client) => {
            return client.apis['API (Collection)'].post_apis_validate_definition({
                    type: 'WSDL',
                    file,
                    'Content-Type': 'multipart/form-data',
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });
        return promised_validationResponse;
    }

    getWSDL(apiId) {
        const promised_wsdlResponse = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId__wsdl({
                    apiId,
                },
                this._requestMetaData({
                    Accept: 'application/octet-stream'
                }),
            );
        });
        return promised_wsdlResponse;
    }

    /**
     * Get all threat protection policies
     */
    getThreatProtectionPolicies() {
        const promisedPolicies = this.client.then((client) => {
            return client.apis['Threat Protection Policies'].get_threat_protection_policies();
        });
        return promisedPolicies;
    }

    /**
     * Retrieve a single threat protection policy
     * @param id Threat protection policy id
     */
    getThreatProtectionPolicy(id) {
        const promisedPolicies = this.client.then((client) => {
            return client.apis['Threat Protection Policy'].get_threat_protection_policies__policyId_({
                policyId: id
            });
        });
        return promisedPolicies;
    }

    /**
     * Add threat protection policy to an API
     * @param apiId APIID
     * @param policyId Threat protection policy id
     */
    addThreatProtectionPolicyToApi(apiId, policyId) {
        const promisedPolicies = this.client.then((client) => {
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
     */
    deleteThreatProtectionPolicyFromApi(apiId, policyId) {
        console.log(apiId);
        const promisedDelete = this.client.then((client) => {
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
     */
    updateHasOwnGateway(api_id, body) {
        const promised_updateDedicatedGateway = this.client.then((client) => {
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
     */
    getHasOwnGateway(id) {
        const promised_getDedicatedGateway = this.client.then((client) => {
            return client.apis['DedicatedGateway (Individual)'].get_apis__apiId__dedicated_gateway({
                    apiId: id
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
        const promised_getAPIThumbnail = this.client.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId__thumbnail({
                    apiId: id
                },
                this._requestMetaData(),
            );
        });

        return promised_getAPIThumbnail;
    }

    /**
     * Add new thumbnail image to an API
     *
     * @param {String} api_id id of the API
     * @param {File} imageFile thumbnail image to be uploaded
     */
    addAPIThumbnail(api_id, imageFile) {
        const promised_addAPIThumbnail = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                file: imageFile,
                'Content-Type': imageFile.type,
            };
            return client.apis['API (Individual)'].updateAPIThumbnail(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });

        return promised_addAPIThumbnail;
    }

    /**
     * Add new comment to an existing API
     * @param apiId apiId of the api to which the comment is added
     * @param commentInfo comment text
     */
    addComment(apiId, commentInfo, callback = null) {
        let promise = this.client.then(
            (client) => {
                return client.apis["Comment (Individual)"].post_apis__apiId__comments(
                    {apiId: apiId, body: commentInfo}, this._requestMetaData());
            }
        ).catch(
            error => {
                console.error(error);
            }
        );
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

    /**
     * Get all comments for a particular API
     * @param apiId api id of the api to which the comment is added
     */
    getAllComments(apiId, callback = null) {
        let promise_get = this.client.then(
            (client) => {
                return client.apis["Comment (Collection)"].get_apis__apiId__comments(
                    {apiId: apiId}, this._requestMetaData());
            }
        ).catch(
            error => {
                console.error(error);
            }
        );
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
     */
    deleteComment(apiId, commentId, callback = null) {
        let promise = this.client.then(
            (client) => {
                return client.apis["Comment (Individual)"].delete_apis__apiId__comments__commentId_(
                    {apiId: apiId, commentId: commentId}, this._requestMetaData());
            }
        ).catch(
            error => {
                console.error(error);
            }
        );
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
     */
    updateComment(apiId, commentId, commentInfo, callback = null) {
        let promise = this.client.then(
            (client) => {
                return client.apis["Comment (Individual)"].put_apis__apiId__comments__commentId_(
                    {apiId: apiId, commentId: commentId, body: commentInfo}, this._requestMetaData());
            }
        ).catch(
            error => {
                console.error(error);
            }
        );
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
    getDataFromSpecFields(client){
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
     *
     * Static method for get all APIs for current environment user.
     * @static
     * @param {Object} params APIs filtering parameters i:e { "name": "MyBank API"}
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    static all(params) {
        let query = "";
        if (params && 'query' in params) {
            for (const [key, value] of Object.entries(params.query)) {
                query += `${key}:${value},`;
            }
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedAPIs = apiClient.then((client) => {
            return client.apis['API (Collection)'].get_apis(params, Resource._requestMetaData());
        });

        return promisedAPIs.then((response) => {
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
        let query = "";
        if (params && 'query' in params) {
            for (const [key, value] of Object.entries(params.query)) {
                query += `${key}:${value},`;
            }
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then((client) => {
            return client.apis['API Product (Collection)'].get_api_products(params, Resource._requestMetaData());
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
        return apiClient.then((client) => {
            return client.apis['API (Collection)'].get_search(params, Resource._requestMetaData());
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
        const promisedAPI = apiClient.then((client) => {
            return client.apis['API (Individual)'].get_apis__apiId_({
                apiId: id
            }, this._requestMetaData());
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
        const promisedAPI = apiClient.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId_({
                apiProductId: id
            }, this._requestMetaData());
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
        return apiClient.then((client) => {
            return client.apis['API (Individual)'].delete_apis__apiId_({
                apiId: id
            }, this._requestMetaData());
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
        return apiClient.then((client) => {
            return client.apis['API Product (Individual)'].delete_api_products__apiProductId_({
                apiProductId: id
            }, this._requestMetaData());
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
        return apiClient.then((client) => {
            return client.apis["Throttling Policies"].getAllThrottlingPolicies({
                    policyLevel: policyLevel
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
        return apiClient.then((client) => {
           return client.apis['Certificates (Collection)'].get_endpoint_certificates();
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
        return apiClient.then((client) => {
            return client.apis['Certificates (Individual)'].post_endpoint_certificates({
                certificate: certificateFile,
                endpoint,
                alias
            });
        }, this._requestMetaData({
            'Content-Type': 'multipart/form-data'
        }));
    }

    /**
     * Get the status of the endpoint certificate which matches the given alias.
     *
     * @param {string} alias The alias of the certificate which the information required.
     * */
    static getCertificateStatus(alias) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then((client) => {
            return client.apis['Certificates (Individual)'].get_endpoint_certificates__alias_({
                alias: alias
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
        return apiClient.then((client) => {
            console.log(client.apis['Certificates (Individual)']);
            return client.apis['Certificates (Individual)'].delete_endpoint_certificates__alias_({
                alias
            });
        }, this._requestMetaData());
    }
}

API.CONSTS = {
    API: 'API',
    APIProduct: 'APIProduct',
}

Object.freeze(API.CONSTS);

export default API;
