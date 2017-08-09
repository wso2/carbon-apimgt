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

var DOC = function () {
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

DOC.prototype._getSwaggerURL = function _getSwaggerURL() {
    return swaggerURL;
};

/**
 *
 * @param data
 * @returns {{clientAuthorizations: {api_key: SwaggerClient.ApiKeyAuthorization}, requestContentType: (*|string)}}
 * @private
 */


DOC.prototype._requestMetaData = function _requestMetaData() {
    var data = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

    var access_key_header = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    var request_meta = {
        clientAuthorizations: {
            OAuth2Security: new SwaggerClient.ApiKeyAuthorization("Authorization", access_key_header, "header")
        },
        requestContentType: data['Content-Type'] || "application/json"
    };
    return request_meta;
};

/**
 * Get list of all the available Docs for an API, If the call back is given (TODO: need to ask for fallback
 * sequence as well tmkb)
 * It will be invoked upon receiving the response from REST service.Else will return a promise.
 * @param callback {function} A callback function to invoke after receiving successful response.
 * @returns {promise} With given callback attached to the success chain else API invoke promise.
 */


DOC.prototype.getAll = function getAll(callback,apiId) {
    var _this3 = this;

    var promise_get_all = _this3.client.then(function (client) {
        return client["API (individual)"].get_apis_apiId_documents({apiId: apiId}, _this3._requestMetaData()).catch(unauthorizedErrorHandler);
    });
    if (callback) {
        return promise_get_all.then(callback);
    } else {
        return promise_get_all;
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