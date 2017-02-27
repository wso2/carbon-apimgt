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
var authManager = {};
authManager.isLogged = false;
authManager.user = {};
authManager.getAuthStatus = function () {
    //this.isLogged = !(!$.cookie('token') && !$.cookie('user'));
    return this.isLogged;
};
authManager.setAuthStatus = function (status) {
    this.isLogged = status;
};
authManager.setUserName = function (username) {
    this.user.username = username;
};
authManager.getUserName = function () {
    return this.user.username;
};
authManager.setUserScope = function (scope) {
    this.user.scope = scope;
};
authManager.getUserScope = function () {
    return this.user.scope;
};
authManager.login = function () {
    var params = {
        username: $('#username').val(),
        password: $('#password').val(),
        grant_type: 'password',
        validity_period: '3600',
        scopes: 'apim:api_view apim:api_create'

    };
    var referrer = document.referrer;
    var url = contextPath + '/auth/apis/login/token';
    return $.ajax({
        type: 'POST',
        url: url,
        async: false,
        data: params,
        traditional:true,
        headers: {
            'Authorization': 'Basic deidwe',
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer
        }
    });
};
authManager.logout = function () {
    var url = contextPath + '/auth/apis/login/remove';
    return $.ajax({
        type: 'POST',
        url: url,
        headers: {
            'Accept': 'application/json'
        }
    });
};

var doLogout = function () {
    var logoutPromise = authManager.logout();
    logoutPromise.then(function (data, status, xhr) {
        delete_cookie("WSO2_AM_TOKEN_1");
        window.location.href = contextPath + "/auth/login";
    })

};

function delete_cookie(name) {
    document.cookie = name +'=; Path=' + contextPath + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
};
