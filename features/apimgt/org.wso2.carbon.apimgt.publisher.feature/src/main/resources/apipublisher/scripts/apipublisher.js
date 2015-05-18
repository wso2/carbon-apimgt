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
        var apiObj = new Packages.org.json.simple.JSONObject();
        apiObj.put("provider", api.provider);
        apiObj.put("name", api.apiName);
        apiObj.put("version", api.version);
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
        apiObj.put("thumbnailUrl", api.thumbnailUrl);
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
        var defaultVersion = this.getDefaultVersion(identifier);
        var hasDefaultVersion = (defaultVersion != null);
        var api;
        try {
            result = this.impl.getAPI(identifier);
            if (log.isDebugEnabled()) {
                log.debug("getAPI : " + stringify(result));
            }
            api = {
                name: result.get('name'),
                version: result.get('version'),
                description: result.get('description'),
                endpoint: result.get('name'),
                wsdl: result.get('wsdlUrl'),
                tags: result.get('tags'),
                availableTiers: result.get('tiers'),
                status: result.get('status'),
                thumb: result.get('thumbnailUrl'),
                context: result.get('context'),
                lastUpdated: result.get('lastUpdatedTime'),
                subs: result.get('subscribersCount'),
                templates: result.get('name'),
                sandbox: result.get('sandboxUrl'),
                tierDescs: result.get('tierDescriptions'),
                bizOwner: result.get('businessOwner'),
                bizOwnerMail: result.get('businessOwnerMail'),
                techOwner: result.get('techOwner'),
                techOwnerMail: result.get('techOwnerMail'),
                wadl: result.get('wadlUrl'),
                visibility: result.get('visibility'),
                roles: result.get('visibleRoles'),
                tenants: result.get('visibleTenants'),
                epUsername: result.get('UTUsername'),
                epPassword: result.get('UTPassword'),
                endpointTypeSecured: result.get('isEndpointSecured'),
                provider: result.get('provider'),
                transport_http: result.get('httpTransport'),
                transport_https: result.get('httpsTransport'),
                apiStores: result.get('externalAPIStores'),
                inSequence: result.get('insequence'),
                outSequence: result.get('outsequence'),
                subscriptionAvailability: result.get('subscriptionAvailability'),
                subscriptionTenants: result.get('subscriptionAvailableTenants'),
                endpointConfig: result.get('endpointConfig'),
                responseCache: result.get('responseCache'),
                cacheTimeout: result.get('cacheTimeout'),
                availableTiersDisplayNames: result.get('tierDislayNames'),
                faultSequence: result.get('faultsequence'),
                destinationStats: result.get('destinationStatsEnabled'),
                resources: result.get('apiResources'),
                scopes: result.get('scopes'),
                isDefaultVersion: result.get('defaultVersion'),
                implementation: result.get('implementation'),
                environments: result.get('publishedEnvironments'),
                hasDefaultVersion: hasDefaultVersion,
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
        var availableTiers = this.impl.getTiers(tenantDomain);
        return JSON.parse(availableTiers);
    };

    APIProviderProxy.prototype.getSubscriberAPIs = function (subscriberName) {
        return this.impl.getSubscriberAPIs(subscriberName);
    };

    APIProviderProxy.prototype.checkIfAPIExists = function (apiProvider, apiName, apiVersion) {
        return this.impl.checkIfAPIExists(apiProvider, apiName, apiVersion);
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

    APIProviderProxy.prototype.getSwagger12Resource = function (apiProvider, apiName, apiVersion) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(apiProvider, apiName, apiVersion);
        return JSON.parse(this.impl.getSwagger12Definition(identifier));
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

    APIProviderProxy.prototype.isContextExist = function (context,oldContext) {
        var exists, log = new Log();
        try {
            exists = this.impl.isContextExist(context);
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
        var result, log = new Log();
        try {
            result = this.impl.isURLValid(type,url);
            if (log.isDebugEnabled()) {
                log.debug("Invoke isURLValid" );
            }
            return {
                error:false,
                response:result
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    APIProviderProxy.prototype.validateRoles = function(roles,username) {
        var validRole, log = new Log();
        try {
            validRole = this.impl.validateRoles(roles,username);
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
                log.debug("hasPublishPermission method " );
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
})(apipublisher);

