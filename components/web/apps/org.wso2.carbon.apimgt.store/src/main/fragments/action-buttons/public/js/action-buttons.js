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

/**
 * This function validates Buttons based on the logged in user scopes against scopes defined for rest api call
 * @param {String} Array of Button ids
 *
 */
function validateActionButtons() {
    var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    var loggedInUserScopes = localStorage.getItem('userScopes');
    var response = loadSwaggerJson();
    if (response === undefined) {
        console.warn("Store swagger definition could not be loaded.");
        return;
    }
    var storeSwaggerJson = JSON.parse(response);
    for (var i = 0; i < arguments.length; i++) {
        var id = arguments[i];
        var restApiResourcePath = $(id).data('resource-path');
        var restApiResourceMethod = $(id).filter('[data-resource-method]').data('resource-method');
        if (restApiResourcePath !== undefined && restApiResourceMethod !== undefined) {
            var scopesToValidate = storeSwaggerJson["paths"][restApiResourcePath][restApiResourceMethod]["x-scope"];
            if (!loggedInUserScopes.includes(scopesToValidate)) {
                $(id).prop("disabled", true);
            }
        }
    }
}

/**
 * This function validates Buttons based on the logged in user scopes against scopes defined for rest api call
 * @param  {string} - restApiResourcePath
 * @param  {string} - restApiResourceMethod
 * @return {boolean} - Returns whether user has the required scope to access the <restApiResourcePath> <restApiResourceMethod>
 *
 */
function hasValidScopes(restApiResourcePath, restApiResourceMethod) {
    var loggedInUserScopes = localStorage.getItem('userScopes');
    if (loggedInUserScopes !== null) {
        var response = loadSwaggerJson();
        if (response !== undefined) {
            var storeSwaggerJson = JSON.parse(response);
            if (restApiResourcePath !== undefined && restApiResourceMethod !== undefined) {
                var scopesToValidate = storeSwaggerJson["paths"][restApiResourcePath][restApiResourceMethod]["x-scope"];
                if (loggedInUserScopes.includes(scopesToValidate)) {
                    return true;
                }
            }
        } else {
            console.warn("Store swagger definition could not be loaded.");
            return true;
        }
    }
    return false;
}

/*
 *  This function reads the store rest api definition and stores in localStorage
 *  @return {string} - storeSwaggerJson
*/
function loadSwaggerJson() {
    // Retrieve the object from storage
    var storeSwaggerJson = localStorage.getItem('storeSwaggerJson');
    if (storeSwaggerJson === null || storeSwaggerJson === undefined) {
        var request = new XMLHttpRequest();
        request.overrideMimeType("application/json");
        request.open('GET', swaggerURL, true);
        request.onreadystatechange = function() {
            if (request.readyState == 4 && request.status == 200) {
                //                     Put the object into storage
                localStorage.setItem('storeSwaggerJson', request.responseText);
                //                    Required use of an anonymous callback as .open will NOT return a value but simply returns
                //                    undefined in asynchronous mode
                return request.responseText;
            } else if (request.status !== 200) {
                console.warn('warning: Store SwaggerJson could not be loaded for scope validaation.');
            }
        };
        request.send(null);
    } else {
        return storeSwaggerJson;
    }
}
