/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This backendServiceInvoker contains the wrappers for back end jaggary calls.
 */
var backendServiceInvoker = function () {

    var log = new Log("js/backend-service-invoker.js")
    var publicXMLHTTPInvokers = {};
    var privateMethods = {};
    var publicWSInvokers = {};
    var publicHTTPClientInvokers = {};
    var constants = require("constants.js");
    var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9+/=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/rn/g,"n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
    var analyticsConfig = Packages.org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
    var analyticsConfigInstance = analyticsConfig.getInstance();
    var authorizationHeader = null;

    if(analyticsConfigInstance.isAnalyticsEnabled()){
        authorizationHeader = constants.BASIC_PREFIX + Base64.encode(analyticsConfigInstance.getDasServerUser() + ":" + analyticsConfigInstance.getDasServerPassword());
    }

    /**
     * This method add Oauth authentication header to outgoing XMLHTTP Requests if Oauth authentication is enabled.
     * @param method HTTP request type.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param count a counter which hold the number of recursive execution
     */
     privateMethods.execute = function (method, url, successCallback, errorCallback, payload, count){
        var xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.open(method, url);
        xmlHttpRequest.setRequestHeader(constants.CONTENT_TYPE_IDENTIFIER, constants.APPLICATION_JSON);
        xmlHttpRequest.setRequestHeader(constants.ACCEPT_IDENTIFIER, constants.APPLICATION_JSON);

        if(authorizationHeader != null){
            xmlHttpRequest.setRequestHeader(constants.AUTHORIZATION_HEADER, authorizationHeader);
        }

        if (payload) {
            xmlHttpRequest.send(payload);
        } else {
            xmlHttpRequest.send();
        }

        if ((xmlHttpRequest.status >= 200 && xmlHttpRequest.status < 300) || xmlHttpRequest.status == 302) {
            if (xmlHttpRequest.responseText != null) {
                return successCallback(parse(xmlHttpRequest.responseText));
            } else {
                return successCallback({"statusCode": 200, "messageFromServer": "Operation Completed"});
            }
        } else if (xmlHttpRequest.status == 401) {
            return errorCallback(xmlHttpRequest);
        } else if (xmlHttpRequest.status == 500) {
            return errorCallback(xmlHttpRequest);
        } else {
            return errorCallback(xmlHttpRequest);
        }
    };

    /**
     * This method add Oauth authentication header to outgoing XMLHTTP Requests if Oauth authentication is enabled.
     * @param method HTTP request type.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    privateMethods.initiateXMLHTTPRequest = function (method, url, successCallback, errorCallback, payload) {
        return privateMethods.execute(method, url, successCallback, errorCallback, payload, 0);
    };

    /**
     * This method add Oauth authentication header to outgoing HTTPClient Requests if Oauth authentication is enabled.
     * @param method HTTP request type.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    privateMethods.initiateHTTPClientRequest = function (method, url, successCallback, errorCallback, payload) {
        var HttpClient = Packages.org.apache.commons.httpclient.HttpClient;
        var httpMethodObject;

        switch (method) {
            case constants.HTTP_POST:
                var PostMethod = Packages.org.apache.commons.httpclient.methods.PostMethod;
                httpMethodObject = new PostMethod(url);
                break;
            case constants.HTTP_PUT:
                var PutMethod = Packages.org.apache.commons.httpclient.methods.PutMethod;
                httpMethodObject = new PutMethod(url);
                break;
            case constants.HTTP_GET:
                var GetMethod = Packages.org.apache.commons.httpclient.methods.GetMethod;
                httpMethodObject = new GetMethod(url);
                break;
            case constants.HTTP_DELETE:
                var DeleteMethod = Packages.org.apache.commons.httpclient.methods.DeleteMethod;
                httpMethodObject = new DeleteMethod(url);
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP request type: " + method);
        }
        var Header = Packages.org.apache.commons.httpclient.Header;
        var header = new Header();
        header.setName(constants.CONTENT_TYPE_IDENTIFIER);
        header.setValue(constants.APPLICATION_JSON);
        httpMethodObject.addRequestHeader(header);
        header = new Header();
        header.setName(constants.ACCEPT_IDENTIFIER);
        header.setValue(constants.APPLICATION_JSON);
        httpMethodObject.addRequestHeader(header);
        
        var stringRequestEntity = new StringRequestEntity(stringify(payload));
        httpMethodObject.setRequestEntity(stringRequestEntity);
        var client = new HttpClient();
        try {
            client.executeMethod(httpMethodObject);
            var status = httpMethodObject.getStatusCode();
            if (status == 200) {
                return successCallback(httpMethodObject.getResponseBody());
            } else {
                return errorCallback(httpMethodObject.getResponseBody());
            }
        } catch (e) {
            return errorCallback(response);
        } finally {
            method.releaseConnection();
        }
    };

    /**
     * This method invokes return initiateXMLHttpRequest for get calls
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicXMLHTTPInvokers.get = function (url, successCallback, errorCallback) {
        return privateMethods.initiateXMLHTTPRequest("GET", url, successCallback, errorCallback);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for post calls
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicXMLHTTPInvokers.post = function (url, payload, successCallback, errorCallback) {
        return privateMethods.initiateXMLHTTPRequest("POST", url, successCallback, errorCallback, payload);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for put calls
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicXMLHTTPInvokers.put = function (url, payload, successCallback, errorCallback) {
        return privateMethods.initiateXMLHTTPRequest(constants.HTTP_PUT, url, successCallback, errorCallback, payload);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for delete calls
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicXMLHTTPInvokers.delete = function (url, successCallback, errorCallback) {
        return privateMethods.initiateXMLHTTPRequest(constants.HTTP_DELETE, url, successCallback, errorCallback);
    };


    /**
     * This method invokes return initiateHTTPClientRequest for get calls
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicHTTPClientInvokers.get = function (url, successCallback, errorCallback) {
        return privateMethods.initiateHTTPClientRequest(constants.HTTP_GET, url, successCallback, errorCallback);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for post calls
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicHTTPClientInvokers.post = function (url, payload, successCallback, errorCallback) {
        return privateMethods.
            initiateHTTPClientRequest(constants.HTTP_POST, url, successCallback, errorCallback, payload);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for put calls
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicHTTPClientInvokers.put = function (url, payload, successCallback, errorCallback) {
        return privateMethods.initiateHTTPClientRequest(constants.HTTP_PUT, url, successCallback, errorCallback, payload);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for delete calls
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     */
    publicHTTPClientInvokers.delete = function (url, successCallback, errorCallback) {
        return privateMethods.initiateHTTPClientRequest(constants.HTTP_DELETE, url, successCallback, errorCallback);
    };

    var publicInvokers = {};
    publicInvokers.XMLHttp = publicXMLHTTPInvokers;
    publicInvokers.WS = publicWSInvokers;
    publicInvokers.HttpClient = publicHTTPClientInvokers;
    return publicInvokers;
}();
