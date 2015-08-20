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
    var APIConstants = Packages.org.wso2.carbon.apimgt.impl.APIConstants;
    var Application = Packages.org.wso2.carbon.apimgt.api.model.Application;
    var API= Packages.org.wso2.carbon.apimgt.api.model.API;
    var Date = Packages.java.util.Date;
    var Tier= Packages.org.wso2.carbon.apimgt.api.model.Tier;
    var URITemplate= Packages.org.wso2.carbon.apimgt.api.model.URITemplate;

    var JSONValue=Packages.org.json.simple.JSONValue;
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

    function appendDomainByUser(user){
        
        var username = user.username;
        var domain = user.domain;
        var carbon = require('carbon');
        if(domain == null || domain == 'null'){
            domain = carbon.server.tenantDomain();
        }
        
        if(username == '__wso2.am.anon__'){

        }else{
           var superTenantDomain = user.superTenantDomain;
           if(superTenantDomain == domain || (user.username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR) > -1  ||  user.username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT) > -1)){
            username = APIUtil.replaceEmailDomainBack(username);
            } else{
                username = username + APIConstants.EMAIL_DOMAIN_SEPARATOR+domain;
            } 
        }
        return username;
    }

    StoreAPIProxy.prototype.appendDomainToUser = function (username){
        var tenantUser = username;
        if(username == '__wso2.am.anon__'){

        }else{
            var domain = this.user.domain;
            if(domain == null || domain == 'null'){
                var carbon = require('carbon');
                domain = carbon.server.tenantDomain();
            }
            var superTenantDomain = this.user.superTenantDomain;
            if(superTenantDomain == domain || (username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR) > -1  ||  username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT) > -1)){
                tenantUser = APIUtil.replaceEmailDomainBack(username);
            }else{
                tenantUser = username + APIConstants.EMAIL_DOMAIN_SEPARATOR+domain;
            }
        }
        

        return tenantUser;
    }

    function StoreAPIProxy(user) {
        this.user = user;
        var userNameWithDomain = appendDomainByUser(user);
        this.username = userNameWithDomain;
        this.impl = APIManagerFactory.getInstance().getAPIConsumer(userNameWithDomain);
    }

    apistore.instance = function (user) {
        return new StoreAPIProxy(user);

    };

    StoreAPIProxy.prototype.getAllSubscriptions = function (userName, appName, startSubIndex, endSubIndex) {
        var result=this.impl.getAllSubscriptions(this.appendDomainToUser(userName), appName, startSubIndex, endSubIndex,null);
        return new APIUtil().stringifyAPISubscriptions(result);
    };

    StoreAPIProxy.prototype.getApplications = function (userName) {
        var resultArray = new Packages.org.json.simple.JSONArray();
        //var applications=new Application[];
        var subscriber = new APISubscriber(this.appendDomainToUser(userName));
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
        var tiers=new HashSet();
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
        var subscriber = new APISubscriber(this.appendDomainToUser(userName));
        var application = new Application(appName, subscriber);
        application.setTier(tier);
        application.setCallbackUrl(callbackUrl);
        application.setDescription(description);
        var groupId="";
        if (groupId != null) {
            application.setGroupId(groupId);
        }
        return this.impl.addApplication(application,this.appendDomainToUser(userName));
    };

    /*
     * This function update the application according to the given arguments.
     */
    StoreAPIProxy.prototype.updateApplication = function (appName, userName, appId, tier, callbackUrl, description) {
        var subscriber = new APISubscriber(this.appendDomainToUser(userName));
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
        var subscriber = new APISubscriber(this.appendDomainToUser(userName));
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
        var jsonParams={"username":this.appendDomainToUser(userId)};
        return this.impl.generateApplicationKey(this.appendDomainToUser(userId), applicationName, tokenType,
                                                tokenScopes, validityPeriod, callbackUrl, arr,stringify(jsonParams),null);
    };

    StoreAPIProxy.prototype.getSubscriber = function (userName) {
        return this.impl.getSubscriber(this.appendDomainToUser(userName));
    };

    StoreAPIProxy.prototype.addSubscriber = function (userName, tenantId) {
        var subscriber = new APISubscriber(this.appendDomainToUser(userName));
        subscriber.setSubscribedDate(new Date());
        subscriber.setEmail("");
        subscriber.setTenantId(tenantId);
        //TO-DO- The second parameter [group id] need to be added later
        return this.impl.addSubscriber(subscriber,null);
    };

    StoreAPIProxy.prototype.getAPISubscriptions = function (provider, apiname, version, username) {
        return this.impl.getSubscriptions(this.appendDomainToUser(provider), apiname, version, this.appendDomainToUser(username),null);
    };

    StoreAPIProxy.prototype.getAPI = function (provider, name, version) {
        var identifier = new Packages.org.wso2.carbon.apimgt.api.model.APIIdentifier(this.appendDomainToUser(provider), name, version);

        var apiOb;
        try {
            api = this.impl.getAPI(identifier);
            var tiers = api.getTierSetAsArray();
            var tierSet = '';
            var tiersDisplayNamesSet = '';
            var tiersDescSet = '';
            for(var i = 0; i < tiers.length  ; i++) {
                tierSet += tiers[i].getName();
                tiersDisplayNamesSet += tiers[i].getDisplayName();
                tiersDescSet += tiers[i].getDescription();
                if (i != tierSet.length - 1) {
                    tierSet += ',';
                    tiersDisplayNamesSet += ',';
                    tiersDescSet += ',';
                }
            }

            var uriTemplates = api.getUriTemplates();
            if (uriTemplates.size() != 0) {
                var uriTempArr = new Array();
                var iterator = uriTemplates.iterator();
                var uriTemplatesArr = new Array();
                while (iterator.hasNext()) {
                    var utArr = new Array();
                    var ut = iterator.next();
                    utArr.push(ut.getUriTemplate());
                    utArr.push(ut.getMethodsAsString().replaceAll("\\s", ","));
                    utArr.push(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                    utArr.push(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                    var utNArr = new Array();
                    for (var p = 0; p < utArr.length; p++) {
                        utNArr.push(utArr[p]);
                    }
                    uriTemplatesArr.push(utNArr);
                }

                for (var c = 0; c < uriTemplatesArr.length; c++) {
                    uriTempArr.push(uriTemplatesArr[c]);
                }
            }


            var dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z");
            var dateFormatted = dateFormat.format(api.getLastUpdated());

            apiOb = {
                name: api.getId().getApiName(),
                description: api.getDescription(),
                endpoint: api.getUrl(),
                wsdl: api.getWsdlUrl(),
                version: api.getId().getVersion(),
                tags: api.getTagSetAsString(),
                tiers: tierSet,
                status: api.getStatus().toString(),
                thumb: APIUtil.getWebContextRoot(api.getThumbnailUrl()),
                context: api.getContext(),
                updatedDate: dateFormatted,
                tierDescs:tiersDescSet,
                bizOwner: api.getBusinessOwner(),
                templates: uriTempArr,
                bizOwnerMail: api.getBusinessOwnerEmail(),
                techOwner: api.getTechnicalOwner(),
                techOwnerMail: api.getTechnicalOwnerEmail(),
                wadl: api.getWadlUrl(),
                visibility: api.getVisibility(),
                roles: api.getVisibleRoles(),
                tenants: api.getVisibleTenants(),
                provider: APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                transport_http: APIUtil.checkTransport("http", api.getTransports()),
                transport_https: APIUtil.checkTransport("https", api.getTransports()),
                responseCache: api.getResponseCache(),
                cacheTimeout: api.getCacheTimeout(),
                availableTiersDisplayNames: tiersDisplayNamesSet,
                isDefaultVersion: api.isDefaultVersion(),
                serverURL:new APIUtil().getEnvironmentsOfAPI(api)

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

    StoreAPIProxy.prototype.getTiers = function () {

        try {
            var availableTiers = this.impl.getTiers();
            var tierList = [];
            var tier;
            var sortedTierList = APIUtil.sortTiers(availableTiers);
            for (var i = 0 ; i < sortedTierList.size() ; i ++) {
                tier = sortedTierList.get(i);
                tierList.push({
                                  "tierName": tier.getName(),
                                  "tierDisplayName": tier.getDisplayName(),
                                  "tierDescription": tier.getDescription(),
                                  "defaultTier": 0
                              });
            }
            if (log.isDebugEnabled()) {
                log.debug("Invoke getTiers()" );
            }
            return {
                error:false,
                tiers:tierList
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    StoreAPIProxy.prototype.addSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(this.appendDomainToUser(provider));
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.addSubscription(apiIdentifier, this.appendDomainToUser(user), appId);
    };

    /*
     * This function returns the refresh tokens to my subscriptions page.
     */
    StoreAPIProxy.prototype.getRefreshToken = function (userId, applicationName, requestedScopes,
                                                        oldAccessToken, accessAllowDomainsArr,
                                                        consumerKey, consumerSecret, validityTime) {

        var response = Packages.org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
        response = this.impl.renewAccessToken(oldAccessToken,consumerKey,consumerSecret,validityTime,accessAllowDomainsArr,requestedScopes,null);
        var scopes="";
        for(var i=0;i<response.getScopes().length;i++){
            scopes += response.getScopes()[i]+" ";
        }
        var resultJson = new Packages.org.json.simple.JSONObject();
        resultJson.put("accessToken", response.getAccessToken());
        resultJson.put("consumerKey", consumerKey);
        resultJson.put("consumerSecret", consumerSecret);
        resultJson.put("validityTime", validityTime);
        resultJson.put("responseParams", response.getJSONString());
        resultJson.put("tokenScope", scopes);
        resultJson.put("allowedDomains", accessAllowDomainsArr);
        return resultJson;

    };

    /*
     * This function remove the subscription for the application.
     */
    StoreAPIProxy.prototype.removeSubscription = function (apiname, version, provider, user, tier, appId) {
        provider = APIUtil.replaceEmailDomain(this.appendDomainToUser(provider));
        var apiIdentifier = new APIIdentifier(provider, apiname, version);
        apiIdentifier.setTier(tier);
        return this.impl.removeSubscription(apiIdentifier, this.appendDomainToUser(user), appId);
    };

    /*
     * This function update the allowed domains by splitting the accessAllowDomains by ','.
     */
    StoreAPIProxy.prototype.updateAccessAllowDomains = function (accessToken, accessAllowDomains) {
        var domains = accessAllowDomains.split(",");
        try {
            return this.impl.updateAccessAllowDomains(accessToken, domains);

            if (log.isDebugEnabled()) {
                log.debug("Invoke updateAccessAllowDomains()" );
            }
            return {
                error:false,
                success:true
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e,
                success:false
            };
        }
    };

    /*
     * This method returns the UUID of an artifact
     */
    StoreAPIProxy.prototype.getUUIDByApi = function (provider, name, version) {
        return this.impl.getUUIDByApi(this.appendDomainToUser(provider), name, version);
    };

    /*
     * This method returns the UUID of an artifact
     */
    StoreAPIProxy.prototype.getAllDocumentation = function (provider, name, version, loggedInUser) {
        try {
            var documentList = [];
            var apiIdentifier = new APIIdentifier(this.appendDomainToUser(provider), name, version);
            var documents = this.impl.getAllDocumentation(apiIdentifier, loggedInUser);
            for (var i = 0 ; i < documents.size() ; i ++) {
                document = documents.get(i);
                var sourceTypes = {};
                var content, documentationType, otherTypeName, otherType = false;
                var sourceType = document.getSourceType().getType();
                if ('INLINE' == sourceType.toUpperCase()) {
                    sourceTypes.inline = true;
                    sourceTypes.url = false;
                    sourceTypes.file = false;
                    content = this.impl.getDocumentationContent(apiIdentifier, document.getName());
                } else if ('URL' == sourceType.toUpperCase()) {
                    sourceTypes.inline = false;
                    sourceTypes.url = true;
                    sourceTypes.file = false;
                } else {
                    sourceTypes.inline = false;
                    sourceTypes.url = false;
                    sourceTypes.file = true;
                }

                documentationType = document.getType().getType();

                if (documentationType == 'Other') {
                    otherType = true;
                    otherTypeName =  document.getOtherTypeName();
                }


                documentList.push({
                                  "name": document.getName(),
                                  "sourceType": sourceType,
                                  "summary": document.getSummary(),
                                  "content": content,
                                  "sourceTypes": sourceTypes,
                                  "sourceUrl": document.getSourceUrl(),
                                  "filePath": document.getFilePath(),
                                  "type": documentationType,
                                  "otherType": otherType,
                                  "otherTypeName": otherTypeName
                              });
            }
            if (log.isDebugEnabled()) {
                log.debug("Invoke getAllDocumentation()" );
            }
            return {
                error:false,
                documents:documentList
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };
 StoreAPIProxy.prototype.getSwaggerContent=function(providerVal,apiNameVal,apiVersionVal,envName){  
        providerVal = this.appendDomainToUser(providerVal);     

        var url = request.getRequestURL();
        var host = getLocation(url).host;
        log.info("hostttt"+host);
        var port = getLocation(url).port;
        var accessProtocol = getLocation(url).protocol;        
        var tenantDomain = "";
        var tenantID = -1234;
	    var isTenantFlowStarted = false;

        if(providerVal.indexOf("@") > -1){
            var MultitenantUtils = Packages.org.wso2.carbon.utils.multitenancy.MultitenantUtils;
	    tenantDomain = MultitenantUtils.getTenantDomain(providerVal);
            providerVal = providerVal.replace("@","-AT-");
            if(tenantDomain){
                tenantID = carbon.server.osgiService('org.wso2.carbon.user.core.service.RealmService').getTenantManager().getTenantId(tenantDomain);
            }
        }
	log.info("providerValll"+providerVal);
        if(providerVal.indexOf("-DOM-") > -1){
             providerVal = providerVal.replace("-DOM-","/");
        }

    	try {
    		//start tenant flow before fetching swagger resource from the registry
    		if (tenantDomain != "" && tenantDomain != 'carbon.super') {
    			var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;
    			isTenantFlowStarted = true;
    			PrivilegedCarbonContext.startTenantFlow();
    		        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    		}

            var APIUtil = Packages.org.wso2.carbon.apimgt.impl.utils.APIUtil;
            var apiUtil = new APIUtil();
            var swaggerPath = apiUtil.getSwagger20DefinitionFilePath(apiNameVal,apiVersionVal, providerVal);
    		var registry = carbon.server.osgiService('org.wso2.carbon.registry.core.service.RegistryService').getGovernanceUserRegistry("wso2.anonymous.user", tenantID);

    		url = swaggerPath + "swagger.json";

    		var data = registry.get(url);
                log.info("swagger contentttttt"+data);
    	}finally {
    		if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
    	}
        var output = new Packages.java.lang.String(data.content);
        var jsonObj = JSON.parse(output);
        log.info("swagger jsonnn contentttttt"+data);
        var basePathValue = jsonObj.basePath;
        var username=null;
        var apistore = new StoreAPIProxy(providerVal);    
        result =  apistore.getAPI(providerVal,
                                 apiNameVal,apiVersionVal);
        log.info("get "+result);
        var api = result.api;
        var serverURL = api.serverURL;
        log.info("api.serverURL "+api.serverURL);
        var context = api.context;
        log.info("api.contexttt "+api.context);
        var splitVal = JSON.parse(serverURL);
        var urlArr = new Array();
        var count = 0;
        for(var type in splitVal){
            var environmentsToType = splitVal[type];
            for(var name in environmentsToType ){
                var environmentURL;
                if(envName == name){
                    environmentURL = environmentsToType[name];
                    urlArr[count] = environmentURL[accessProtocol.replace(":","")];
                    count++;
                    break;
                }else{
                var environmentURL = environmentsToType[name];
                urlArr[count] = environmentURL[accessProtocol.replace(":","")];
                count++;
                }
           
            }
        }
        if(urlArr.length===1){
            // We do not need to append the version since with the context versioning, the version is embedded in the context.
            jsonObj.basePath=urlArr[0]+context;
            //jsonObj.basePath=urlArr[0]+context+"/"+apiVersionVal;
        }else{
            for(var i=0; i < urlArr.length ; i++){
                if(urlArr[i].indexOf(accessProtocol) > -1){
                    jsonObj.basePath=urlArr[i]+context;
                    // jsonObj.basePath=urlArr[i]+context+"/"+apiVersionVal;
                }
            }
        }

        jsonObj.basePath = api.context;

	//assign default value to host with first environment address
	if (urlArr.length > 0 ) {
	        jsonObj.host = urlArr[0].trim().replace(accessProtocol+"//","");
	}


        var paths = {}; //fix for /*, {url-template}*
        for (var property in jsonObj.paths) {
            if (jsonObj.paths.hasOwnProperty(property)) {
                paths[property.replace("*","")] = jsonObj.paths[property];
            }
        }
        jsonObj.paths = paths;

	//assign default value to host with first environment address
	if (urlArr.length > 0 ) {
	        jsonObj.host = urlArr[0].trim().replace(accessProtocol+"//","");
	}


        var paths = {}; //fix for /*, {url-template}*
        for (var property in jsonObj.paths) {
            if (jsonObj.paths.hasOwnProperty(property)) {
                paths[property.replace("*","")] = jsonObj.paths[property];
            }
        }
        jsonObj.paths = paths;
        return jsonObj;

    };
   function sendReceive (httpMethod,data,url){

    var headers = this.getRequestHeaders(false);
    var type = "json"; // response format
    var response;
    switch (httpMethod){
        case  "GET":
            response = get(url,{},headers,type);
            break;
        case   "POST":
            response = post(url,stringify(data),headers,type);
            break;
        case    "PUT":
            response = put(url,stringify(data),headers,type);
            break;
        case    "DELETE":
            response = del(url,stringify(data),headers,type);
            break;
        default :
            log.error("Error in the programme flow.");
    }
    log.debug("---------------------:" + stringify(response));
    if(response.data.Error) {
        session.put("get-status", response.data.Error.errorMessage);
    } else {
        session.put("get-status", "succeeded");
    }
    return response;
};

function getRequestHeaders (ssoEnabled){
    var requestHeaders;
    if(ssoEnabled){
        var accessToken = this.getAccessTokenFromSession();
        requestHeaders = {
            "Authorization": "Bearer "+accessToken,
            "Content-Type": "application/json"
        };
    }else{
        requestHeaders = {
            "Content-Type": "application/json",
            "Cookie": "JSESSIONID="+session.get("JSESSIONID")
        };
    }
    return requestHeaders;
};

function getLocation(href) {
    var match = href.match(/^(https?\:)\/\/(([^:\/?#]*)(?:\:([0-9]+))?)(\/[^?#]*)(\?[^#]*|)(#.*|)$/);
    return match && {
        protocol: match[1],
        host: match[2],
        hostname: match[3],
        port: match[4],
        pathname: match[5],
        search: match[6],
        hash: match[7]
    }
}

    /*
     * This method returns the UUID of an artifact
     */
    StoreAPIProxy.prototype.getDocument = function (username, resourcepath, tenantDomain) {
        var document = {};
        try {
            username = this.appendDomainToUser(username);
            if (username == null || username == '') {
                username = APIConstants.END_USER_ANONYMOUS;
            }

            var content = APIUtil.getDocument(username, resourcepath, tenantDomain);
            document.data = content.get("Data");
            document.contentType = content.get("contentType");
            document.name = content.get("name");
            if (log.isDebugEnabled()) {
                log.debug("Invoke getAllDocumentation()" );
            }
            return {
                error:false,
                document:document
            };
        } catch (e) {
            log.error(e.message);
            return {
                error:e
            };
        }
    };

    StoreAPIProxy.prototype.getInlineContent = function (provider, name, version, docName) {
        var document = {}, result;
        try {
            var apiIdentifier = new APIIdentifier(this.appendDomainToUser(provider), name, version);
            result = this.impl.getDocumentationContent(apiIdentifier, docName);
            document.docName = docName;
            if (result != null && result != '') {
                document.content = result;
                document.contentAvailable = true;
            } else {
                document.contentAvailable = false;
            }
            if (log.isDebugEnabled()) {
                log.debug("getInlineContent for : " + docName);
            }

            return {
                error: false,
                doc: document
            };
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                doc: null
            };
        }
    };

    StoreAPIProxy.prototype.getGatewayendpoint = function(transports){
        try {
            var endPoint = APIUtil.getGatewayendpoint(transports);
            return {
                error: false,
                endPoint: endPoint
            };
        }catch(e){
            log.error(e.message);
            return {
                error:e
            };
        }
    };
    
    StoreAPIProxy.prototype.mapExistingOauthClient = function(saveAuthAppParams){
        try {
            var result = this.impl.mapExistingOAuthClient(saveAuthAppParams.jsonParams, saveAuthAppParams.username,
                saveAuthAppParams.client_id, saveAuthAppParams.applicationName, saveAuthAppParams.keytype, [saveAuthAppParams.authorizedDomains]);
            result = APIUtil().stringifyKeyDetails(result);
            return result;
        }catch(e){
            log.error(e.message);
            return {
                error:e
            };
        }
    };

})(apistore);

