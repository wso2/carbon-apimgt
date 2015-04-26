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
        var apiObj = new Packages.org.json.simple.JSONObject();
        apiObj.put("provider", api.provider);
        apiObj.put("context", api.context);
        apiObj.put("name", api.name);
        apiObj.put("version", api.version);
        return this.impl.designAPI(apiObj);
    };
    APIProviderProxy.prototype.implementAPI = function (api) {
        return this.impl.implementAPI(api);
    };
    APIProviderProxy.prototype.manageAPI = function (api) {
        return this.impl.manageAPI(api);
    };
    APIProviderProxy.prototype.updateDesignAPI = function (api) {
             var apiObj = new Packages.org.json.simple.JSONObject();
             apiObj.put("provider", api.provider);
             apiObj.put("context", api.context);
             apiObj.put("name", api.name);
             apiObj.put("version", api.version);
             apiObj.put("description", api.description);
             apiObj.put("tags", api.tags);
             apiObj.put("visibility", api.visibility);
             apiObj.put("visibleRoles", api.visibility);
             apiObj.put("swagger", api.swagger);
             apiObj.put("techOwner", api.techOwner);
             apiObj.put("techOwnerEmail", api.techOwnerEmail);
             apiObj.put("bizOwner", api.bizOwner);
             apiObj.put("bizOwnerEmail", api.bizOwnerEmail);
        return this.impl.updateDesignAPI(apiObj);
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
    APIProviderProxy.prototype.getAPI = function (apiProvider, apiName, apiVersion) {
        var identifier = new Packages.org.json.simple.JSONObject();
        identifier.put(API_PROVIDER, apiProvider);
        identifier.put(API_NAME, apiName);
        identifier.put(API_VERSION, apiVersion);
        defaultVersion= this.getDefaultVersion(identifier);
        hasDefaultVersion=(defaultVersion!=null);
        try {
            result = this.impl.getAPI(apiName, apiProvider, apiVersion);
            if (log.isDebugEnabled()) {
                log.debug("getAPI : " + stringify(result));
            }

            api = {
                name:result[0],
                version:result[4],
                description:result[1],
                endpoint:result[2],
                wsdl:result[3],
                tags:result[5],
                availableTiers:result[6],
                status:result[7],
                thumb:result[8],
                context:result[9],
                lastUpdated:result[10],
                subs:result[11],
                templates:result[12],
                sandbox:result[13],
                tierDescs:result[14],
                bizOwner:result[15],
                bizOwnerMail:result[16],
                techOwner:result[17],
                techOwnerMail:result[18],
                wadl:result[19],
                visibility:result[20],
                roles:result[21],
                tenants:result[22],
                epUsername:result[23],
                epPassword:result[24],
                endpointTypeSecured:result[25],
                provider:result[26],
                transport_http:result[27],
                transport_https:result[28],
                apiStores:result[29],
                inSequence:result[30],
                outSequence:result[31],
                subscriptionAvailability:result[32],
                subscriptionTenants:result[33],
                endpointConfig:result[34],
                responseCache:result[35],
                cacheTimeout:result[36],
                availableTiersDisplayNames:result[37],
                faultSequence:result[38],
                destinationStats:result[39],
                resources:result[40],
                scopes:result[41],
                isDefaultVersion:result[42],
                implementation:result[43],
                environments:result[44],
                hasDefaultVersion:hasDefaultVersion,
                currentDefaultVersion: defaultVersion
            };
            return {
                error:false,
                api:api
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                api:null,
                message:e.message.split(":")[1]
            };
        }
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
        var tierSet = this.impl.getTiers(tenantDomain);
        var tiers = new Array();
        for(var i = 0; i < tierSet.length; i++) {
            tiers[i].tierName = tierSet[i].getName();
            tiers[i].tierDisplayName = tierSet[i].getDisplayName();
            tiers[i].tierDescription = t != null ? tierSet[i].getDescription() : "";
        }
        return tiers;
    };
    APIProviderProxy.prototype.getSubscriberAPIs = function (subscriberName) {
        return this.impl.getSubscriberAPIs(subscriberName);
    };
    APIProviderProxy.prototype.checkIfAPIExists = function (apiProvider, apiName, apiVersion) {       
        return this.impl.checkIfAPIExists(apiProvider, apiName, apiVersion);
    };
})(apipublisher);

