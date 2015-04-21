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

    var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
    var log = new Log("jaggery-modules.api-manager.publisher");

    function APIProviderProxy(username) {
        this.username = username;
        this.impl = APIManagerFactory.getInstance().getAPIProvider(this.username);
    }

    apipublisher.getAllProviders = function () {
        return this.impl.getAllProviders();
    };
    apipublisher.designAPI = function (api) {
        return this.impl.designAPI(api);
    };
    apipublisher.implementAPI = function (api) {
        return this.impl.implementAPI(api);
    };
    apipublisher.manageAPI = function (api) {
        return this.impl.manageAPI(api);
    };
    apipublisher.updateDesignAPI = function (APIProvider, api) {
        return this.impl.updateDesignAPI(api);
    };
    apipublisher.addDocumentation = function (api, document) {
        return this.impl.addDocumentation(api);
    };
    apipublisher.addInlineContent = function (api, docName, content) {
        return this.impl.addInlineContent(api, docName, content);
    };
    apipublisher.createNewAPIVersion = function (api, newVersion) {
        return this.impl.createNewAPIVersion(api, newVersion);
    };
    apipublisher.getAllAPIUsageByProvider = function (providerName) {
        return this.impl.getAllAPIUsageByProvider(providerName);
    };
    apipublisher.getSubscribersOfAPI = function (apiId) {
        return this.impl.getSubscribersOfAPI(apiId);
    };
    apipublisher.getAPIsByProvider = function (providerName) {
        return this.impl.getAPIsByProvider(providerName);
    };
    apipublisher.getSubscribersOfAPI = function (apiId) {
        return this.impl.getSubscribersOfAPI(apiId);
    };
    apipublisher.getDefaultVersion = function (apiId) {
        return this.impl.getDefaultVersion(apiId);
    };
    apipublisher.getCustomFaultSequences = function () {
        return this.impl.getCustomFaultSequences();
    };
    apipublisher.getCustomInSequences = function () {
        return this.impl.getCustomInSequences();
    };
    apipublisher.getCustomOutSequences = function () {
        return this.impl.getCustomOutSequences();
    };
    apipublisher.updateSubscription = function (apiId, status, appId) {
        return this.impl.updateSubscription(apiId, status, appId);
    };
    apipublisher.removeDocumentation = function (apiId, docName, docType) {
        return this.impl.removeDocumentation(apiId, docName, docType);
    };
    apipublisher.deleteAPI = function (apiId) {
        return this.impl.deleteAPI(apiId);
    };
    apipublisher.getAPI = function (apiId) {
        return this.impl.getAPI(apiId);
    };
    apipublisher.getAllDocumentation = function (apiId) {
        return this.impl.getAllDocumentation(apiId);
    };
    apipublisher.getAllDocumentation = function (apiId) {
        return this.impl.getAllDocumentation(apiId);
    };
    apipublisher.getInlineContent = function (apiId, docName) {
        return this.impl.getDocumentationContent(apiId, docName);
    };
    apipublisher.getTiers = function (tenantDomain) {
        return this.impl.getTiers(tenantDomain);
    };
    apipublisher.getSubscriberAPIs = function (subscriberName) {
        return this.impl.getSubscriberAPIs(subscriberName);
    };
})(apipublisher);

