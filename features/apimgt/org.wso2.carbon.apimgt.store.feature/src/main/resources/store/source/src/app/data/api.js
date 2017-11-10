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
import SingleClient from './SingleClient'

/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor() {
        this.client = new SingleClient().client;
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData(data = {}) {
        AuthManager.refreshTokenOnExpire();
        /* TODO: This should be moved to an interceptor ~tmkb*/
        return {
            requestContentType: data['Content-Type'] || "application/json"
        };
    }

    /**
     * Get list of all the available APIs, If the call back is given (TODO: need to ask for fallback sequence as well tmkb)
     * It will be invoked upon receiving the response from REST service.Else will return a promise.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllAPIs(callback = null) {
        var promise_get_all = this.client.then(
            (client) => {
                console.info("this._requestMetaData()", this._requestMetaData());
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
    getAPIById(id, callback = null) {
        var promise_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId_(
                    {apiId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            console.info("returninng promise");
            return promise_get;
        }
    }

    /**
     * Get the Documents of an API
     * @param id {String} UUID of the API in which the documents needed
     * @param callback {function} Function which needs to be called upon success of getting documents
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getDocumentsByAPIId(id, callback = null) {

        var promise_get = this.client.then(
            (client) => {
                return client.apis["API (Individual)"].get_apis__apiId__documents(
                    {apiId: id}, this._requestMetaData()
                );
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            console.info("returninng promise");
            return promise_get;
        }
    }

    /**
     * Get the Document content of an API by document Id
     * @param api_id {String} UUID of the API in which the document needed
     * @param docId {String} UUID of the Document need to view
     * @param callback {function} Function which needs to be called upon success of of getting document.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getFileForDocument(api_id, docId) {
        var promised_getDocContent = this.client.then(
            (client) => {
                let payload = {apiId: api_id, documentId: docId, "Accept": "application/octet-stream"};
                return client.apis["API (Individual)"].get_apis__apiId__documents__documentId__content(
                    payload, this._requestMetaData({"Content-Type": "multipart/form-data"}));
            }
        );
        return promised_getDocContent;
    }

    /**
     * Get the swagger of an API
     * @param id {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIId(id, callback = null) {

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
     * Get application by id
     * @param id {String} UUID of the application
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getApplication(id, callback = null) {
        let promise_get = this.client.then(
            (client) => {
                return client.apis["Application (Individual)"].get_applications__applicationId_(
                    {applicationId: id}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get application by id
     * @param id {String} UUID of the application
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllApplications(callback = null) {
        let promise_get = this.client.then(
            (client) => {
                return client.apis["Application (Collection)"].get_applications(
                    {}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get application by id
     * @param tierLevel
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllTiers(tierLevel, callback = null) {
        var promise_get_all = this.client.then(
            (client) => {
                return client.apis["Tier (Collection)"].get_policies__tierLevel_(
                    {tierLevel: tierLevel}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get_all.then(callback);
        } else {
            return promise_get_all;
        }
    }

    /**
     * Create application
     * @param application content
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createApplication(application, callback = null) {
        var promise_create = this.client.then(
            (client) => {
                let payload = {body: application};
                return client.apis["Application (Individual)"].post_applications(
                    payload, {'Content-Type': 'application/json'});
            }
        );
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

    /**
     * Update an application
     * @param application content that need to updated with the application id
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    updateApplication(application, callback = null) {
        //debugger;
        var promise_get = this.client.then(
            (client) => {
                let payload = {applicationId: application.id, body: application};
                return client.apis["Application (Individual)"].put_applications__applicationId_(
                    {payload}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Generate application keys
     * @param applicationId id of the application that needs to generate the keys
     * @param request_content payload of generate key request
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    generateKeys(applicationId, request_content, callback = null) {
        //debugger;
        var promise_get = this.client.then(
            (client) => {
                let payload = {applicationId: applicationId, body: request_content};
                return client.apis["Application (Individual)"].post_applications__applicationId__generate_keys(
                    payload, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Generate token
     * @param applicationId id of the application that needs to generate the token
     * @param request_content payload of generate token request
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    generateToken(applicationId, request_content, callback = null) {
        //debugger;
        var promise_get = this.client.then(
            (client) => {
                let payload = {applicationId: applicationId, body: request_content};
                return client.apis["Application (Individual)"].post_applications__applicationId__generate_token(
                    payload, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get keys of an application
     * @param applicationId id of the application that needs to get the keys
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getKeys(applicationId, callback = null) {
        //debugger;
        var promise_get = this.client.then(
            (client) => {
                return client.apis["Application (Individual)"].get_applications__applicationId__keys(
                    {applicationId: applicationId}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Get keys of an application
     * @param applicationId id of the application that needs to get the keys
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSubscriptions(apiId, applicationId, callback = null) {
        //debugger;
        var promise_get = this.client.then(
            (client) => {
                return client.apis["Subscription (Collection)"].get_subscriptions(
                    {apiId: apiId, applicationId: applicationId}, this._requestMetaData());
            }
        );
        if (callback) {
            return promise_get.then(callback);
        } else {
            return promise_get;
        }
    }

    /**
     * Create a subscription
     * @param apiId id of the API that needs to be subscribed
     * @param applicationId id of the application that needs to be subscribed
     * @param policy throttle policy applicable for the subscription
     * @param callback callback url
     */
    subscribe(apiId, applicationId, policy, callback = null) {
        var promise_create_subscription = this.client.then(
            (client) => {
                let subscriptionData = {apiIdentifier: apiId, applicationId: applicationId, policy: policy};
                let payload = {body: subscriptionData};
                return client.apis["Subscription (Individual)"].post_subscriptions(
                    payload, {'Content-Type': 'application/json'}
                );
            }
        );
        if (callback) {
            return promise_create_subscription.then(callback);
        } else {
            return promise_create_subscription;
        }
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

}

export default API
