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
    var scopes = [];
    scopes[0] = 'apim:api_view';
    scopes[1] = 'apim:api_create';
    var params = {
        username: $('#username').val(),
        password: $('#password').val(),
        grant_type: 'password',
        scopes: scopes

    };
    var referrer = document.referrer;
    var url = contextPath + '/auth/apis/login/token';
    var resp;
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
        },
        success: function(data) {
            resp = data;
        }
    });
    return resp;
};
authManager.logout = function () {
    if (this.getAuthStatus()) {
        this.setAuthStatus(false);
        delete this.user;
        $.cookie("token", null, { path: '/' });
        $.cookie("user", null, { path: '/' });
        $.cookie("userRole", null, { path: '/' });
        //TODO revoke the token
        route.routTo(loginPageUri);
    } else {
        route.routTo(loginPageUri);
    }
};
