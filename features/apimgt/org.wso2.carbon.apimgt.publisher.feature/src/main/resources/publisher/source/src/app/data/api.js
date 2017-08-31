/**
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
"use strict";
import AuthManager from './AuthManager'
import Factory from './Factory'

/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor() {
        //this.client = new SingleClient().client;
        let currentenv = localStorage.getItem("currentEnv");
        this.client = Factory.factoryCheck(currentenv);
        console.log(this.client);
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData(data = {}) {
        AuthManager.refreshTokenOnExpire(); /* TODO: This should be moved to an interceptor ~tmkb*/
        let metaData = {
            requestContentType: data['Content-Type'] || "application/json"
        };
        if (data['Accept']) {
            metaData.responseContentType = data['Accept'];
        }
        return metaData;
    }

    /**
     * Intend to be a private static method in API class,Update the API template with given parameter values.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     */
    _updateTemplate(api_data) {
        let payload;
        let template = {
            "name": null,
            "context": null,
            "version": null,
            "endpoint": []
        };
        var user_keys = Object.keys(api_data);
        for (var index in user_keys) {
            if (!(user_keys[index] in template)) {
                throw 'Invalid key provided, Valid keys are `' + Object.keys(template) + '`';
            }
        }
        payload = Object.assign(template, api_data);
        return payload;
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
            payload = {file: api_data, 'Content-Type': "multipart/form-data"};
            promise_create = this.client.then(
                (client) => {
                    return client.apis["API (Collection)"].post_apis_import_definition(payload,
                        this._requestMetaData({'Content-Type': "multipart/form-data"}));
                }
            );
        } else if (api_data.type === 'swagger-url') {
            payload = {url: api_data.url, 'Content-Type': "multipart/form-data"};
            promise_create = this.client.then(
                (client) => {
                    return client.apis["API (Collection)"].post_apis_import_definition(payload,
                        this._requestMetaData({'Content-Type': "multipart/form-data"}));
                }
            );
        } else {
            payload = {body: this._updateTemplate(api_data), "Content-Type": "application/json"};
            promise_create = this.client.then(
                (client) => {
                    return client.apis["API (Collection)"].post_apis(
                        payload, this._requestMetaData());
                }
            );
        }
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    /**
     * Create an API from WSDL with the given parameters and call the callback method given optional.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     * @param {function} callback - An optional callback method
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
    importWSDL (api_data, callback = null) {
        let payload;
        let promise_create;
        payload = {
            type: "WSDL",
            additionalProperties: api_data.additionalProperties,
            implementationType: api_data.implementationType,
            'Content-Type': "multipart/form-data",
        };
        if (api_data.url) {
            payload.url = api_data.url;
        } else {
            payload.file = api_data.file;
        }
        promise_create = this.client.then(
            (client) => {
                return client.apis["API (Collection)"].post_apis_import_definition(payload,
                    this._requestMetaData({'Content-Type': "multipart/form-data"}));
            }
        );
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    /**
     * Get list of all the available APIs, If the call back is given (TODO: need to ask for fallback sequence as well tmkb)
     * It will be invoked upon receiving the response from REST service.Else will return a promise.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAll(callback = null) {
        var promise_get_all = this.client.then(
            (client) => {
                return client.apis["API (Collection)"].get_apis({}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get_all.then(callback);
        } else {
            return promise_get_all;
        }
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    get(id, callback = null) {
        var promise_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId_(
                    {apiId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Create a new version of a given API
     * @param id {string} UUID of the API.
     * @param version {string} new API version.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createNewAPIVersion(id, version, callback = null) {
        var promise_copy_api = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].post_apis_copy_api(
                    {apiId: id, newVersion: version},
                    this._requestMetaData());
            }
        );
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
        var promise_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId__swagger(
                    {apiId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    updateSwagger(id, swagger) {
        var promised_update = this.client.then(
            (client) => {
                let payload = {
                    "apiId": id,
                    "endpointId": JSON.stringify(swagger),
                    "Content-Type": "multipart/form-data"
                };
                return client.apis["API (Individual)"].put_apis__apiId__swagger(
                    payload, this._requestMetaData({'Content-Type': "multipart/form-data"}));
            }
        );
        return promised_update;
    }

    /**
     * Get the available policies information by tier level.
     * @param {String} tier_level List API or Application or Resource type policies.parameter should be one of api, application and resource
     * @returns {Promise.<TResult>}
     */
    policies(tier_level) {
        var promise_policies = this.client.then(
            (client) => {
                return client.apis["Throttling Tier (Collection)"].get_policies__tierLevel_(
                    {tierLevel: 'subscription'}, this._requestMetaData());
            }
        );
        return promise_policies;
    }

    /**
     * Delete an API given an api identifier
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    deleteAPI(id) {
        var promised_delete = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].delete_apis__apiId_(
                    {apiId: id}, this._requestMetaData());
            }
        );
        return promised_delete;
    }

    /**
     * Get the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    getLcState(id, callback = null) {
        var promise_lc_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId__lifecycle(
                    {apiId: id}, this._requestMetaData());
            }
        );
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
        var promise_lc_history_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId__lifecycle_history(
                    {apiId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_lc_history_get.then(callback);
        } else {
            return promise_lc_history_get;
        }
    }

    /**
     * Update the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param state {string} Target state which need to be transferred
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    updateLcState(id, state, checkedItems, callback = null) {
        var payload = {action: state, apiId: id, lifecycleChecklist: checkedItems, "Content-Type": "application/json"};
        var promise_lc_update = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].post_apis_change_lifecycle(
                    payload, this._requestMetaData());
            }
        );
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
        var promise_deletePendingTask = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].delete_apis_apiId_lifecycle_lifecycle_pending_task({apiId: id},
                    this._requestMetaData());
            }
        );
        return promise_deletePendingTask;
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    update(api) {
        var promised_update = this.client.then(
            (client) => {
                let payload = {apiId: api.id, body: api};
                return client.apis["API (Individual)"].put_apis__apiId_(payload);
            }
        );
        return promised_update;
    }

    /**
     * Get the available subscriptions for a given API
     * @param {String} apiId API UUID
     * @returns {Promise} With given callback attached to the success chain else API invoke promise.
     */
    subscriptions(id, callback = null) {
        var promise_subscription = this.client.then(
            (client) => {
                return client.apis["Subscription (Collection)"].get_subscriptions(
                    {apiId: id},
                    this._requestMetaData()
                );
            }
        );
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
        var promise_subscription = this.client.then(
            (client) => {
                return client.apis["Subscription (Individual)"].post_subscriptions_block_subscription(
                    {subscriptionId: id, blockState: state},
                    this._requestMetaData()
                );
            }
        );
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
        var promise_subscription = this.client.then(
            (client) => {
                return client.apis["Subscription (Individual)"].post_subscriptions_unblock_subscription(
                    {subscriptionId: id},
                    this._requestMetaData()
                );
            }
        );
        if (callback) {
            return promise_subscription.then(callback);
        } else {
            return promise_subscription;
        }
    }

    /**
     * Add endpoint via POST HTTP method, need to provided endpoint properties and callback function as argument
     * @param body {Object} Endpoint to be added
     * @param callback {function} Callback function
     */
    addEndpoint(body) {
        var promised_addEndpoint = this.client.then(
            (client) => {
                let payload = {body: body, "Content-Type": "application/json"};
                return client.apis["Endpoint (Collection)"].post_endpoints(
                    payload, this._requestMetaData());
            }
        );

        return promised_addEndpoint;
    }

    /**
     * Delete an Endpoint given its identifier
     * @param id {String} UUID of the Endpoint which want to delete
     * @returns {promise}
     */
    deleteEndpoint(id) {
        var promised_delete = this.client.then(
            (client) => {
                return client.apis["Endpoint (individual)"].delete_endpoints__endpointId_(
                    {
                        endpointId: id,
                        'Content-Type': 'application/json'
                    }, this._requestMetaData());
            }
        );
        return promised_delete;
    }

    /**
     * Get All Global Endpoints.
     * @returns {Promise} Promised all list of endpoint
     */
    getEndpoints() {
        return this.client.then(
            (client) => {
                return client.apis["Endpoint (Collection)"].get_endpoints(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * Get endpoint object by its UUID.
     * @param id {String} UUID of the endpoint
     * @returns {Promise.<TResult>}
     */
    getEndpoint(id) {
        return this.client.then(
            (client) => {
                return client.apis["Endpoint (individual)"].get_endpoints_endpointId(
                    {
                        endpointId: id,
                        'Content-Type': 'application/json'
                    }, this._requestMetaData());
            }
        );
    }

    /**
     * Update endpoint object.
     * @param data {Object} Endpoint to be updated
     * @returns {Promise.<TResult>}
     */
    updateEndpoint(data) {
        return this.client.then(
            (client) => {
                return client.apis["Endpoint (individual)"].put_endpoints_endpointId(
                    {
                        endpointId: data.id,
                        body: data,
                        'Content-Type': 'application/json'
                    }, this._requestMetaData());
            });
    }


    addDocument(api_id, body) {

        var promised_addDocument = this.client.then(
            (client) => {
                let payload = {apiId: api_id, body: body, "Content-Type": "application/json"};
                return client.apis["Document (Collection)"].post_apis__apiId__documents(
                    payload, this._requestMetaData());
            }
        );
        return promised_addDocument;
    }

    /*
     Add a File resource to a document
     */
    addFileToDocument(api_id, docId, fileToDocument) {
        var promised_addFileToDocument = this.client.then(
            (client) => {
                let payload = {
                    apiId: api_id,
                    documentId: docId,
                    file: fileToDocument,
                    "Content-Type": "application/json"
                };
                return client.apis["Document (Individual)"].post_apis__apiId__documents__documentId__content(
                    payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
            }
        );

        return promised_addFileToDocument;
    }

    /*
     Add inline content to a INLINE type document
     */
    addInlineContentToDocument(api_id, doc_id, inline_content) {
        var promised_addInlineContentToDocument = this.client.then(
            (client) => {
                let payload = {
                    apiId: api_id,
                    documentId: doc_id,
                    inlineContent: inline_content,
                    "Content-Type": "application/json"
                };
                return client.apis["Document (Individual)"].post_apis__apiId__documents__documentId__content(
                    payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
            }
        );
        return promised_addInlineContentToDocument;
    }

    getFileForDocument(api_id, docId) {
        var promised_getDocContent = this.client.then(
            (client) => {
                let payload = {apiId: api_id, documentId: docId, "Accept": "application/octet-stream"};
                return client.apis["Document (Individual)"].get_apis__apiId__documents__documentId__content(
                    payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
            }
        );
        return promised_getDocContent;
    }

    /*
     Get the inline content of a given document
     */
    getInlineContentOfDocument(api_id, docId) {
        var promised_getDocContent = this.client.then(
            (client) => {
                let payload = {apiId: api_id, documentId: docId};
                return client.apis["Document (Individual)"].get_apis__apiId__documents__documentId__content(
                    payload);
            }
        );
        return promised_getDocContent;
    }

    getDocuments(api_id, callback) {
        var promise_get_all = this.client.then(
            (client) => {
                return client.apis["Document (Collection)"].get_apis__apiId__documents({apiId: api_id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get_all.then(callback);
        } else {
            return promise_get_all;
        }
    }

    updateDocument(api_id, docId, body) {
        var promised_updateDocument = this.client.then(
            (client) => {
                let payload = {
                    apiId: api_id,
                    body: body,
                    documentId: docId,
                    "Content-Type": "application/json"
                };
                return client.apis["Document (Individual)"].put_apis__apiId__documents__documentId_(
                    payload, this._requestMetaData());
            }
        );
        return promised_updateDocument;
    }

    getDocument(api_id, docId, callback) {
        var promise_get = this.client.then(
            (client) => {
                return client.apis["Document (Individual)"].get_apis__apiId__documents__documentId_({
                        apiId: api_id,
                        documentId: docId
                    },
                    this._requestMetaData());
            }
        );
        return promise_get;
    }


    deleteDocument(api_id, document_id) {
        var promise_deleteDocument = this.client.then(
            (client) => {
                return client.apis["Document (Individual)"].delete_apis__apiId__documents__documentId_({
                        apiId: api_id,
                        documentId: document_id
                    },
                    this._requestMetaData());
            }
        );
        return promise_deleteDocument;
    }

    /**
     * Get the available labels.
     * @returns {Promise.<TResult>}
     */
    labels() {
        var promise_labels = this.client.then(
            (client) => {
                return client.apis["Label (Collection)"].get_labels({},
                    this._requestMetaData());
            }
        );
        return promise_labels;
    }

    validateWSDLUrl(wsdlUrl) {
        let promised_validationResponse = this.client.then((client) => {
            return client.apis["API (Collection)"]
                .post_apis_validate_definition({
                    "type": "WSDL",
                    "url": wsdlUrl,
                    "Content-Type": "multipart/form-data"}, 
                    this._requestMetaData({"Content-Type": "multipart/form-data"}));
        });
        return promised_validationResponse;
    }

    validateWSDLFile(file) {
        let promised_validationResponse = this.client.then((client) => {
            return client.apis["API (Collection)"]
                .post_apis_validate_definition({
                        "type": "WSDL",
                        "file": file,
                        "Content-Type": "multipart/form-data"},
                    this._requestMetaData({"Content-Type": "multipart/form-data"}));
        });
        return promised_validationResponse;
    }

    getWSDL(apiId) {
        let promised_wsdlResponse = this.client.then((client) => {
            return client.apis["API (Individual)"].get_apis__apiId__wsdl({
                    "apiId": apiId
                },
                this._requestMetaData({"Accept": "application/octet-stream"}));
        });
        return promised_wsdlResponse;
    }
}

export default API