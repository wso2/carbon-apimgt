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
        let key_scope = data.key_scope || 'default';
        let access_key_header = "Bearer " + this.keyMan.getKey(key_scope); //TODO: tmkb Depend on result from promise
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
                        payload, this._requestMetaData({'Content-Type': "multipart/form-data"}));
                }
            );
        } else {
            payload = {body: this._updateTemplate(api_data), "Content-Type": "application/json"};
            promise_create = this.client.then(
                (client) => {
                    return client["API (Collection)"].post_apis(
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
     * Get list of all the available APIs, If the call back is given (TODO: need to ask for fallback sequence as well tmkb)
     * It will be invoked upon receiving the response from REST service.Else will return a promise.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAll(callback) {
        var promise_get_all = this.client.then(
            (client) => {
                return client["API (Collection)"].get_apis();
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
     * Delete an API given an api identifier
     * @param id {String} UUID of the API which want to delete
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    deleteAPI(id, callback = null) {
        var promise_get = this.client.then(
            (client) => {
                return client["API (Individual)"].delete_apis_apiId(
                    {apiId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

}