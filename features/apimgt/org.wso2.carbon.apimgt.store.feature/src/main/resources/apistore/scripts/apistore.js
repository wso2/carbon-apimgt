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
    var API= Packages.org.wso2.carbon.apimgt.api.model.API;    
    var Date = Packages.java.util.Date;
    var Tier= Packages.org.wso2.carbon.apimgt.api.model.Tier;
    var URITemplate= Packages.org.wso2.carbon.apimgt.api.model.URITemplate;

    var Set=Packages.java.util.Set;
    var HashSet=Packages.java.util.HashSet;
    var List=Packages.java.util.List;
    var ArrayList=Packages.java.util.ArrayList;
    var Iterator=Packages.java.util.Iterator;
    var String=Packages.java.lang.String;
    var Object=Packages.java.lang.Object;
    var Map=Packages.java.util.Map;
    var HashMap=Packages.java.util.HashMap;
    var JSONArray=Packages.org.json.simple.JSONArray;

    var DateFormat=Packages.java.text.DateFormat;
    var SimpleDateFormat=Packages.java.text.SimpleDateFormat;

    var TierSet=new HashSet();
    var uriTemplates=new HashSet();
    var attributes=new HashMap();
    var log = new Log("jaggery-modules.api-manager.store");

    function StoreAPIProxy(username) {
        this.username = username;
        this.impl = APIManagerFactory.getInstance().getAPIConsumer(username);
    }

    apistore.instance = function (username) {
        return new StoreAPIProxy(username);
   
    };

    StoreAPIProxy.prototype.getAllSubscriptions = function (userName, appName, startSubIndex, endSubIndex) {
        var result=this.impl.getAllSubscriptions(userName, appName, startSubIndex, endSubIndex,null);
        return new APIUtil().stringifyAPISubscriptions(result);
    };

    StoreAPIProxy.prototype.getApplications = function (userName) {
        var resultArray = new Packages.org.json.simple.JSONArray();
        //var applications=new Application[];
        var subscriber = new APISubscriber(userName);
        var applications=this.impl.getApplications(subscriber,null);
        if (applications) {
            for (var i=0;i<applications.length;i++) {
                var subsCount=this.impl.getSubscriptionCount(subscriber,applications[i].getName(),null);
                var row = new Packages.org.json.simple.JSONObject();
                row.put("name", applications[i].getName());
                row.put("tier", applications[i].getTier());
                row.put("id", applications[i].getId());
                row.put("callbackUrl", applications[i].getCallbackUrl());
                row.put("status", applications[i].getStatus());
                row.put("description", applications[i].getDescription());
                row.put("apiCount", subsCount);
                resultArray.add(row);
            }
        }
        return resultArray;
    };

    StoreAPIProxy.prototype.getDeniedTiers = function () {
        var tiers=new Set();
        tiers= this.impl.getDeniedTiers();
        var deniedTiers = new Packages.org.json.simple.JSONArray();
        for (var i=0;i<tiers.size();i++) {
            var row = new Packages.org.json.simple.JSONObject();
            row.put("tierName", tiers[i]);
            deniedTiers.add(row);
        }
        return deniedTiers;
    };


    StoreAPIProxy.prototype.addApplication = function (appName, userName, tier, callbackUrl, description) {
        var subscriber = new APISubscriber(username);
        var application = new Application(appName, subscriber);
        application.setTier(tier);
        application.setCallbackUrl(callbackUrl);
        application.setDescription(description);
        var groupId="";
        if (groupId != null) {
            application.setGroupId(groupId);
        }
        return this.impl.addApplication(application,userName);
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

    /*
     * This function returns fresh(new) tokens to my subscriptions page.
     */
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
        //TO-DO- The second parameter [group id] need to be added later
        return this.impl.addSubscriber(subscriber,null);
    };

    StoreAPIProxy.prototype.getAPISubscriptions = function (provider, apiname, version, username) {
        return this.impl.getSubscriptions(provider, apiname, version, username);
    };

    StoreAPIProxy.prototype.getAPI = function (provider, name, version) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(provider,name,version);
        API=this.impl.getAPI(identifier);
        var myn= new Packages.org.json.simple.JSONArray();
        //JSONfy the result
        if (API) {
            var row = new Packages.org.json.simple.JSONObject();
            APIIdentifier = API.getId();
            row.put("name", APIIdentifier.getApiName());
            row.put("provider", APIUtil.replaceEmailDomainBack(APIIdentifier.getProviderName()));
            row.put("version", APIIdentifier.getVersion());
            row.put("description", API.getDescription());
            row.put("rates", API.getRating());
            row.put("endpoint", API.getUrl());
            row.put("wsdl", API.getWsdlUrl());
            row.put("wadl", API.getWadlUrl());
            var dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z");
            var dateFormatted = dateFormat.format(API.getLastUpdated());
            row.put("updatedDate", dateFormatted);
            row.put("context", API.getContext());
            row.put("status", API.getStatus().getStatus());
            row.put("serverURL", getEnvironmentsOfAPI(api).toJSONString());
            var tierArr = new Packages.org.json.simple.JSONArray();
            TierSet = API.getAvailableTiers();
            if (TierSet) {
                Iterator =TierSet.iterator();
                while (it.hasNext()) {
                    var tierObj = new Packages.org.json.simple.JSONObject();
                    var tierObject = Packages.org.wso2.carbon.apimgt.api.model.Tier;
                    tierObject=it.next();
                    var tier = tierObject;
                    tierObj.put("tierName", tier.getName());
                    tierObj.put("tierDisplayName", tier.getDisplayName());
                    tierObj.put("tierDescription", tier.getDescription() != null ? tier.getDescription() : "");
                    if (tier.getTierAttributes()) {
                        attributes = tier.getTierAttributes();
                        var attributesList = "";
                        for (var j=0;j<attributes.entrySet().size();j++) {
                            attributesList += attribute.getKey() + "::" + attribute.getValue() + ",";

                        }
                        tierObj.put("tierAttributes", tierObj, attributesList);
                    }
                    tierArr.add(tierObj);


                }
            }
            row.put("tiers", tierArr);
            row.put("subscribed", isSubscribed);
            row.put("bizOwner", API.getBusinessOwner());
            row.put("bizOwnerMail", API.getBusinessOwnerEmail());
            row.put("techOwner", API.getTechnicalOwner());
            row.put("techOwnerMail", API.getTechnicalOwnerEmail());
            row.put("visibility", API.getVisibility());
            row.put("visibleRoles", API.getVisibleRoles());

            uriTemplates = API.getUriTemplates();
            var uriTemplatesArr = new ArrayList();
            if (uriTemplates.size() != 0) {
                var uriTempArr = new Packages.org.json.simple.JSONArray(uriTemplates.size());
                var i = uriTemplates.iterator();

                while (i.hasNext()) {
                    var utArr = new ArrayList();
                    var ut = Packages.org.wso2.carbon.apimgt.api.model.URITemplate;
                    ut=i.next();
                    utArr.add(ut.getUriTemplate());
                    utArr.add(ut.getMethodsAsString().replaceAll("\\s", ","));
                    utArr.add(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                    utArr.add(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                    var  utNArr = new Packages.org.json.simple.JSONArray(utArr.size());
                    for (var p = 0; p < utArr.size(); p++) {
                        utNArr.put(p, utNArr, utArr.get(p));
                    }
                    uriTemplatesArr.add(utNArr);
                }

                for (var c = 0; c < uriTemplatesArr.size(); c++) {
                    uriTempArr.add(uriTemplatesArr.get(c));
                }

                myn.add(1, uriTempArr);
            }
            row.put("uriTemplates", uriTemplatesArr.toString());
            var apiOwner = API.getApiOwner();
            if (!apiOwner) {
                apiOwner = APIUtil.replaceEmailDomainBack(APIIdentifier.getProviderName());
            }
            row.put("apiOwner", apiOwner);
            row.put("isAdvertiseOnly", API.isAdvertiseOnly());
            row.put("redirectURL", API.getRedirectURL());

            row.put("subscriptionAvailability", API.getSubscriptionAvailability());
            row.put("subscriptionAvailableTenants", API.getSubscriptionAvailableTenants());
            row.put("isDefaultVersion",API.isDefaultVersion());
            row.put("transports",API.getTransports());

            myn.add(0, row);
        }
        return myn;

    };

    StoreAPIProxy.prototype.addSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(provider);
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.addSubscription(apiIdentifier, appId, user);
    };

    /*
     * This function returns the refresh tokens to my subscriptions page.
     */
    StoreAPIProxy.prototype.getRefreshToken = function (userId, applicationName, requestedScopes,
                                                        oldAccessToken, accessAllowDomainsArr,
                                                        consumerKey, consumerSecret, validityTime) {

        var response = Packages.org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
        var arr = new Packages.org.json.simple.JSONArray();
        var domains = accessAllowDomainsArr.split(",");
        for (var index = 0; index < domains.length; index++) {
            arr.add(domains[index]);
        }

        var scopesArr = new Packages.org.json.simple.JSONArray();
        var scopes = requestedScopes.split(",");
        for (var index = 0; index < scopes.length; index++) {
            scopesArr.add(scopes[index]);
        }
        response= this.impl.renewAccessToken(oldAccessToken,consumerKey,consumerSecret,validityTime,arr,scopesArr,null);
        var resultJson = new Packages.org.json.simple.JSONObject();
        resultJson.put("accessToken", response.getAccessToken());
        resultJson.put("consumerKey", response.getConsumerKey());
        resultJson.put("consumerSecret", response.getConsumerKey());
        resultJson.put("validityTime", response.getValidityPeriod());
        resultJson.put("responseParams", response.getJSONString());
        resultJson.put("tokenScope", response.getScopes());
        resultJson.put("enableRegenarate", response.isRegenarateOptionEnabled);
        return resultJson;

    };

    /*
     * This function remove the subscription for the application.
     */
    StoreAPIProxy.prototype.removeSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(provider);
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.removeSubscription(apiIdentifier, user, appId);
    };

    /*
     * This function update the allowed domains by splitting the accessAllowDomains by ','.
     */
    StoreAPIProxy.prototype.updateAccessAllowDomains = function (accessToken, accessAllowDomains) {
        var domains = accessAllowDomains.split(",");
        return this.impl.updateAccessAllowDomains(accessToken, domains);
    };

})(apistore);

