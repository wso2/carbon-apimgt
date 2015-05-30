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
    var APISubscriber = Packages.org.wso2.carbon.apimgt.api.model.Subscriber;
    var APIIdentifier = Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier;
    var API= Packages.org.wso2.carbon.apimgt.api.model.API;
    var Application=Packages.org.wso2.carbon.apimgt.api.model.Applications;
    var APIUtil = Packages.org.wso2.carbon.apimgt.impl.utils.APIUtil;
    var Date = Packages.java.util.Date;
    var Tier= Packages.org.wso2.carbon.apimgt.api.model.Tier;
    var URITemplate= Packages.org.wso2.carbon.apimgt.api.model.URITemplate;

    var Set=Packages.java.util.Set;
    var List=Packages.java.util.List;
    var ArrayList=Packages.java.util.ArrayList;
    var Iterator=Packages.java.util.Iterator;
    var String=Packages.java.lang.String;
    var Object=Packages.java.lang.Object;
    var Map=Packages.java.util.Map;
    var Long=Packages.java.lang.Long;
    var HashMap=Packages.java.util.HashMap;
    var JSONArray=Packages.org.json.simple.JSONArray;

    var DateFormat=Packages.java.text.DateFormat;
    var SimpleDateFormat=Packages.java.text.SimpleDateFormat;

    var log = new Log("jaggery-modules.api-manager.publisher");
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

    APIProviderProxy.prototype.createAPI = function (api) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(api.provider, api.name, api.version);
        var uuid;
        try {
            uuid = this.impl.createAPI(identifier, api.context);
            if (log.isDebugEnabled()) {
                log.debug("Error while creating API" );
            }
            return {
                error:false,
                uuid:uuid
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.implementAPI = function (api) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(api.provider, api.apiName, api.version);
        var apiOb = new Packages.org.wso2.carbon.apimgt.api.model.API(identifier);

        apiObj.put("implementation_type", api.implementation_type);
        apiObj.put("wsdl", api.wsdl);
        apiObj.put("wadl", api.wadl);
        apiObj.put("endpointSecured", api.endpointSecured);
        apiObj.put("endpointUTUsername", api.endpointUTUsername);
        apiObj.put("endpointUTPassword", api.endpointUTPassword);
        apiObj.put("endpoint_config", api.endpoint_config);
        apiObj.put("destinationStats", api.destinationStats);
        apiObj.put("swagger", api.swagger);
        apiObj.put("wadl", api.wadl);
        return this.impl.implementAPI(apiObj);
    };

    APIProviderProxy.prototype.manageAPI = function (api) {
        return this.impl.manageAPI(api);
    };

    APIProviderProxy.prototype.updateDesignAPI = function (api) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(api.provider, api.name, api.version);
        var apiOb = new Packages.org.wso2.carbon.apimgt.api.model.API(identifier);
        apiOb.setContext(api.context);
        apiOb.setDescription(api.description);
        apiOb.setVisibility(api.visibility);
        apiOb.setVisibleRoles(api.visibleRoles);
        apiOb.setThumbnailUrl(api.thumbnailUrl);
        return this.impl.updateAPIDesign(apiOb,  api.tags, api.swagger);
    };

    APIProviderProxy.prototype.addDocumentation = function (api, document) {
        return this.impl.addDocumentation(api);
    };

    APIProviderProxy.prototype.addInlineContent = function (api, docName, content) {
        return this.impl.addInlineContent(api, docName, content);
    };

    APIProviderProxy.prototype.createNewAPIVersion = function (api, newVersion) {
        var apiObj = new Packages.org.json.simple.JSONObject();
        apiObj.put("provider", api.provider);
        apiObj.put("version", api.version);
        apiObj.put("name", api.name);
        apiObj.put("defaultVersion", api.defaultVersion);

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

    /*
     * This method returns the subscription details needed for api-subscriptions page.
     */
    APIProviderProxy.prototype.getSubscribersOfAPI = function (provider, name, version) {
        var apiObj = new Packages.org.json.simple.JSONObject();
        apiObj.put("provider", provider);
        apiObj.put("name", name);
        apiObj.put("version", version);
        return this.impl.getSubscribersOfAPI(apiObj);
    };

    /*
     * This method returns the UUID of an artifact
     */
    APIProviderProxy.prototype.getUUIDByApi = function (provider, name, version) {
        return this.impl.getUUIDByApi(provider, name, version);
    };

    APIProviderProxy.prototype.getDefaultVersion = function (apiId) {
        return this.impl.getDefaultVersion(apiId);
    };

    APIProviderProxy.prototype.getCustomFaultSequences = function () {
        var sequences;
        try {
            sequences = JSON.parse(this.impl.getCustomFaultSequences());
            if (log.isDebugEnabled()) {
                log.debug("getCustomInSequences " +  " : " + sequences);
            }

            return {
                error:false,
                sequences:sequences
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                sequences:null
            };
        }
    };

    APIProviderProxy.prototype.getCustomInSequences = function () {
        var sequences;
        try {
            sequences = JSON.parse(this.impl.getCustomInSequences());
            if (log.isDebugEnabled()) {
                log.debug("getCustomInSequences " +  " : " + sequences);
            }

            return {
                error:false,
                sequences:sequences
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                sequences:null
            };
        }
    };

    APIProviderProxy.prototype.getCustomOutSequences = function () {
        var sequences;
        try {
            sequences = JSON.parse(this.impl.getCustomOutSequences());
            if (log.isDebugEnabled()) {
                log.debug("getCustomOutSequences " +  " : " + sequences);
            }

            return {
                error:false,
                sequences:sequences
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                sequences:null
            };
        }
    };

    APIProviderProxy.prototype.getEnvironments = function () {
        var environments;
        try {
            environments = JSON.parse(this.impl.getEnvironments());
            if (log.isDebugEnabled()) {
                log.debug("getCustomOutSequences " +  " : " + sequences);
            }

            return {
                error:false,
                environments:environments
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                environments:null
            };
        }
    };

    /*
     * This method is used to update the application wise and user wise subscription status
     */
    APIProviderProxy.prototype.updateSubscription = function (apiProvider, apiName, apiVersion, appId, status) {
        var identifier = new Packages.org.json.simple.JSONObject();
        identifier.put(API_PROVIDER, apiProvider);
        identifier.put(API_NAME, apiName);
        identifier.put(API_VERSION, apiVersion);
        return this.impl.updateSubscription(identifier, status, appId);
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
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(apiProvider, apiName, apiVersion);
        var success, log = new Log();
        try {
            success = result = this.impl.deleteAPI(identifier);
            if (log.isDebugEnabled()) {
                log.debug("Error while deleting the API : " + apiName + " : " + exists);
            }
            return {
                error:false,
                success:success
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.getAPI = function (apiProvider, apiName, apiVersion) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(apiProvider, apiName, apiVersion);
        var defaultVersion = this.getDefaultVersion(identifier);
        var hasDefaultVersion = (defaultVersion != null);
        var apiOb;
        try {
            api = this.impl.getAPI(identifier);
            var subscriberCount = this.impl.getSubscriberCount(identifier);
            var tiers = api.getTierSetAsArray();
            var tierSet = '';
            var tiersDisplayNamesSet = '';
            var tiersDescSet = '';
            for(var i = 0; i < tiers.length  ; i++) {
                tierSet += tiers[0].getName();
                tiersDisplayNamesSet += tiers[0].getDisplayName();
                tiersDescSet += tiers[0].getDescription();
                if (i != tierSet.length - 1) {
                    tierSet += ',';
                    tiersDisplayNamesSet += ',';
                    tiersDescSet += ',';
                }
            }

            apiOb = {
                name: api.getId().getApiName(),
                description: api.getDescription(),
                url: api.getUrl(),
                wsdl: api.getWsdlUrl(),
                version: api.getId().getVersion(),
                tags: api.getTagSetAsString(),
                availableTiers: tierSet,
                status: api.getStatus().toString(),
                thumb: APIUtil.getWebContextRoot(api.getThumbnailUrl()),
                context: api.getContext(),
                lastUpdated: Long.valueOf(api.getLastUpdated().getTime()).toString(),
                subs: subscriberCount,
                sandbox: api.getSandboxUrl(),
                tierDescs:tiersDescSet,
                bizOwner: api.getBusinessOwner(),
                bizOwnerMail: api.getBusinessOwnerEmail(),
                techOwner: api.getTechnicalOwner(),
                techOwnerMail: api.getTechnicalOwnerEmail(),
                wadl: api.getWadlUrl(),
                visibility: api.getVisibility(),
                roles: api.getVisibleRoles(),
                tenants: api.getVisibleTenants(),
                epUsername: api.getEndpointUTUsername(),
                epPassword: api.getEndpointUTPassword(),
                endpointTypeSecured: api.isEndpointSecured(),
                provider: APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                transport_http: APIUtil.checkTransport("http", api.getTransports()),
                transport_https: APIUtil.checkTransport("https", api.getTransports()),
                inSequence: api.getInSequence(),
                outSequence: api.getOutSequence(),
                subscriptionAvailability: api.getSubscriptionAvailability(),
                subscriptionTenants: api.getSubscriptionAvailableTenants(),
                endpointConfig: api.getEndpointConfig(),
                responseCache: api.getResponseCache(),
                cacheTimeout: api.getCacheTimeout(),
                availableTiersDisplayNames: tiersDisplayNamesSet,
                faultSequence: api.getFaultSequence(),
                destinationStats: api.getDestinationStatsEnabled(),
                isDefaultVersion: api.isDefaultVersion(),
                implementation: api.getImplementation(),
                hasDefaultVersion: hasDefaultVersion,
                currentDefaultVersion: defaultVersion
            };
            return {
                error:false,
                api:apiOb
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
        var availableTiers = this.impl.getTiers(tenantDomain);
        return JSON.parse(availableTiers);
    };

    APIProviderProxy.prototype.getSubscriberAPIs = function (subscriberName) {
        return this.impl.getSubscriberAPIs(subscriberName);
    };

    APIProviderProxy.prototype.checkIfAPIExists = function (apiProvider, apiName, apiVersion) {

        var exists;
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(apiProvider, apiName, apiVersion);
        try {
            exists = this.impl.checkIfAPIExists(identifier);
            if (log.isDebugEnabled()) {
                log.debug("Error while checking api exist for : " + apiName + " : " + exists);
            }
            return {
                error:false,
                exist:exists
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.isSynapseGateway = function () {
        var result;
        try {
            result = this.impl.isSynapseGateway();
            if (log.isDebugEnabled()) {
                log.debug("Invoke getExternalAPIStores()" );
            }
            return {
                error:false,
                isSynapseGateway:result
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.getSwagger20Definition = function (apiProvider, apiName, apiVersion) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(apiProvider, apiName, apiVersion);
        var data;
        try {
            data = JSON.parse(this.impl.getSwagger20Definition(identifier));
            if (log.isDebugEnabled()) {
                log.debug("Error while getting swagger20 resource" );
            }
            return {
                error:false,
                data:data
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.isMultipleTenantsAvailable = function(){
        try {
            result = this.impl.isMultipleTenantsAvailable();
            if (log.isDebugEnabled()) {
                log.debug("Invoke isMultipleTenantsAvailable()" );
            }
            return {
                error:false,
                status:result
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:true,
                message:e
            };
        }
    };

    APIProviderProxy.prototype.manageAPI = function (api) {
        var success;
        var log = new Log();
        try {
            var apiJson = new Packages.org.json.simple.JSONObject();
            apiJson.put("name", api.apiName);
            apiJson.put("provider", api.provider);
            apiJson.put("version", api.version);
            apiJson.put("context", api.context);
            apiJson.put("defaultVersion", api.defaultVersion);
            apiJson.put("swagger", api.swagger);
            apiJson.put("tier", api.tier);
            apiJson.put("inSequence", api.inSequence);
            apiJson.put("outSequence", api.outSequence);
            apiJson.put("responseCache", api.responseCache);
            apiJson.put("subscriptionAvailability", api.subscriptionAvailability);
            apiJson.put("subscriptionTenants", api.subscriptionTenants);
            apiJson.put("bizOwner", api.bizOwner);
            apiJson.put("bizOwnerMail", api.bizOwnerMail);
            apiJson.put("techOwner", api.techOwner);
            apiJson.put("techOwnerMail", api.techOwnerMail);
            apiJson.put("faultSequence", api.faultSequence);
            apiJson.put("cacheTimeout", api.cacheTimeout);
            apiJson.put("destinationStats", api.destinationStats);
            apiJson.put("environments", api.environments);
            success = this.impl.manageAPI(apiJson);
            // log.info("=============================================");
            if (log.isDebugEnabled()) {
                log.debug("manageAPI : " + api.name + "-" + api.version);
            }
            if(success){
                var failedToPublishEnvironments = JSON.parse(success).PUBLISHED;
                var failedToUnPublishEnvironments = JSON.parse(success).UNPUBLISHED;
                if(failedToPublishEnvironments == "" && failedToUnPublishEnvironments == ""){
                    return {
                        error:false
                    };
                }else{
                    return {
                        error:true,
                        message:success + '||warning'
                    };
                }
            }else{
                return {
                    error:true
                }; }
        } catch (e) {
            log.error(e);
            return {
                error:true,
                message:e.message.split(":")[1]
            };
        }
    };

    APIProviderProxy.prototype.isApiNameExist = function (apiName) {
        var exists, log = new Log();
        try {
            exists = result = this.impl.isApiNameExist(apiName);
            if (log.isDebugEnabled()) {
                log.debug("isApiName exist for : " + apiName + " : " + exists);
            }
            return {
                error:false,
                exist:exists
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.isContextExist = function (context, oldContext) {
        var exists, log = new Log();
        if(context == oldContext) {
            return {
                error:false,
                exist:true
            };
        }

        try {
            exists = this.impl.isDuplicateContextTemplate(context);
            if (log.isDebugEnabled()) {
                log.debug("isContext exist for : " + context + " : " + exists);
            }
            return {
                error:false,
                exist:exists
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.isURLValid = function (type,url) {
        var result, valid = false, log = new Log();
        try {
            result = APIUtil.isURLValid(type,url);
            if(result == 'success') {
                valid = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("Invoke isURLValid" );
            }
            return {
                error:false,
                response:result,
                valid : valid
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.validateRoles = function(roles) {

        var validRole, log = new Log();
        var roleList ;
        if(roles != null && !roles == '') {
            roleList = roles.split(',');
        } else {
            return {
                error:true,
                valid: false,
                errorMsg : 'Please provide non empty roles set to valid'
            };
        }

        try {
            validRole = this.impl.validateRoles(roleList);
            if (log.isDebugEnabled()) {
                log.debug("Invoke validateRoles function.");
            }
            return {
                error:false,
                valid: validRole
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.updateAPIStatus = function (api) {
        var log = new Log();
        try {
            var apiData = new Packages.org.json.simple.JSONObject();
            apiData.put("provider", api.provider);
            apiData.put("apiName", api.apiName);
            apiData.put("version", api.version);
            apiData.put("status", api.status);
            apiData.put("publishToGateway", api.publishToGateway);
            apiData.put("deprecateOldVersions", api.deprecateOldVersions);
            apiData.put("makeKeysForwardCompatible", api.makeKeysForwardCompatible);
            var success = this.impl.updateAPIStatus(apiData);
            if (log.isDebugEnabled()) {
                log.debug("updateAPIStatus : " + api.name + "-" + api.version);
            }

            if (!success) {
                return {
                    error:true
                };
            } else {
                var failedToPublishEnvironments = JSON.parse(success).PUBLISHED;
                var failedToUnPublishEnvironments = JSON.parse(success).UNPUBLISHED;
                if(failedToPublishEnvironments == "" && failedToUnPublishEnvironments == ""){
                    return {
                        error:false
                    };
                }else{
                    return {
                        error:true,
                        message:success + '||warning'
                    };
                }
            }

        } catch (e) {
            log.error(e.message);
            return {
                error:true,
                message:e.message.split(":")[1]

            };
        }
    };

    APIProviderProxy.prototype.hasPublishPermission = function () {
        var success, log = new Log();
        try {
            success = this.impl.hasPublishPermission();
            if (log.isDebugEnabled()) {
                log.debug("check whether user has publisher permission" );
            }
            if(success){
                return {
                    error:false,
                    permitted:success
                };
            }else{
                return {
                    error:true,
                    permitted:success
                }; }
        } catch (e) {
            log.error(e.message);
            return {
                error:true,
                permitted:false
            };
        }
    };

    APIProviderProxy.prototype.isScopeExist = function (scope, tenantId) {
        var result, log = new Log();
        try {
            result = this.impl.isScopeExist(scope, tenantId);
            if (log.isDebugEnabled()) {
                log.debug("Invoke isScopeExist()" );
            }
            return {
                error:false,
                isScopeExist:result
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };
})(apipublisher);

