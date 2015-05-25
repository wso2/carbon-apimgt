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
    var APISubscriber = Packages.org.wso2.carbon.apimgt.api.model.Subscriber;
    var APIIdentifier = Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier;
    var APIUtil = Packages.org.wso2.carbon.apimgt.impl.utils.APIUtil;
    var Application = Packages.org.wso2.carbon.apimgt.api.model.Application;
    var Date = Packages.java.util.Date;
    var log = new Log("jaggery-modules.api-manager.store");

    function StoreAPIProxy(username) {
        this.username = username;
        this.impl = APIManagerFactory.getInstance().getAPIConsumer(username);
    }

    apistore.instance = function (username) {
        return new StoreAPIProxy(username);
    };

    StoreAPIProxy.prototype.getAllTags = function () {
        return this.impl.getAllTags();
    };

    StoreAPIProxy.prototype.getTagsWithAttributes = function () {
        return this.impl.getTagsWithAttributes();
    };

    StoreAPIProxy.prototype.getRecentlyAddedAPIs = function (limit) {
        return this.impl.getRecentlyAddedAPIs(limit);
    };

    StoreAPIProxy.prototype.getPublishedAPIsByProvider = function (providerId, limit) {
        return this.impl.getPublishedAPIsByProvider(providerId, limit);
    };

    StoreAPIProxy.prototype.getSubscriptions = function (providerName, apiName, version, user) {
        return this.impl.getSubscriptions(providerName, apiName, version, user);
    };

    StoreAPIProxy.prototype.getAllSubscriptions = function (userName, appName, startSubIndex, endSubIndex) {
        return this.impl.getAllSubscriptions(userName, appName, startSubIndex, endSubIndex);
    };

    StoreAPIProxy.prototype.getApplications = function (userName) {
        return this.impl.getApplications(userName);
    };

    StoreAPIProxy.prototype.getSwaggerResource = function () {
        return this.impl.getSwaggerResource();
    };

    StoreAPIProxy.prototype.getDeniedTiers = function () {
        return this.impl.getDeniedTiers();
    };

    StoreAPIProxy.prototype.getSubscriptionsByApplication = function (applicationName, userName) {
        return this.impl.getSubscriptionsByApplication(applicationName, userName);
    };

    StoreAPIProxy.prototype.getPaginatedAPIsWithTag = function (tag, start, end) {
        return this.impl.getPaginatedAPIsWithTag(tag, start, end);
    };

    StoreAPIProxy.prototype.addApplication = function (appName, userName, tier, callbackUrl, description) {
        return this.impl.addApplication(appName, userName, tier, callbackUrl, description);
    };

    /*
    * This function update the application according to the given arguments.
    */
    StoreAPIProxy.prototype.updateApplication = function (appName, userName, appId, tier, callbackUrl, description) {
        var subscriber = new APISubscriber(userName);
        var application = new Application(appName, subscriber);
        application.setId(appId);
        application.setTier(tier);
        application.setCallbackUrl(callbackUrl);
        application.setDescription(description);
        return this.impl.updateApplication(application);
    };

    /*
     * This function delete the application according to the arguments.
     */
    StoreAPIProxy.prototype.removeApplication = function (appName, userName, appId) {
        var subscriber = new APISubscriber(userName);
        var application = new Application(appName, subscriber);
        application.setId(appId);
        return this.impl.removeApplication(application);
    };

    StoreAPIProxy.prototype.getAllPaginatedAPIsByStatus = function (tenantDomain, start, end, apiStatus) {
        return this.impl.getAllPaginatedAPIsByStatus(tenantDomain, start, end, apiStatus);
    };

    StoreAPIProxy.prototype.getApplicationKey = function (userId, applicationName, tokenType, tokenScopes,
                                                          validityPeriod, callbackUrl, accessAllowDomains) {
        var arr = new Packages.org.json.simple.JSONArray();
        var domains = accessAllowDomains.split(",");
        for (var index = 0; index < domains.length; index++) {
            arr.add(domains[index]);
        }
        return this.impl.getApplicationKey(userId, applicationName, tokenType,
            tokenScopes, validityPeriod, callbackUrl, arr);
    };

    StoreAPIProxy.prototype.getSubscriber = function (userName) {
        return this.impl.getSubscriber(userName);
    };

    StoreAPIProxy.prototype.addSubscriber = function (userName, tenantId) {
        var subscriber = new APISubscriber(userName);
        subscriber.setSubscribedDate(new Date());
        subscriber.setEmail("");
        subscriber.setTenantId(tenantId);
        return this.impl.addSubscriber(subscriber);
    };

    StoreAPIProxy.prototype.getAPISubscriptions = function (provider, apiname, version, username) {
        return this.impl.getSubscriptions(provider, apiname, version, username);
    };

    StoreAPIProxy.prototype.getAPI = function (provider, name, version) {
        var identifier = new Packages.org.json.simple.JSONObject();
        identifier.put("provider", provider);
        identifier.put("name", name);
        identifier.put("version", version);
        return this.impl.getAPI(identifier);
    };

    StoreAPIProxy.prototype.addSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(provider);
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.addSubscription(apiIdentifier, appId, user);
    };

    StoreAPIProxy.prototype.getRefreshToken = function (userId, applicationName, requestedScopes,
                                                        oldAccessToken, accessAllowDomainsArr,
                                                        consumerKey, consumerSecret, validityTime) {
        var arr = new Packages.org.json.simple.JSONArray();
        var domains = accessAllowDomainsArr.split(",");
        for (var index = 0; index < domains.length; index++) {
            arr.add(domains[index]);
        }
        return this.impl.getRefreshToken(userId, applicationName, requestedScopes, oldAccessToken,
            arr, consumerKey, consumerSecret, validityTime);
    };

    //removeSubscription(APIIdentifier identifier, String userId, int applicationId)
    StoreAPIProxy.prototype.removeSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(provider);
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.removeSubscription(apiIdentifier, user, appId);
    };

})(apistore);

