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


    apipublisher.getAPIProvider = function (username){
    var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
    };

    apipublisher.getAllProviders = function (APIProvider) {
    return APIProvider.getAllProviders();
    };

    apipublisher.designAPI = function (APIProvider,api) {
        return APIProvider.designAPI(api);
    };
    apipublisher.implementAPI = function (APIProvider,api) {
        return APIProvider.implementAPI(api);
    };
    apipublisher.manageAPI = function (APIProvider,api) {
        return APIProvider.manageAPI(api);
    };
    apipublisher.updateDesignAPI = function (APIProvider,api) {
        return APIProvider.updateDesignAPI(api);
    };
    apipublisher.addDocumentation = function (APIProvider,api,document) {
        return APIProvider.addDocumentation(api);
    };
    apipublisher.addInlineContent = function (APIProvider,api,docName,content) {
        return APIProvider.addInlineContent(api,docName,content);
    };
    apipublisher.createNewAPIVersion = function (APIProvider,api,newVersion) {
        return APIProvider.createNewAPIVersion(api,newVersion);
    };
    apipublisher.getAllAPIUsageByProvider = function (APIProvider,providerName) {
        return APIProvider.getAllAPIUsageByProvider(providerName);
    };
    apipublisher.getSubscribersOfAPI = function (APIProvider,apiId) {
        return APIProvider.getSubscribersOfAPI(apiId);
    };
    apipublisher.getAPIsByProvider = function (APIProvider,providerName) {
        return APIProvider.getAPIsByProvider(providerName);
    };
    apipublisher.getSubscribersOfAPI = function (APIProvider,apiId) {
        return APIProvider.getSubscribersOfAPI(apiId);
    };
    apipublisher.getDefaultVersion = function (APIProvider,apiId) {
        return APIProvider.getDefaultVersion(apiId);
    };
    apipublisher.getCustomFaultSequences = function (APIProvider) {
        return APIProvider.getCustomFaultSequences();
    };
    apipublisher.getCustomInSequences = function (APIProvider) {
        return APIProvider.getCustomInSequences();
    };
    apipublisher.getCustomOutSequences = function (APIProvider) {
        return APIProvider.getCustomOutSequences();
    };
    apipublisher.updateSubscription = function (APIProvider,apiId,status,appId) {
        return APIProvider.updateSubscription(apiId,status,appId);
    };
    apipublisher.removeDocumentation = function (APIProvider,apiId,docName,docType) {
        return APIProvider.removeDocumentation(apiId,docName,docType);
    };
    apipublisher.deleteAPI = function (APIProvider,apiId) {
        return APIProvider.deleteAPI(apiId);
    };
    apipublisher.getAPI = function (APIProvider,apiId) {
        return APIProvider.getAPI(apiId);
    };
    apipublisher.getAllDocumentation = function (APIProvider,apiId) {
        return APIProvider.getAllDocumentation(apiId);
    };
    apipublisher.getAllDocumentation = function (APIProvider,apiId) {
        return APIProvider.getAllDocumentation(apiId);
    };
    apipublisher.getInlineContent = function (APIProvider,apiId,docName) {
        return APIProvider.getDocumentationContent(apiId,docName);
    };
    apipublisher.getTiers = function (APIProvider,tenantDomain) {
        return APIProvider.getTiers(tenantDomain);
    };
    apipublisher.getSubscriberAPIs = function (APIProvider,subscriberName) {
        return APIProvider.getSubscriberAPIs(subscriberName);
    };


})(apipublisher);

