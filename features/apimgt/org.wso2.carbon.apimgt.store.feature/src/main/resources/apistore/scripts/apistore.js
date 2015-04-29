/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var apistore = {};

(function (apistore) {

	var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
    var log = new Log("jaggery-modules.api-manager.store");

    function StoreAPIProxy (username){
        this.username = username;
        this.impl = APIManagerFactory.getInstance().getAPIConsumer(username);
    }

    apistore.instance = function(username){
        return new StoreAPIProxy(username);
    };

    StoreAPIProxy.prototype.getAllTags = function(){
        return this.impl.getAllTags();
    };

    StoreAPIProxy.prototype.getTagsWithAttributes = function(){
        return this.impl.getTagsWithAttributes();
    };

    StoreAPIProxy.prototype.getRecentlyAddedAPIs = function(limit){
        return this.impl.getRecentlyAddedAPIs(limit);
    };

    StoreAPIProxy.prototype.getPublishedAPIsByProvider = function(providerId, limit){
        return this.impl.getPublishedAPIsByProvider(providerId, limit);
    };

    StoreAPIProxy.prototype.getSubscriptions = function(providerName, apiName, version, user){
        return this.impl.getSubscriptions(providerName, apiName, version, user);
    };

    StoreAPIProxy.prototype.getAllSubscriptions = function(userName, appName, startSubIndex, endSubIndex){
        return this.impl.getAllSubscriptions(userName, appName, startSubIndex, endSubIndex);
    };

    StoreAPIProxy.prototype.getApplications = function(userName){
        return this.impl.getApplications(userName);
    };

    StoreAPIProxy.prototype.getSwaggerResource = function(){
        return this.impl.getSwaggerResource();
    };

    StoreAPIProxy.prototype.getDeniedTiers = function(){
        return this.impl.getDeniedTiers();
    };

    StoreAPIProxy.prototype.getSubscriptionsByApplication = function(applicationName, userName){
        return this.impl.getSubscriptionsByApplication(applicationName,userName);
    };

    StoreAPIProxy.prototype.getPaginatedAPIsWithTag = function(tag, start, end){
        return this.impl.getPaginatedAPIsWithTag(tag, start, end);
    };

    StoreAPIProxy.prototype.addApplication = function (appName, userName, tier, callbackUrl, description) {
        return this.impl.addApplication(appName, userName, tier, callbackUrl, description);
    };

    StoreAPIProxy.prototype.getAllPaginatedAPIsByStatus = function (tenantDomain, start, end,apiStatus) {
        return this.impl.getAllPaginatedAPIsByStatus(tenantDomain, start, end,apiStatus);
    };
    
    StoreAPIProxy.prototype.createApplicationKeys = function (userId, applicationName, tokenType, tokenScope) {
        return this.impl.createApplicationKeys(userId, applicationName, tokenType, tokenScope);
    };

    StoreAPIProxy.prototype.addSubscription = function (providerName, apiName,version, tier, applicationId, userId) {
        return this.impl.addSubscription(providerName, apiName,version, tier, applicationId, userId);
    };

})(apistore);

