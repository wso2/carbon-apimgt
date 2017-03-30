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



Policy.prototype.getAllPoliciesByTier = function (tierLevel, callback) {
    let param = {"tierLevel" : tierLevel};
    let promise_create = this.client.then(
            (client) => {
                return client["Throttling Tier Collection"].get_policies_tierLevel_tierLevel(
                        param, this._requestMetaData()).catch(unauthorizedErrorHandler)
            })
    if (callback) {
        return promise_create.then(callback);
    } else {
        return promise_create;
    }
};

/**
 * Delete an API given an api identifier
 * @param policyId {String} PolicyName
 * @param callback {function} Function which needs to be called upon success of the API deletion
 * @returns {promise} With given callback attached to the success chain else API invoke promise.
 */
Policy.prototype.deletePolicy = function(policyTier, policyId, callback) {
    let param = {tierLevel: policyTier, tierName: policyId};
    var promised_delete = this.client.then(
            (client) => {
                return client["Throttling Tier (Individual)"].delete_policies_tierLevel_tierLevel_tierName_tierName(
                        param, this._requestMetaData()).catch(unauthorizedErrorHandler);
            })
    if (callback) {
        return promised_delete.then(callback);
    } else {
        return promised_delete;
    }
};

Policy.prototype.create = function(policy, callback) {
    let payload;
    let promise_create;
        payload = {tierLevel: policy.tierLevel, body: policy, "Content-Type": "application/json"};
        promise_create = this.client.then(
                (client) => {
                return client["Throttling Tier (individual)"].post_policies_tierLevel_tierLevel(
                    payload, this._requestMetaData()).catch(unauthorizedErrorHandler);
                })
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


