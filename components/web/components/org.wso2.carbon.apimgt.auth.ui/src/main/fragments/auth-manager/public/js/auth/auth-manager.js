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
var bearer = "Bearer ";
var swaggerId = "";
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
    var allAvailableScopes = retrieveScopes();
    if(allAvailableScopes === "") {
        noty({
            text: "Scopes could not be loaded from the swagger definition. Try again after refreshing the login page.",
            type: 'error',
            dismissQueue: true,
            modal: true,
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10,
        });
        return;
    }
    var params = {
        username: $('#username').val(),
        password: $('#password').val(),
        grant_type: 'password',
        validity_period: '3600',
        remember_me : $("#rememberMe").is(':checked'),
        scopes: allAvailableScopes
    };
    var referrer = (document.referrer.indexOf("https") !== -1) ? document.referrer:null;
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

authManager.refresh = function (authzHeader) {
    var allAvailableScopes = retrieveScopes();
    if(allAvailableScopes === "") {
        noty({
            text: "Scopes could not be loaded from the swagger definition. Try again after refreshing the login page.",
            type: 'error',
            dismissQueue: true,
            modal: true,
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10,
        });
        return;
    }
    var params = {
        grant_type: 'refresh_token',
        validity_period: '3600',
        scopes: allAvailableScopes
    };
    var referrer = (document.referrer.indexOf("https") !== -1) ? document.referrer:null;
    var url = contextPath + '/auth/apis/login/token';
    return $.ajax({
        type: 'POST',
        url: url,
        async: false,
        data: params,
        traditional:true,
        headers: {
            'Authorization': authzHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer
        }
    });

};
authManager.logout = function (authzHeader) {
    var url = contextPath + '/auth/apis/login/logout';
    return $.ajax({
        type: 'POST',
        url: url,
        headers: {
            'Accept': 'application/json',
            'Authorization': authzHeader
        }
    });
};

var doLogout = function () {
    var logoutPromise = authManager.logout(bearer + getCookie("WSO2_AM_TOKEN_1"));
    logoutPromise.then(function (data, status, xhr) {
        delete_cookie("WSO2_AM_TOKEN_1");
        window.location.href = contextPath + "/auth/login";
    });
    logoutPromise.error(
        function (error) {
            var message = "Error while logging out";
            noty({
                text: message,
                type: 'error',
                dismissQueue: true,
                modal: true,
                progressBar: true,
                timeout: 5000,
                layout: 'top',
                theme: 'relax',
                maxVisible: 10
            });

        }
    );

};

var getCookie = function(name) {
    var value = "; " + document.cookie; // append ; to use the same splitting logic for first cookie as well. Then
    // we can get the cookie value that is after "; {name}=" and before next ";"
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
};

function delete_cookie(name) {
    document.cookie = name +'=; Path=' + contextPath + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
};

/*
 *  This function reads rest api definition and stores in localStorage
 */
authManager.loadSwaggerJson = function(async) {
    // Retrieve the object from storage
    var swaggerJson = null;
    if(swaggerURL.indexOf("store") > -1) {
        swaggerId = 'storeSwaggerJson';
    } else if(swaggerURL.indexOf("publisher") > -1 || swaggerURL.indexOf("editor") > -1){
        swaggerId = 'publisherSwaggerJson';
    } else if (swaggerURL.indexOf("admin") > -1) {
        swaggerId = 'adminSwaggerJson';
    }
    swaggerJson = localStorage.getItem(swaggerId);
    if (swaggerJson === null) {
        var request = new XMLHttpRequest();
        request.overrideMimeType("application/json");
        request.open('GET', swaggerURL, async);
        request.onreadystatechange = function() {
            if (request.readyState == 4 && request.status == 200) {
                // Put the request.responseText into storage
                localStorage.setItem(swaggerId, request.responseText);
                // Required use of an anonymous callback as .open will NOT return a value but simply returns
                // undefined in asynchronous mode
            } else if (request.status !== 200) {
                console.warn('warning: SwaggerJson could not be loaded for scope validation.');
            }
        };
        request.send(null);
    }
};

/*
 * Helper method to Extracts scopes from the swagger definition
 */
function retrieveScopes() {
    if (localStorage.getItem(swaggerId) === null) {
        authManager.loadSwaggerJson(false); //synchronous call
    }
    return extractScopesFromSwagger();
}
/*
 * Extracts scopes from the swagger definition
 * @return {string} scopes separated by space
 */
function extractScopesFromSwagger() {
    var scopes = '';
    var swaggerJson = JSON.parse(localStorage.getItem(swaggerId));
    if (swaggerJson !== null) {
        var paths = swaggerJson["paths"];
        for (var path in paths) {
            var resource = paths[path];
            for (var attr in resource) {
                if (paths[path][attr]["x-scope"] !== undefined) {
                    if (scopes.indexOf(paths[path][attr]["x-scope"]) === -1) {
                        scopes = scopes + " " + paths[path][attr]["x-scope"];
                    }
                }
            }
        }
    }
    return scopes;
}
