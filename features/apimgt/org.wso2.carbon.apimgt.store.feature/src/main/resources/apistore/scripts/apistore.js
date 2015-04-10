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
var apistore = {};

(function (apistore) {

	var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
    var log = new Log("jaggery-modules.api-manager.store");

    apistore.getAPIConsumer = function (username){
		return APIManagerFactory.getInstance().getAPIConsumer(username);
	};

    apistore.getAllTags(){
    	return this.getAPIConsumer.getAllTags();
    };

    apistore.getTagsWithAttributes(){
    	return this.getAPIConsumer.getTagsWithAttributes();
    };

    apistore.getRecentlyAddedAPIs(limit){
    	return this.getAPIConsumer.getRecentlyAddedAPIs(limit);
    };

    apistore.getPublishedAPIsByProvider(providerId, limit){
    	return this.getAPIConsumer.getPublishedAPIsByProvider(providerId, limit);
    };

    apistore.getSubscriptions(providerName, apiName, version, user){
    	return this.getAPIConsumer.getSubscriptions(providerName, apiName, version, user);
    };

    apistore.getAllSubscriptions(userName, appName, startSubIndex, endSubIndex){
    	return this.getAPIConsumer.getAllSubscriptions(userName, appName, startSubIndex, endSubIndex);
    };

    apistore.getApplications(userName){
    	return this.getAPIConsumer.getApplications(userName);
    };

    apistore.getSwaggerResource(){
    	return this.getAPIConsumer.getSwaggerResource();
    };

    apistore.getDeniedTiers(){
    	return this.getAPIConsumer.getDeniedTiers();
    };

    apistore.getSubscriptionsByApplication(applicationName, userName){
    	return this.getAPIConsumer.getSubscriptionsByApplication(applicationName,userName);
    };

    apistore.getPaginatedAPIsWithTag(tag, start, end){
    	return this.getAPIConsumer.getPaginatedAPIsWithTag();
    };


})(apistore);

