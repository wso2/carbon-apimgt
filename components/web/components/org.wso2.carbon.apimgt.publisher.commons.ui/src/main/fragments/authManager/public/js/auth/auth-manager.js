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
var authManager = {};
authManager.isLogged = false;
authManager.user = {};
authManager.getAuthStatus = function () {
    this.isLogged = !(!$.cookie('token') && !$.cookie('user'));
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
        scope: scopes
    };

    return $.ajax({
        type: 'POST',
        url: tokenEndpoint,
        data: params,
        headers: {
            'Authorization': 'Basic ' + keyAndSecret,
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });
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
