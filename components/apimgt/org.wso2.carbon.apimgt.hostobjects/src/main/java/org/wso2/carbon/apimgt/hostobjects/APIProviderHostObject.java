/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.hostobjects;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.parser.SwaggerParser;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hostobjects.util.Json;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.UserAwareAPIProvider;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.reportgen.util.ReportGenUtil;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.cache.Caching;
import javax.xml.namespace.QName;

@SuppressWarnings("unused")
public class APIProviderHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    //private static Pattern endpointURIPattern=Pattern.compile("^((http(?:s)?:\\/\\/)?([a-zA-Z0-9\\-\\.]{1,})+((\\:([0-9]{1,5}))?)(?:(\\/?|(?:\\/((\\{uri\\.var\\.[\\w]+\\})|[\\w\\-]+)))*)(?:\\/?|\\/\\w+\\.[a-zA-Z0-9]{1,})(?:\\?|(\\?(?:[\\w]+)(?:\\=([\\w\\-]+|\\{uri\\.var\\.[\\w]+\\}))?))?(?:\\&|(\\&(?:[\\w]+)(?:\\=([\\w\\-]+|\\{uri\\.var\\.[\\w]+\\}))?))*)$");
    private static Pattern pathParamExtractorPattern = Pattern.compile("\\{.*?\\}");
    private static Pattern pathParamValidatorPattern = Pattern.compile("\\{uri\\.var\\.[\\w]+\\}");

    private String username;
    private static String VERSION_PARAM = "{version}";
    private static String ICON_PATH = "tmp/icon";
    private static final String ALIAS = "alias";
    private static final String END_POINT = "endpoint";
    private static final String TIER = "tier";

    private APIProvider apiProvider;

    public String getClassName() {
        return "APIProvider";
    }

    // API definitions from swagger v2.0
    static APIDefinition definitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();

    // The zero-argument constructor used for create instances for runtime
    public APIProviderHostObject() throws APIManagementException {

    }

    public APIProviderHostObject(String loggedUser) throws APIManagementException {
        username = loggedUser;
        apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);
    }

    public String getUsername() {
        return username;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws APIManagementException {


        if (args != null && args.length != 0) {
            String username = (String) args[0];
            return new APIProviderHostObject(username);
        }
        return new APIProviderHostObject();
    }

    public APIProvider getApiProvider() {
        return apiProvider;
    }

    private static APIProvider getAPIProvider(Scriptable thisObj) {
        return ((APIProviderHostObject) thisObj).getApiProvider();
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    private static void handleFaultGateWayException(FaultGatewaysException e) throws FaultGatewaysException {
        throw e;
    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {

        if (args == null || args.length == 0 || !isStringValues(args)) {
            handleException("Invalid input parameters to the login method");
        }

        String username = (String) args[0];
        String password = (String) args[1];

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        NativeObject row = new NativeObject();
        try {

            UserAdminStub userAdminStub = new UserAdminStub(url + "UserAdmin");
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, userAdminStub._getServiceClient());
            //If multiple user stores are in use, and if the user hasn't specified the domain to which
            //he needs to login to
            /* Below condition is commented out as per new multiple users-store implementation,users from
            different user-stores not needed to input domain names when tried to login,APIMANAGER-1392*/
            // if (userAdminStub.hasMultipleUserStores() && !username.contains("/")) {
            //      handleException("Domain not specified. Please provide your username as domain/username");
            // }
        } catch (Exception e) {
            log.error("Error occurred while checking for multiple user stores", e);
        }

        boolean isTenantFlowStarted = false;
        try {
            ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(configurationContext, url +
                    "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            //update permission cache before validate user
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                handleException("Invalid tenant domain.");
            }
            PermissionUpdateUtil.updatePermissionTree(tenantId);

            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();

            String host = new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Login failed. Please recheck the username and password and try again..");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            String usernameWithDomain = APIUtil.getLoggedInUserInfo(sessionCookie, url).getUserName();
            usernameWithDomain = APIUtil.setDomainNameToUppercase(usernameWithDomain);
            boolean isSuperTenant = false;

            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                isSuperTenant = true;
                // If email user name is not enabled and user name is an email user name then only append
                // carbon.super to the username
                if (!MultitenantUtils.isEmailUserName() && MultitenantUtils.getTenantAwareUsername(username)
                        .contains(APIConstants.EMAIL_DOMAIN_SEPARATOR)) {
                    usernameWithDomain = usernameWithDomain + APIConstants.EMAIL_DOMAIN_SEPARATOR + tenantDomain;
                }
            } else {
                usernameWithDomain = usernameWithDomain + "@" + tenantDomain;
            }
            boolean authorized =
                    APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_CREATE) ||
                            APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_PUBLISH);

            boolean displayStoreUrlFromPublisher = false;
            if (config != null) {
                displayStoreUrlFromPublisher = Boolean.parseBoolean(
                        config.getFirstProperty(APIConstants.SHOW_API_STORE_URL_FROM_PUBLISHER));
            }
            if (authorized) {
                row.put("user", row, usernameWithDomain);
                row.put("sessionId", row, sessionCookie);
                row.put("isSuperTenant", row, isSuperTenant);
                row.put("error", row, false);
                row.put("showStoreURL", row, displayStoreUrlFromPublisher);
            } else {
                CarbonConstants.AUDIT_LOG.info('\'' + usernameWithDomain + APIConstants.EMAIL_DOMAIN_SEPARATOR
                        + tenantDomain + " [" + tenantId + "]' login denied due to insufficient privileges");
                handleException("Login failed. Insufficient privileges.");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return row;
    }


    /**
     * This method is used to update the permission cache from jaggery side. user name should be passed as a parameter
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if update successful, false otherwise
     * @throws APIManagementException
     */
    public static boolean jsFunction_updatePermissionCache(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters to the login method");
        }
        String username = (String) args[0];
        boolean updated = false;
        try {

            APIUtil.updatePermissionCache(username);
            updated = true;
        } catch (Exception e) {
            // If the user creation or permission change done in another node in distributed setup, users may not be
            // able to login into the system using SSO until permission cache updated. We call this method internally
            // to update the permission cache, when user trying to login into the system. If this request fails user
            // may not able to login into the systems and user will be getting an invalid credentials message. User
            // can login into the system once permission cache automatically updated in predefined interval.
            log.error("Error while updating permissions for user " + username, e);
        }
        return updated;
    }

    public static String jsFunction_getAuthServerURL(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }
        return url;
    }

    public static String jsFunction_getHTTPsURL(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        String backendHttpsPort = HostObjectUtils.getBackendPort("https");
        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }
        return "https://" + hostName + ":" + backendHttpsPort;

    }

    /**
     * This method is to functionality of managing an API in API-Provider     *
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static boolean jsFunction_manageAPI(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException, ParseException {
        boolean success = false;

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);

        String subscriptionAvailability = (String) apiData.get("subscriptionAvailability", apiData);
        String subscriptionAvailableTenants = "";
        if (subscriptionAvailability != null && subscriptionAvailability.equals(
                APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
            subscriptionAvailableTenants = (String) apiData.get("subscriptionTenants", apiData);
        }

        String defaultVersion = (String) apiData.get("defaultVersion", apiData);
        String transport = getTransports(apiData);

        String tier = (String) apiData.get("tier", apiData);
        String apiLevelPolicy = null;
        if (APIUtil.isAdvanceThrottlingEnabled()) {
            apiLevelPolicy = (String) apiData.get("apiPolicy", apiData);
        }
        String businessOwner = (String) apiData.get("bizOwner", apiData);
        String businessOwnerEmail = (String) apiData.get("bizOwnerMail", apiData);
        String technicalOwner = (String) apiData.get("techOwner", apiData);
        String technicalOwnerEmail = (String) apiData.get("techOwnerMail", apiData);
        String environments = (String) apiData.get("environments", apiData);
        String responseCache = (String) apiData.get("responseCache", apiData);
        String corsConfiguraion = (String) apiData.get("corsConfiguration", apiData);
        String additionalProperties = (String) apiData.get("additionalProperties", apiData);
        String swagger =  (String) apiData.get("swagger", apiData);
        JSONObject properties = null;
        if (!StringUtils.isEmpty(additionalProperties)) {
            JSONParser parser = new JSONParser();
            properties = (JSONObject) parser.parse(additionalProperties);
        }
        String authorizationHeader = (String) apiData.get("authorizationHeader", apiData);
        String apiSecurity = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
        Object apiSecurityObject = apiData.get("apiSecurity", apiData);

        if (apiSecurityObject instanceof String || apiSecurityObject instanceof ConsString) {
            apiSecurity = String.valueOf(apiSecurityObject);
        }
        int cacheTimeOut = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
        if (APIConstants.ENABLED.equalsIgnoreCase(responseCache)) {
            responseCache = APIConstants.ENABLED;
            try {
                cacheTimeOut = Integer.parseInt((String) apiData.get("cacheTimeout", apiData));
            } catch (NumberFormatException e) {
                //ignore
            }
        } else {
            responseCache = APIConstants.DISABLED;
        }


        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        API api = null;
        boolean isTenantFlowStarted = false;
        String tenantDomain = null;
        try {
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = apiProvider.getAPI(apiId);
            if (api == null) {
                return false;
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        api.setTransports(transport);
        api.setSubscriptionAvailability(subscriptionAvailability);
        api.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        api.setResponseCache(responseCache);
        api.setCacheTimeout(cacheTimeOut);
        api.setAsDefaultVersion("default_version".equals(defaultVersion));

        if (apiData.keySet().contains("gatewayLabels")) {
            //get the label list from UI and set it here
            String gatewayLabels = (String) apiData.get("gatewayLabels", apiData);
            attachLabelsToAPI(api, gatewayLabels, provider);
        }

        String productionTps = (String) apiData.get("productionTps", apiData);
        String sandboxTps = (String) apiData.get("sandboxTps", apiData);

        if (!"none".equals(productionTps)) {
            api.setProductionMaxTps(productionTps);
        }

        if (!"none".equals(sandboxTps)) {
            api.setSandboxMaxTps(sandboxTps);
        }

        if (!"none".equals(businessOwner)) {
            api.setBusinessOwner(businessOwner);
        }
        if (!"none".equals(businessOwnerEmail)) {
            api.setBusinessOwnerEmail(businessOwnerEmail);
        }
        if (!"none".equals(technicalOwner)) {
            api.setTechnicalOwner(technicalOwner);
        }
        if (!"none".equals(technicalOwnerEmail)) {
            api.setTechnicalOwnerEmail(technicalOwnerEmail);
        }
        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environments));

        CORSConfiguration corsConfiguration = APIUtil.getCorsConfigurationDtoFromJson(corsConfiguraion);
        if (corsConfiguration != null) {
            api.setCorsConfiguration(corsConfiguration);
        }
        api.setAdditionalProperties(properties);
        api.setAuthorizationHeader(authorizationHeader);
        api.setApiSecurity(apiSecurity);

        Set<Tier> availableTier = new HashSet<Tier>();
        String[] tierNames;
        if (tier != null) {
            tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.removeAllTiers();
            api.addAvailableTiers(availableTier);
        }

        if (apiLevelPolicy != null) {
            if ("none".equals(apiLevelPolicy)) {
                api.setApiLevelPolicy(null);
            } else {
                api.setApiLevelPolicy(apiLevelPolicy);
            }
        } else {
            api.setApiLevelPolicy(null);
        }

        api.setLastUpdated(new Date());

        if (!apiData.get("swagger", apiData).equals("null")) {

            //Read URI Templates from swagger resource and set to api object
            Set<URITemplate> uriTemplates =
                    definitionFromOpenAPISpec.getURITemplates(api, String.valueOf(apiData.get("swagger", apiData)));
            api.setUriTemplates(uriTemplates);

            //scopes
            Set<Scope> scopes = definitionFromOpenAPISpec.getScopes(String.valueOf(apiData.get("swagger", apiData)));
            api.setScopes(scopes);

            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getTenantId(tenantDomain);
                for (URITemplate uriTemplate : uriTemplates) {
                    Scope scope = uriTemplate.getScope();
                    if (scope != null && !(APIUtil.isWhiteListedScope(scope.getKey()))) {
                        if (apiProvider.isScopeKeyAssigned(apiId, scope.getKey(), tenantId)) {
                            handleException("Scope " + scope.getKey() + " is already assigned by another API");
                        }
                    }
                }
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information ", e);
            }


            //Save swagger in the registry
            apiProvider.saveSwagger20Definition(api.getId(), (String) apiData.get(APIConstants.SWAGGER, apiData));
            apiProvider.addSwaggerToLocalEntry(api, (String) apiData.get(APIConstants.SWAGGER, apiData));
        }

        //get new key manager instance for  resource registration.
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

        Map registeredResource = keyManager.getResourceByApiId(api.getId().toString());

        if (registeredResource == null) {
            boolean isNewResourceRegistered = keyManager.registerNewResource(api, null);
            if (!isNewResourceRegistered) {
                handleException("APIResource registration is failed while adding the API- " + api.getId().getApiName()
                        + "-" + api.getId().getVersion());
            }
        } else {
            //update APIResource.
            String resourceId = (String) registeredResource.get("resourceId");
            if (resourceId == null) {
                handleException("APIResource update is failed because of empty resourceID.");
            }
            keyManager.updateRegisteredResource(api, registeredResource);
        }
        return saveAPI(apiProvider, api, null, false);
    }


    /**
     * This method is to functionality of update implementation of an API in API-Provider     *
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static boolean jsFunction_updateAPIImplementation(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException {

        // Get the InSeq or outSeq here and put into registry

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String implementationType = (String) apiData.get("implementation_type", apiData);
        String corsConfiguraion = (String) apiData.get("corsConfiguration", apiData);
        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        API api = null;
        boolean isTenantFlowStarted = false;
        String tenantDomain;
        try {
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = apiProvider.getAPI(apiId);
            if (api == null) {
                return false;
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        api.setLastUpdated(new Date());

        String wsdl = (String) apiData.get("wsdl", apiData);
        String wadl = (String) apiData.get("wadl", apiData);
        String endpointSecured = (String) apiData.get("endpointSecured", apiData);
        String endpointAuthDigest = (String) apiData.get("endpointAuthDigest", apiData);
        String endpointUTUsername = (String) apiData.get("endpointUTUsername", apiData);
        String endpointUTPassword = (String) apiData.get("endpointUTPassword", apiData);

        api.setWadlUrl(wadl);
        if (wsdl != null && !wsdl.isEmpty()) {
            api.setWsdlUrl(wsdl);
        }
        api.setEndpointConfig((String) apiData.get("endpoint_config", apiData));

        if (implementationType.equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_INLINE)) {
            api.setImplementation(APIConstants.IMPLEMENTATION_TYPE_INLINE);
        } else if (implementationType.equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT)) {
            api.setImplementation(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT);
            // Validate endpoint URI format
            validateEndpointURI(api.getEndpointConfig());
        } else {
            throw new APIManagementException("Invalid Implementation Type.");
        }

        //set secured endpoint parameters
        if ("secured".equals(endpointSecured)) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(endpointUTUsername);
            api.setEndpointUTPassword(endpointUTPassword);
            if ("digestAuth".equals(endpointAuthDigest)) {
                api.setEndpointAuthDigest(true);
            } else {
                api.setEndpointAuthDigest(false);
            }
        } else {
            api.setEndpointSecured(false);
            api.setEndpointAuthDigest(false);
            api.setEndpointUTUsername(null);
            api.setEndpointUTPassword(null);
        }

        if (!apiData.get("swagger", apiData).equals("null")) {
            //Read swagger from the registry todo: check why was this done
            //String swaggerFromRegistry = apiProvider.getOpenAPIDefinition(api.getId());


            //Read URI Templates from swagger resource and set to api object
            Set<URITemplate> uriTemplates = definitionFromOpenAPISpec.getURITemplates(api,
                    (String) apiData.get("swagger", apiData));
            api.setUriTemplates(uriTemplates);

            apiProvider.saveSwagger20Definition(api.getId(), (String) apiData.get("swagger", apiData));
        }

        String inSequence = (String) apiData.get("inSequence", apiData);
        String outSequence = (String) apiData.get("outSequence", apiData);
        String faultSequence = (String) apiData.get("faultSequence", apiData);

        api.removeCustomSequences();

        if (!"none".equals(inSequence)) {
            api.setInSequence(inSequence);
        }

        if (!"none".equals(outSequence)) {
            api.setOutSequence(outSequence);
        }

        if (!"none".equals(faultSequence)) {
            api.setFaultSequence(faultSequence);
        }


        CORSConfiguration corsConfiguration = APIUtil.getCorsConfigurationDtoFromJson(corsConfiguraion);
        if (corsConfiguration != null) {
            api.setCorsConfiguration(corsConfiguration);
        }
        return saveAPI(apiProvider, api, null, false);

    }

    private static String uploadSequenceFile(APIProvider apiProvider, FileHostObject seqFile, String filePath,
                                             APIIdentifier apiIdentifier)
            throws APIManagementException, ScriptException {
        ResourceFile inSeq = new ResourceFile(seqFile.getInputStream(), seqFile.getJavaScriptFile().getContentType());
        String seqFileName;
        boolean isTenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            String tenantDomain = null;
            if (apiIdentifier.getProviderName().contains("-AT-")) {
                String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            OMElement seqElment = APIUtil.buildOMElement(seqFile.getInputStream());
            String localName = seqElment.getLocalName();
            seqFileName = seqElment.getAttributeValue(new QName("name"));
            if ("sequence".equals(localName) && seqFileName != null) {
                apiProvider.addResourceFile(filePath + seqFileName, inSeq);
            } else {
                throw new APIManagementException("Sequence is malformed");
            }
        } catch (Exception e) {
            String errorMsg = "An Error has occurred while reading custom sequence file";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return seqFileName;
    }

    /**
     * This method is to functionality of uploading sequence to the registry     *
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the sequence was uploaded successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static String jsFunction_uploadSequenceFile(Context cx, Scriptable thisObj,
                                                       Object[] args, Function funObj)
            throws APIManagementException, ScriptException {

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        String inSeqFileName = null;

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String sequenceType = (String) apiData.get("seqType", apiData);
        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIProvider apiProvider = getAPIProvider(thisObj);
        if (apiData.get("seqFile", apiData) != null) {
            FileHostObject seqFile = (FileHostObject) apiData.get("seqFile", apiData);
            String inSeqPath = APIUtil.getSequencePath(apiId, sequenceType);
            inSeqFileName = uploadSequenceFile(apiProvider, seqFile, inSeqPath, apiId);
        }
        return inSeqFileName;

    }

    public static boolean jsFunction_isAPIUpdateValid(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException {

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        boolean success = false;

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        API api = null;
        boolean isTenantFlowStarted = false;
        String tenantDomain;
        try {
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = apiProvider.getAPI(apiId);
            success = api != null && apiProvider.isAPIUpdateValid(api);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return success;
    }

    /**
     * This method is to functionality of update design API in API-Provider     *
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static boolean jsFunction_updateAPIDesign(Context cx, Scriptable thisObj,
                                                     Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException {

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        boolean success = false;

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
//        String contextVal = (String) apiData.get("context", apiData);
        String description = (String) apiData.get("description", apiData);
        String type = (String) apiData.get("type", apiData);
        /* Business Information*/
        String techOwner = (String) apiData.get("techOwner", apiData);
        String techOwnerEmail = (String) apiData.get("techOwnerEmail", apiData);
        String bizOwner = (String) apiData.get("bizOwner", apiData);
        String bizOwnerEmail = (String) apiData.get("bizOwnerEmail", apiData);
        String schemaValidation = (String) apiData.get("schemaValidation", apiData);
//        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
//        String providerDomain = MultitenantUtils.getTenantDomain(provider);

        //TODO: check and remove
      /*  if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context= "/t/"+ providerDomain+context;
        }*/

        String tags = (String) apiData.get("tags", apiData);
        Set<String> tag = new HashSet<String>();

        if (tags != null) {
            if (tags.contains(",")) {
                String[] userTag = tags.split(",");
                tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
            } else {
                tag.add(tags);
            }
        }

        String visibility = (String) apiData.get("visibility", apiData);
        String visibleRoles = "";


        if (visibility != null && visibility.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            visibleRoles = (String) apiData.get("visibleRoles", apiData);
        }

        String publisherAccessControl = (String) apiData.get(APIConstants.ACCESS_CONTROL_PARAMETER, apiData);
        String publisherAccessControlRoles = "";
        if (publisherAccessControl != null && publisherAccessControl.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            publisherAccessControlRoles = (String) apiData.get(APIConstants.ACCESS_CONTROL_ROLES_PARAMETER, apiData);
            if (publisherAccessControlRoles != null) {
                publisherAccessControlRoles = publisherAccessControlRoles.trim();
            }
        }

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        API api = null;
        boolean isTenantFlowStarted = false;
        String tenantDomain;
        try {
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = apiProvider.getAPI(apiId);
            boolean isValid = false;
            if (api != null) {
                isValid = apiProvider.isAPIUpdateValid(api);
            }
            if(!isValid){
        		throw new APIManagementException(" User doesn't have permission for update");
        	}
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        if (apiData.containsKey("wsdl")) {
            String wsdl = (String) apiData.get("wsdl", apiData);
            if (StringUtils.isNotEmpty(wsdl)) {
                api.setWsdlUrl(wsdl);
            }
        }

        api.setDescription(description);
        HashSet<String> deletedTags = new HashSet<String>(api.getTags());
        deletedTags.removeAll(tag);
        api.removeTags(deletedTags);
        api.addTags(tag);
        api.setType(type);
        api.setBusinessOwner(bizOwner);
        api.setBusinessOwnerEmail(bizOwnerEmail);
        api.setTechnicalOwner(techOwner);
        api.setTechnicalOwnerEmail(techOwnerEmail);
        api.setVisibility(visibility);
        api.setVisibleRoles(visibleRoles != null ? visibleRoles.trim() : null);
        api.setLastUpdated(new Date());
        api.setAccessControl(publisherAccessControl);
        api.setAccessControlRoles(publisherAccessControlRoles);
        api.setEnableSchemaValidation("schemaValidation".equals(schemaValidation));

        FileHostObject wsdlFile;
        if (apiData.containsKey(APIConstants.WSDL_FILE)) {
            wsdlFile = (FileHostObject) apiData.get(APIConstants.WSDL_FILE, apiData);
            if (wsdlFile != null) {
                if (wsdlFile.getName().endsWith(APIConstants.ZIP_FILE_EXTENSION)) {
                    WSDLArchiveInfo archiveInfo = APIUtil.extractAndValidateWSDLArchive(wsdlFile.getInputStream());
                    if (archiveInfo != null) {
                        api.setWsdlArchivePath(archiveInfo.getAbsoluteFilePath());
                        if (log.isDebugEnabled()) {
                            log.debug("WSDL Archive in the file path " + archiveInfo.getAbsoluteFilePath()
                                    + "is extracted and validated.");
                        }
                        ResourceFile wsdlResource = new ResourceFile(wsdlFile.getInputStream(),
                                wsdlFile.getJavaScriptFile().getContentType());
                        api.setWsdlArchive(wsdlResource);
                        APIFileUtil.deleteDirectory(archiveInfo.getLocation());
                    }
                } else if (wsdlFile.getName().endsWith(APIConstants.WSDL_FILE_EXTENSION)) {
                    String path = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator
                            + APIConstants.WSDL_ARCHIVES_TEMP_FOLDER + File.separator + UUID.randomUUID().toString();
                    String wsdlFilePath = path + File.separator + APIConstants.WSDL_FILE
                            + APIConstants.WSDL_FILE_EXTENSION;
                    APIFileUtil.extractSingleWSDLFile(wsdlFile.getInputStream(), path, wsdlFilePath);
                    api.setWsdlUrl(APIConstants.FILE_URI_PREFIX + wsdlFilePath);
                } else {
                    throw new APIManagementException("Invalid file archive file extension: Use .zip format");
                }
            }
        }


        if (apiData.get("swagger", apiData) != null) {
            // Read URI Templates from swagger resource and set it to api object
            Set<URITemplate> uriTemplates = definitionFromOpenAPISpec.getURITemplates(api,
                    (String) apiData.get("swagger", apiData));
            api.setUriTemplates(uriTemplates);
            apiProvider.validateResourceThrottlingTiers(api, tenantDomain);
            // Save the swagger definition in the registry
            apiProvider.saveSwaggerDefinition(api, (String) apiData.get("swagger", apiData));
        }
        return saveAPI(apiProvider, api, fileHostObject, false);
    }

    /**
     * This method is to functionality of create a new API in API-Provider     *
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static boolean jsFunction_createAPI(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException {

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }


        NativeObject apiData = (NativeObject) args[0];

        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String type = (String) apiData.get("type", apiData);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);

        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (apiProvider.isAPIAvailable(apiId)) {
            handleException("Error occurred while adding the API. A duplicate API already exists for " +
                    name + "-" + version);
        }

        API api = new API(apiId);
        api.setStatus(APIConstants.CREATED);
        api.setType(type);
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        api.setContextTemplate(context);
        context = updateContextWithVersion(version, contextVal, context);

        api.setContext(context);
        api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        api.setLastUpdated(new Date());
        api.setAccessControl(APIConstants.NO_ACCESS_CONTROL);

        return saveAPI(apiProvider, api, null, true);
    }

    /**
     * Returns the Swagger12 definition //todo this actually returns swagger v2.0, create a new method
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     * @throws ScriptException
     */
    public static NativeObject jsFunction_getOpenAPIDefinitionResource(Context cx, Scriptable thisObj,
                                                               Object[] args, Function funObj) throws APIManagementException, ScriptException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        boolean isTenantFlowStarted = false;
        String apiJSON = null;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIProvider apiProvider = getAPIProvider(thisObj);
            if (apiProvider.getAPI(apiId) == null) {
                return null;
            }
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            int tenantId;
            UserRegistry registry;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                registry = registryService.getGovernanceSystemRegistry(tenantId);

                apiJSON = definitionFromOpenAPISpec.getAPIDefinition(apiId, registry); //apiProvider.getSwagger12Definition(apiId);
            } catch (RegistryException e) {
                handleException("Error when create registry instance ", e);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information ", e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        NativeObject row = new NativeObject();

        row.put("swagger", row, apiJSON);

        return row;
    }

    /**
     * This method save or update the API object
     *
     * @param apiProvider
     * @param api
     * @param fileHostObject
     * @param isNewApi
     * @return true if the API was added successfully
     * @throws APIManagementException
     */
    private static boolean saveAPI(APIProvider apiProvider, API api, FileHostObject fileHostObject, boolean isNewApi)
            throws APIManagementException, FaultGatewaysException {
        boolean success = false;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain =
                    MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {

                String thumbPath = addThumbIcon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType(), apiProvider, api);
            }
            if (isNewApi) {
                apiProvider.addAPI(api);
            } else {
                apiProvider.manageAPI(api);
            }
            success = true;
        } catch (ScriptException e) {
            handleException("Error while adding the API- " + api.getId().getApiName() + "-" + api.getId().getVersion(),
                    e);
            return false;
        } catch (FaultGatewaysException e) {
            handleFaultGateWayException(e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return success;
    }

    /**
     * This method is to functionality of add a new API in API-Provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     * @throws FaultGatewaysException
     */
    public static boolean jsFunction_addAPI(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException, ScriptException, FaultGatewaysException, ParseException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        boolean success;
        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }

        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String defaultVersion = (String) apiData.get("defaultVersion", apiData);
        String description = (String) apiData.get("description", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
        String sandboxUrl = (String) apiData.get("sandbox", apiData);
        String visibility = (String) apiData.get("visibility", apiData);
        String thumbUrl = (String) apiData.get("thumbUrl", apiData);
        String environments = (String) apiData.get("environments", apiData);
        String visibleRoles = "";
        String publisherAccessControl = (String) apiData.get(APIConstants.ACCESS_CONTROL_PARAMETER, apiData);
        String publisherAccessControlRoles = "";
        String additionalProperties = (String) apiData.get("additionalProperties", apiData);
        String schemaValidation = (String) apiData.get("schemaValidation", apiData);
        JSONObject properties = null;
        if (!StringUtils.isEmpty(additionalProperties)) {
            JSONParser parser = new JSONParser();
            properties = (JSONObject) parser.parse(additionalProperties);
        }

        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                handleException("API name is not specified");
            }
        }

        if (version != null) {
            version = version.trim();
            if (version.isEmpty()) {
                handleException("Version not specified for API " + name);
            }
        }

        if (visibility != null && visibility.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            visibleRoles = (String) apiData.get("visibleRoles", apiData);
        }

        if (publisherAccessControl != null && publisherAccessControl.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            publisherAccessControlRoles = (String) apiData.get(APIConstants.ACCESS_CONTROL_ROLES_PARAMETER, apiData);
        }
        String apiSecurity = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
        Object apiSecurityObject = apiData.get("apiSecurity", apiData);

        if (apiSecurityObject instanceof String || apiSecurityObject instanceof ConsString) {
            apiSecurity = String.valueOf(apiSecurityObject);
        }

        if (sandboxUrl != null && sandboxUrl.trim().length() == 0) {
            sandboxUrl = null;
        }

        if (endpoint != null && endpoint.trim().length() == 0) {
            endpoint = null;
        }

        if (endpoint != null && !endpoint.startsWith("http") && !endpoint.startsWith("https")) {
            endpoint = "http://" + endpoint;
        }
        if (sandboxUrl != null && !sandboxUrl.startsWith("http") && !sandboxUrl.startsWith("https")) {
            sandboxUrl = "http://" + sandboxUrl;
        }

        String redirectURL = (String) apiData.get("redirectURL", apiData);
        boolean advertiseOnly = Boolean.parseBoolean((String) apiData.get("advertiseOnly", apiData));
        String apiOwner = (String) apiData.get("apiOwner", apiData);

        if (apiOwner == null || apiOwner.equals("")) {
            apiOwner = provider;
        }

        String wsdl = (String) apiData.get("wsdl", apiData);
        String wadl = (String) apiData.get("wadl", apiData);
        String tags = (String) apiData.get("tags", apiData);

        String subscriptionAvailability = (String) apiData.get("subscriptionAvailability", apiData);
        String subscriptionAvailableTenants = "";
        if (subscriptionAvailability != null && subscriptionAvailability.equals(APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
            subscriptionAvailableTenants = (String) apiData.get("subscriptionTenants", apiData);
        }

        Set<String> tag = new HashSet<String>();

        if (tags != null) {
            if (tags.contains(",")) {
                String[] userTag = tags.split(",");
                tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
            } else {
                tag.add(tags);
            }
        }

        String transport = getTransports(apiData);

        String tier = (String) apiData.get("tier", apiData);
        if (StringUtils.isBlank(tier)) {
            handleException("No tier defined for the API");
        }
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String type = (String) apiData.get("type", apiData);
        if (contextVal.isEmpty()) {
            handleException("Context not defined for API");
        }

        if (contextVal.endsWith("/")) {
            handleException("Context cannot end with '/' character");
        }

        APIProvider apiProvider = getAPIProvider(thisObj);
        //check for context exists
        if (apiProvider.isDuplicateContextTemplate(contextVal)) {
            handleException("Error occurred while adding the API. A duplicate API context already exists for "
                    + contextVal);
        }
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        String providerDomain = MultitenantUtils.getTenantDomain(String.valueOf(apiData.get("provider", apiData)));
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);

        String contextTemplate = context;
        context = updateContextWithVersion(version, contextVal, context);

        NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);

        String techOwner = (String) apiData.get("techOwner", apiData);
        String techOwnerEmail = (String) apiData.get("techOwnerEmail", apiData);
        String bizOwner = (String) apiData.get("bizOwner", apiData);
        String bizOwnerEmail = (String) apiData.get("bizOwnerEmail", apiData);

        String endpointSecured = (String) apiData.get("endpointSecured", apiData);
        String endpointAuthDigest = (String) apiData.get("endpointAuthDigest", apiData);
        String endpointUTUsername = (String) apiData.get("endpointUTUsername", apiData);
        String endpointUTPassword = (String) apiData.get("endpointUTPassword", apiData);

        String inSequence = (String) apiData.get("inSequence", apiData);
        String outSequence = (String) apiData.get("outSequence", apiData);
        String faultSequence = (String) apiData.get("faultSequence", apiData);

        String responseCache = (String) apiData.get("responseCache", apiData);
        String corsConfiguraion = (String) apiData.get("corsConfiguration", apiData);

        int cacheTimeOut = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
        if (APIConstants.ENABLED.equalsIgnoreCase(responseCache)) {
            responseCache = APIConstants.ENABLED;
            try {
                cacheTimeOut = Integer.parseInt((String) apiData.get("cacheTimeout", apiData));
            } catch (NumberFormatException e) {
                //ignore
            }
        } else {
            responseCache = APIConstants.DISABLED;
        }

        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        if (apiProvider.isAPIAvailable(apiId)) {
            handleException("Error occurred while adding the API. A duplicate API already exists for " +
                    name + "-" + version);
        }

        API api = new API(apiId);

        // to keep the backword compatibility if swagger not set process from
        // resource_config or old way.
        if (apiData.get("swagger", apiData) == null) {
            if (apiData.get("resource_config", apiData) != null) {
                Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
                JSONParser parser = new JSONParser();
                JSONObject resourceConfig = null;

                try {
                    resourceConfig = (JSONObject) parser.parse((String) apiData.get("resource_config", apiData));
                } catch (ParseException e) {
                    handleException("Invalid resource config", e);
                } catch (ClassCastException e) {
                    handleException("Unable to create JSON object from resource config", e);
                }

                // process scopes
                JSONArray scopes = (JSONArray) resourceConfig.get("scopes");
                Set<Scope> scopeList = new LinkedHashSet<Scope>();
                for (int i = 0; i < scopes.size(); i++) {
                    Map scope = (Map) scopes.get(i); // access with get() method
                    Scope scopeObj = new Scope();
                    scopeObj.setKey((String) scope.get("key"));
                    scopeObj.setName((String) scope.get("name"));
                    scopeObj.setRoles((String) scope.get("roles"));
                    scopeObj.setDescription((String) scope.get("description"));
                    scopeList.add(scopeObj);
                }
                api.setScopes(scopeList);

                JSONArray resources = (JSONArray) resourceConfig.get("resources");
                for (int k = 0; k < resources.size(); k++) {
                    JSONObject resource = (JSONObject) resources.get(k);

                    Map http_verbs = (Map) resource.get("http_verbs");
                    Iterator iterator = http_verbs.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry mapEntry = (Map.Entry) iterator.next();
                        Map mapEntryValue = (Map) mapEntry.getValue();

                        URITemplate template = new URITemplate();
                        String uriTempVal = (String) resource.get("url_pattern");
                        uriTempVal = uriTempVal.startsWith("/") ? uriTempVal : ("/" + uriTempVal);
                        template.setUriTemplate(uriTempVal);
                        String verb = (String) mapEntry.getKey();
                        if (isHTTPMethodValid(verb)) {
                            template.setHTTPVerb(verb);
                        } else {
                            handleException("Specified HTTP verb " + verb + " is invalid");
                        }

                        String authType = (String) mapEntryValue.get("auth_type");
                        if (authType.equals("Application & Application User")) {
                            authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                        }
                        if (authType.equals("Application User")) {
                            authType = "Application_User";
                        }
                        if (authType.equals("Application")) {
                            authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                        }
                        template.setThrottlingTier((String) mapEntryValue.get("throttling_tier"));
                        template.setAuthType(authType);
                        template.setResourceURI(endpoint);
                        template.setResourceSandboxURI(sandboxUrl);
                        Scope scope = APIUtil.findScopeByKey(scopeList, (String) mapEntryValue.get("scope"));
                        template.setScope(scope);
                        uriTemplates.add(template);
                    }
                }
                // todo handle casting exceptions
                api.setUriTemplates(uriTemplates);
                // todo clean out the code.
            } else {
                // following is the old fashioned way of processing resources
                NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);
                NativeArray authTypeArr = (NativeArray) apiData.get("uriAuthMethodArr", apiData);
                NativeArray throttlingTierArr = (NativeArray) apiData.get("throttlingTierArr", apiData);
                if (uriTemplateArr != null && uriMethodArr != null && authTypeArr != null) {
                    if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
                        for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                            String uriMethods = (String) uriMethodArr.get(i, uriMethodArr);
                            String uriMethodsAuthTypes = (String) authTypeArr.get(i, authTypeArr);
                            String[] uriMethodArray = uriMethods.split(",");
                            String[] authTypeArray = uriMethodsAuthTypes.split(",");
                            String uriMethodsThrottlingTiers = (String) throttlingTierArr.get(i, throttlingTierArr);
                            String[] throttlingTierArray = uriMethodsThrottlingTiers.split(",");
                            for (int k = 0; k < uriMethodArray.length; k++) {
                                for (int j = 0; j < authTypeArray.length; j++) {
                                    if (j == k) {
                                        URITemplate template = new URITemplate();
                                        String uriTemp = (String) uriTemplateArr.get(i, uriTemplateArr);
                                        String uriTempVal = uriTemp.startsWith("/") ? uriTemp : ("/" + uriTemp);
                                        template.setUriTemplate(uriTempVal);
                                        String throttlingTier = throttlingTierArray[j];

                                        if (isHTTPMethodValid(uriMethodArray[k])) {
                                            template.setHTTPVerb(uriMethodArray[k]);
                                        } else {
                                            handleException("Specified HTTP verb " + uriMethodArray[k] + " is invalid");
                                        }

                                        String authType = authTypeArray[j];
                                        if (authType.equals("Application & Application User")) {
                                            authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                        }
                                        if (authType.equals("Application User")) {
                                            authType = "Application_User";
                                        }
                                        if (authType.equals("Application")) {
                                            authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                                        }

                                        template.setThrottlingTier(throttlingTier);
                                        template.setAuthType(authType);
                                        template.setResourceURI(endpoint);
                                        template.setResourceSandboxURI(sandboxUrl);

                                        uriTemplates.add(template);
                                        break;
                                    }

                                }
                            }

                        }
                        api.setUriTemplates(uriTemplates);
                    }
                }
            }
        }

        api.setDescription(description);
        api.setWsdlUrl(wsdl);
        api.setWadlUrl(wadl);
        api.setLastUpdated(new Date());
        api.setUrl(endpoint);
        api.setSandboxUrl(sandboxUrl);
        api.addTags(tag);
        api.setTransports(transport);
        api.setApiOwner(apiOwner);
        api.setAdvertiseOnly(advertiseOnly);
        api.setRedirectURL(redirectURL);
        api.setSubscriptionAvailability(subscriptionAvailability);
        api.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        api.setResponseCache(responseCache);
        api.setCacheTimeout(cacheTimeOut);
        api.setAsDefaultVersion("default_version".equals(defaultVersion));

        api.setProductionMaxTps((String) apiData.get("productionTps", apiData));
        api.setSandboxMaxTps((String) apiData.get("sandboxTps", apiData));
        api.setEnableSchemaValidation("schemaValidation".equals(schemaValidation));

        if (!"none".equals(inSequence)) {
            api.setInSequence(inSequence);
        }
        if (!"none".equals(outSequence)) {
            api.setOutSequence(outSequence);
        }

        List<String> sequenceList = apiProvider.getCustomFaultSequences(apiId);
        if (!"none".equals(faultSequence) && sequenceList.contains(faultSequence)) {
            api.setFaultSequence(faultSequence);
        }

        Set<Tier> availableTier = new HashSet<Tier>();
        String[] tierNames;
        if (tier != null) {
            tierNames = tier.split(",");
            if (!APIUtil.isAdvanceThrottlingEnabled()) {
                Set<Tier> definedTiers = apiProvider.getTiers();
                for (String tierName : tierNames) {
                    boolean isTierValid = false;
                    for (Tier definedTier : definedTiers) {
                        if (tierName.equals(definedTier.getName())) {
                            isTierValid = true;
                            break;
                        }
                    }

                    if (!isTierValid) {
                        handleException("Specified tier " + tierName + " does not exist");
                    }
                    availableTier.add(new Tier(tierName));
                }
            } else {
                Policy[] definedTiers = apiProvider.getPolicies(provider, PolicyConstants.POLICY_LEVEL_SUB);
                for (String tierName : tierNames) {
                    boolean isTierValid = false;
                    for (Policy definedTier : definedTiers) {
                        if (tierName.equals(definedTier.getPolicyName())) {
                            isTierValid = true;
                            break;
                        }
                    }

                    if (!isTierValid) {
                        handleException("Specified tier " + tierName + " does not exist");
                    }
                    availableTier.add(new Tier(tierName));
                }
            }

            api.addAvailableTiers(availableTier);
        }
        api.setStatus(APIConstants.CREATED);
        api.setContext(context);
        api.setType(type);
        api.setContextTemplate(contextTemplate);
        api.setBusinessOwner(bizOwner);
        api.setBusinessOwnerEmail(bizOwnerEmail);
        api.setTechnicalOwner(techOwner);
        api.setTechnicalOwnerEmail(techOwnerEmail);
        api.setVisibility(visibility);
        api.setVisibleRoles(visibleRoles != null ? visibleRoles.trim() : null);
        api.setAccessControl(publisherAccessControl);
        api.setAccessControlRoles(publisherAccessControlRoles);
        api.setAdditionalProperties(properties);
        api.setApiSecurity(apiSecurity);
        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environments));
        CORSConfiguration corsConfiguration = APIUtil.getCorsConfigurationDtoFromJson(corsConfiguraion);
        if (corsConfiguration != null) {
            api.setCorsConfiguration(corsConfiguration);
        }
        String endpointConfig = (String) apiData.get("endpoint_config", apiData);
        if (StringUtils.isEmpty(endpointConfig)) {
            handleException("Endpoint Configuration is missing");
        } else {
            api.setEndpointConfig(endpointConfig);
            //Validate endpoint URI format
            validateEndpointURI(api.getEndpointConfig());
        }
        //set secured endpoint parameters
        if ("secured".equals(endpointSecured)) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(endpointUTUsername);
            api.setEndpointUTPassword(endpointUTPassword);
            if ("digestAuth".equals(endpointAuthDigest)) {
                api.setEndpointAuthDigest(true);
            } else {
                api.setEndpointAuthDigest(false);
            }
        }

        checkFileSize(fileHostObject);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            apiProvider.addAPI(api);

            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {

                String thumbPath = addThumbIcon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType(), apiProvider, api);
                apiProvider.updateAPI(api);
            }
            NativeArray externalAPIStores = (NativeArray) apiData.get("externalAPIStores", apiData);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantManager().getTenantId(tenantDomain);
            if (externalAPIStores.getLength() != 0) {
                Set<APIStore> apiStores = new HashSet<APIStore>();
                for (int k = 0; k < externalAPIStores.getLength(); k++) {
                    String apiStoreName = externalAPIStores.get(k, externalAPIStores).toString();
                    apiStores.add(APIUtil.getExternalAPIStore(apiStoreName, tenantId));
                }
                apiProvider.publishToExternalAPIStores(api, apiStores, false);
            }
            success = true;

        } catch (Exception e) {
            handleException("Error while adding the API- " + name + "-" + version, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            try {
                URL url = new URL(thumbUrl);
                String imageType = url.openConnection().getContentType();
                File fileToUploadFromUrl = new File(ICON_PATH);
                if (!fileToUploadFromUrl.exists()) {
                    if (!fileToUploadFromUrl.createNewFile()) {
                        log.error("Unable to create new file under : " + ICON_PATH);
                    }
                }
                FileUtils.copyURLToFile(url, fileToUploadFromUrl);
                FileBody fileBody = new FileBody(fileToUploadFromUrl, imageType);

                checkImageSize(fileToUploadFromUrl);

                String thumbPath = addThumbIcon(fileBody.getInputStream(),
                        url.openConnection().getContentType(), apiProvider, api);

            } catch (IOException e) {
                handleException("[Error] Cannot read data from the URL", e);
            }
            apiProvider.updateAPI(api);

        }

        if (apiData.get("swagger", apiData) != null) {
            // Read URI Templates from swagger resource and set to api object
            Set<URITemplate> uriTemplates =
                    definitionFromOpenAPISpec.getURITemplates(api, String.valueOf(apiData.get("swagger", apiData)));
            api.setUriTemplates(uriTemplates);

            // scopes
            Set<Scope> scopes = definitionFromOpenAPISpec.getScopes(String.valueOf(apiData.get("swagger", apiData)));
            api.setScopes(scopes);

            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                for (URITemplate uriTemplate : uriTemplates) {
                    Scope scope = uriTemplate.getScope();
                    if (scope != null && !(APIUtil.isWhiteListedScope(scope.getKey()))) {
                        if (apiProvider.isScopeKeyAssigned(apiId, scope.getKey(), tenantId)) {
                            handleException("Scope " + scope.getKey() + " is already assigned by another API");
                        }
                    }
                }
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information ", e);
            }

            // Save swagger in the registry
            apiProvider.saveSwagger20Definition(api.getId(), (String) apiData.get("swagger", apiData));
            saveAPI(apiProvider, api, null, false);
        } else {
            String apiDefinitionJSON = definitionFromOpenAPISpec.generateAPIDefinition(api);
            apiProvider.saveSwagger20Definition(api.getId(), apiDefinitionJSON);
        }
        return success;

    }

    private static String addThumbIcon(InputStream inputStream, String contentType, APIProvider apiProvider, API api)
            throws APIManagementException {

        ResourceFile thumbIcon = new ResourceFile(inputStream, contentType);
        String thumbPath = APIUtil.getIconPath(api.getId());
        String thumbnailUrl = apiProvider.addResourceFile(thumbPath, thumbIcon);
        api.setThumbnailUrl(thumbnailUrl);

        /*Set permissions to anonymous role for thumbPath*/
        APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);
        return thumbPath;
    }

    private static boolean isHTTPMethodValid(String httpMethod) {
        boolean isValid = false;

        for (APIConstants.SupportedHTTPVerbs verb : APIConstants.SupportedHTTPVerbs.values()) {
            if (verb.name().equalsIgnoreCase(httpMethod)) {
                isValid = true;
            }
        }

        return isValid;
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(VERSION_PARAM)) {
            if (!context.endsWith("/")) {
                context = context + "/";
            }
            context = context + VERSION_PARAM;
        }
        return context;
    }

    private static String getTransports(NativeObject apiData) {
        String transportStr = String.valueOf(apiData.get("transports", apiData));
        String transport = transportStr;
        if (transportStr != null) {
            if ((transportStr.indexOf(",") == 0) || (transportStr.indexOf(",") == (transportStr.length() - 1))) {
                transport = transportStr.replace(",", "");
            }
        }
        return transport;
    }


    public static boolean jsFunction_updateAPI(Context cx, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj)
            throws APIManagementException, FaultGatewaysException, ParseException {

        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success;
        String provider = String.valueOf(apiData.get("provider", apiData));
        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String defaultVersion = (String) apiData.get("defaultVersion", apiData);
        String description = (String) apiData.get("description", apiData);
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
        String sandboxUrl = (String) apiData.get("sandbox", apiData);
        String techOwner = (String) apiData.get("techOwner", apiData);
        String techOwnerEmail = (String) apiData.get("techOwnerEmail", apiData);
        String bizOwner = (String) apiData.get("bizOwner", apiData);
        String bizOwnerEmail = (String) apiData.get("bizOwnerEmail", apiData);
        String visibility = (String) apiData.get("visibility", apiData);
        String publisherAccessControl = (String) apiData.get(APIConstants.ACCESS_CONTROL_PARAMETER, apiData);
        String thumbUrl = (String) apiData.get("thumbUrl", apiData);
        String environments = (String) apiData.get("environments", apiData);
        String corsConfiguraion = (String) apiData.get("corsConfiguration", apiData);
        String visibleRoles = "";
        String additionalProperties = (String) apiData.get("additionalProperties", apiData);
        String schemaValidation = (String) apiData.get("schemaValidation", apiData);
        JSONObject properties = null;
        String apiSecurity = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
        Object apiSecurityObject = apiData.get("apiSecurity", apiData);

        if (apiSecurityObject instanceof String || apiSecurityObject instanceof ConsString) {
            apiSecurity = String.valueOf(apiSecurityObject);
        }
        if (!StringUtils.isEmpty(additionalProperties)) {
            JSONParser parser = new JSONParser();
            properties = (JSONObject) parser.parse(additionalProperties);
        }
        String publisherAccessControlRoles = "";
        if (visibility != null && visibility.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            visibleRoles = (String) apiData.get("visibleRoles", apiData);
        }
        if (publisherAccessControl != null && publisherAccessControl.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            publisherAccessControlRoles = (String) apiData.get(APIConstants.ACCESS_CONTROL_ROLES_PARAMETER, apiData);
        }

        String visibleTenants = "";
        if (visibility != null && visibility.equals(APIConstants.API_CONTROLLED_VISIBILITY)) {
            visibleTenants = (String) apiData.get("visibleTenants", apiData);
        }
        String endpointSecured = (String) apiData.get("endpointSecured", apiData);
        String endpointAuthDigest = (String) apiData.get("endpointAuthDigest", apiData);
        String endpointUTUsername = (String) apiData.get("endpointUTUsername", apiData);
        String endpointUTPassword = (String) apiData.get("endpointUTPassword", apiData);

        String inSequence = (String) apiData.get("inSequence", apiData);
        String outSequence = (String) apiData.get("outSequence", apiData);
        String faultSequence = (String) apiData.get("faultSequence", apiData);

        String responseCache = (String) apiData.get("responseCache", apiData);
        int cacheTimeOut = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
        if (APIConstants.ENABLED.equalsIgnoreCase(responseCache)) {
            responseCache = APIConstants.ENABLED;
            try {
                cacheTimeOut = Integer.parseInt((String) apiData.get("cacheTimeout", apiData));
            } catch (NumberFormatException e) {
                //ignore
            }
        } else {
            responseCache = APIConstants.DISABLED;
        }

        if (sandboxUrl != null && sandboxUrl.trim().length() == 0) {
            sandboxUrl = null;
        }

        if (endpoint != null && endpoint.trim().length() == 0) {
            endpoint = null;
        }

        if (endpoint != null && !endpoint.startsWith("http") && !endpoint.startsWith("https")) {
            endpoint = "http://" + endpoint;
        }
        if (sandboxUrl != null && !sandboxUrl.startsWith("http") && !sandboxUrl.startsWith("https")) {
            sandboxUrl = "http://" + sandboxUrl;
        }

        String redirectURL = (String) apiData.get("redirectURL", apiData);
        boolean advertiseOnly = Boolean.parseBoolean((String) apiData.get("advertiseOnly", apiData));
        String apiOwner = (String) apiData.get("apiOwner", apiData);

        if (apiOwner == null || apiOwner.equals("")) {
            apiOwner = provider;
        }

        String wsdl = (String) apiData.get("wsdl", apiData);
        String wadl = (String) apiData.get("wadl", apiData);
        String subscriptionAvailability = (String) apiData.get("subscriptionAvailability", apiData);
        String subscriptionAvailableTenants = "";
        if (subscriptionAvailability != null && subscriptionAvailability.equals(APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
            subscriptionAvailableTenants = (String) apiData.get("subscriptionTenants", apiData);
        }

        String tags = (String) apiData.get("tags", apiData);
        Set<String> tag = new HashSet<String>();
        if (tags != null) {
            if (tags.contains(",")) {
                String[] userTag = tags.split(",");
                tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
            } else {
                tag.add(tags);
            }
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier oldApiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }

        API oldApi = apiProvider.getAPI(oldApiId);
        if (oldApi == null) {
            return  false;
        }
        String transport = getTransports(apiData);

        String tier = (String) apiData.get("tier", apiData);
        String apiLevelPolicy = (String) apiData.get("apiPolicy", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String type = (String) apiData.get("type", apiData);
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        String providerDomain = MultitenantUtils.getTenantDomain(String.valueOf(apiData.get("provider", apiData)));
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && !context.contains("/t/" + providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);

        String contextTemplate = context;
        context = updateContextWithVersion(version, contextVal, context);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        API api = new API(apiId);

        // to keep the backword compatibility if swagger not set process from
        // resource_config or old way.
        if (apiData.get("swagger", apiData) == null) {
            if (apiData.get("resource_config", apiData) != null) {
                Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
                JSONParser parser = new JSONParser();
                JSONObject resourceConfig = null;
                try {
                    resourceConfig = (JSONObject) parser.parse((String) apiData.get("resource_config", apiData));
                } catch (ParseException e) {
                    handleException("Invalid resource config", e);
                } catch (ClassCastException e) {
                    handleException("Unable to create JSON object from resource config", e);
                }

                //process scopes
                JSONArray scopes = (JSONArray) resourceConfig.get("scopes");
                Set<Scope> scopeList = new LinkedHashSet<Scope>();
                for (int i = 0; i < scopes.size(); i++) {
                    Map scope = (Map) scopes.get(i); //access with get() method
                    Scope scopeObj = new Scope();
                    scopeObj.setKey((String) scope.get("key"));
                    scopeObj.setName((String) scope.get("name"));
                    scopeObj.setRoles((String) scope.get("roles"));
                    scopeObj.setDescription((String) scope.get("description"));
                    scopeList.add(scopeObj);
                }
                api.setScopes(scopeList);


                JSONArray resources = (JSONArray) resourceConfig.get("resources");
                for (int k = 0; k < resources.size(); k++) {
                    JSONObject resource = (JSONObject) resources.get(k);


                    Map http_verbs = (Map) resource.get("http_verbs");
                    Iterator iterator = http_verbs.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry mapEntry = (Map.Entry) iterator.next();
                        Map mapEntryValue = (Map) mapEntry.getValue();

                        URITemplate template = new URITemplate();
                        String uriTempVal = (String) resource.get("url_pattern");
                        uriTempVal = uriTempVal.startsWith("/") ? uriTempVal : ("/" + uriTempVal);
                        template.setUriTemplate(uriTempVal);
                        template.setHTTPVerb((String) mapEntry.getKey());
                        String authType = (String) mapEntryValue.get("auth_type");
                        if (authType.equals("Application & Application User")) {
                            authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                        }
                        if (authType.equals("Application User")) {
                            authType = "Application_User";
                        }
                        if (authType.equals("Application")) {
                            authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                        }
                        template.setThrottlingTier((String) mapEntryValue.get("throttling_tier"));
                        template.setAuthType(authType);
                        template.setResourceURI(endpoint);
                        template.setResourceSandboxURI(sandboxUrl);
                        Scope scope = APIUtil.findScopeByKey(scopeList, (String) mapEntryValue.get("scope"));
                        template.setScope(scope);
                        uriTemplates.add(template);
                    }
                }
                //todo handle casting exceptions
                api.setUriTemplates(uriTemplates);
                //todo clean out the code.
            } else {
                //following is the old fashioned way of processing resources
                NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);
                NativeArray authTypeArr = (NativeArray) apiData.get("uriAuthMethodArr", apiData);
                NativeArray throttlingTierArr = (NativeArray) apiData.get("throttlingTierArr", apiData);
                NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);
                if (uriTemplateArr != null && uriMethodArr != null && authTypeArr != null) {
                    if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
                        for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                            String uriMethods = (String) uriMethodArr.get(i, uriMethodArr);
                            String uriMethodsAuthTypes = (String) authTypeArr.get(i, authTypeArr);
                            String[] uriMethodArray = uriMethods.split(",");
                            String[] authTypeArray = uriMethodsAuthTypes.split(",");
                            String uriMethodsThrottlingTiers = (String) throttlingTierArr.get(i, throttlingTierArr);
                            String[] throttlingTierArray = uriMethodsThrottlingTiers.split(",");
                            for (int k = 0; k < uriMethodArray.length; k++) {
                                for (int j = 0; j < authTypeArray.length; j++) {
                                    if (j == k) {
                                        URITemplate template = new URITemplate();
                                        String uriTemp = (String) uriTemplateArr.get(i, uriTemplateArr);
                                        String uriTempVal = uriTemp.startsWith("/") ? uriTemp : ("/" + uriTemp);
                                        template.setUriTemplate(uriTempVal);
                                        String throttlingTier = throttlingTierArray[j];
                                        template.setHTTPVerb(uriMethodArray[k]);
                                        String authType = authTypeArray[j];
                                        if (authType.equals("Application & Application User")) {
                                            authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                        }
                                        if (authType.equals("Application User")) {
                                            authType = "Application_User";
                                        }
                                        if (authType.equals("Application")) {
                                            authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                                        }
                                        template.setThrottlingTier(throttlingTier);
                                        template.setAuthType(authType);
                                        template.setResourceURI(endpoint);
                                        template.setResourceSandboxURI(sandboxUrl);

                                        uriTemplates.add(template);
                                        break;
                                    }

                                }
                            }

                        }
                        api.setUriTemplates(uriTemplates);
                    }
                }
            }
        }

        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environments));
        CORSConfiguration corsConfiguration = APIUtil.getCorsConfigurationDtoFromJson(corsConfiguraion);
        if (corsConfiguration != null) {
            api.setCorsConfiguration(corsConfiguration);
        }
        api.setDescription(description);
        api.setLastUpdated(new Date());
        api.setUrl(endpoint);
        api.setSandboxUrl(sandboxUrl);
        api.addTags(tag);
        api.setContext(context);
        api.setType(type);
        api.setContextTemplate(contextTemplate);
        api.setVisibility(visibility);
        api.setVisibleRoles(visibleRoles != null ? visibleRoles.trim() : null);

        if (apiData.keySet().contains("gatewayLabels")) {
            //get the label list and set it here
            String gatewayLabels = (String) apiData.get("gatewayLabels", apiData);
            attachLabelsToAPI(api, gatewayLabels, provider);
        }

        api.setVisibleTenants(visibleTenants != null ? visibleTenants.trim() : null);
        api.setAccessControl(publisherAccessControl);
        api.setAccessControlRoles(publisherAccessControlRoles);
        api.setApiSecurity(apiSecurity);
        api.setAdditionalProperties(properties);
        Set<Tier> availableTier = new HashSet<Tier>();
        if (tier != null) {
            String[] tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.addAvailableTiers(availableTier);
        }

        if (apiLevelPolicy != null) {
            if ("none".equals(apiLevelPolicy)) {
                api.setApiLevelPolicy(null);
            } else {
                api.setApiLevelPolicy(apiLevelPolicy);
            }
        }


        api.setStatus(oldApi.getStatus());
        api.setWsdlUrl(wsdl);
        api.setWadlUrl(wadl);
        api.setLastUpdated(new Date());
        api.setBusinessOwner(bizOwner);
        api.setBusinessOwnerEmail(bizOwnerEmail);
        api.setTechnicalOwner(techOwner);
        api.setTechnicalOwnerEmail(techOwnerEmail);
        api.setTransports(transport);
        if (!"none".equals(inSequence)) {
            api.setInSequence(inSequence);
        }
        if (!"none".equals(outSequence)) {
            api.setOutSequence(outSequence);
        }

        List<String> sequenceList = apiProvider.getCustomFaultSequences(apiId);
        if (!"none".equals(faultSequence) && sequenceList.contains(faultSequence)) {
            api.setFaultSequence(faultSequence);
        }
        api.setOldInSequence(oldApi.getInSequence());
        api.setOldOutSequence(oldApi.getOutSequence());
        api.setOldFaultSequence(oldApi.getFaultSequence());
        api.setRedirectURL(redirectURL);
        api.setApiOwner(apiOwner);
        api.setAdvertiseOnly(advertiseOnly);

        // @todo needs to be validated
        api.setEndpointConfig((String) apiData.get("endpoint_config", apiData));
        //Validate endpoint URI format
        validateEndpointURI(api.getEndpointConfig());

        api.setProductionMaxTps((String) apiData.get("productionTps", apiData));
        api.setSandboxMaxTps((String) apiData.get("sandboxTps", apiData));

        api.setSubscriptionAvailability(subscriptionAvailability);
        api.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        api.setResponseCache(responseCache);
        api.setCacheTimeout(cacheTimeOut);
        api.setAsDefaultVersion("default_version".equals(defaultVersion));
        api.setEnableSchemaValidation("schemaValidation".equals(schemaValidation));
        //set secured endpoint parameters
        if ("secured".equals(endpointSecured)) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(endpointUTUsername);
            api.setEndpointUTPassword(endpointUTPassword);
            if ("digestAuth".equals(endpointAuthDigest)) {
                api.setEndpointAuthDigest(true);
            } else {
                api.setEndpointAuthDigest(false);
            }
        }

        try {
            checkFileSize(fileHostObject);

            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {

                String thumbPath = addThumbIcon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType(), apiProvider, api);

            } else if (oldApi.getThumbnailUrl() != null) {
                // retain the previously uploaded image
                api.setThumbnailUrl(oldApi.getThumbnailUrl());
            }

            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                try {
                    URL url = new URL(thumbUrl);
                    String imageType = url.openConnection().getContentType();

                    File fileToUploadFromUrl = new File("tmp/icon");
                    if (!fileToUploadFromUrl.exists()) {
                        if (!fileToUploadFromUrl.createNewFile()) {
                            log.error("Unable to create new file under tmp/icon");
                        }
                    }
                    FileUtils.copyURLToFile(url, fileToUploadFromUrl);
                    FileBody fileBody = new FileBody(fileToUploadFromUrl, imageType);

                    checkImageSize(fileToUploadFromUrl);

                    String thumbPath = addThumbIcon(fileBody.getInputStream(), url.openConnection().getContentType(),
                            apiProvider, api);

                } catch (IOException e) {
                    handleException("[Error] Cannot read data from the URL", e);
                }
            }

            if (apiData.get("swagger", apiData) != null) {
                // Read URI Templates from swagger resource and set to api object
                Set<URITemplate> uriTemplates = definitionFromOpenAPISpec.getURITemplates(api,
                        String.valueOf(apiData.get("swagger", apiData)));
                api.setUriTemplates(uriTemplates);

                // scopes
                Set<Scope> scopes = definitionFromOpenAPISpec.getScopes(String.valueOf(apiData.get("swagger", apiData)));
                api.setScopes(scopes);

                try {
                    int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(tenantDomain);
                    for (URITemplate uriTemplate : uriTemplates) {
                        Scope scope = uriTemplate.getScope();
                        if (scope != null && !(APIUtil.isWhiteListedScope(scope.getKey()))) {
                            if (apiProvider.isScopeKeyAssigned(apiId, scope.getKey(), tenantId)) {
                                handleException("Scope " + scope.getKey() + " is already assigned by another API");
                            }
                        }
                    }
                } catch (UserStoreException e) {
                    handleException("Error while reading tenant information ", e);
                }

                // Save swagger in the registry
                apiProvider.saveSwagger20Definition(api.getId(), (String) apiData.get("swagger", apiData));
                saveAPI(apiProvider, api, null, false);
            } else {
                String apiDefinitionJSON = definitionFromOpenAPISpec.generateAPIDefinition(api);
                apiProvider.saveSwagger20Definition(api.getId(), apiDefinitionJSON);
                apiProvider.updateAPI(api);
            }

            success = true;
        } catch (Exception e) {
            handleException("Error while updating the API- " + name + "-" + version, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return success;
    }

    /**
     * This method is used to attach labels to a given API
     *
     * @param api API
     * @param gatewayLabels label as a comma separated text sent from the UI
     * @param provider API provider
     * @throws APIManagementException if failed to attach labels to the API
     */
    private static void attachLabelsToAPI(API api, String gatewayLabels, String provider) throws APIManagementException {

        if (!StringUtils.isEmpty(gatewayLabels)) {
            List<Label> gatewayLabelList = new ArrayList<Label>();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            List<Label> allLabelList = APIUtil.getAllLabels(tenantDomain);
            String[] labels = gatewayLabels.split(",");
            for (String currentLabelName : labels) {
                Label label = new Label();
                label.setName(currentLabelName);
                //set the description and access URLs
                for (Label currentLabel : allLabelList) {
                    if (currentLabelName.equalsIgnoreCase(currentLabel.getName())) {
                        label.setDescription(currentLabel.getDescription());
                        label.setAccessUrls(currentLabel.getAccessUrls());
                    }
                }
                gatewayLabelList.add(label);
            }
            api.setGatewayLabels(gatewayLabelList);
        } else {
            //clear the labels
            api.setGatewayLabels(null);
        }
    }

    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace("/" + VERSION_PARAM, "");
        } else {
            context = context.replace(VERSION_PARAM, version);
        }
        return context;
    }

    /**
     * This method used to change status of API
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully
     * @throws APIManagementException if API couldn't found
     * @throw FaultGatewaysException if any gateway couldn't update or create api
     */
    public static boolean jsFunction_updateAPIStatus(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException, FaultGatewaysException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success = false;
        String provider = (String) apiData.get("provider", apiData);
        String providerTenantMode = (String) apiData.get("provider", apiData);
        provider = APIUtil.replaceEmailDomain(provider);
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String status = (String) apiData.get("status", apiData);
        boolean publishToGateway = Boolean.parseBoolean((String) apiData.get("publishToGateway", apiData));
        boolean deprecateOldVersions = Boolean.parseBoolean((String) apiData.get("deprecateOldVersions", apiData));
        boolean makeKeysForwardCompatible =
                Boolean.parseBoolean((String) apiData.get("makeKeysForwardCompatible",
                        apiData));
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIProvider apiProvider = getAPIProvider(thisObj);
            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = apiProvider.getAPI(apiId);
            if (api != null) {
                String oldStatus = api.getStatus();
                String newStatus = status.toUpperCase();
                String currentUser = ((APIProviderHostObject) thisObj).getUsername();
                apiProvider.changeAPIStatus(api, newStatus, currentUser, publishToGateway);

                if ((APIConstants.CREATED.equals(oldStatus) || APIConstants.PROTOTYPED.equals(oldStatus))
                        && APIConstants.PUBLISHED.equals(newStatus)) {
                    if (makeKeysForwardCompatible) {
                        apiProvider.makeAPIKeysForwardCompatible(api);
                    }

                    if (deprecateOldVersions) {
                        List<API> apiList = apiProvider.getAPIsByProvider(provider);
                        APIVersionComparator versionComparator = new APIVersionComparator();
                        for (API oldAPI : apiList) {
                            if (oldAPI.getId().getApiName().equals(name) &&
                                versionComparator.compare(oldAPI, api) < 0 &&
                                (oldAPI.getStatus().equals(APIConstants.PUBLISHED))) {
                                apiProvider.changeAPIStatus(oldAPI, APIConstants.DEPRECATED,
                                                                             currentUser, publishToGateway);
                            }
                        }
                    }
                }
                success = true;
            } else {
                handleException("Couldn't find an API with the name-" + name + "version-" + version);
            }
        } catch (FaultGatewaysException e) {
            handleFaultGateWayException(e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_updateSubscriptionStatus(Context cx, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success = false;
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        String newStatus = (String) args[1];
        int appId = Integer.parseInt((String) args[2]);

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            apiProvider.updateSubscription(apiId, newStatus, appId);
            return true;

        } catch (APIManagementException e) {
            handleException("Error while updating subscription status: " + e.getMessage(), e);
            return false;
        }

    }

    private static void checkFileSize(FileHostObject fileHostObject)
            throws ScriptException, APIManagementException {
        if (fileHostObject != null) {
            long length = fileHostObject.getJavaScriptFile().getLength();
            if (length / 1024.0 > APIConstants.MAX_FILE_SIZE) {
                handleException("Image file exceeds the maximum limit of 1MB");
            }
        }
    }

    private static void checkImageSize(File file)
            throws ScriptException, APIManagementException, IOException {

        if (file.exists()) {
            long length = file.length();
            if (length / 1024 > APIConstants.MAX_FILE_SIZE) {
                handleException("Image file exceeds the maximum limit of 1MB");
            }
        }
    }

    public static boolean jsFunction_updateTierPermissions(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }

        NativeObject tierData = (NativeObject) args[0];
        boolean success = false;
        String tierName = (String) tierData.get("tierName", tierData);
        String permissionType = (String) tierData.get("permissiontype", tierData);
        String roles = (String) tierData.get("roles", tierData);

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            apiProvider.updateTierPermissions(tierName, permissionType, roles);
            return true;

        } catch (APIManagementException e) {
            handleException("Error while updating subscription status", e);
            return false;
        }

    }

    public static boolean jsFunction_updateThrottleTierPermissions(Context cx, Scriptable thisObj, Object[] args,
                                                                   Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }

        NativeObject tierData = (NativeObject) args[0];
        boolean success = false;
        String tierName = (String) tierData.get("policyName", tierData);
        String permissionType = (String) tierData.get("permissiontype", tierData);
        String roles = (String) tierData.get("roles", tierData);

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            apiProvider.updateThrottleTierPermissions(tierName, permissionType, roles);
            return true;

        } catch (APIManagementException e) {
            handleException("Error while updating subscription status", e);
            return false;
        }

    }

    public static NativeArray jsFunction_getTierPermissions(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
         /* Create an array with everyone role */
        String everyOneRoleName = ServiceReferenceHolder.getInstance().getRealmService().
                getBootstrapRealmConfiguration().getEveryOneRoleName();
        String defaultRoleArray[] = new String[1];
        defaultRoleArray[0] = everyOneRoleName;
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            Set<TierPermissionDTO> tierPermissions = apiProvider.getTierPermissions();
            int i = 0;
            if (tiers != null) {

                for (Tier tier : tiers) {
                    NativeObject row = new NativeObject();
                    boolean found = false;
                    for (TierPermissionDTO permission : tierPermissions) {
                        if (permission.getTierName().equals(tier.getName())) {
                            row.put("tierName", row, permission.getTierName());
                            row.put("tierDisplayName", row, tier.getDisplayName());
                            row.put("permissionType", row,
                                    permission.getPermissionType());
                            String[] roles = permission.getRoles();
                             /*If no roles defined return default role list*/
                            if (roles == null || roles.length == 0) {
                                row.put("roles", row, defaultRoleArray);
                            } else {
                                row.put("roles", row,
                                        permission.getRoles());
                            }
                            found = true;
                            break;
                        }
                    }
                     /* If no permissions has defined for this tier*/
                    if (!found) {
                        row.put("tierName", row, tier.getName());
                        row.put("tierDisplayName", row, tier.getDisplayName());
                        row.put("permissionType", row,
                                APIConstants.TIER_PERMISSION_ALLOW);
                        row.put("roles", row, defaultRoleArray);
                    }
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }


    public static NativeArray jsFunction_getThrottleTierPermissions(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
         /* Create an array with everyone role */
        String everyOneRoleName = ServiceReferenceHolder.getInstance().getRealmService().
                getBootstrapRealmConfiguration().getEveryOneRoleName();
        String defaultRoleArray[] = new String[1];
        defaultRoleArray[0] = everyOneRoleName;
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            Set<TierPermissionDTO> tierPermissions = apiProvider.getThrottleTierPermissions();
            int i = 0;
            if (tiers != null) {

                for (Tier tier : tiers) {
                    NativeObject row = new NativeObject();
                    boolean found = false;
                    for (TierPermissionDTO permission : tierPermissions) {
                        if (permission.getTierName().equals(tier.getName())) {
                            row.put("policyName", row, permission.getTierName());
                            row.put("tierDisplayName", row, tier.getDisplayName());
                            row.put("permissionType", row,
                                    permission.getPermissionType());
                            String[] roles = permission.getRoles();
                             /*If no roles defined return default role list*/
                            if (roles == null || roles.length == 0) {
                                row.put("roles", row, defaultRoleArray);
                            } else {
                                row.put("roles", row,
                                        permission.getRoles());
                            }
                            found = true;
                            break;
                        }
                    }
                     /* If no permissions has defined for this tier*/
                    if (!found) {
                        row.put("policyName", row, tier.getName());
                        row.put("tierDisplayName", row, tier.getDisplayName());
                        row.put("permissionType", row,
                                APIConstants.TIER_PERMISSION_ALLOW);
                        row.put("roles", row, defaultRoleArray);
                    }
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }


    public static String jsFunction_getDefaultAPIVersion(Context cx, Scriptable thisObj, Object[] args,
                                                         Function funObj) throws APIManagementException {
        String provider = args[0].toString();
        provider = APIUtil.replaceEmailDomain(provider);
        String apiname = args[1].toString();
        String version = ""; // unused attribute

        APIIdentifier apiid = new APIIdentifier(provider, apiname, version);
        APIProvider apiProvider1 = getAPIProvider(thisObj);
        return apiProvider1.getDefaultVersion(apiid);
    }

    public static boolean jsFunction_checkIfResourceExists(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws APIManagementException {
        boolean result = false;

        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters or their types.");
        }

        NativeObject apiData = (NativeObject) args[0];

        String providerName = String.valueOf(apiData.get("provider", apiData));
//        String providerNameTenantFlow = args[0].toString();
        providerName = APIUtil.replaceEmailDomain(providerName);
        String apiName = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.
                    getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        setTenantDomain(tenantDomain, true);
            }
            result = apiProvider.checkIfAPIExists(apiId);
        } catch (Exception e) {
            handleException("Error occurred while checking if API exists " + apiName +
                    "-" + version, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    public static NativeArray jsFunction_getScopes(Context cx, Scriptable thisObj,
                                                   Object[] args,
                                                   Function funObj) throws APIManagementException {
        NativeArray myn = new NativeArray(0);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        String providerName = args[0].toString();
//        String providerNameTenantFlow = args[0].toString();
        providerName = APIUtil.replaceEmailDomain(providerName);
        String scopeKey = args[1].toString();

        if (scopeKey != null && providerName != null) {
            Set<Scope> scopeSet = APIUtil.getScopeByScopeKey(scopeKey, providerName);
            JSONArray scopesNative = new JSONArray();
            for (Scope scope : scopeSet) {
                JSONObject scopeNative = new JSONObject();
                scopeNative.put("id", scope.getId());
                scopeNative.put("key", scope.getKey());
                scopeNative.put("name", scope.getName());
                scopeNative.put("roles", scope.getRoles());
                scopeNative.put("description", scope.getDescription());
                scopesNative.add(scopeNative);
            }
            myn.put(41, myn, scopesNative.toJSONString());
        } else {
            handleException("Scope Key or Provider Name not valid.");
        }
        return myn;
    }

    /**
     * This method is to functionality of getting an existing API to API-Provider based
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) throws APIManagementException {
        NativeArray myn = new NativeArray(0);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        String providerName = args[0].toString();
        String providerNameTenantFlow = args[0].toString();
        providerName = APIUtil.replaceEmailDomain(providerName);
        String apiName = args[1].toString();
        String version = args[2].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerNameTenantFlow));
            String userTenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(((APIProviderHostObject) thisObj).getUsername()));
            if (!tenantDomain.equals(userTenantDomain)) {
                throw new APIManagementException("Invalid Operation: Cannot access API:" + apiId + "from current tenant.");
            }
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            API api = null;
            try {
                api = apiProvider.getAPI(apiId);
            } catch (APIManagementException e) {
                handleException("Cannot find the requested API- " + apiName +
                        "-" + version, e);
            }

            if (api != null) {
                Set<URITemplate> uriTemplates = api.getUriTemplates();

                myn.put(0, myn, checkValue(api.getId().getApiName()));
                myn.put(1, myn, checkValue(api.getDescription()));
                myn.put(2, myn, checkValue(api.getUrl()));
                myn.put(3, myn, checkValue(api.getWsdlUrl()));
                myn.put(4, myn, checkValue(api.getId().getVersion()));
                StringBuilder tagsSet = new StringBuilder("");
                for (int k = 0; k < api.getTags().toArray().length; k++) {
                    tagsSet.append(api.getTags().toArray()[k].toString());
                    if (k != api.getTags().toArray().length - 1) {
                        tagsSet.append(",");
                    }
                }
                myn.put(5, myn, checkValue(tagsSet.toString()));
                StringBuilder tiersSet = new StringBuilder("");
                StringBuilder tiersDisplayNamesSet = new StringBuilder("");
                StringBuilder tiersDescSet = new StringBuilder("");
                Set<Tier> tierSet = api.getAvailableTiers();
                Iterator it = tierSet.iterator();
                int j = 0;
                while (it.hasNext()) {
                    Object tierObject = it.next();
                    Tier tier = (Tier) tierObject;
                    tiersSet.append(tier.getName());
                    tiersDisplayNamesSet.append(tier.getDisplayName());
                    tiersDescSet.append(tier.getDescription());
                    if (j != tierSet.size() - 1) {
                        tiersSet.append(",");
                        tiersDisplayNamesSet.append(",");
                        tiersDescSet.append(",");
                    }
                    j++;
                }

                myn.put(6, myn, checkValue(tiersSet.toString()));
                myn.put(7, myn, checkValue(api.getStatus()));
                myn.put(8, myn, getWebContextRoot(api.getThumbnailUrl()));
                myn.put(9, myn, api.getContext());
                myn.put(10, myn, checkValue(Long.valueOf(api.getLastUpdated().getTime()).toString()));
                myn.put(11, myn, getSubscriberCount(apiId, thisObj));

                if (uriTemplates.size() != 0) {
                    NativeArray uriTempArr = new NativeArray(uriTemplates.size());
                    Iterator i = uriTemplates.iterator();
                    List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
                    while (i.hasNext()) {
                        List<String> utArr = new ArrayList<String>();
                        URITemplate ut = (URITemplate) i.next();
                        utArr.add(ut.getUriTemplate());
                        utArr.add(ut.getMethodsAsString().replaceAll("\\s", ","));
                        utArr.add(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                        utArr.add(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                        NativeArray utNArr = new NativeArray(utArr.size());
                        for (int p = 0; p < utArr.size(); p++) {
                            utNArr.put(p, utNArr, utArr.get(p));
                        }
                        uriTemplatesArr.add(utNArr);
                    }

                    for (int c = 0; c < uriTemplatesArr.size(); c++) {
                        uriTempArr.put(c, uriTempArr, uriTemplatesArr.get(c));
                    }

                    myn.put(12, myn, uriTempArr);
                }

                myn.put(13, myn, checkValue(api.getSandboxUrl()));
                myn.put(14, myn, checkValue(tiersDescSet.toString()));
                myn.put(15, myn, checkValue(api.getBusinessOwner()));
                myn.put(16, myn, checkValue(api.getBusinessOwnerEmail()));
                myn.put(17, myn, checkValue(api.getTechnicalOwner()));
                myn.put(18, myn, checkValue(api.getTechnicalOwnerEmail()));
                myn.put(19, myn, checkValue(api.getWadlUrl()));
                myn.put(20, myn, checkValue(api.getVisibility()));
                myn.put(21, myn, checkValue(api.getVisibleRoles()));
                myn.put(22, myn, checkValue(api.getVisibleTenants()));
                myn.put(23, myn, checkValue(api.getEndpointUTUsername()));
                myn.put(24, myn, checkValue(api.getEndpointUTPassword()));
                myn.put(25, myn, checkValue(Boolean.toString(api.isEndpointSecured())));
                myn.put(26, myn, APIUtil.replaceEmailDomainBack(checkValue(api.getId().getProviderName())));
                myn.put(27, myn, checkValue("http", api.getTransports()));
                myn.put(28, myn, checkValue("https", api.getTransports()));
                Set<APIStore> storesSet = apiProvider.getExternalAPIStores(api.getId());
                if (storesSet != null && storesSet.size() != 0) {
                    NativeArray apiStoresArray = new NativeArray(0);
                    int i = 0;
                    for (APIStore store : storesSet) {
                        NativeObject storeObject = new NativeObject();
                        storeObject.put("name", storeObject, store.getName());
                        storeObject.put("displayName", storeObject, store.getDisplayName());
                        storeObject.put("published", storeObject, store.isPublished());
                        apiStoresArray.put(i, apiStoresArray, storeObject);
                        i++;
                    }
                    myn.put(29, myn, apiStoresArray);
                }
                myn.put(30, myn, checkValue(api.getInSequence()));
                myn.put(31, myn, checkValue(api.getOutSequence()));

                myn.put(32, myn, checkValue(api.getSubscriptionAvailability()));
                myn.put(33, myn, checkValue(api.getSubscriptionAvailableTenants()));

                //@todo need to handle backword compatibility
                myn.put(34, myn, checkValue(api.getEndpointConfig()));

                myn.put(35, myn, checkValue(api.getResponseCache()));
                myn.put(36, myn, checkValue(Integer.toString(api.getCacheTimeout())));
                myn.put(37, myn, checkValue(tiersDisplayNamesSet.toString()));

                myn.put(38, myn, checkValue(api.getFaultSequence()));

                //todo implement resource load

                if (uriTemplates.size() != 0) {
                    JSONArray resourceArray = new JSONArray();
                    Iterator i = uriTemplates.iterator();
                    List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
                    while (i.hasNext()) {
                        JSONObject resourceObj = new JSONObject();
                        URITemplate ut = (URITemplate) i.next();

                        resourceObj.put("url_pattern", ut.getUriTemplate());
                        resourceObj.put("http_verbs", JSONValue.parse(ut.getResourceMap()));

                        resourceArray.add(resourceObj);
                    }

                    myn.put(40, myn, JSONValue.toJSONString(resourceArray));
                }


                Set<Scope> scopes = api.getScopes();
                JSONArray scopesNative = new JSONArray();
                for (Scope scope : scopes) {
                    JSONObject scopeNative = new JSONObject();
                    scopeNative.put("id", scope.getId());
                    scopeNative.put("key", scope.getKey());
                    scopeNative.put("name", scope.getName());
                    scopeNative.put("roles", scope.getRoles());
                    scopeNative.put("description", scope.getDescription());
                    scopesNative.add(scopeNative);
                }
                myn.put(41, myn, scopesNative.toJSONString());
                myn.put(42, myn, checkValue(Boolean.toString(api.isDefaultVersion())));
                myn.put(43, myn, api.getImplementation());
                myn.put(44, myn, APIUtil.writeEnvironmentsToArtifact(api));
                //get new key manager
                KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
                Map registeredResource = keyManager.getResourceByApiId(api.getId().toString());
                myn.put(45, myn, JSONObject.toJSONString(registeredResource));
                myn.put(46, myn, checkValue(api.getProductionMaxTps()));
                myn.put(47, myn, checkValue(api.getSandboxMaxTps()));
                myn.put(48, myn, checkValue(Boolean.toString(api.isEndpointAuthDigest())));
                CORSConfiguration corsConfigurationDto = api.getCorsConfiguration();
                if (corsConfigurationDto == null) {
                    corsConfigurationDto =
                            new CORSConfiguration(false, Collections.EMPTY_LIST, false, Collections.EMPTY_LIST,
                                    Collections.EMPTY_LIST);
                }
                String corsJson = APIUtil.getCorsConfigurationJsonFromDto(corsConfigurationDto);
                myn.put(49, myn, corsJson);

                StringBuilder policiesSet = new StringBuilder("");

                myn.put(50, myn, checkValue(policiesSet.toString()));
                myn.put(51, myn, checkValue(api.getApiLevelPolicy()));
                myn.put(52, myn, checkValue(api.getType()));
                myn.put(53, myn, checkValue((api.getAccessControl())));
                myn.put(54, myn, checkValue((api.getAccessControlRoles())));
                myn.put(55, myn, checkValue(api.getAdditionalProperties().toJSONString()));
                myn.put(56, myn, checkValue(api.getAuthorizationHeader()));

                //put the labels to the native array which represents the API
                List<Label> labelList = api.getGatewayLabels();
                if (labelList != null && labelList.size() > 0) {
                    NativeArray apiLabelsArray = new NativeArray(labelList.size());
                    int i = 0;
                    for (Label label : labelList) {
                        NativeObject labelObject = new NativeObject();
                        labelObject.put(APIConstants.LABEL_NAME, labelObject, label.getName());
                        labelObject.put(APIConstants.LABEL_DESCRIPTION, labelObject, label.getDescription());
                        apiLabelsArray.put(i, apiLabelsArray, labelObject);
                        i++;
                    }
                    myn.put(57, myn, apiLabelsArray);
                }
                myn.put(58, myn, checkValue("oauth2", api.getApiSecurity()));
                myn.put(59, myn, checkValue("mutualssl", api.getApiSecurity()));
                myn.put(60, myn, checkValue("basic_auth", api.getApiSecurity()));
                myn.put(61, myn, checkValue("mutualssl_mandatory", api.getApiSecurity()));
                myn.put(62, myn, checkValue("oauth_basic_auth_mandatory", api.getApiSecurity()));
                myn.put(63, myn, checkValue(Boolean.toString(api.isEnabledSchemaValidation())));
            } else {
                handleException("Cannot find the requested API- " + apiName +
                        "-" + version);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscriberCountByAPIs(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {
                List<API> apiSet;
                if (providerName.equals("__all_providers__")) {
                    apiSet = apiProvider.getAllAPIs();
                } else {
                    apiSet = apiProvider.getAPIsByProvider(APIUtil.replaceEmailDomain(providerName));
                }

                Map<String, Long> subscriptions = new TreeMap<String, Long>();
                for (API api : apiSet) {
                    if (APIConstants.CREATED.equals(api.getStatus())) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    if (count == 0) {
                        continue;
                    }

                    String[] apiData = {api.getId().getApiName(), api.getId().getVersion(), api.getId().getProviderName()};

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(0, apiData[0]);
                    jsonArray.add(1, apiData[1]);
                    jsonArray.add(2, apiData[2]);
                    String key = jsonArray.toJSONString();

                    Long currentCount = subscriptions.get(key);
                    if (currentCount != null) {
                        subscriptions.put(key, currentCount + count);
                    } else {
                        subscriptions.put(key, count);
                    }
                }

                List<APISubscription> subscriptionData = new ArrayList<APISubscription>();
                for (Map.Entry<String, Long> entry : subscriptions.entrySet()) {
                    APISubscription sub = new APISubscription();
                    sub.name = entry.getKey();
                    sub.count = entry.getValue();
                    subscriptionData.add(sub);
                }

                int i = 0;
                for (APISubscription sub : subscriptionData) {
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, sub.name);
                    row.put("count", row, sub.count);
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            handleException("Error while getting subscribers of the provider: " + providerName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        NativeArray myn = new NativeArray(1);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            List<Tier> tierList = APIUtil.sortTiers(tiers);
            int i = 0;
            if (tiers != null) {
                for (Tier tier : tierList) {
                    NativeObject row = new NativeObject();
                    row.put("tierName", row, tier.getName());
                    row.put("tierDisplayName", row, tier.getDisplayName());
                    row.put("tierDescription", row,
                            tier.getDescription() != null ? tier.getDescription() : "");
                    row.put("defaultTier", row, i == 0);
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }


    public static NativeArray jsFunction_getSubscriberCountByAPIVersions(Context cx,
                                                                         Scriptable thisObj,
                                                                         Object[] args,
                                                                         Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        String apiName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiName = (String) args[1];
            if (providerName != null && apiName != null) {
                Map<String, Long> subscriptions = new TreeMap<String, Long>();
                Set<String> versions = apiProvider.getAPIVersions(APIUtil.replaceEmailDomain(providerName), apiName);
                for (String version : versions) {
                    APIIdentifier id = new APIIdentifier(providerName, apiName, version);
                    API api = apiProvider.getAPI(id);
                    if (api == null || APIConstants.CREATED.equals(api.getStatus())) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());

                    subscriptions.put(api.getId().getVersion(), count);
                }

                int i = 0;
                for (Map.Entry<String, Long> entry : subscriptions.entrySet()) {
                    NativeObject row = new NativeObject();
                    row.put("apiVersion", row, entry.getKey());
                    row.put("count", row, entry.getValue());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting subscribers of the " +
                    "provider: " + providerName + " and API: " + apiName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    private static int getSubscriberCount(APIIdentifier apiId, Scriptable thisObj)
            throws APIManagementException {
        APIProvider apiProvider = getAPIProvider(thisObj);
        Set<Subscriber> subs = apiProvider.getSubscribersOfAPI(apiId);
        Set<String> subscriberNames = new HashSet<String>();
        if (subs != null) {
            for (Subscriber sub : subs) {
                subscriberNames.add(sub.getName());
            }
            return subscriberNames.size();
        } else {
            return 0;
        }
    }

    private static String checkValue(String compare, String value) throws APIManagementException {
        if (value != null) {
            List<String> transportList = new ArrayList<String>();
            transportList.addAll(Arrays.asList(value.split(",")));
            if (transportList.contains(compare)) {
                return "checked";
            } else {
                return "";
            }

        } else {
            return "";
        }
    }

    /**
     * This method is to functionality of getting all the APIs stored
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeArray jsFunction_getAllAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);

        try {
            List<API> apiList = apiProvider.getAllAPIs();
            if (apiList != null) {
                Iterator it = apiList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("status", row, checkValue(api.getStatus()));
                    row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                    row.put("subs", row, getSubscriberCount(apiIdentifier, thisObj));
                    myn.put(i, myn, row);
                    i++;

                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the APIs", e);
        }
        return myn;
    }

    /**
     * This method is to functionality of getting all the APIs stored per provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeArray jsFunction_getAPIsByProvider(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        if (providerName != null) {
            APIProvider apiProvider = getAPIProvider(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                List<API> apiList = apiProvider.getAPIsByProvider(APIUtil.replaceEmailDomain(providerName));
                if (apiList != null) {
                    Iterator it = apiList.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        NativeObject row = new NativeObject();
                        Object apiObject = it.next();
                        API api = (API) apiObject;
                        APIIdentifier apiIdentifier = api.getId();
                        row.put("name", row, apiIdentifier.getApiName());
                        row.put("version", row, apiIdentifier.getVersion());
                        row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                        row.put("lastUpdatedDate", row, api.getLastUpdated().toString());
                        myn.put(i, myn, row);
                        i++;
                    }
                }
            } catch (Exception e) {
                handleException("Error occurred while getting APIs for " +
                        "the provider: " + providerName, e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws APIManagementException {
        String userName = null;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        try {
            userName = (String) args[0];
            Subscriber subscriber = new Subscriber(userName);
            Set<API> apiSet = apiProvider.getSubscriberAPIs(subscriber);
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("apiName", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("updatedDate", row, api.getLastUpdated().toString());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the subscribed APIs information " +
                    "for the subscriber-" + userName, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllAPIUsageByProvider(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws APIManagementException {

        NativeArray myn = new NativeArray(0);
        String providerName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        try {
            providerName = (String) args[0];
            if (providerName != null) {
                UserApplicationAPIUsage[] apiUsages = apiProvider.getAllAPIUsageByProvider(providerName);
                for (int i = 0; i < apiUsages.length; i++) {
                    NativeObject row = new NativeObject();
                    row.put("userName", row, apiUsages[i].getUserId());
                    row.put("application", row, apiUsages[i].getApplicationName());
                    row.put("appId", row, "" + apiUsages[i].getAppId());
                    row.put("token", row, apiUsages[i].getAccessToken());
                    row.put("tokenStatus", row, apiUsages[i].getAccessTokenStatus());
                    row.put("subStatus", row, apiUsages[i].getSubStatus());

                    StringBuilder apiSet = new StringBuilder("");
                    for (int k = 0; k < apiUsages[i].getApiSubscriptions().length; k++) {
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getSubStatus());
                        apiSet.append("::");
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getApiName());
                        apiSet.append("::");
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getVersion());
                        apiSet.append("::");
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getSubCreatedStatus());
                        if (k != apiUsages[i].getApiSubscriptions().length - 1) {
                            apiSet.append(",");
                        }
                    }
                    row.put("apis", row, apiSet.toString());
                    myn.put(i, myn, row);
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting subscribers of the provider: " + providerName, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        String apiName = null;
        String version = null;
        String providerName;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = (String) args[0];
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiName = (String) args[1];
            version = (String) args[2];
            APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);

            List<Documentation> docsList = apiProvider.getAllDocumentation(apiId);
            Iterator it = docsList.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject row = new NativeObject();
                Object docsObject = it.next();
                Documentation doc = (Documentation) docsObject;
                Object objectSourceType = doc.getSourceType();
                String strSourceType = objectSourceType.toString();
                row.put("docName", row, doc.getName());
                row.put("docType", row, doc.getType().getType());
                row.put("sourceType", row, strSourceType);
                row.put("visibility", row, doc.getVisibility().name());
                row.put("docLastUpdated", row, (Long.valueOf(doc.getLastUpdated().getTime()).toString()));
                //row.put("sourceType", row, doc.getSourceType());
                if (Documentation.DocumentSourceType.URL.equals(doc.getSourceType())) {
                    row.put("sourceUrl", row, doc.getSourceUrl());
                }

                if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType())) {
                    row.put("filePath", row, doc.getFilePath());
                }

                if (doc.getType() == DocumentationType.OTHER) {
                    row.put("otherTypeName", row, doc.getOtherTypeName());
                }

                row.put("summary", row, doc.getSummary());
                myn.put(i, myn, row);
                i++;

            }

        } catch (Exception e) {
            handleException("Error occurred while getting documentation of the api - " +
                    apiName + "-" + version, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args,
                                                          Function funObj)
            throws APIManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content;
        NativeArray myn = new NativeArray(0);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];
        docName = (String) args[3];
        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);

        try {
            content = apiProvider.getDocumentationContent(apiId, docName);
        } catch (Exception e) {
            handleException("Error while getting Inline Document Content ", e);
            return null;
        }

        NativeObject row = new NativeObject();
        row.put("providerName", row, APIUtil.replaceEmailDomainBack(providerName));
        row.put("apiName", row, apiName);
        row.put("apiVersion", row, version);
        row.put("docName", row, docName);
        row.put("content", row, content);
        myn.put(0, myn, row);
        return myn;
    }

    public static void jsFunction_addInlineContent(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj) throws APIManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String docContent;

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];
        docName = (String) args[3];
        docContent = (String) args[4];

        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName,
                version);
        APIProvider apiProvider = getAPIProvider(thisObj);
//        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
        try {
            API api = apiProvider.getAPI(apiId);
            if (api != null) {
                apiProvider.addDocumentationContent(api, docName, docContent);
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while adding the content of the documentation- " + docName, e);
        }
    }

    public static boolean jsFunction_addDocumentation(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws APIManagementException, ScriptException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success = false;
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];
        String summary = (String) args[5];
        String sourceType = (String) args[6];
        String otherTypeName = (String) args[9];

        //validate Source Type
        if (sourceType == null) {
            throw new APIManagementException("Invalid Source Type.");
        }
        sourceType = sourceType.trim();

        String visibility = (String) args[11];
        FileHostObject fileHostObject = null;
        String sourceURL;

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
            Documentation doc = new Documentation(getDocType(docType), docName);
            APIProvider apiProvider = getAPIProvider(thisObj);

            //add documentation is allowed only if document name does not already exist for this api
            if (apiProvider.isDocumentationExist(apiId, docName)) {
                handleException("Error occurred while adding the document. " + docName +
                        " already exists for API " + apiName + '-' + version);
            }

            if (doc.getType() == DocumentationType.OTHER) {
                //validate otherTypeName
                if (otherTypeName == null || otherTypeName.trim().isEmpty()) {
                    throw new APIManagementException("Other Type Name Cannot be Empty.");
                }
                doc.setOtherTypeName(otherTypeName.trim());
            }

            if (Documentation.DocumentSourceType.URL.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.URL);
                sourceURL = args[7].toString();
                //validate urls
                if (sourceURL == null || !isURL(sourceURL.trim())) {
                    throw new APIManagementException("Invalid document URL format.");
                }
                sourceURL = sourceURL.trim();
                doc.setSourceUrl(sourceURL);
            } else if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.FILE);
                fileHostObject = (FileHostObject) args[8];
            } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.INLINE);
            } else if (Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.MARKDOWN);
            } else {
                throw new APIManagementException("Invalid Source Type.");
            }

            doc.setSummary(summary);

            if (visibility == null) {
                visibility = APIConstants.DOC_API_BASED_VISIBILITY;
            }
            if (Documentation.DocumentVisibility.API_LEVEL.toString().equalsIgnoreCase(visibility)) {
                doc.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
            } else if (Documentation.DocumentVisibility.PRIVATE.toString().equalsIgnoreCase(visibility)) {
                doc.setVisibility(Documentation.DocumentVisibility.PRIVATE);
            } else {
                doc.setVisibility(Documentation.DocumentVisibility.OWNER_ONLY);
            }

            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                String extension = FilenameUtils.getExtension(fileHostObject.getJavaScriptFile().getName());
                if (extension.contains("exe")) {
                    throw new APIManagementException("File type .exe is not supported!");
                }
                String contentType = (String) args[10];
                apiProvider
                        .addFileToDocumentation(apiId, doc, fileHostObject.getName(), fileHostObject.getInputStream(),
                                contentType);
            } else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
                throw new APIManagementException("Empty File Attachment.");
            }

            apiProvider.addDocumentation(apiId, doc);
            success = true;
        } catch (ScriptException e) {
            handleException("The attachment cannot be found for document- " + docName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_removeDocumentation(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success;
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];

        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);

        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            apiProvider.removeDocumentation(apiId, docName, docType);
            success = true;
        } catch (APIManagementException e) {
            handleException("Error occurred while removing the document- " + docName +
                    ".", e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_createNewAPIVersion(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {

        boolean success;
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String newVersion = (String) args[3];
        String defaultVersion = (String) args[4];

        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
        API api = new API(apiId);
        api.setAsDefaultVersion(defaultVersion.equals("default_version"));

        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiProvider.createNewAPIVersion(api, newVersion);
            success = true;
        } catch (DuplicateAPIException e) {
            handleException("Error occurred while creating a new API version. " + e.getMessage());
            return false;
        } catch (Exception e) {
            handleException("Error occurred while creating a new API version: " + newVersion, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static NativeArray jsFunction_getSubscribersOfAPI(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        String apiName;
        String version;
        String providerName;
        NativeArray myn = new NativeArray(0);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }

        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        Set<Subscriber> subscribers;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            subscribers = apiProvider.getSubscribersOfAPI(apiId);
            Iterator it = subscribers.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object subscriberObject = it.next();
                Subscriber user = (Subscriber) subscriberObject;
                row.put("userName", row, user.getName());
                row.put("subscribedDate", row, checkValue(Long.valueOf(user.getSubscribedDate().getTime()).toString()));
                myn.put(i, myn, row);
                i++;
            }

        } catch (APIManagementException e) {
            handleException("Error occurred while getting subscribers of the API- " + apiName +
                    "-" + version, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscriptionsOfAPI(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        String apiName;
        String version;
        String provider;
        NativeArray myn = new NativeArray(0);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }

        apiName = (String) args[0];
        version = (String) args[1];
        provider = (String) args[2];

        List<SubscribedAPI> subscriptions;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            subscriptions = apiProvider.getSubscriptionsOfAPI(apiName, version, provider);
            Iterator it = subscriptions.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object subscriptionObject = it.next();
                SubscribedAPI subscription = (SubscribedAPI) subscriptionObject;
                row.put("subscriber", row, subscription.getSubscriber().getName());
                row.put("application", row, subscription.getApplication().getName());
                row.put("appId", row, subscription.getApplication().getId());
                row.put("subscriptionStatus", row, subscription.getSubStatus());
                row.put("subscriptionCreatedStatus", row, subscription.getSubCreatedStatus());
                row.put("subscribedDate", row, subscription.getCreatedTime());
                myn.put(i, myn, row);
                i++;
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while getting subscriptions of the API- " + apiName +
                    "-" + version, e);
        }
        return myn;
    }

    public static String jsFunction_isContextExist(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        Boolean contextExist = false;
        if (args != null && isStringValues(args)) {
            String context = (String) args[0];
            String oldContext = (String) args[1];

            if (context.equals(oldContext)) {
                return contextExist.toString();
            }
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                contextExist = apiProvider.isDuplicateContextTemplate(context);
            } catch (APIManagementException e) {
                handleException("Error while checking whether context exists", e);
            }
        } else {
            handleException("Input context value is null");
        }
        return contextExist.toString();
    }

    public static String jsFunction_isApiNameExist(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        Boolean apiExist = false;
        if (args != null && isStringValues(args)) {
            String apiName = (String) args[0];
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                apiExist = apiProvider.isApiNameExist(apiName);
            } catch (APIManagementException e) {
                handleException("Error from registry while checking the api name is already exist", e);
            }
        } else {
            handleException("Input api name value is null");
        }
        return apiExist.toString();
    }

    public static String jsFunction_isApiNameWithDifferentCaseExist(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws APIManagementException {
        Boolean apiWithDifferentCaseExist = false;
        if (args != null && isStringValues(args)) {
            String apiName = (String) args[0];
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                apiWithDifferentCaseExist = apiProvider.isApiNameWithDifferentCaseExist(apiName);
            } catch (APIManagementException e) {
                handleException(
                        "Error from registry while checking whether a different letter case api name already exists",
                        e);
            }
        } else {
            handleException("Input api name value is null");
        }
        return apiWithDifferentCaseExist.toString();
    }

    private static DocumentationType getDocType(String docType) {
        DocumentationType docsType = null;
        for (DocumentationType type : DocumentationType.values()) {
            if (type.getType().equalsIgnoreCase(docType)) {
                docsType = type;
            }
        }
        return docsType;
    }

    private static boolean isStringValues(Object[] args) {
        int i = 0;
        for (Object arg : args) {

            if (!(arg instanceof String)) {
                return false;

            }
            i++;
        }
        return true;
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }


    public static NativeObject jsFunction_searchPaginatedAPIs(Context cx, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj) throws APIManagementException {
        if (args == null || args.length < 4) {
            handleException("Invalid number of parameters.");
        }

        NativeArray myn = new NativeArray(0);
        NativeObject resultObj = new NativeObject();
        Map<String, Object> result = new HashMap<String, Object>();

        String providerName = (String) args[0];
        providerName = APIUtil.replaceEmailDomain(providerName);
        String inputSearchQuery = (String) args[1];
        int start = Integer.parseInt((String) args[2]);
        int end = Integer.parseInt((String) args[3]);
        boolean limitAttributes = false;
        if (args.length == 5) {
            limitAttributes = Boolean.parseBoolean((String) args[4]);
        }
        String newSearchQuery  = APIUtil.constructNewSearchQuery(inputSearchQuery);
        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(
                    ((APIProviderHostObject) thisObj).getUsername()));
            result = apiProvider.searchPaginatedAPIs(newSearchQuery, tenantDomain, start, end, limitAttributes);

            if (newSearchQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX_WITH_EQUALS)) {
                Map<Documentation, API> apiDocMap = (Map<Documentation, API>) result.get("apis");
                if (apiDocMap != null) {
                    int i = 0;
                    for (Map.Entry<Documentation, API> entry : apiDocMap.entrySet()) {
                        Documentation doc = entry.getKey();
                        API api = entry.getValue();
                        APIIdentifier apiIdentifier = api.getId();

                        NativeObject currentApi = new NativeObject();

                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
                        currentApi.put("provider", currentApi,
                                APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                        currentApi.put("version", currentApi,
                                apiIdentifier.getVersion());
                        currentApi.put("status", currentApi, checkValue(api.getStatus()));
                        currentApi.put("thumb", currentApi, getWebContextRoot(api.getThumbnailUrl()));
                        currentApi.put("subs", currentApi, apiProvider.getSubscribersOfAPI(api.getId()).size());
                        if (providerName != null) {
                            currentApi.put("lastUpdatedDate", currentApi, checkValue(api.getLastUpdated().toString()));
                        }

                        currentApi.put("docName", currentApi, doc.getName());
                        currentApi.put("docSummary", currentApi, doc.getSummary());
                        currentApi.put("docSourceURL", currentApi, doc.getSourceUrl());
                        currentApi.put("docFilePath", currentApi, doc.getFilePath());

                        myn.put(i, myn, currentApi);
                        i++;
                    }
                    resultObj.put("apis", resultObj, myn);
                    resultObj.put("totalLength", resultObj, result.get("length"));
                }

            } else if (newSearchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX)) {
                ArrayList<Object> compoundResult = (result.get("apis") != null) ?
                        (ArrayList<Object>) result.get("apis") : null;
                NativeArray apiArray = new NativeArray(0);

                if (compoundResult != null && compoundResult.size() > 0) {
                    Iterator it = compoundResult.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        Object apiObject = it.next();
                        if (apiObject instanceof API) {
                            API api = (API) apiObject;
                            APIIdentifier apiIdentifier = api.getId();
                            NativeObject row = new NativeObject();
                            row.put("name", row, apiIdentifier.getApiName());
                            row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                            row.put("version", row, apiIdentifier.getVersion());
                            row.put("status", row, checkValue(api.getStatus()));
                            row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                            row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                            if (providerName != null) {
                                row.put("lastUpdatedDate", row, checkValue(api.getLastUpdated().toString()));
                            }
                            row.put("resultType", row, "API");
                            apiArray.put(i, apiArray, row);
                            i++;
                        } else if (apiObject instanceof Map.Entry) {
                            Map.Entry<Documentation, API> docEntry = (Map.Entry<Documentation, API>) apiObject;
                            NativeObject row = new NativeObject();
                            Documentation doc = docEntry.getKey();
                            API api = docEntry.getValue();
                            APIIdentifier apiIdentifier = api.getId();

                            row.put("name", row, apiIdentifier.getApiName());
                            row.put("provider", row,
                                    APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                            row.put("version", row, apiIdentifier.getVersion());
                            row.put("status", row, checkValue(api.getStatus()));
                            row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                            row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                            if (providerName != null) {
                                row.put("lastUpdatedDate", row, checkValue(api.getLastUpdated().toString()));
                            }

                            row.put("docName", row, doc.getName());
                            row.put("docSummary", row, doc.getSummary());
                            row.put("docSourceURL", row, doc.getSourceUrl());
                            row.put("docFilePath", row, doc.getFilePath());
                            row.put("docSourceType", row, doc.getSourceType().name());
                            row.put("resultType", row, "Document");
                            apiArray.put(i, apiArray, row);
                            i++;
                        }
                    }
                }

                resultObj.put("apis", resultObj, apiArray);
                resultObj.put("totalLength", resultObj, result.get("length"));
                resultObj.put("isMore", resultObj, result.get("isMore"));
            } else {
                Set<API> apiSet = (Set<API>) result.get("apis");
                //List<API> searchedList = apiProvider.searchAPIs(searchTerm, searchType, providerName);
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("status", row, checkValue(api.getStatus()));
                    row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                    row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                    if (providerName != null) {
                        row.put("lastUpdatedDate", row, checkValue(api.getLastUpdated().toString()));
                    }
                    myn.put(i, myn, row);
                    i++;
                }
                resultObj.put("apis", resultObj, myn);
                resultObj.put("totalLength", resultObj, result.get("length"));
                resultObj.put("isMore", resultObj, result.get("isMore"));
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the searched API- " + inputSearchQuery, e);
        }
        return resultObj;
    }

    public static NativeArray jsFunction_searchAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj) throws APIManagementException {
        NativeArray myn = new NativeArray(0);

        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String fullyQualifiedProviderName = (String) args[0];
        String providerName = APIUtil.replaceEmailDomain(fullyQualifiedProviderName);
        String searchValue = (String) args[1];
        String searchTerm;
        String searchType;

        if (searchValue.contains(":")) {
            if (searchValue.split(":").length > 1) {
                searchType = searchValue.split(":")[0];
                searchTerm = searchValue.split(":")[1];
            } else {
                throw new APIManagementException("Search term is missing. Try again with valid search query.");
            }

        } else {
            searchTerm = searchValue;
            searchType = "default";
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(fullyQualifiedProviderName);
        boolean isTenantFlowStarted = false;

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                isTenantFlowStarted = true;
            }
            if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                searchTerm = searchTerm.replaceFirst("\\*", ".*");
            }
            APIProvider apiProvider = getAPIProvider(thisObj);

            if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchType)) {
                Map<Documentation, API> apiDocMap = apiProvider.searchAPIsByDoc(searchTerm, searchType);
                if (apiDocMap != null) {
                    int i = 0;
                    for (Map.Entry<Documentation, API> entry : apiDocMap.entrySet()) {
                        Documentation doc = entry.getKey();
                        API api = entry.getValue();
                        APIIdentifier apiIdentifier = api.getId();

                        NativeObject currentApi = new NativeObject();

                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
                        currentApi.put("provider", currentApi,
                                APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                        currentApi.put("version", currentApi,
                                apiIdentifier.getVersion());
                        currentApi.put("status", currentApi, checkValue(api.getStatus()));
                        currentApi.put("thumb", currentApi, getWebContextRoot(api.getThumbnailUrl()));
                        currentApi.put("subs", currentApi, apiProvider.getSubscribersOfAPI(api.getId()).size());
                        if (providerName != null) {
                            currentApi.put("lastUpdatedDate", currentApi, checkValue(api.getLastUpdated().toString()));
                        }

                        currentApi.put("docName", currentApi, doc.getName());
                        currentApi.put("docSummary", currentApi, doc.getSummary());
                        currentApi.put("docSourceURL", currentApi, doc.getSourceUrl());
                        currentApi.put("docFilePath", currentApi, doc.getFilePath());

                        myn.put(i, myn, currentApi);
                        i++;
                    }
                }

            } else {
                List<API> searchedList = apiProvider.searchAPIs(searchTerm, searchType, providerName);
                Iterator it = searchedList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("status", row, checkValue(api.getStatus()));
                    row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                    row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                    if (providerName != null) {
                        row.put("lastUpdatedDate", row, checkValue(api.getLastUpdated().toString()));
                    }
                    myn.put(i, myn, row);
                    i++;


                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the searched API- " + searchValue, e);
        } finally {
            if(isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }


    public static boolean jsFunction_hasCreatePermission(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkCreatePermission();
                return true;
            } catch (APIManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean jsFunction_hasManageTierPermission(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkManageTiersPermission();
                return true;
            } catch (APIManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean jsFunction_hasUserPermissions(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String username = (String) args[0];
        return APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_CREATE) ||
                APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_PUBLISH);
    }

    public static boolean jsFunction_hasPublishPermission(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkPublishPermission();
                return true;
            } catch (APIManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static void jsFunction_loadRegistryOfTenant(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        String tenantDomain = args[0].toString();
        if (tenantDomain != null && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error("Could not load tenant registry. Error while getting tenant id from tenant domain " +
                        tenantDomain, e);
            } catch (RegistryException e) {
                log.error("Could not load tenant registry for tenant " + tenantDomain, e);
            }
        }

    }


    /**
     * load axis configuration for the tenant
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     */
    public static void jsFunction_loadAxisConfigOfTenant(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj) {
        String tenantDomain = args[0].toString();
        if (tenantDomain != null &&
                !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            APIUtil.loadTenantConfig(tenantDomain);
        }
    }

    public static NativeArray jsFunction_getLifeCycleEvents(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws APIManagementException {
        NativeArray lifeCycles = new NativeArray(0);
        if (args == null) {
            handleException("Invalid input parameters.");
        }
        NativeObject apiData = (NativeObject) args[0];
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            List<LifeCycleEvent> lifeCycleEvents = apiProvider.getLifeCycleEvents(apiId);
            int i = 0;
            if (lifeCycleEvents != null) {
                for (LifeCycleEvent lcEvent : lifeCycleEvents) {
                    NativeObject event = new NativeObject();
                    event.put("username", event, APIUtil.replaceEmailDomainBack(checkValue(lcEvent.getUserId())));
                    event.put("newStatus", event, lcEvent.getNewStatus() != null ? lcEvent.getNewStatus().toString() : "");
                    event.put("oldStatus", event, lcEvent.getOldStatus() != null ? lcEvent.getOldStatus().toString() : "");

                    event.put("date", event, checkValue(Long.valueOf(lcEvent.getDate().getTime()).toString()));
                    lifeCycles.put(i, lifeCycles, event);
                    i++;
                }
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while checking the input context is already exist", e);
        }
        return lifeCycles;
    }

    public static void jsFunction_removeAPI(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj)
            throws APIManagementException {
        if (args == null) {
            handleException("Invalid input parameters.");
        }
        NativeObject apiData = (NativeObject) args[0];

        String provider = (String) apiData.get("provider", apiData);
        provider = APIUtil.replaceEmailDomain(provider);
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIProvider apiProvider = getAPIProvider(thisObj);
            // delete the local Entry for given API ID
            API api = apiProvider.getAPI(apiId);
            apiProvider.deleteSwaggerLocalEntry(api);
            apiProvider.deleteAPI(apiId);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

            if (apiId.toString() != null) {
                keyManager.deleteRegisteredResourceByAPIId(apiId.toString());
            }

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private static class APISubscription {
        private String name;
        private long count;
        private String version;
    }

    public static boolean jsFunction_updateDocumentation(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws APIManagementException, ScriptException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success = false;
        String providerName = (String) args[0];
        providerName = APIUtil.replaceEmailDomain(providerName);
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];
        String summary = (String) args[5];
        String sourceType = (String) args[6];
        String visibility = (String) args[10];
        String sourceURL = null;
        FileHostObject fileHostObject = null;

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
            Documentation doc = new Documentation(getDocType(docType), docName);
            APIProvider apiProvider = getAPIProvider(thisObj);

            //update documentation is allowed only if documentation name already exists for this api
            if (!apiProvider.isDocumentationExist(apiId, docName)) {
                handleException("Error occurred while updating the document. " + docName +
                        " does not exist for API " + apiName + '-' + version);
            }

            if (doc.getType() == DocumentationType.OTHER) {
                doc.setOtherTypeName(args[9].toString());
            }

            if (Documentation.DocumentSourceType.URL.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.URL);
                sourceURL = args[7].toString();
            } else if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.FILE);
                fileHostObject = (FileHostObject) args[8];
            } else if (Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                doc.setSourceType(Documentation.DocumentSourceType.MARKDOWN);
            } else {
                doc.setSourceType(Documentation.DocumentSourceType.INLINE);
            }
            doc.setSummary(summary);
            doc.setSourceUrl(sourceURL);
            if (visibility == null) {
                visibility = APIConstants.DOC_API_BASED_VISIBILITY;
            }
            if (Documentation.DocumentVisibility.API_LEVEL.toString().equalsIgnoreCase(visibility)) {
                doc.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
            } else if (Documentation.DocumentVisibility.PRIVATE.toString().equalsIgnoreCase(visibility)) {
                doc.setVisibility(Documentation.DocumentVisibility.PRIVATE);
            } else {
                doc.setVisibility(Documentation.DocumentVisibility.OWNER_ONLY);
            }

            Documentation oldDoc = apiProvider.getDocumentation(apiId, doc.getType(), doc.getName());

            try {
                if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                    String extension = FilenameUtils.getExtension(fileHostObject.getJavaScriptFile().getName());
                    if (extension.contains("exe")) {
                        throw new APIManagementException("File type .exe is not supported!");
                    }
                    ResourceFile resourceFile = new ResourceFile(fileHostObject.getInputStream(),
                            fileHostObject.getJavaScriptFile().getContentType());
                    String filePath = APIUtil.getDocumentationFilePath(apiId, fileHostObject.getName());
                    doc.setFilePath(apiProvider.addResourceFile(filePath, resourceFile));
                } else if (oldDoc.getFilePath() != null) {
                    doc.setFilePath(oldDoc.getFilePath());
                }
            } catch (APIManagementException e) {
                handleException("Failed to add file to document " + doc.getName(), e);
            }
            apiProvider.updateDocumentation(apiId, doc);
            success = true;

        } catch (ScriptException e) {
            handleException("The attachment cannot be found for document- " + docName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_isAPIOlderVersionExist(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws APIManagementException {
        boolean apiOlderVersionExist = false;
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String provider = (String) apiData.get("provider", apiData);
        provider = APIUtil.replaceEmailDomain(provider);
        String name = (String) apiData.get("name", apiData);
        String currentVersion = (String) apiData.get("version", apiData);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIProvider apiProvider = getAPIProvider(thisObj);
            Set<String> versions = apiProvider.getAPIVersions(provider, name);
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            for (String version : versions) {
                if (comparator.compare(version, currentVersion) < 0) {
                    apiOlderVersionExist = true;
                    break;
                }
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return apiOlderVersionExist;
    }

    /**
     * This method is used to edit the endpoint URL provided during the implementation stage in the publisher
     * based on the resource url patterns provided in the design stage of an API.It is used in jsFunction_isURLValid()
     */
    public static NativeObject editEndpointUrlToTest(String urlVal, Context cx, Scriptable thisObj, Object[] args,
                                                     Function funObj) throws APIManagementException {

        String urlValue = urlVal;
        boolean isContainUriTemplatesOnly = false;
        NativeObject data = new NativeObject();

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }

        String providerName = args.length > 2 ? (String) args[2] : "";
        String apiName = args.length > 3 ? (String) args[3] : "";
        String apiVersion = args.length > 4 ? (String) args[4] : "";

        if (providerName != null) {
            providerName = APIUtil.replaceEmailDomain(providerName);
        }

        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
        APIProvider apiProvider = getAPIProvider(thisObj);

        API api = null;
        try {
            api = apiProvider.getAPI(apiIdentifier);
        } catch (APIManagementException e) {
            handleException("Cannot find the requested API- " + apiName + "-" + apiVersion, e);
        }

        if (api != null) {

            Set<URITemplate> uriTemplates = api.getUriTemplates();

            if (uriTemplates.size() != 0) {

                Iterator i = uriTemplates.iterator();
                List<String> urlPatternArray = new ArrayList<String>();
                while (i.hasNext()) {
                    URITemplate ut = (URITemplate) i.next();
                    urlPatternArray.add(ut.getUriTemplate());
                }

                if (urlPatternArray.contains("/*")) { //Checking whether the urlPatternArray contains /*
                    data.put("urlValue", data, urlValue);
                    data.put("isContainUriTemplatesOnly", data, false);
                    return data;
                } else {
                    for (String urlPattern : urlPatternArray) {
                        //to check whether it is a uri-template
                        Matcher matcher = pathParamExtractorPattern.matcher(urlPattern);

                        if (matcher.find()) {
                            isContainUriTemplatesOnly = true;
                        } else {
                            urlValue = urlValue + urlPattern;
                            isContainUriTemplatesOnly = false;
                            break;
                        }
                    }
                }

            }
        }

        data.put("urlValue", data, urlValue);
        data.put("isContainUriTemplatesOnly", data, isContainUriTemplatesOnly);
        return data;

    }

    public static NativeObject jsFunction_isURLValid(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        boolean isConnectionError = true;
        String response = null;
        boolean isContainUriTemplatesOnly = false;//To check whether the resources contain only uri templates
        NativeObject data = new NativeObject();

        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String urlVal = (String) args[1];
        String type = (String) args[0];
        String providerName = args.length > 2 ? (String) args[2] : "";
        String apiName = args.length > 3 ? (String) args[3] : "";
        String apiVersion = args.length > 4 ? (String) args[4] : "";
        String invalidStatusCodesRegex = args.length > 5 ? (String) args[5] : "404";
        if (urlVal != null && !urlVal.isEmpty()) {
            urlVal = urlVal.trim();

            try {

                if (type != null && type.equals("wsdl")) {
                    validateWsdl(urlVal);
                    response = "success";
                    isConnectionError = false;
                } else {
                    // checking http,https endpoints up to resource level by doing
                    // http HEAD. And other end point
                    // validation do through basic url connect
                    if (!StringUtils.equals(providerName, "") && !StringUtils.equals(apiName, "") && !StringUtils
                            .equals(apiVersion, "")) { //To escape editing the url for auto validation of wsdl endpoints

                        NativeObject obj = editEndpointUrlToTest(urlVal, cx, thisObj, args, funObj);
                        urlVal = (String) obj.get("urlValue");

                        if (obj.get("isContainUriTemplatesOnly").equals(true)) {
                            isContainUriTemplatesOnly = true;
                        }
                    }

                    URL url = new URL(urlVal);

                    if (url.getProtocol().matches("https")) {
                        ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
                        String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
                        String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
                        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);


                        String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
                        String keyStoreType = serverConfig.getFirstProperty("Security.KeyStore.Type");
                        String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
                        System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
                        System.setProperty("javax.net.ssl.keyStore", keyStore);
                        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

                        NativeObject headRequestResult = HostObjectUtils.sendHttpHEADRequest(urlVal, invalidStatusCodesRegex);
                        headRequestResult.put("isContainUriTemplatesOnly", headRequestResult, isContainUriTemplatesOnly);
                        return headRequestResult;

                    } else if (url.getProtocol().matches("http")) {
                        NativeObject headRequestResult = HostObjectUtils.sendHttpHEADRequest(urlVal, invalidStatusCodesRegex);
                        headRequestResult.put("isContainUriTemplatesOnly", headRequestResult, isContainUriTemplatesOnly);
                        return headRequestResult;
                    }
                }
            } catch (Exception e) {
                response = e.getMessage();
            }
        }

        data.put("response", data, response);
        data.put("isConnectionError", data, isConnectionError);
        data.put("isContainUriTemplatesOnly", data, isContainUriTemplatesOnly);
        return data;

    }

    private boolean resourceMethodMatches(String[] resourceMethod1,
                                          String[] resourceMethod2) {
        for (String m1 : resourceMethod1) {
            for (String m2 : resourceMethod2) {
                if (m1.equals(m2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void validateWsdl(String url) throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(url);
        wsdlReader.validateBaseURI();
    }

    private static String getWebContextRoot(String postfixUrl) {
        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (postfixUrl != null && webContext != null && !webContext.equals("/")) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }


    public static NativeArray jsFunction_searchAccessTokens(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws Exception {
        NativeObject tokenInfo;
        NativeArray tokenInfoArr = new NativeArray(0);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String searchValue = (String) args[0];
        String searchTerm;
        String searchType;
        APIProvider apiProvider = getAPIProvider(thisObj);
        Map<Integer, APIKey> tokenData;
        String loggedInUser = ((APIProviderHostObject) thisObj).getUsername();

        if (searchValue.contains(":")) {
            searchTerm = searchValue.split(":")[1];
            searchType = searchValue.split(":")[0];
            if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                searchTerm = searchTerm.replaceFirst("\\*", ".*");
            }
            tokenData = apiProvider.searchAccessToken(searchType, searchTerm, loggedInUser);
        } else {
            //Check whether old access token is already available
            if (apiProvider.isApplicationTokenExists(searchValue)) {
                APIKey tokenDetails = apiProvider.getAccessTokenData(searchValue);
                if (tokenDetails.getAccessToken() == null) {
                    throw new APIManagementException("The requested access token is already revoked or No access token available as per requested.");
                }
                tokenData = new HashMap<Integer, APIKey>();
                tokenData.put(0, tokenDetails);
            } else {
                if ("*".equals(searchValue) || searchValue.startsWith("*")) {
                    searchValue = searchValue.replaceFirst("\\*", ".*");
                }
                tokenData = apiProvider.searchAccessToken(null, searchValue, loggedInUser);
            }
        }
        if (tokenData != null && tokenData.size() != 0) {
            for (int i = 0; i < tokenData.size(); i++) {
                tokenInfo = new NativeObject();
                tokenInfo.put("token", tokenInfo, tokenData.get(i).getAccessToken());
                tokenInfo.put("user", tokenInfo, tokenData.get(i).getAuthUser());
                tokenInfo.put("scope", tokenInfo, tokenData.get(i).getTokenScope());
                tokenInfo.put("createTime", tokenInfo, tokenData.get(i).getCreatedDate());
                if (tokenData.get(i).getValidityPeriod() == Long.MAX_VALUE) {
                    tokenInfo.put("validTime", tokenInfo, "Won't Expire");
                } else {
                    tokenInfo.put("validTime", tokenInfo, tokenData.get(i).getValidityPeriod());
                }
                tokenInfo.put("consumerKey", tokenInfo, tokenData.get(i).getConsumerKey());
                tokenInfoArr.put(i, tokenInfoArr, tokenInfo);
            }
        } else {
            throw new APIManagementException("The requested access token is already revoked or No access token available as per requested.");
        }

        return tokenInfoArr;

    }

    public static void jsFunction_revokeAccessToken(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws Exception {
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String accessToken = (String) args[0];
        String consumerKey = (String) args[1];
        String authUser = (String) args[2];
        APIProvider apiProvider = getAPIProvider(thisObj);

        try {
            SubscriberKeyMgtClient keyMgtClient = HostObjectUtils.getKeyManagementClient();
            if (keyMgtClient != null) {
                keyMgtClient.revokeAccessToken(accessToken, consumerKey, authUser);
            }

            Set<APIIdentifier> apiIdentifierSet = apiProvider.getAPIByAccessToken(accessToken);
            List<org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping> mappings = new ArrayList<org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping>();
            for (APIIdentifier apiIdentifier : apiIdentifierSet) {
                org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping mapping = new org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping();
                API apiDefinition = apiProvider.getAPI(apiIdentifier);
                if (apiDefinition != null) {
                    mapping.setApiVersion(apiIdentifier.getVersion());
                    mapping.setContext(apiDefinition.getContext());
                    mapping.setKey(accessToken);
                    mappings.add(mapping);
                }
            }
            if (mappings.size() > 0) {
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();
                for (Environment environment : gatewayEnvs.values()) {
                    APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                    client.invalidateKeys(mappings);
                }


            }
        } catch (Exception e) {
            handleException("Error while revoking the access token: " + accessToken, e);

        }


    }

    public static boolean jsFunction_validateRoles(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj) {
        if (args == null || args.length == 0) {
            return false;
        }

        boolean valid = false;
        String inputRolesSet = (String) args[0];
        String username = (String) args[1];
        String[] inputRoles = null;
        boolean foundUserRole = false;
        boolean validateAgainstUserRoles = false;
        if (inputRolesSet != null) {
            inputRoles = inputRolesSet.replaceAll("\\s+", "").split(",");
        }
        if (args.length == 3 && Boolean.parseBoolean((String) args[2])) {
            validateAgainstUserRoles = true;
        }

        try {
            String[] userRoleList = null;

            if (validateAgainstUserRoles) {
                if (APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)){
                    foundUserRole = true;
                } else {
                    userRoleList = APIUtil.getListOfRoles(username);
                }
            }
            if (inputRoles != null) {
                for (String inputRole : inputRoles) {
                    if (validateAgainstUserRoles && !foundUserRole) {
                        if (APIUtil.compareRoleList(userRoleList, inputRole)) {
                            foundUserRole = true;
                        }
                    }
                    if (!APIUtil.isRoleNameExist(username, inputRole)) {
                        return false;
                    }
                }
                return !validateAgainstUserRoles || foundUserRole;
            }
        } catch (Exception e) {
            log.error("Error while validating the input roles.", e);
        }
        return false;
    }

    /**
     * Retrieves custom sequences from registry
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static NativeArray jsFunction_getCustomOutSequences(Context cx, Scriptable thisObj,
                                                               Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || args.length < 3) {
            handleException("Invalid input parameters.");
        }
        APIProvider apiProvider = getAPIProvider(thisObj);
        String apiName = (String) args[0];
        String apiVersion = (String) args[1];
        String provider = (String) args[2];

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, apiVersion);

        List<String> sequenceList = apiProvider.getCustomOutSequences(apiIdentifier);

        NativeArray myn = new NativeArray(0);
        if (sequenceList == null) {
            return null;
        } else {
            for (int i = 0; i < sequenceList.size(); i++) {
                myn.put(i, myn, sequenceList.get(i));
            }
            return myn;
        }

    }

    /**
     * Retrieves custom sequences from registry
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static NativeArray jsFunction_getCustomInSequences(Context cx, Scriptable thisObj,
                                                              Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || args.length < 3) {
            handleException("Invalid input parameters.");
        }
        APIProvider apiProvider = getAPIProvider(thisObj);

        String apiName = (String) args[0];
        String apiVersion = (String) args[1];
        String provider = (String) args[2];

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, apiVersion);

        List<String> sequenceList = apiProvider.getCustomInSequences(apiIdentifier);

        NativeArray myn = new NativeArray(0);
        if (sequenceList == null) {
            return null;
        } else {
            for (int i = 0; i < sequenceList.size(); i++) {
                myn.put(i, myn, sequenceList.get(i));
            }
            return myn;
        }

    }

    /**
     * Retrieves custom fault sequences from registry
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static NativeArray jsFunction_getCustomFaultSequences(Context cx, Scriptable thisObj,
                                                                 Object[] args, Function funObj)
            throws APIManagementException {
        List<String> sequenceList = null;

        APIProvider apiProvider = getAPIProvider(thisObj);

        if (args == null || args.length >= 3) {
            String apiName = (String) args[0];
            String apiVersion = (String) args[1];
            String provider = (String) args[2];

            if (provider != null) {
                provider = APIUtil.replaceEmailDomain(provider);
            }
            APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, apiVersion);

            sequenceList = apiProvider.getCustomFaultSequences(apiIdentifier);
        } else {
            sequenceList = apiProvider.getCustomFaultSequences();
        }


        NativeArray myn = new NativeArray(0);
        if (sequenceList == null) {
            return null;
        } else {
            for (int i = 0; i < sequenceList.size(); i++) {
                myn.put(i, myn, sequenceList.get(i));
            }
            return myn;
        }
    }

    public static boolean jsFunction_isSynapseGateway(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        APIProvider provider = getAPIProvider(thisObj);
        return provider.isSynapseGateway();
    }

    public static boolean jsFunction_updateExternalAPIStores(Context cx, Scriptable thisObj, Object[] args,
                                                             Function funObj)
            throws APIManagementException {
        boolean updated = false;
        boolean isTenantFlowStarted = false;

        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIProvider apiProvider = getAPIProvider(thisObj);

            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = apiProvider.getAPI(apiId);
            //Getting selected external API stores from UI and publish API to them.
            NativeArray externalAPIStores = (NativeArray) apiData.get("externalAPIStores", apiData);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantManager().getTenantId(tenantDomain);
            //Check if no external APIStore selected from UI
            if (api != null && externalAPIStores != null) {
                Set<APIStore> inputStores = new HashSet<APIStore>();
                for (Object store : externalAPIStores) {
                    inputStores.add(APIUtil.getExternalAPIStore((String) store, tenantId));
                }
                Set<String> versions = apiProvider.getAPIVersions(provider, name);
                APIVersionStringComparator comparator = new APIVersionStringComparator();
                boolean apiOlderVersionExist = false;
                for (String tempVersion : versions) {
                    if (comparator.compare(tempVersion, version) < 0) {
                        apiOlderVersionExist = true;
                        break;
                    }
                }
                updated = apiProvider.updateAPIsInExternalAPIStores(api, inputStores, apiOlderVersionExist);

            }
            return updated;
        } catch (UserStoreException e) {
            handleException("Error while updating external api stores", e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public static String jsFunction_getAPIStoreURL(Context cx, Scriptable thisObj, Object[] args,
                                                   Function funObj) throws APIManagementException {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();

        //if a tenant is passed return the tenant store url
        if (args != null && args.length > 0 && args[0] != null) {
            String tenantDomain = args[0].toString();
            APIProvider apiProvider = getAPIProvider(thisObj);
            Map<String, String> domains = apiProvider.getTenantDomainMappings(tenantDomain, APIConstants.API_DOMAIN_MAPPINGS_STORE);
            if (domains != null && domains.size() != 0) {
                Iterator entries = domains.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) entries.next();
                    return "https://" + thisEntry.getValue();
                }
            }
        }

        if (config != null) {
            return config.getFirstProperty(APIConstants.API_STORE_URL);
        } else {
            return null;
        }
    }

    public static boolean jsFunction_isDataPublishingEnabled(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        return HostObjectUtils.checkDataPublishingEnabled();
    }

    public static boolean jsFunction_showAPIStoreURL(Context cx, Scriptable thisObj, Object[] args,
                                                     Function funObj) {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();

        return config != null && Boolean.parseBoolean(config.getFirstProperty(APIConstants.SHOW_API_STORE_URL_FROM_PUBLISHER));
    }

    public static boolean jsFunction_showAPIDocVisibility(Context cx, Scriptable thisObj, Object[] args,
                                                          Function funObj) {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();

        return config != null
                && Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));

    }

    /**
     * Evaluate HTTP end-point URI to validate path parameter and query
     * parameter formats<br>
     * Sample URI format<br>
     * http[s]//[www.]anyhost[.com][:port]/{uri.var.param}?param1=value&param2={uri.var.value}
     *
     * @param endpointConfig JSON representation of end-point configuration.
     * @return true if valid URI
     * @throws APIManagementException If the endpointConfig is invalid or URI is invalid
     */
    private static boolean validateEndpointURI(String endpointConfig)
            throws APIManagementException {
        if (endpointConfig != null) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(endpointConfig);
                Object epType = jsonObject.get("endpoint_type");

                if (StringUtils.isEmpty(ObjectUtils.toString(epType))) {
                    handleException("No endpoint type defined.");

                } else if (epType instanceof String && "http".equals(epType)) {
                    // extract production uri from config
                    Object prodEPs = jsonObject.get("production_endpoints");
                    Object sandEPs = jsonObject.get("sandbox_endpoints");

                    if (prodEPs == null && sandEPs == null) {
                        handleException("At least one endpoint from Production Endpoint or Sandbox Endpoint must be defined.");
                    }
                    if (prodEPs instanceof JSONObject) {
                        Object url = ((JSONObject) prodEPs).get("url");//check whether the URL is null or not

                        if (StringUtils.isBlank(ObjectUtils.toString(url))) {
                            handleException("URL of production Endpoint is not defined.");
                        }
                        if (url instanceof String && !isValidURI(url.toString())) {
                            handleException("Invalid Production Endpoint URI. Please refer HTTP Endpoint " +
                                    "documentation of the WSO2 ESB for details.");
                        }
                    }
                    // extract sandbox uri from config
                    if (sandEPs instanceof JSONObject) {
                        Object url = ((JSONObject) sandEPs).get("url");

                        if (StringUtils.isBlank(ObjectUtils.toString(url))) {
                            handleException("URL of sandbox Endpoint is not defined.");
                        }
                        if (url instanceof String && !isValidURI(url.toString())) {
                            handleException("Invalid Sandbox Endpoint URI. Please refer HTTP Endpoint " +
                                    "documentation of the WSO2 ESB for details.");
                        }
                    }
                }
            } catch (ParseException e) {
                handleException("Invalid Endpoint config", e);
            }
        }
        return true;
    }

    /**
     * This method returns whether the given url is contain valid uri params or not
     *
     * @param url URL to be validated
     * @return true if URI doesn't contain params or contains valid params
     */
    private static boolean isValidURI(String url) {
        boolean isInvalid = false;
        // validate only if uri contains { or }
        if (url != null && (url.contains("{") || url.contains("}"))) {
            // check { and } are matched or not. otherwise invalid
            int startCount = 0, endCount = 0;
            for (char c : url.toCharArray()) {
                if (c == '{') {
                    startCount++;
                } else if (c == '}') {
                    endCount++;
                }
                // this check guarantee the order of '{' and '}'. Ex: {uri.var.name} not }uri.var.name{
                if (endCount > startCount) {
                    isInvalid = true;
                    break;
                }
            }
            // continue only if the matching no of brackets are found. otherwise invalid
            if (startCount == endCount) {
                // extract content including { } brackets
                Matcher pathParamMatcher = pathParamExtractorPattern.matcher(url);
                while (pathParamMatcher.find()) {
                    // validate the format of { } content
                    Matcher formatMatcher = pathParamValidatorPattern.matcher(pathParamMatcher.group());
                    if (!formatMatcher.matches()) {
                        isInvalid = true;
                        break;
                    }
                }
            } else {
                isInvalid = true;
            }
        }
        return !isInvalid;
    }

    /**
     * Validate the backend by sending HTTP HEAD
     *
     * @param urlVal                  - backend URL
     * @param invalidStatusCodesRegex - Regex for the invalid status code
     * @return - status of HTTP HEAD Request to backend
     */
    private static NativeObject sendHttpHEADRequest(String urlVal, String invalidStatusCodesRegex) {

        boolean isConnectionError = true;
        String response = null;

        NativeObject data = new NativeObject();

        HttpClient client = new DefaultHttpClient();
        HttpHead head = new HttpHead(urlVal);
        // extract the host name and add the Host http header for sanity
        head.addHeader("Host", urlVal.replaceAll("https?://", "").replaceAll("(/.*)?", ""));
        client.getParams().setParameter("http.socket.timeout", 4000);
        client.getParams().setParameter("http.connection.timeout", 4000);


        if (System.getProperty(APIConstants.HTTP_PROXY_HOST) != null &&
                System.getProperty(APIConstants.HTTP_PROXY_PORT) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Proxy configured, hence routing through configured proxy");
            }
            String proxyHost = System.getProperty(APIConstants.HTTP_PROXY_HOST);
            String proxyPort = System.getProperty(APIConstants.HTTP_PROXY_PORT);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
        }

        try {
            HttpResponse httpResponse = client.execute(head);
            String statusCode = String.valueOf(httpResponse.getStatusLine().getStatusCode());
            String reasonPhrase = String.valueOf(httpResponse.getStatusLine().getReasonPhrase());
            //If the endpoint doesn't match the regex which specify the invalid status code, it will return success.
            if (!statusCode.matches(invalidStatusCodesRegex)) {
                if (log.isDebugEnabled() && statusCode.equals(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED))) {
                    log.debug("Endpoint doesn't support HTTP HEAD");
                }
                response = "success";
                isConnectionError = false;

            } else {
                //This forms the real backend response to be sent to the client
                data.put("statusCode", data, statusCode);
                data.put("reasonPhrase", data, reasonPhrase);
                response = "";
                isConnectionError = false;
            }
        } catch (IOException e) {
            // sending a default error message.
            log.error("Error occurred while connecting to backend : " + urlVal + ", reason : " + e.getMessage(), e);
            String[] errorMsg = e.getMessage().split(": ");
            if (errorMsg.length > 1) {
                response = errorMsg[errorMsg.length - 1]; //This is to get final readable part of the error message in the exception and send to the client
                isConnectionError = false;
            }
        } finally {
            client.getConnectionManager().shutdown();
        }
        data.put("response", data, response);
        data.put("isConnectionError", data, isConnectionError);
        return data;
    }

    /**
     * retrieves active tenant domains and return true or false to display private
     * visibility
     *
     * @return boolean true If display private visibility
     */
    public static boolean jsFunction_isMultipleTenantsAvailable() {
        int tenantsDomainSize;
        Object cacheObj = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                getCache(APIConstants.APIPROVIDER_HOSTCACHE).get(APIConstants.TENANTCOUNT_CACHEKEY);
        //if tenantDomainSize is not in the cache, Then the cache object is null
        if (cacheObj == null) {
            tenantsDomainSize = 0;
        } else {
            tenantsDomainSize = Integer.parseInt(cacheObj.toString());
        }
        //if there only super tenant in the system, tenantDomainSize is 1
        if (tenantsDomainSize < 2) {
            try {
                Set<String> tenantDomains = APIUtil.getActiveTenantDomains();
                //if there is more than than one tenant
                if (tenantDomains.size() > 1) {
                    Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                            getCache(APIConstants.APIPROVIDER_HOSTCACHE).
                            put(APIConstants.TENANTCOUNT_CACHEKEY, String.valueOf(tenantDomains.size()));
                    return true;
                } else {
                    return false;
                }
            } catch (UserStoreException e) {
                /*If there are errors in getting active tenant domains from user store,
                 Minimum privileges are allocated to the user
                */
                log.error("Errors in getting active tenants form UserStore " + e.getMessage(), e);
                return false;
            }
        } else {
            return true;
        }
    }

    /*
	* here return boolean with checking all objects in array is string
	*/
    public static boolean isStringArray(Object[] args) {
//        int argsCount = args.length;
        for (Object arg : args) {
            if (!(arg instanceof String)) {
                return false;
            }
        }
        return true;

    }

    /**
     * This method is to Download API-DOCS from APIPublisher
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return NativeObject that contains Input stream of Downloaded File
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeObject jsFunction_getDocument(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws ScriptException,
            APIManagementException {
        if (args == null || args.length != 2 || !isStringArray(args)) {
            handleException("Invalid input parameters expected resource Url and tenantDomain");
        }
        NativeObject data = new NativeObject();

        String username = ((APIProviderHostObject) thisObj).getUsername();
        // Set anonymous user if no user is login to the system
        if (username == null) {
            username = APIConstants.END_USER_ANONYMOUS;
        }
        String resource = (String) args[1];
        String tenantDomain = (String) args[0];
        Map<String, Object> docResourceMap = APIUtil.getDocument(username, resource, tenantDomain);
        if (!docResourceMap.isEmpty()) {
            data.put("Data", data,
                    cx.newObject(thisObj, "Stream", new Object[]{docResourceMap.get("Data")}));
            data.put("contentType", data, docResourceMap.get("contentType"));
            data.put("name", data, docResourceMap.get("name"));
        } else {
            handleException("Resource couldn't found for " + resource);
        }
        return data;
    }

    /**
     * This method is to functionality of get list of environments that list in api-manager.xml
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return list of environments with details of environments
     */
    public static NativeArray jsFunction_getEnvironments(Context cx, Scriptable thisObj,
                                                         Object[] args,
                                                         Function funObj) {
        NativeArray myn = new NativeArray(1);
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        int i = 0;
        if (environments != null) {
            for (Environment environment : environments.values()) {
                NativeObject row = new NativeObject();
                row.put("name", row, environment.getName());
                row.put("description", row, environment.getDescription());
                row.put("type", row, environment.getType());
                row.put("serverURL", row, environment.getServerURL());
                row.put("apiConsole", row, environment.isShowInConsole());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static String jsFunction_isScopeExist(Context cx, Scriptable thisObj,
                                                 Object[] args, Function funObj)
            throws APIManagementException {
        Boolean scopeExist = false;
        if (args != null && isStringValues(args)) {
            String scopeKey = (String) args[0];
            String username = (String) args[1];

            if (!APIUtil.isWhiteListedScope(scopeKey)) {
                String tenantDomain = MultitenantUtils.getTenantDomain(username);
                //update permission cache before validate user
                int tenantId = -1234;
                try {
                    tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    handleException("Error while reading tenant information ", e);
                }

                APIProvider apiProvider = getAPIProvider(thisObj);

                try {
                    scopeExist = apiProvider.isScopeKeyExist(scopeKey, tenantId);
                } catch (APIManagementException e) {
                    handleException("Error from registry while checking the input context is already exist", e);
                }
            }
        } else {
            handleException("Input context value is null");
        }
        return scopeExist.toString();
    }

    /**
     * This method is used to upload backend certificate related to endpoints.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments {userName, alias, endpoint, certificate}
     * @param funObj  Function object
     * @return : True if uploading certificate is successful. False otherwise.
     */
    public static int jsFunction_uploadCertificate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if ((args == null) || (args.length != 4) || !isStringValues(args)) {
            handleException("Invalid number of arguments. Expect User Name, Alias, Endpoint and Certificate String.");
        }

        String userName = (String) args[0];
        String alias = (String) args[1];
        String endpoint = (String) args[2];
        String certificate = (String) args[3];

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.addCertificate(userName, certificate, alias, endpoint);
    }

    /**
     * This method is used to upload client certificate to support mutual ssl.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments {userName, api name, version, provider, certificate, alias}
     * @param funObj  Function object
     * @return : True if uploading certificate is successful. False otherwise.
     */
    public static int jsFunction_uploadClientCertificate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if ((args == null) || (args.length != 7) || !isStringValues(args)) {
            handleException(
                    "Invalid number of arguments. The method expects user name, api name, version. provider, alias, "
                            + "certificate string and tier name");
        }
        String userName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String provider = (String) args[3];
        String alias = (String) args[4];
        String certificate = (String) args[5];
        String tierName = (String) args[6];
        APIProvider apiProvider = getAPIProvider(thisObj);
        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName, version);
        return apiProvider.addClientCertificate(userName, apiIdentifier, certificate, alias, tierName);
    }

    /**
     * This method is used to remove backend certificate for the given alias and endpoint.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments {userName, alias, endpoint}
     * @param funObj  Function object
     * @return : True if deleting certificate is successful. False otherwise.
     */
    public static int jsFunction_deleteCertificate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        if ((args == null) || (args.length != 3) || !isStringValues(args)) {
            handleException("Invalid number of arguments. Expect User Name, Alias and Endpoint.");
        }

        String userName = (String) args[0];
        String alias = (String) args[1];
        String endpoint = (String) args[2];

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.deleteCertificate(userName, alias, endpoint);
    }

    /**
     * This method is used to remove client certificate for the given alias and API.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments {userName, alias, api name, version, provider}
     * @param funObj  Function object
     * @return : True if deleting certificate is successful. False otherwise.
     */
    public static int jsFunction_deleteClientCertificate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        if ((args == null) || (args.length != 5) || !isStringValues(args)) {
            handleException("Invalid number of arguments. The method expects user name, alias, api name, version and "
                    + "provider.");
        }
        String userName = (String) args[0];
        String alias = (String) args[1];
        String apiName = (String) args[2];
        String apiVersion = (String) args[3];
        String providerName = (String) args[4];
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider
                .deleteClientCertificate(userName, new APIIdentifier(providerName, apiName, apiVersion), alias);
    }

    /**
     * This method is to retrieve all the certificates belongs to the given tenant.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments {username}
     * @param funObj  Function object
     * @return A list of uploaded certificate metadata.
     */
    public static NativeArray jsFunction_getCertificates(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        NativeArray certificateMetaDataArray = new NativeArray(0);

        NativeObject certificateMetaData = new NativeObject();
        if ((args == null) || (args.length != 2) || !isStringValues(args)) {
            log.error("Invalid arguments. Expect User Name and Endpoint");
            return null;
        }

        String userName = (String) args[0];
        String endpoint = (String) args[1];

        APIProvider apiProvider = getAPIProvider(thisObj);
        List<CertificateMetadataDTO> certificates = apiProvider.getCertificates(userName);

        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        int i = 0;

        if (certificates != null) {
            for (CertificateMetadataDTO certificateMetadata : certificates) {
                NativeObject obj = new NativeObject();
                obj.put(ALIAS, obj, certificateMetadata.getAlias());
                obj.put(END_POINT, obj, certificateMetadata.getEndpoint());
                certificateMetaDataArray.put(i, certificateMetaDataArray, obj);
                i++;
            }
        }
        return certificateMetaDataArray;
    }

    /**
     * To get the client certificates related with an API.
     *
     * @param cx      Context.
     * @param thisObj Scriptable object
     * @param args    Arguments.
     * @param funObj  Function object.
     * @return Array of certificates uploaded against API.
     * @throws APIManagementException API Management Exception.
     */
    public static NativeArray jsFunction_getClientCertificates(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws APIManagementException {
        if ((args == null) || (args.length != 4) || !isStringValues(args)) {
            handleException(
                    "Invalid number of arguments. The method expects user name, api name, provider and version.");
        }
        String userName = (String) args[0];
        String apiName = (String) args[1];
        String provider = (String) args[2];
        String apiVersion = (String) args[3];
        NativeArray clientCertificateMetaData = new NativeArray(0);
        NativeObject certificateMetaData = new NativeObject();
        APIProvider apiProvider = getAPIProvider(thisObj);
        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName, apiVersion);
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        List<ClientCertificateDTO> clientCertificateDTOList = apiProvider
                .searchClientCertificates(APIUtil.getTenantIdFromTenantDomain(tenantDomain), null, apiIdentifier);
        int index = 0;
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                NativeObject obj = new NativeObject();
                obj.put(ALIAS, obj, clientCertificateDTO.getAlias());
                obj.put(TIER, obj, clientCertificateDTO.getTierName());
                clientCertificateMetaData.put(index, clientCertificateMetaData, obj);
                index++;
            }
        }
        return clientCertificateMetaData;
    }


    /**
     * This method is to check whether the required configuration is done in the AM distribution.
     *
     * @return : True if the configuration is present, false otherwise.
     */
    public static boolean jsFunction_isConfigured(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.isConfigured();
    }

    /**
     * This method check the whether the client certificate based authentication is enabled in AM level.
     *
     * @param cx      Context.
     * @param thisObj Scriptable object.
     * @param args    Arguments.
     * @param funObj  Function Object.
     * @return true if the client certificate based authentication is enabled in AM level.
     */
    public static boolean jsFunction_isClientCertificateBasedAuthenticationConfigured(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.isClientCertificateBasedAuthenticationConfigured();
    }

    /**
     * @param failedGateways map of failed environments
     * @return json string of input map
     */
    private static String createFailedGatewaysAsJsonString(Map<String, List<String>> failedGateways) {
        String failedJson = "{\"PUBLISHED\" : \"\" ,\"UNPUBLISHED\":\"\"}";
        if (failedGateways != null) {
            if (!failedGateways.isEmpty()) {
                StringBuilder failedToPublish = new StringBuilder();
                StringBuilder failedToUnPublish = new StringBuilder();
                for (String environmentName : failedGateways.get("PUBLISHED")) {
                    failedToPublish.append(environmentName + ",");
                }
                for (String environmentName : failedGateways.get("UNPUBLISHED")) {
                    failedToUnPublish.append(environmentName + ",");
                }
                if (!"".equals(failedToPublish.toString())) {
                    failedToPublish.deleteCharAt(failedToPublish.length() - 1);
                }
                if (!"".equals(failedToUnPublish.toString())) {
                    failedToUnPublish.deleteCharAt(failedToUnPublish.length() - 1);
                }
                failedJson = "{\"PUBLISHED\" : \"" + failedToPublish.toString() + "\" ,\"UNPUBLISHED\":\"" +
                        failedToUnPublish.toString() + "\"}";
            }
        }
        return failedJson;
    }

    public static String userAgentParser(String userAgent) {
        String userBrowser;
        if (userAgent.contains("Chrome")) {
            userBrowser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            userBrowser = "Firefox";
        } else if (userAgent.contains("Opera")) {
            userBrowser = "Opera";
        } else if (userAgent.contains("MSIE")) {
            userBrowser = "Internet Explorer";
        } else {
            userBrowser = "Other";
        }
        return userBrowser;
    }

    /**
     * Url validator, Allow any url with https and http.
     * Allow any url without fully qualified domain
     *
     * @param url Url as string
     * @return boolean type stating validated or not
     */
    private static boolean isURL(String url) {
        Pattern pattern = Pattern.compile("^(http|https)://(.)+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static NativeObject jsFunction_getAllPaginatedAPIs(Context cx, Scriptable thisObj,
                                                              Object[] args, Function funObj)
            throws APIManagementException {

        APIProvider provider = getAPIProvider(thisObj);
        String tenantDomain;

        if (args[0] != null) {
            tenantDomain = (String) args[0];
        } else {
            tenantDomain = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        int start = Integer.parseInt((String) args[1]);
        int end = Integer.parseInt((String) args[2]);


        return getPaginatedAPIs(provider, tenantDomain, start, end, thisObj);


    }

    private static NativeObject getPaginatedAPIs(APIProvider apiProvider, String tenantDomain, int start,
                                                 int end, Scriptable thisObj) throws APIManagementException {

        List<API> apiList;
        Map<String, Object> resultMap;
        NativeArray myn = new NativeArray(0);
        NativeObject result = new NativeObject();
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                isTenantFlowStarted = true;
            }
            resultMap = apiProvider.getAllPaginatedAPIs(tenantDomain, start, end);

        } finally {
            if (isTenantFlowStarted) {

                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        if (resultMap != null) {
            apiList = (List<API>) resultMap.get("apis");
            if (apiList != null) {
                Iterator it = apiList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("status", row, checkValue(api.getStatus()));
                    row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                    row.put("subs", row, getSubscriberCount(apiIdentifier, thisObj));
                    myn.put(i, myn, row);
                    i++;
                }
                result.put("apis", result, myn);
                result.put("totalLength", result, resultMap.get("totalLength"));
                result.put("isMore", result, resultMap.get("isMore"));
            }
        }
        return result;
    }
    
    /**
     * Download microgateway usage report
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return NativeObject that contains Input stream of Downloaded File
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeObject jsFunction_getMicroGatewayRequestSummeryReport(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) throws ScriptException, APIManagementException {
        NativeObject data = new NativeObject();
        if (args == null || args.length != 2 || !isStringArray(args)) {
            handleException("Invalid input parameters expected username and date");
        }

        String date = (String) args[1];
        String username = (String) args[0];
        //TODO implement for multitenant use case
        InputStream stream = ReportGenUtil.getMicroGatewayRequestSummaryReport(username, date);
        if (stream != null) {
            data.put("Data", data, cx.newObject(thisObj, "Stream", new Object[] { stream }));
        } else {
            handleException("Resource strean couldn't be found to generate report");
        }

        return data;
    }

}
