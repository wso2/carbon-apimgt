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
$(function(){
    if (location.protocol != 'https:') {
        window.location = loginPageUri;
    }
    if(authManager.getAuthStatus()){
        route.routTo(loginRedirectUri);
    }
    var async = true;
    authManager.loadSwaggerJson(async);
    var doLogin = function(){
        var loginPromise = authManager.login();
        loginPromise.then(function(data,status,xhr){
            authManager.setAuthStatus(true);
            authManager.setUserName(data.authUser);//data.user.username;
            authManager.setUserScope(data.scopes);//data.user.role;
            var expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
            window.localStorage.setItem("expiresIn", expiresIn);
            window.localStorage.setItem("user", data.authUser);
            window.localStorage.setItem("rememberMe", $("#rememberMe").is(':checked'));
            if(data.scopes !== null) {
                window.localStorage.setItem("userScopes", data.scopes.split(" "));
            }
            /*$.cookie('token', data.access_token, { path: '/' });
            $.cookie('user', 'admin', { path: '/' });
            $.cookie('userScope', data.scope, { path: '/' });*/
            var redirectUri = (xhr.getResponseHeader("Referer") == '' || !xhr.getResponseHeader("Referer") || xhr.getResponseHeader("Referer") == "null") ? contextPath + loginRedirectUri : xhr.getResponseHeader("Referer");
            window.location = redirectUri;
        });
        loginPromise.error(
            function (error) {
                /*var element = $("#general-alerts").find('.alert-danger');
                element.find('.alert-message').html(error.responseText);
                element.fadeIn('slow');*/
                var error_data = JSON.parse(error.responseText);
                var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message ;
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
                });

            }
        );
    };
    $('#loginForm').on('keydown','input.form-control',function(e){
        if(e.keyCode == 13){
            doLogin();
        }
    });
    $('#loginButton').click(doLogin);
});
