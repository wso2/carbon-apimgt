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
/**
 * Manage API access keys with corresponding keys,Not related to the keymanager used in backend
 */
class KeyManager {
    /**
     *
     * @param {string} access_key - Default access key if only one access key is using
     */
    constructor(access_key = '') {
        this._keys = {};
        this.addKey(access_key);
    }

    /**
     *
     * @param {string} access_key - Access key to be saved
     * @param {string} scope - Scope of the access key provided above
     * @returns {string} - Return newly added key in success
     */
    addKey(access_key, scope = 'default') {
        if (!(scope in this._keys)) {
            this._keys[scope] = access_key;
            return this._keys[scope];
        } else {
            throw 'Key already exist `' + this._keys[scope] + '` for scope `' + scope + '`';
        }

    }

    /**
     * Replace existing key with new valid key
     * @param {string} access_key
     * @param {string} scope
     * @returns {string} - Return updated key
     */
    updateKey(access_key, scope = 'default') {
        this._keys[scope] = access_key;
        return this._keys;
    }

    /**
     * Get key by giving the scope of the key
     * @param {string} scope
     * @returns {string|null} - If key found return it else null
     */
    getKey(scope) {
        if (scope === undefined) {
            scope = 'default';
            if (this.size() > 1) {
                throw 'Should provide scope parameter when there are more than single key'
            }
        }
        if (scope in this._keys) {
            return this._keys[scope]
        } else {
            return null;
        }
    }

    /**
     * Retrun the number of keys in the hash
     * @returns {number} Count of the access keys
     */
    size() {
        var keys_count = 0;
        for (var index in this._keys) {
            if (this._keys.hasOwnProperty(index) && this._keys[index]) {
                keys_count++;
            }
        }
        return keys_count;
    }
}

class AuthClient {

    static refreshTokenOnExpire(){
        var currentTimestamp =  Math.floor(Date.now() / 1000);
        var tokenTimestamp = window.localStorage.getItem("expiresIn");
        if(tokenTimestamp - currentTimestamp < 100) {
            var bearerToken = "Bearer " + AuthClient.getCookie("WSO2_AM_REFRESH_TOKEN_1");
            var loginPromise = authManager.refresh(bearerToken);
            loginPromise.then(function(data,status,xhr){
                authManager.setAuthStatus(true);
                var expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
                window.localStorage.setItem("expiresIn", expiresIn);
            });
            loginPromise.error(
                function (error) {
                    var error_data = JSON.parse(error.responseText);
                    var message = "Error while refreshing token" + "<br/> You will be redirect to the login page ..." ;
                    noty({
                        text: message,
                        type: 'error',
                        dismissQueue: true,
                        modal: true,
                        progressBar: true,
                        timeout: 5000,
                        layout: 'top',
                        theme: 'relax',
                        maxVisible: 10,
                        callback: {
                            afterClose: function () {
                                window.location = loginPageUri;
                            },
                        }
                    });

                }
            );
        }
    }

    /**
     * Static method to handle unauthorized user action error catch, It will look for response status code and skip !401 errors
     * @param {object} error_response
     */
    static unauthorizedErrorHandler(error_response) {
        if (error_response.status !== 401) { /* Skip unrelated response code to handle in unauthorizedErrorHandler*/
            console.debug(error_response);
            throw error_response;
            /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain*/
        }
        var message = "The session has expired" + ".<br/> You will be redirect to the login page ...";
        noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            modal: true,
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10,
            callback: {
                afterClose: function () {
                    window.location = loginPageUri;
                },
            }
        });
    }

    static getCookie(name) {
        var value = "; " + document.cookie;
        var parts = value.split("; " + name + "=");
        if (parts.length == 2) return parts.pop().split(";").shift();
    }

}
/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor(access_key) {
        this.client = new SwaggerClient({
            url: this._getSwaggerURL(),
            usePromise: true,
            requestInterceptor: this._getRequestInterceptor(),
            responseInterceptor: this._getResponseInterceptor()
        });
        this.auth_client = new AuthClient();
        this.client.then(
            (swagger) => {
                swagger.setHost(location.host);
                this.keyMan = new KeyManager(access_key);
                let scopes = swagger.swaggerObject["x-wso2-security"].apim["x-wso2-scopes"];
                for (var index in scopes) {
                    if (scopes.hasOwnProperty(index)) {
                        let scope_key = scopes[index].key;
                        this.keyMan.addKey(null, scope_key);
                        /* Fill with available scopes */
                    }
                }
            }
        );
        this.client.catch(
            error => {
                var n = noty({
                    text: error,
                    type: 'warning',
                    dismissQueue: true,
                    layout: 'top',
                    theme: 'relax',
                    progressBar: true,
                    timeout: 5000,
                    closeWith: ['click']
                });
            }
        );
    }

    /**
     * Get the ETag of a given resource key from the session storage
     * @param key {string} key of resource.
     * @returns {string} ETag value for the given key
     */
    static getETag(key) {
        return sessionStorage.getItem("etag_" + key);
    }

    /**
     * Add an ETag to a given resource key into the session storage
     * @param key {string} key of resource.
     * @param etag {string} etag value to be stored against the key
     */
    static addETag(key, etag) {
        sessionStorage.setItem("etag_" + key, etag);
    }

    _getResponseInterceptor() {
        var responseInterceptor = {
            apply: function (data) {
                if (data.headers.etag) {
                    API.addETag(data.url, data.headers.etag);
                }
                return data;
            }
        };
        return responseInterceptor;
    }

    _getRequestInterceptor() {
        var requestInterceptor = {
            apply: function (data) {
                if (API.getETag(data.url) && (data.method == "PUT" || data.method == "DELETE"
                    || data.method == "POST")) {
                    data.headers["If-Match"] = API.getETag(data.url);
                }
                return data;
            }
        };
        return requestInterceptor;
    }

    _getSwaggerURL() {
        return swaggerURL;
    }

    /**
     *
     * @param data
     * @returns {{clientAuthorizations: {api_key: SwaggerClient.ApiKeyAuthorization}, requestContentType: (*|string)}}
     * @private
     */
    _requestMetaData(data = {}) {
        AuthClient.refreshTokenOnExpire();
        let access_key_header = "Bearer " + AuthClient.getCookie("WSO2_AM_TOKEN_1");
        let request_meta = {
            clientAuthorizations: {
                api_key: new SwaggerClient.ApiKeyAuthorization("Authorization", access_key_header, "header")
            },
            requestContentType: data['Content-Type'] || "application/json"
        };
        return request_meta;
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
        if (api_data.constructor.name === Blob.name || api_data.constructor.name === File.name) {
            payload = {file: api_data, 'Content-Type': "multipart/form-data"};
            promise_create = this.client.then(
                (client) => {
                    return client["API (Collection)"].post_apis_import_definition(payload,
                    this._requestMetaData({'Content-Type': "multipart/form-data"})).catch(AuthClient.unauthorizedErrorHandler);
                }
            );
        } else if (api_data.type == 'swagger-url') {
            payload = {url: api_data.url, 'Content-Type': "multipart/form-data"};
            promise_create = this.client.then(
                    (client) => {
                    return client["API (Collection)"].post_apis_import_definition(payload,
                    this._requestMetaData({'Content-Type': "multipart/form-data"})).catch(AuthClient.unauthorizedErrorHandler);
                    }
            );
        } else {
            payload = {body: this._updateTemplate(api_data), "Content-Type": "application/json"};
            promise_create = this.client.then(
                (client) => {
                    return client["API (Collection)"].post_apis(
                        payload, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
     * Get list of all the available APIs, If the call back is given (TODO: need to ask for fallback sequence as well tmkb)
     * It will be invoked upon receiving the response from REST service.Else will return a promise.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAll(callback) {
        var promise_get_all = this.client.then(
            (client) => {
                return client["API (Collection)"].get_apis({}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["API (Individual)"].get_apis_apiId(
                    {apiId: id}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
    createNewAPIVersion(id,version,callback = null) {
        var promise_copy_api = this.client.then(
            (client) => {
                return client["API (Individual)"].post_apis_copy_api(
                    {apiId: id, newVersion: version},
                    this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                        return client["API (Individual)"].get_apis_apiId_swagger(
                            {apiId: id}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                    let payload = {"apiId": id, "endpointId": JSON.stringify(swagger), "Content-Type": "multipart/form-data"};
                    return client["API (Individual)"].put_apis_apiId_swagger(
                        payload, this._requestMetaData({'Content-Type': "multipart/form-data"})).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["Throttling Tier (Collection)"].get_policies_tierLevel(
                    {tierLevel: 'subscription'}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["API (Individual)"].delete_apis_apiId(
                    {apiId: id}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["API (Individual)"].get_apis_apiId_lifecycle(
                    {apiId: id}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["API (Individual)"].get_apis_apiId_lifecycle_history(
                    {apiId: id}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
        var payload = {action: state, apiId: id, lifecycleChecklist: checkedItems,"Content-Type": "application/json"};
        var promise_lc_update = this.client.then(
            (client) => {
                return client["API (Individual)"].post_apis_change_lifecycle(
                    payload, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
            }
        );
        if (callback) {
            return promise_lc_update.then(callback);
        } else {
            return promise_lc_update;
        }
    }

    /**
     * Update an api via PUT HTTP method, Need to give the updated API object as the argument.
     * @param api {Object} Updated API object(JSON) which needs to be updated
     */
    update(api) {
        var promised_update = this.client.then(
            (client) => {
                let payload = {apiId: api.id, body: api, "Content-Type": "application/json"};
                return client["API (Individual)"].put_apis_apiId(
                    payload, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["Subscription (Collection)"].get_subscriptions(
                    {apiId: id},
                    this._requestMetaData()
                ).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["Subscription (Individual)"].post_subscriptions_block_subscription(
                    {subscriptionId: id,blockState: state},
                    this._requestMetaData()
                ).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["Subscription (Individual)"].post_subscriptions_unblock_subscription(
                    {subscriptionId: id},
                    this._requestMetaData()
                ).catch(AuthClient.unauthorizedErrorHandler);
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
                return client["Endpoint (Collection)"].post_endpoints(
                    payload, this._requestMetaData());
            }
        ).catch(AuthClient.unauthorizedErrorHandler);

        return promised_addEndpoint;
    }

    /**
     * Get endpoint object by its UUID.
     * @param id {String} UUID of the endpoint
     * @returns {Promise.<TResult>}
     */
    getEndpoint(id) {
        return this.client.then(
            (client) => {
                return client["Endpoint (individual)"].get_endpoints_endpointId(
                    {
                        endpointId: id,
                        'Content-Type': 'application/json'
                    }, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
            return client["Endpoint (individual)"].put_endpoints_endpointId(
                {
                    endpointId: data.id,
                    body: data,
                    'Content-Type': 'application/json'
                }, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
        }).catch(AuthClient.unauthorizedErrorHandler);
    }


    addDocument(api_id,body) {

            var promised_addDocument = this.client.then(
                (client) => {
                    let payload = {apiId: api_id, body:body,"Content-Type": "application/json"};
                    return client["Document (Collection)"].post_apis_apiId_documents(
                        payload, this._requestMetaData());
                }
            ).catch(AuthClient.unauthorizedErrorHandler);

            return promised_addDocument;
        }

    addFileToDocument(api_id,docId,fileToDocument) {
            var promised_addFileToDocument = this.client.then(
                (client) => {
                    let payload = {apiId: api_id, documentId: docId, file:fileToDocument, "Content-Type": "application/json"};
                    return client["Document (Individual)"].post_apis_apiId_documents_documentId_content(
                        payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
                }
            ).catch(AuthClient.unauthorizedErrorHandler);

            return promised_addFileToDocument;
     }

    getFileForDocument(api_id,docId){
            var promised_getDocContent = this.client.then(
                (client) => {
                    let payload = {apiId: api_id, documentId: docId, "Accept":"application/octet-stream"};
                    return client["Document (Individual)"].get_apis_apiId_documents_documentId_content(
                        payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
                }
            ).catch(AuthClient.unauthorizedErrorHandler);

            return promised_getDocContent;

    }


    getDocuments(api_id, callback) {
            var promise_get_all = this.client.then(
                (client) => {
                    return client["Document (Collection)"].get_apis_apiId_documents({apiId: api_id}, this._requestMetaData()).
                    catch(AuthClient.unauthorizedErrorHandler);
                }
            );
            if (callback) {
                return promise_get_all.catch(AuthClient.unauthorizedErrorHandler).then(callback);
            } else {
                return promise_get_all;
            }
    }

    updateDocument(api_id, docId, body) {
            var promised_updateDocument = this.client.then(
                (client) => {
                   let payload = {apiId: api_id, body:body, documentId:$('#docId').val(),"Content-Type": "application/json"};
                       return client["Document (Individual)"].put_apis_apiId_documents_documentId(
                            payload, this._requestMetaData());
                   }
                ).catch(AuthClient.unauthorizedErrorHandler);
                return promised_updateDocument;
            }

    getDocument(api_id, docId, callback) {
            var promise_get = this.client.then(
                (client) => {
                    return client["Document (Individual)"].get_apis_apiId_documents_documentId({apiId: api_id, documentId: docId},
                    this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
                }
            );
            return promise_get;
    }


    deleteDocument(api_id,document_id) {
            var promise_deleteDocument = this.client.then(
                (client) => {
                    return client["Document (Individual)"].delete_apis_apiId_documents_documentId({apiId: api_id, documentId:document_id},
                    this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
                }
            );
            return promise_deleteDocument;
    }
    /**
     * Get the available labels.
     * @returns {Promise.<TResult>}
     */
    labels() {
        var promise_labels = this.client.then (
            (client) => {
                return client["Label (Collection)"].get_labels({},
                    this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
            }
        );
        return promise_labels;
    }

}
