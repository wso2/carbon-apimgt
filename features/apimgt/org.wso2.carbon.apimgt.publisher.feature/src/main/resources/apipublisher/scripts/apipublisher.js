/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

//TODO add proper introduction to the module
var apipublisher = {};

(function (apipublisher) {

    //Defining constant fields
    var API_PROVIDER = "provider";
    var API_NAME = "name";
    var API_VERSION = "version";

    var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
    var log = new Log("jaggery-modules.api-manager.publisher");

    function APIProviderProxy(username) {
        this.username = username;
        this.impl = APIManagerFactory.getInstance().getAPIProvider(this.username);
    }

    apipublisher.instance = function(username){
        return new APIProviderProxy(username);
    };

    APIProviderProxy.prototype.getAllProviders = function () {
        return this.impl.getAllProviders();
    };
    APIProviderProxy.prototype.designAPI = function (api) {
        return this.impl.designAPI(api);
    };
    APIProviderProxy.prototype.implementAPI = function (api) {
        return this.impl.implementAPI(api);
    };
    APIProviderProxy.prototype.manageAPI = function (api) {
        return this.impl.manageAPI(api);
    };
    APIProviderProxy.prototype.updateDesignAPI = function (APIProvider, api) {
        return this.impl.updateDesignAPI(api);
    };
    APIProviderProxy.prototype.addDocumentation = function (api, document) {
        return this.impl.addDocumentation(api);
    };
    APIProviderProxy.prototype.addInlineContent = function (api, docName, content) {
        return this.impl.addInlineContent(api, docName, content);
    };
    APIProviderProxy.prototype.createNewAPIVersion = function (api, newVersion) {
        return this.impl.createNewAPIVersion(api, newVersion);
    };
    APIProviderProxy.prototype.getAllAPIUsageByProvider = function (providerName) {
        return this.impl.getAllAPIUsageByProvider(providerName);
    };
    APIProviderProxy.prototype.getSubscribersOfAPI = function (apiId) {
        return this.impl.getSubscribersOfAPI(apiId);
    };
    APIProviderProxy.prototype.getAPIsByProvider = function (providerName) {
        return this.impl.getAPIsByProvider(providerName);
    };
    APIProviderProxy.prototype.getSubscribersOfAPI = function (apiId) {
        return this.impl.getSubscribersOfAPI(apiId);
    };
    APIProviderProxy.prototype.getDefaultVersion = function (apiId) {
        return this.impl.getDefaultVersion(apiId);
    };
    APIProviderProxy.prototype.getCustomFaultSequences = function () {
        return this.impl.getCustomFaultSequences();
    };
    APIProviderProxy.prototype.getCustomInSequences = function () {
        return this.impl.getCustomInSequences();
    };
    APIProviderProxy.prototype.getCustomOutSequences = function () {
        return this.impl.getCustomOutSequences();
    };
    APIProviderProxy.prototype.updateSubscription = function (apiId, status, appId) {
        return this.impl.updateSubscription(apiId, status, appId);
    };
    APIProviderProxy.prototype.removeDocumentation = function (apiId, docName, docType) {
        return this.impl.removeDocumentation(apiId, docName, docType);
    };

    /**
     * Delete a API
     * @param apiProvider name of the api provider
     * @param apiName name of the api to be removed
     * @param version the version of the api to be removed
     * @returns {boolean} whether successfully removed or not
     */
    APIProviderProxy.prototype.deleteAPI = function (apiProvider, apiName, apiVersion) {
        var identifier = new Packages.org.json.simple.JSONObject();
        identifier.put(API_PROVIDER, apiProvider);
        identifier.put(API_NAME, apiName);
        identifier.put(API_VERSION, apiVersion);
        return this.impl.deleteAPI(identifier);
    };
    APIProviderProxy.prototype.getAPI = function (apiId) {
        return this.impl.getAPI(apiId);
    };
    APIProviderProxy.prototype.getAllDocumentation = function (apiId) {
        return this.impl.getAllDocumentation(apiId);
    };
    APIProviderProxy.prototype.getAllDocumentation = function (apiId) {
        return this.impl.getAllDocumentation(apiId);
    };
    APIProviderProxy.prototype.getInlineContent = function (apiId, docName) {
        return this.impl.getDocumentationContent(apiId, docName);
    };
    APIProviderProxy.prototype.getTiers = function (tenantDomain) {
        return this.impl.getTiers(tenantDomain);
    };
    APIProviderProxy.prototype.getSubscriberAPIs = function (subscriberName) {
        return this.impl.getSubscriberAPIs(subscriberName);
    };
})(apipublisher);

