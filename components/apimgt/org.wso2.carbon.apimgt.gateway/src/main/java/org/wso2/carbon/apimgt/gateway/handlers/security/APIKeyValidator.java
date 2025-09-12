/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.api.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.RESTDispatcher;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.WSAPIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequest;
import org.wso2.carbon.apimgt.gateway.mcp.request.Params;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.token.InternalAPIKeyGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.JWTUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;

/**
 * This class is used to validate a given API key against a given API context and a version.
 * Actual validation operations are carried out by invoking back-end authentication and
 * key validation services. In order to minimize the network overhead, this implementation
 * caches some API key authentication information in memory. This implementation and the
 * underlying caching implementation are thread-safe. An instance of this class must not be
 * shared among multiple APIs, API handlers or authenticators.
 */
public class APIKeyValidator {

    protected APIKeyDataStore dataStore;

    private boolean gatewayKeyCacheEnabled;

    private boolean isGatewayAPIResourceValidationEnabled;

    protected Log log = LogFactory.getLog(getClass());

    private ArrayList<URITemplate> uriTemplates = null;
    private final boolean jwtGenerationEnabled;
    private static Long mcpInternalTokenExpiryTime;

    public APIKeyValidator() {

        this.dataStore = new WSAPIKeyDataStore();

        this.gatewayKeyCacheEnabled = isGatewayTokenCacheEnabled();

        this.isGatewayAPIResourceValidationEnabled = isAPIResourceValidationEnabled();
        this.jwtGenerationEnabled = isJWTGenerationEnabled();
    }

    protected Cache getGatewayKeyCache() {
        return CacheProvider.getGatewayKeyCache();
    }

    protected Cache getCache(final String cacheManagerName, final String cacheName, final long modifiedExp,
                             long accessExp) {
        return APIUtil.getCache(cacheManagerName, cacheName, modifiedExp, accessExp);
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    protected Cache getGatewayTokenCache() {
        return CacheProvider.getGatewayTokenCache();
    }

    protected Cache getInvalidTokenCache() {
        return CacheProvider.getInvalidTokenCache();
    }

    @MethodStats
    protected Cache getResourceCache() {
        return CacheProvider.getResourceCache();
    }

    /**
     * Get the API key validated against the specified API. If generateInternalKey is true, an internal JWT token
     * will be generated for MCP APIs.
     *
     * @param context               API context
     * @param apiKey                API key to be validated
     * @param apiVersion            API version number
     * @param authenticationScheme  Authentication scheme (e.g., "OAuth" or "Basic")
     * @param matchingResource      The resource which matches the request
     * @param httpVerb              HTTP verb of the request (e.g., "GET", "POST")
     * @param defaultVersionInvoked True if the default version of the API is invoked
     * @param keyManagers           list of key managers to authenticate the API
     * @param generateInternalKey   True if an internal JWT token to be generated for MCP APIs
     * @param referencedApiUuids    List of API UUIDs that are referenced in the MCP request
     * @return An APIKeyValidationInfoDTO object
     * @throws APISecurityException If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getKeyValidationInfo(String context, String apiKey, String apiVersion,
                                                        String authenticationScheme, String matchingResource,
                                                        String httpVerb, boolean defaultVersionInvoked,
                                                        List<String> keyManagers, boolean generateInternalKey,
                                                        List<String> referencedApiUuids)
            throws APISecurityException {

        APIKeyValidationInfoDTO info = getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme,
                matchingResource, httpVerb, defaultVersionInvoked, keyManagers);

        if (info != null && info.isAuthorized() && generateInternalKey) {
            if (log.isDebugEnabled()) {
                log.info("Internal key generation enabled for API with context: " + context + " and version: " +
                        apiVersion);
            }
            final String cacheKey = apiKey + ":" + APIConstants.API_TYPE_MCP;
            String mcpUpstreamToken = null;
            ExtendedJWTConfigurationDto jwtConfigurationDto =
                    org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                            .getJwtConfigurationDto();
            if (gatewayKeyCacheEnabled) {
                Object cached = getGatewayTokenCache().get(cacheKey);
                if (cached instanceof String) {
                    String candidate = (String) cached;
                    long skewMs = getTimeStampSkewInSeconds() * 1000L;
                    if (JWTUtil.isJWTValid(candidate, jwtConfigurationDto.getJwtDecoding(), skewMs)) {
                        mcpUpstreamToken = candidate;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Cached MCP upstream token for key: " + cacheKey +
                                    " is invalid, removing from cache");
                        }
                        getGatewayTokenCache().remove(cacheKey);
                    }
                }
            }
            if (mcpUpstreamToken == null) {
                JwtTokenInfoDTO token = buildInternalJWTToken(info, jwtConfigurationDto, referencedApiUuids);
                try {
                    mcpUpstreamToken = new InternalAPIKeyGenerator().generateToken(token);
                } catch (APIManagementException e) {
                    String errorMsg = "Error occurred while generating upstream token for MCP";
                    log.error(errorMsg, e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, errorMsg, e);
                }
                if (gatewayKeyCacheEnabled && StringUtils.isNotBlank(mcpUpstreamToken)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Caching generated MCP upstream token with key: " + cacheKey);
                    }
                    getGatewayTokenCache().put(cacheKey, mcpUpstreamToken);
                }
            }
            info.setMcpUpstreamToken(mcpUpstreamToken);
        }
        return info;
    }

    /**
     * Get the API key validated against the specified API
     *
     * @param context    API context
     * @param apiKey     API key to be validated
     * @param apiVersion API version number
     * @param keyManagers list of key managers to authenticate the API
     * @return An APIKeyValidationInfoDTO object
     * @throws APISecurityException If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getKeyValidationInfo(String context, String apiKey,
                                                        String apiVersion, String authenticationScheme,
                                                        String matchingResource, String httpVerb,
                                                        boolean defaultVersionInvoked, List<String> keyManagers)
            throws APISecurityException {

        String prefixedVersion = apiVersion;
        boolean valid;
        //Check if client has invoked the default version API.
        if (defaultVersionInvoked) {
            //Prefix the version so that it looks like _default_1.0 (_default_<version>)).
            //This is so that the Key Validator knows that this request is coming through a default api version
            prefixedVersion = APIConstants.DEFAULT_VERSION_PREFIX + prefixedVersion;
        }

        String cacheKey = APIUtil.getAccessTokenCacheKey(apiKey, context, prefixedVersion, matchingResource,
                httpVerb, authenticationScheme);
        //If Gateway key caching is enabled.
        if (gatewayKeyCacheEnabled) {
            //Get the access token from the first level cache.
            String cachedToken = (String) getGatewayTokenCache().get(apiKey);

            //If the access token exists in the first level cache.
            if (cachedToken != null) {
                APIKeyValidationInfoDTO info = (APIKeyValidationInfoDTO) getGatewayKeyCache().get(cacheKey);

                if (info != null) {
                    if (APIUtil.isAccessTokenExpired(info)) {
                        log.info("Invalid OAuth Token : Access Token " + GatewayUtils.getMaskedToken(apiKey) + " " +
                                "expired.");
                        info.setAuthorized(false);
                        // in cache, if token is expired  remove cache entry.
                        getGatewayKeyCache().remove(cacheKey);

                        //Remove from the first level token cache as well.
                        getGatewayTokenCache().remove(apiKey);
                        final String mcpInternalTokenCacheKey = apiKey + ":" + APIConstants.API_TYPE_MCP;
                        getGatewayTokenCache().remove(mcpInternalTokenCacheKey);
                        // Put into invalid token cache
                        getInvalidTokenCache().put(apiKey, cachedToken);
                        info.setExpired(true);
                    }
                    if (info.getEndUserToken() == null) {
                        return info;
                    }
                    long timestampSkew = getTimeStampSkewInSeconds() * 1000;
                    ExtendedJWTConfigurationDto jwtConfigurationDto =
                            org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder.getInstance()
                                    .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                                    .getJwtConfigurationDto();
                    valid = JWTUtil.isJWTValid(
                            info.getEndUserToken(), jwtConfigurationDto.getJwtDecoding(), timestampSkew);
                    if (valid) {
                        return info;
                    }
                }
            } else {
                // Check token available in invalidToken Cache
                String revokedCachedToken = (String) getInvalidTokenCache().get(apiKey);
                if (revokedCachedToken != null) {
                    // Token is revoked/invalid or expired
                    APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
                    apiKeyValidationInfoDTO.setAuthorized(false);
                    apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus
                            .API_AUTH_INVALID_CREDENTIALS);
                    return apiKeyValidationInfoDTO;
                }
            }
        }

        String tenantDomain = getTenantDomain();
        APIKeyValidationInfoDTO info = doGetKeyValidationInfo(context, prefixedVersion, apiKey, authenticationScheme,
                matchingResource, httpVerb, tenantDomain, keyManagers);
        if (info != null) {
            if (gatewayKeyCacheEnabled) {
                //Get the tenant domain of the API that is being invoked.

                if (info.getValidationStatus() == APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS) {
                    // if Token is not valid token (expired,invalid,revoked) put into invalid token cache
                    getInvalidTokenCache().put(apiKey, tenantDomain);
                } else {
                    // Add into 1st level cache and Key cache
                    getGatewayTokenCache().put(apiKey, tenantDomain);
                    getGatewayKeyCache().put(cacheKey, info);
                }

                //If this is NOT a super-tenant API that is being invoked
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
                    //to remove the entry when the need occurs to clear this particular cache entry.
                    try {
                        startTenantFlow();

                        if (info.getValidationStatus() == APIConstants.KeyValidationStatus
                                .API_AUTH_INVALID_CREDENTIALS) {
                            // if Token is not valid token (expired,invalid,revoked) put into invalid token cache in
                            // tenant cache
                            getInvalidTokenCache().put(apiKey, tenantDomain);
                        } else {
                            // add into to tenant token cache
                            getGatewayTokenCache().put(apiKey, tenantDomain);
                        }
                    } finally {
                        endTenantFlow();
                    }
                }
            }

            return info;
        } else {
            String warnMsg = "API key validation service returns null object";
            log.warn(warnMsg);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    warnMsg);
        }
    }

    /**
     * Build the internal JWT token for MCP APIs
     *
     * @param info                APIKeyValidationInfoDTO object
     * @param jwtConfigurationDto Extended JWT configuration
     * @param referencedApiUuids  List of API UUIDs that are referenced in the MCP request
     * @return JwtTokenInfoDTO object
     */
    private JwtTokenInfoDTO buildInternalJWTToken(APIKeyValidationInfoDTO info,
                                                  ExtendedJWTConfigurationDto jwtConfigurationDto,
                                                  List<String> referencedApiUuids) {

        JwtTokenInfoDTO dto = new JwtTokenInfoDTO();
        dto.setEndUserName(info.getEndUserName());
        dto.setKeyType(info.getType());
        if (mcpInternalTokenExpiryTime == null) {
            mcpInternalTokenExpiryTime = getMcpInternalTokenExpiryTime();
        }
        dto.setExpirationTime(mcpInternalTokenExpiryTime);
        Map<String, String> custom = new HashMap<>();
        custom.put(APIMgtGatewayConstants.MCP_AUTH_CLAIM, Boolean.TRUE.toString());
        if (jwtGenerationEnabled) {
            String dialect = jwtConfigurationDto.getConsumerDialectUri();
            if (dialect == null || dialect.isEmpty() || "/".equals(dialect)) {
                dialect = APIConstants.DEFAULT_CARBON_DIALECT + "/";
            } else if (!dialect.endsWith("/")) {
                dialect = dialect + "/";
            }
            if (StringUtils.isNotEmpty(info.getSubscriber())) {
                custom.put(dialect + APIMgtGatewayConstants.SUBSCRIBER_CLAIM, info.getSubscriber());
            }
            if (StringUtils.isNotEmpty(info.getApplicationId())) {
                custom.put(dialect + APIMgtGatewayConstants.APPLICATION_ID_CLAIM, info.getApplicationId());
            }
            if (StringUtils.isNotEmpty(info.getApplicationName())) {
                custom.put(dialect + APIMgtGatewayConstants.APPLICATION_NAME_CLAIM, info.getApplicationName());
            }
            if (StringUtils.isNotEmpty(info.getApplicationTier())) {
                custom.put(dialect + APIMgtGatewayConstants.APPLICATION_TIER_CLAIM, info.getApplicationTier());
            }
            if (StringUtils.isNotEmpty(info.getTier())) {
                custom.put(dialect + APIMgtGatewayConstants.TIER_CLAIM, info.getTier());
            }
            if (StringUtils.isNotEmpty(info.getApplicationUUID())) {
                custom.put(dialect + APIMgtGatewayConstants.APPLICATION_UUID_CLAIM, info.getApplicationUUID());
            }
            if (StringUtils.isNotEmpty(info.getType())) {
                custom.put(dialect + APIMgtGatewayConstants.KEY_TYPE_CLAIM, info.getType());
            }
            if (StringUtils.isNotEmpty(info.getEndUserName())) {
                custom.put(dialect + APIMgtGatewayConstants.END_USER_CLAIM, info.getEndUserName());
            }
        }
        dto.setCustomClaims(custom);
        HashSet<String> audiences = new LinkedHashSet<>();
        if (referencedApiUuids != null) {
            audiences.addAll(referencedApiUuids);
        }
        dto.setAudience(new ArrayList<>(audiences));
        return dto;
    }

    protected void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    protected long getTimeStampSkewInSeconds() {
        return OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();
    }

    protected void startTenantFlow() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
    }

    protected String getTenantDomain() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, String apiKey,
                                                             String authenticationScheme,
                                                             String matchingResource, String httpVerb,
                                                             String tenantDomain, List<String> keyManagers)
            throws APISecurityException {

        return dataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                matchingResource, httpVerb, tenantDomain, keyManagers);
    }

    public void cleanup() {
        dataStore.cleanup();
    }

    public boolean isGatewayTokenCacheEnabled() {
        try {
            APIManagerConfiguration config = getApiManagerConfiguration();
            String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(cacheEnabled);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. Use default configuration" + e);
        }
        return true;
    }

    /**
     * Check whether JWT generation is enabled in the API Manager configuration.
     *
     * @return true if JWT generation is enabled, false otherwise
     */
    private boolean isJWTGenerationEnabled() {

        APIManagerConfiguration config = getApiManagerConfiguration();
        ExtendedJWTConfigurationDto jwtConfigurationDto = (config != null) ? config.getJwtConfigurationDto() : null;
        return jwtConfigurationDto != null && jwtConfigurationDto.isEnabled();
    }

    public boolean isAPIResourceValidationEnabled() {
        try {
            APIManagerConfiguration config = getApiManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.GATEWAY_RESOURCE_CACHE_ENABLED);
            return Boolean.parseBoolean(serviceURL);
        } catch (Exception e) {
            log.error("Did not found valid API Resource Validation Information cache configuration. Use default configuration" + e);
        }
        return true;
    }

    @MethodStats
    public String getResourceAuthenticationScheme(MessageContext synCtx) throws APISecurityException {
        String authType = "";
        List<VerbInfoDTO> verbInfoList;
        try {
            verbInfoList = findMatchingVerb(synCtx);
            if (verbInfoList != null && verbInfoList.toArray().length > 0) {
                for (VerbInfoDTO verb : verbInfoList) {
                    authType = verb.getAuthType();
                    if (authType == null || !StringUtils.capitalize(APIConstants.AUTH_TYPE_NONE.toLowerCase())
                            .equals(authType)) {
                        authType = StringUtils.capitalize(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN
                                .toLowerCase());
                        break;
                    }
                }
                synCtx.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);
            }
        } catch (ResourceNotFoundException e) {
            log.error("Could not find matching resource for request", e);
            return APIConstants.NO_MATCHING_AUTH_SCHEME;
        }

        if (!authType.isEmpty()) {
            return authType;
        } else {
            //No matching resource found. return the highest level of security
            return APIConstants.NO_MATCHING_AUTH_SCHEME;
        }
    }

    public List<VerbInfoDTO> findMatchingVerb(MessageContext synCtx) throws ResourceNotFoundException, APISecurityException {

        List<VerbInfoDTO>  verbInfoList =  new ArrayList<>();
        String resourceCacheKey;
        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullRequestPath = (String) synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String electedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        org.wso2.carbon.apimgt.keymgt.model.entity.API api = GatewayUtils.getAPI(synCtx);
        if (api != null && api.getApiType() != null && StringUtils.equals(api.getApiType(),
                APIConstants.API_TYPE_MCP)) {
            McpRequest requestBody = (McpRequest) synCtx.getProperty(APIMgtGatewayConstants.MCP_REQUEST_BODY);
            if (requestBody != null) {
                Params params = requestBody.getParams();
                String toolName = params != null ? params.getToolName() : null;
                if (!StringUtils.isEmpty(toolName)) {
                    URLMapping extendedOperation = api.getUrlMappings()
                            .stream()
                            .filter(operation -> operation.getUrlPattern().equals(toolName))
                            .findFirst()
                            .orElse(null);
                    if (extendedOperation != null) {
                        BackendOperation backendOperation = null;
                        if (StringUtils.equals(api.getSubtype(), APIConstants.API_SUBTYPE_DIRECT_BACKEND) ||
                                StringUtils.equals(api.getSubtype(), APIConstants.API_SUBTYPE_SERVER_PROXY)) {
                            BackendOperationMapping backendAPIOperationMapping = extendedOperation.getBackendOperationMapping();
                            backendOperation = backendAPIOperationMapping.getBackendOperation();
                        } else if (StringUtils.equals(api.getSubtype(), APIConstants.API_SUBTYPE_EXISTING_API))  {
                            APIOperationMapping existingAPIOperationMapping = extendedOperation.getApiOperationMapping();
                            backendOperation = existingAPIOperationMapping.getBackendOperation();
                            apiContext = existingAPIOperationMapping.getApiContext();
                            apiVersion = existingAPIOperationMapping.getApiVersion();
                        }
                        if (backendOperation != null) {
                            httpMethod = backendOperation.getVerb().toString();
                            electedResource = backendOperation.getTarget();
                        }
                    }
                }
            }
        }
        ArrayList<String> resourceArray = null;

        if (electedResource != null) {
            if (APIConstants.GRAPHQL_API.equalsIgnoreCase((String)synCtx.getProperty(APIConstants.API_TYPE))) {
                resourceArray = new ArrayList<>(Arrays.asList(electedResource.split(",")));
            } else {
                resourceArray = new ArrayList<>(Arrays.asList(electedResource));
            }
        }

        String requestPath = null;
        if (api != null && StringUtils.equals(api.getSubtype(), APIConstants.API_SUBTYPE_EXISTING_API)) {
            requestPath = electedResource;
        } else {
            requestPath = getRequestPath(synCtx, apiContext, apiVersion, fullRequestPath);
        }

        if ("".equals(requestPath)) {
            requestPath = "/";
        }
        if (log.isDebugEnabled()) {
            log.debug("Setting REST_SUB_REQUEST_PATH in msg context: " + requestPath);
        }
        synCtx.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, requestPath);


        //This function is used by more than one handler. If on one execution of this function, it has found and placed
        //the matching verb in the cache, the same can be re-used from all handlers since all handlers share the same
        //MessageContext. The API_RESOURCE_CACHE_KEY property will be set in the MessageContext to indicate that the
        //verb has been put into the cache.
        if (resourceArray != null) {
            for (String resourceString : resourceArray) {
                VerbInfoDTO verbInfo;
                if (isGatewayAPIResourceValidationEnabled) {
                    String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);
                    if (!getResourceCache().containsKey(apiCacheKey)) {
                        break;
                    }
                    resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                            resourceString, httpMethod);
                    verbInfo = (VerbInfoDTO) getResourceCache().get(resourceCacheKey);
                    //Cache hit
                    if (verbInfo != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found resource in Cache for key: " + resourceCacheKey);
                        }
                        verbInfoList.add(verbInfo);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Resource not found in cache for key: " + resourceCacheKey);
                        }
                    }
                }
            }
            if (resourceArray.size() == verbInfoList.size()) {
                return verbInfoList;
            }
        } else {
            // This block won't get executed for MCP Servers and GraphQL APIs
            API selectedApi = Utils.getSelectedAPI(synCtx);
            Resource selectedResource = null;
            String resourceString;

            if (selectedApi != null) {
                Resource[] selectedAPIResources = selectedApi.getResources();

                List<Resource> acceptableResourcesList = new LinkedList<>();

                for (Resource resource : selectedAPIResources) {
                    //If the requesting method is OPTIONS or if the Resource contains the requesting method
                    if (RESTConstants.METHOD_OPTIONS.equals(httpMethod) &&
                            (resource.getMethods() != null && Arrays.asList(resource.getMethods()).contains(httpMethod))) {
                        acceptableResourcesList.add(0, resource);
                    } else if (RESTConstants.METHOD_OPTIONS.equals(httpMethod) ||
                            (resource.getMethods() != null && Arrays.asList(resource.getMethods()).contains(httpMethod))) {
                        acceptableResourcesList.add(resource);
                    }
                }

                Set<Resource> acceptableResources = new LinkedHashSet<>(acceptableResourcesList);

                if (acceptableResources.size() > 0) {
                    for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                        Resource resource = dispatcher.findResource(synCtx, acceptableResources);
                        if (resource != null && Arrays.asList(resource.getMethods()).contains(httpMethod)) {
                            selectedResource = resource;
                            if (selectedResource.getDispatcherHelper()
                                    .getString() != null && !selectedResource.getDispatcherHelper().getString()
                                    .contains("/*")) {
                                break;
                            }
                        }
                    }
                }
            }

            if (selectedResource == null) {
                //No matching resource found.
                String msg = "Could not find matching resource for " + requestPath;
                log.error(msg);
                throw new ResourceNotFoundException(msg);
            }

            resourceString = selectedResource.getDispatcherHelper().getString();
            resourceArray = new ArrayList<>(Arrays.asList(resourceString));
            resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion, resourceString, httpMethod);
            synCtx.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
            synCtx.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
            synCtx.setProperty(APIConstants.REST_METHOD, httpMethod);

            if (log.isDebugEnabled()) {
                log.debug("Selected Resource: " + resourceString);
            }
            //Set the elected resource
            synCtx.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
            if (isGatewayAPIResourceValidationEnabled) {
                VerbInfoDTO verbInfo;
                verbInfo = (VerbInfoDTO) getResourceCache().get(resourceCacheKey);
                //Cache hit
                if (verbInfo != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Got Resource from cache for key: " + resourceCacheKey);
                    }
                    verbInfoList.add(verbInfo);
                    return verbInfoList;
                } else if (log.isDebugEnabled()) {
                    log.debug("Cache miss for Resource for key: " + resourceCacheKey);
                }
            }
        }

        String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);
        APIInfoDTO apiInfoDTO = null;

        if (isGatewayAPIResourceValidationEnabled) {
            apiInfoDTO = (APIInfoDTO) getResourceCache().get(apiCacheKey);
        }

        //Cache miss
        if (apiInfoDTO == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find API object in cache for key: " + apiCacheKey);
            }

            String apiType = (String) synCtx.getProperty(APIMgtGatewayConstants.API_TYPE);

            if (APIConstants.ApiTypes.PRODUCT_API.name().equalsIgnoreCase(apiType)) {
                apiInfoDTO = doGetAPIProductInfo(synCtx, apiContext, apiVersion);
            } else {
                apiInfoDTO = doGetAPIInfo(synCtx, apiContext, apiVersion);
            }

            if (isGatewayAPIResourceValidationEnabled) {
                getResourceCache().put(apiCacheKey, apiInfoDTO);
            }
        }
        if (apiInfoDTO.getResources() != null) {
            for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                Set<VerbInfoDTO> verbDTOList = resourceInfoDTO.getHttpVerbs();
                for (VerbInfoDTO verb : verbDTOList) {
                    //mcp direct_endpoint scenario
                    String httpVerb = verb.getHttpVerb();
                    if (verb.getBackendAPIOperationMapping() != null) {
                        BackendOperation backendOperation = verb.getBackendAPIOperationMapping().getBackendOperation();
                        if (backendOperation != null) {
                            httpVerb = backendOperation.getVerb().toString();
                        }
                    }
                    if (httpVerb.equals(httpMethod)) {
                        for (String resourceString : resourceArray) {
                            if (isResourcePathMatching(resourceString, resourceInfoDTO)) {
                                resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                                        resourceString, httpMethod);
                                verb.setRequestKey(resourceCacheKey);
                                verbInfoList.add(verb);
                                if (isGatewayAPIResourceValidationEnabled) {
                                    //Store verb in cache
                                    //Set cache key in the message c\ontext so that it can be used by the subsequent handlers.
                                    if (log.isDebugEnabled()) {
                                        log.debug("Putting resource object in cache with key: " + resourceCacheKey);
                                    }
                                    getResourceCache().put(resourceCacheKey, verb);
                                    synCtx.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (verbInfoList.size() == 0) {
            verbInfoList = null;
        }
        return verbInfoList;
    }

    private String getRequestPath(MessageContext synCtx, String apiContext, String apiVersion, String fullRequestPath) {
        String requestPath;
        String versionStrategy = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY);

        if (VersionStrategyFactory.TYPE_URL.equals(versionStrategy)) {
            // most used strategy. server:port/context/version/resource
            requestPath = fullRequestPath.substring((apiContext + apiVersion).length() + 1, fullRequestPath.length());
        } else {
            // default version. assume there is no version is used
            requestPath = fullRequestPath.substring(apiContext.length(), fullRequestPath.length());
        }
        return requestPath;
    }

    private boolean isResourcePathMatching(String resourceString, ResourceInfoDTO resourceInfoDTO) {
        String resource = resourceString.trim();
        String urlPattern = resourceInfoDTO.getUrlPattern().trim();

        //MCP direct_ep
        if (resourceInfoDTO.getHttpVerbs() != null && !resourceInfoDTO.getHttpVerbs().isEmpty()) {
            VerbInfoDTO verbInfoDTO = (VerbInfoDTO) resourceInfoDTO.getHttpVerbs().toArray()[0];
            BackendOperationMapping backendAPIOperationMapping = verbInfoDTO.getBackendAPIOperationMapping();
            if (backendAPIOperationMapping != null) {
                BackendOperation backendOperation = backendAPIOperationMapping.getBackendOperation();
                if (backendOperation != null) {
                    urlPattern = backendOperation.getTarget();
                }
            }
        }

        if (resource.equalsIgnoreCase(urlPattern)) {
            return true;
        }

        // If the urlPattern is only one character longer than the resource and the urlPattern ends with a '/'
        if (resource.length() + 1 == urlPattern.length() && urlPattern.endsWith("/")) {
            // Check if resource is equal to urlPattern if the trailing '/' of the urlPattern is ignored
            String urlPatternWithoutSlash = urlPattern.substring(0, urlPattern.length() - 1);
            return resource.equalsIgnoreCase(urlPatternWithoutSlash);
        }

        return false;
    }

    @MethodStats
    private APIInfoDTO doGetAPIInfo(MessageContext messageContext, String context, String apiVersion) throws APISecurityException {
        ArrayList<URITemplate> uriTemplates = getAllURITemplates(messageContext, context, apiVersion);

        return mapToAPIInfo(uriTemplates, context, apiVersion);
    }

    @MethodStats
    private APIInfoDTO doGetAPIProductInfo(MessageContext messageContext, String context, String apiVersion) throws APISecurityException {
        ArrayList<URITemplate> uriTemplates = getAPIProductURITemplates(messageContext, context, apiVersion);

        return mapToAPIInfo(uriTemplates, context, apiVersion);
    }

    private APIInfoDTO mapToAPIInfo(ArrayList<URITemplate> uriTemplates, String context, String apiVersion) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();

        apiInfoDTO.setApiName(context);
        apiInfoDTO.setContext(context);
        apiInfoDTO.setVersion(apiVersion);
        apiInfoDTO.setResources(new LinkedHashSet<ResourceInfoDTO>());

        ResourceInfoDTO resourceInfoDTO = null;
        VerbInfoDTO verbInfoDTO = null;

        // The following map is used to retrieve already created ResourceInfoDTO rather than iterating -
        // the resource Set in apiInfoDTO.
        LinkedHashMap<String, ResourceInfoDTO> resourcesMap = new LinkedHashMap<String, ResourceInfoDTO>();
        for (URITemplate uriTemplate : uriTemplates) {
            resourceInfoDTO = resourcesMap.get(uriTemplate.getUriTemplate());
            if (null == resourceInfoDTO) {
                resourceInfoDTO = new ResourceInfoDTO();
                resourceInfoDTO.setUrlPattern(uriTemplate.getUriTemplate());
                resourceInfoDTO.setHttpVerbs(new LinkedHashSet());
                apiInfoDTO.getResources().add(resourceInfoDTO);
                resourcesMap.put(uriTemplate.getUriTemplate(), resourceInfoDTO);
            }
            verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
            verbInfoDTO.setAuthType(uriTemplate.getAuthType());
            verbInfoDTO.setThrottling(uriTemplate.getThrottlingTier());
            verbInfoDTO.setContentAware(uriTemplate.checkContentAwareFromThrottlingTiers());
            verbInfoDTO.setThrottlingConditions(uriTemplate.getThrottlingConditions());
            verbInfoDTO.setConditionGroups(uriTemplate.getConditionGroups());
            verbInfoDTO.setApplicableLevel(uriTemplate.getApplicableLevel());

            //MCP
            BackendOperationMapping backendAPIOperationMapping = uriTemplate.getBackendOperationMapping();
            if (backendAPIOperationMapping != null) {
                verbInfoDTO.setBackendOperationMapping(backendAPIOperationMapping);
            }

            resourceInfoDTO.getHttpVerbs().add(verbInfoDTO);
        }

        return apiInfoDTO;
    }

    /**
     * @param messageContext     The message context
     * @param context     API context of API
     * @param apiVersion  Version of API
     * @param requestPath Incoming request path
     * @param httpMethod  http method of request
     * @return verbInfoDTO which contains throttling tier for given resource and verb+resource key
     */
    public VerbInfoDTO getVerbInfoDTOFromAPIData(MessageContext messageContext, String context, String apiVersion, String requestPath, String httpMethod)
            throws APISecurityException {

        String cacheKey = context + ':' + apiVersion;
        APIInfoDTO apiInfoDTO = null;
        if (isGatewayAPIResourceValidationEnabled) {
            apiInfoDTO = (APIInfoDTO) getResourceCache().get(cacheKey);
        }
        if (apiInfoDTO == null) {
            apiInfoDTO = doGetAPIInfo(messageContext, context, apiVersion);
            if (isGatewayAPIResourceValidationEnabled) {
                getResourceCache().put(cacheKey, apiInfoDTO);
            }
        }

        //Match the case where the direct api context is matched
        if ("/".equals(requestPath)) {
            String requestCacheKey = context + '/' + apiVersion + requestPath + ':' + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (isGatewayAPIResourceValidationEnabled) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }
            //On a cache hit
            if (matchingVerb != null) {
                matchingVerb.setRequestKey(requestCacheKey);
                return matchingVerb;
            } else {
                if (apiInfoDTO.getResources() != null) {
                    for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                        String urlPattern = resourceInfoDTO.getUrlPattern();

                        //If the request patch is '/', it can only be matched with a resource whose url-context is '/*'
                        if ("/*".equals(urlPattern)) {
                            for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                                if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                    //Store verb in cache
                                    if (isGatewayAPIResourceValidationEnabled) {
                                        getResourceCache().put(requestCacheKey, verbDTO);
                                    }
                                    verbDTO.setRequestKey(requestCacheKey);
                                    return verbDTO;
                                }
                            }
                        }
                    }
                }
            }
        }

        //Remove the ending '/' from request
        requestPath = RESTUtils.trimTrailingSlashes(requestPath);

        while (requestPath.length() > 1) {

            String requestCacheKey = context + '/' + apiVersion + requestPath + ':' + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (isGatewayAPIResourceValidationEnabled) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }

            //On a cache hit
            if (matchingVerb != null) {
                matchingVerb.setRequestKey(requestCacheKey);
                return matchingVerb;
            }
            //On a cache miss
            else {
                for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                    String urlPattern = resourceInfoDTO.getUrlPattern();
                    if (urlPattern.endsWith("/*")) {
                        //Remove the ending '/*'
                        urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
                    }
                    //If the urlPattern ends with a '/', remove that as well.
                    urlPattern = RESTUtils.trimTrailingSlashes(urlPattern);

                    if (requestPath.endsWith(urlPattern)) {

                        for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                            if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                //Store verb in cache
                                if (isGatewayAPIResourceValidationEnabled) {
                                    getResourceCache().put(requestCacheKey, verbDTO);
                                }
                                verbDTO.setRequestKey(requestCacheKey);
                                return verbDTO;
                            }
                        }
                    }
                }
            }

            //Remove the section after the last occurrence of the '/' character
            int index = requestPath.lastIndexOf('/');
            requestPath = requestPath.substring(0, index <= 0 ? 0 : index);
        }
        //nothing found. return the highest level of security
        return null;
    }


    @MethodStats
    protected ArrayList<URITemplate> getAllURITemplates(MessageContext messageContext, String context, String apiVersion)
            throws APISecurityException {
        if (uriTemplates == null) {
            synchronized (this) {
                if (uriTemplates == null) {
                    uriTemplates = dataStore.getAllURITemplates(context, apiVersion);
                }
            }
        }
        return uriTemplates;
    }

    @MethodStats
    protected ArrayList<URITemplate> getAPIProductURITemplates(MessageContext messageContext, String context, String apiVersion)
            throws APISecurityException {
        if (uriTemplates == null) {
            synchronized (this) {
                uriTemplates = dataStore.getAPIProductURITemplates(context, apiVersion);
            }
        }
        return uriTemplates;
    }

    protected void setGatewayAPIResourceValidationEnabled(boolean gatewayAPIResourceValidationEnabled) {
        isGatewayAPIResourceValidationEnabled = gatewayAPIResourceValidationEnabled;
    }

    public APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey,
                                                        String tenantDomain, String keyManager)
            throws APISecurityException {
        return dataStore.validateSubscription(context, version, consumerKey,tenantDomain, keyManager);
    }

    public APIKeyValidationInfoDTO validateSubscription(String context, String version, int appID,
                                                        String tenantDomain, String keyType)
            throws APISecurityException {
        return dataStore.validateSubscription(context, version, appID,tenantDomain, keyType);
    }

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes of the token.
     *
     * @param tokenValidationContext Token validation context
     * @param tenantDomain           Tenant domain
     * @return <code>true</code> if scope validation is successful and
     * <code>false</code> if scope validation failed
     * @throws APISecurityException in case of scope validation failure
     */
    public boolean validateScopes(TokenValidationContext tokenValidationContext, String tenantDomain)
            throws APISecurityException {
        return dataStore.validateScopes(tokenValidationContext, tenantDomain);
    }

    public Map<String, Scope> retrieveScopes(String tenantDomain) {
        return dataStore.retrieveScopes(tenantDomain);
    }

    /**
     * Get MCP internal token expiry time with buffer time
     *
     * @return MCP internal token expiry time in seconds
     */
    private Long getMcpInternalTokenExpiryTime() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String tokenCacheExpiryTime = apiManagerConfiguration.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
            if (StringUtils.isNotEmpty(tokenCacheExpiryTime)) {
                try {
                    return applyMcpInternalTokenBuffer(Long.parseLong(tokenCacheExpiryTime));
                } catch (NumberFormatException e) {
                    log.warn("Error while parsing the MCP internal token expiry time. Falling back to default "
                            + APIConstants.DEFAULT_TIMEOUT);
                    return applyMcpInternalTokenBuffer(APIConstants.DEFAULT_TIMEOUT);
                }
            }
        }
        return applyMcpInternalTokenBuffer(APIConstants.DEFAULT_TIMEOUT);
    }

    /**
     * Add buffer time to the MCP internal token expiry time to avoid edge cases
     * where the token is expired by the time it reaches MCP.
     */
    private Long applyMcpInternalTokenBuffer(Long mcpAuthTokenExpirationTime) {

        return mcpAuthTokenExpirationTime + APIMgtGatewayConstants.MCP_AUTH_TOKEN_EXPIRATION_BUFFER_TIME;
    }
}
