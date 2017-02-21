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
        var error_data = JSON.parse(error_response.data);
        var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".<br/> You will be redirect to the login page ...";
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
                    window.location = contextPath + "/auth/login";
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
            usePromise: true
        });
        this.auth_client = new AuthClient();
        this.client.then(
            (swagger) => {
                swagger.setSchemes(["http"]);
                swagger.setHost("localhost:9090");
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
            "version": null
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
        if (api_data.constructor.name == "Blob") {
            payload = {file: api_data, 'Content-Type': "multipart/form-data"};
            promise_create = this.client.then(
                (client) => {
                    return client["API (Collection)"].post_apis_import_definition(
                        payload, this._requestMetaData({'Content-Type': "multipart/form-data"})).catch(AuthClient.unauthorizedErrorHandler);
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
     * Get the available policies information by tier level.
     * @param {String} tier_level List API or Application or Resource type policies.parameter should be one of api, application and resource
     * @returns {Promise.<TResult>}
     */
    policies(tier_level) {
        var promise_policies = this.client.then(
            (client) => {
                return client["Throttling Tier (Collection)"].get_policies_tierLevel(
                    {tierLevel: 'api'}, this._requestMetaData()).catch(AuthClient.unauthorizedErrorHandler);
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
                    {apiId: id}, this._requestMetaData());
            }
        ).catch(AuthClient.unauthorizedErrorHandler);
        return promised_delete;
    }

    /**
     * Update the life cycle state of an API given its id (UUID)
     * @param id {string} UUID of the api
     * @param state {string} Target state which need to be transferred
     * @param callback {function} Callback function which needs to be executed in the success call
     */
    updateLcState(id, state, callback = null) {
        var payload = {action: state, apiId: id, "Content-Type": "application/json"};
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

}