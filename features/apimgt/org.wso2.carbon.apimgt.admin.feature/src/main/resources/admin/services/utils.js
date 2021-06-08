/* eslint-disable*/
// Disable eslint check since this is only used in jaggeryjs codes
/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = require('/site/public/conf/settings.js').AppConfig.app;
var utils = Packages.org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Get the loopback (localhost) origin (scheme + hostname + port), This origin is used for making
 * internal(within the web app node), API calls. For example DCR call, Token generation, User Info, Token Introspect,
 * Revoke etc.
 */
var getLoopbackOrigin = function() {
    var mgtTransportPort = utils.getCarbonTransportPort("https"); // This is the actual server port (management) , Not the proxy port
    var origin = 'https://' + app.origin.host + ":" + mgtTransportPort;
    return origin; // Unless there is a port offset this is https://localhost:9443
};

function getIDPOrigin() {
    return utils.getExternalIDPOrigin();
}

function getIDPCheckSessionEndpoint() {
    return utils.getExternalIDPCheckSessionEndpoint();
}

/* 
Deciding what to process as app context.
If the setting.js has the following definition
( case 1 ) - appContext is '/publisher'
context: '/publisher',
( case 2 ) - appContext is still '/publisher'
context: '/publisher'
proxy_context_path: '/apim',
*/
var getAppContextForServerUrl = function () {
    var appContext = app.context;
    var proxyContextPath = app.proxy_context_path;
    if (proxyContextPath !== null && proxyContextPath !== '') {
        appContext = appContext.replace(proxyContextPath, '');
    }
    return appContext;
}