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
    if(authManager.getAuthStatus()){
        route.routTo(loginRedirectUri);
    }
    var doLogin = function(){
        var loginPromise = authManager.login();
        loginPromise.then(function(data,status,xhr){
            authManager.setAuthStatus(true);
            authManager.setUserName('admin');//data.user.username;
            authManager.setUserScope(data.scope);//data.user.role;
            /*$.cookie('token', data.access_token, { path: '/' });
            $.cookie('user', 'admin', { path: '/' });
            $.cookie('userScope', data.scope, { path: '/' });*/
            var redirectUri = (xhr.getResponseHeader("Referer") == '' || !xhr.getResponseHeader("Referer")) ? contextPath + loginRedirectUri : xhr.getResponseHeader("Referer");
            window.location = redirectUri;
        });
    };
    $('#loginForm').on('keydown','input.form-control',function(e){
        if(e.keyCode == 13){
            doLogin();
        }
    });
    $('#loginButton').click(doLogin);
});
