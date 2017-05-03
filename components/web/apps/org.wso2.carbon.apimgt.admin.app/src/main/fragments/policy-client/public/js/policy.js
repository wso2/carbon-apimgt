/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Policy = function () {
    this.client = new SwaggerClient({
        url: this._getSwaggerURL(),
        usePromise: true
    });
    this.client.then(function (swagger) {
        swagger.setHost(location.host);
    });
    this.client.catch(function (error) {
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
    });

};

Policy.prototype._getSwaggerURL = function _getSwaggerURL() {
    return swaggerURL;
};

/**
 *
 * @param data
 * @returns {{clientAuthorizations: {api_key: SwaggerClient.ApiKeyAuthorization}, requestContentType: (*|string)}}
 * @private
 */


Policy.prototype._requestMetaData = function _requestMetaData() {
    var data = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

    var access_key_header = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    var request_meta = {
        clientAuthorizations: {
            api_key: new SwaggerClient.ApiKeyAuthorization("Authorization", access_key_header, "header")
        },
        requestContentType: data['Content-Type'] || "application/json"
    };
    return request_meta;
};

Policy.prototype.getAllAdvancePolicies = function (callback) {
    let param = {};
    let promise_create = this.client.then(
        (client) => {
            return client["Advanced Policies"].get_policies_throttling_advanced(
                param, this._requestMetaData()).catch(unauthorizedErrorHandler)
        })
    if (callback) {
        return promise_create.then(callback);
    } else {
        return promise_create;
    }
};


Policy.prototype.getAllApplicationPolicies = function (callback) {
    let param = {};
    let promise_create = this.client.then(
        (client) => {
            return client["Application Policies"].get_policies_throttling_application(
                param, this._requestMetaData()).catch(unauthorizedErrorHandler)
        })
    if (callback) {
        return promise_create.then(callback);
    } else {
        return promise_create;
    }
};

Policy.prototype.getAllSubscriptionPolicies = function (callback) {
    let param = {};
    let promise_create = this.client.then(
        (client) => {
            return client["Subscription Policies"].get_policies_throttling_subscription(
                param, this._requestMetaData()).catch(unauthorizedErrorHandler)
        })
    if (callback) {
        return promise_create.then(callback);
    } else {
        return promise_create;
    }
};


Policy.prototype.getPoliciesByUuid = function (uuid, tierLevel, callback) {
    let param = { "uuid": uuid,"tierLevel": tierLevel};

    let promised_get;
    if (param.tierLevel == "api") {
        promised_get = this.client.then(
            (client) => {
                return client["Advanced Policies"].get_policies_throttling_advanced_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (param.tierLevel == "application") {
        promised_get = this.client.then(
            (client) => {
                return client["Application Policies"].get_policies_throttling_application_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (param.tierLevel == "subscription") {
        promised_get = this.client.then(
            (client) => {
                return client["Subscription Policies"].get_policies_throttling_subscription_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    }


    if (callback) {
        return promised_get.then(callback);
    } else {
        return promised_get;
    }
};

/**
 * Delete an API given an api identifier
 * @param policyTier {String} tier type
 * @param uuid {String} uuid
 * @param callback {function} Function which needs to be called upon success of the API deletion
 * @returns {promise} With given callback attached to the success chain else API invoke promise.
 */
Policy.prototype.deletePolicyByUuid = function (policyTier, uuid, callback) {
    let param = {tierLevel: policyTier, policyId: uuid};
    let promised_delete;
    if (param.tierLevel == "api") {
        promised_delete = this.client.then(
            (client) => {
                return client["Advanced Policies"].delete_policies_throttling_advanced_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (param.tierLevel == "application") {
        promised_delete = this.client.then(
            (client) => {
                return client["Application Policies"].delete_policies_throttling_application_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (param.tierLevel == "subscription") {
        promised_delete = this.client.then(
            (client) => {
                return client["Subscription Policies"].delete_policies_throttling_subscription_policyId(
                    param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    }

    if (callback) {
        return promised_delete.then(callback);
    } else {
        return promised_delete;
    }
};


Policy.prototype.create = function (policy, callback) {
    let payload;
    let promise_create;
    payload = {tierLevel: policy.tierLevel, body: policy, "Content-Type": "application/json"};

    if (payload.tierLevel == "api") {
        promise_create = this.client.then(
            (client) => {
                return client["Advanced Policies"].post_policies_throttling_advanced(
                    payload, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (payload.tierLevel == "application") {
        promise_create = this.client.then(
            (client) => {
                return client["Application Policies"].post_policies_throttling_application(
                    payload, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    } else if (payload.tierLevel == "subscription") {
        promise_create = this.client.then(
            (client) => {
                return client["Subscription Policies"].post_policies_throttling_subscription(
                    payload, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    }
    if (callback) {
        return promise_create.then(callback);
    } else {
        return promise_create;
    }
};

function getCookie(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
}

function unauthorizedErrorHandler(error_response) {
    if (error_response.status !== 401) { /* Skip unrelated response code to handle in unauthorizedErrorHandler*/
        console.debug(error_response);
        throw error_response;
        /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain*/
    }
    var error_data = JSON.parse(error_response.data);
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


