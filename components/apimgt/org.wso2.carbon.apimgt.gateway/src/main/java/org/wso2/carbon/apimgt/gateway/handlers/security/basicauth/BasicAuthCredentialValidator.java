/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Caching;

/**
 * This class will validate the basic auth credentials.
 */
public class BasicAuthCredentialValidator {

    private boolean gatewayKeyCacheEnabled;

    protected Log log = LogFactory.getLog(getClass());
    private APIKeyMgtRemoteUserStoreMgtServiceStub apiKeyMgtRemoteUserStoreMgtServiceStub;
    private APIKeyValidator apiKeyValidator;
    /**
     * Initialize the validator with the synapse environment.
     *
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    public BasicAuthCredentialValidator() throws APISecurityException {
        this.gatewayKeyCacheEnabled = isGatewayTokenCacheEnabled();
        this.getGatewayUsernameCache();
        this.apiKeyValidator = new APIKeyValidator();
        ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        EventHubConfigurationDto eventHubConfigurationDto = config.getEventHubConfigurationDto();
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        String url = eventHubConfigurationDto.getServiceUrl();
        if (url == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "API key manager URL unspecified");
        }

        try {
            apiKeyMgtRemoteUserStoreMgtServiceStub = new APIKeyMgtRemoteUserStoreMgtServiceStub(configurationContext, url +
                    "/services/APIKeyMgtRemoteUserStoreMgtService");
            ServiceClient client = apiKeyMgtRemoteUserStoreMgtServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setCallTransportCleanup(true);
            options.setManageSession(true);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
        } catch (AxisFault axisFault) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, axisFault.getMessage(), axisFault);
        }
    }

    /**
     * Validates the given username and password against the users in the user store.
     *
     * @param username given username
     * @param password given password
     * @return true if the validation passed
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    @MethodStats
    public BasicAuthValidationInfoDTO validate(String username, String password) throws APISecurityException {
        boolean isAuthenticated;
        String cachedPasswordHash = null;
        String providedPasswordHash = null;
        String invalidCachedPasswordHash;
        if (gatewayKeyCacheEnabled) {
            providedPasswordHash = GatewayUtils.hashString(password.getBytes(StandardCharsets.UTF_8));
            BasicAuthValidationInfoDTO cachedValidationInfoObj = (BasicAuthValidationInfoDTO) getGatewayUsernameCache()
                    .get(username);
            if (cachedValidationInfoObj != null) {
                cachedPasswordHash = cachedValidationInfoObj.getHashedPassword();
                cachedValidationInfoObj.setCached(true);
            }
            if (cachedPasswordHash != null && cachedPasswordHash.equals(providedPasswordHash)) {
                log.debug("Basic Authentication: <Valid Username Cache> Username & password authenticated");
                return cachedValidationInfoObj;
            } else {
                BasicAuthValidationInfoDTO invalidCacheValidationInfoObj = (BasicAuthValidationInfoDTO) getInvalidUsernameCache()
                        .get(username);
                if (invalidCacheValidationInfoObj != null) {
                    invalidCacheValidationInfoObj.setCached(true);
                    invalidCachedPasswordHash = invalidCacheValidationInfoObj.getHashedPassword();
                    if (invalidCachedPasswordHash != null && invalidCachedPasswordHash.equals(providedPasswordHash)) {
                        log.debug(
                                "Basic Authentication: <Invalid Username Cache> Username & password authentication failed");
                        invalidCacheValidationInfoObj
                                .setAuthenticated(false); //If (username->password) is in the invalid cache
                        return invalidCacheValidationInfoObj;
                    }
                }
            }
        }

        BasicAuthValidationInfoDTO basicAuthValidationInfoDTO;
        try {
            org.wso2.carbon.apimgt.impl.dto.xsd.BasicAuthValidationInfoDTO generatedInfoDTO = apiKeyMgtRemoteUserStoreMgtServiceStub
                    .getUserAuthenticationInfo(username, password);
            basicAuthValidationInfoDTO = convertToDTO(generatedInfoDTO);
            isAuthenticated = basicAuthValidationInfoDTO.isAuthenticated();
        } catch (APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException | RemoteException e) {
            log.error(
                    "Basic Authentication: Error while accessing backend services to validate user authentication for user : "
                            + username);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }

        if (gatewayKeyCacheEnabled) {
            basicAuthValidationInfoDTO.setHashedPassword(providedPasswordHash);
            if (isAuthenticated) {
                // put (username->password) into the valid cache
                getGatewayUsernameCache().put(username, basicAuthValidationInfoDTO);
            } else {
                // put (username->password) into the invalid cache
                getInvalidUsernameCache().put(username, basicAuthValidationInfoDTO);
            }
        }

        return basicAuthValidationInfoDTO;
    }

    private BasicAuthValidationInfoDTO convertToDTO(
            org.wso2.carbon.apimgt.impl.dto.xsd.BasicAuthValidationInfoDTO generatedDto) {
        BasicAuthValidationInfoDTO dto = new BasicAuthValidationInfoDTO();
        dto.setAuthenticated(generatedDto.getAuthenticated());
        dto.setHashedPassword(generatedDto.getHashedPassword());
        dto.setDomainQualifiedUsername(generatedDto.getDomainQualifiedUsername());
        dto.setUserRoleList(generatedDto.getUserRoleList());
        return dto;
    }

    /**
     * Validates the roles of the given user against the roles of the scopes of the API resource.
     *
     * @param username     given username
     * @param openAPI      OpenAPI of the API
     * @param synCtx       The message to be authenticated
     * @param userRoleList The list of roles of the user
     * @return true if the validation passed
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    @MethodStats
    public boolean validateScopes(String username, OpenAPI openAPI, MessageContext synCtx,
                                  BasicAuthValidationInfoDTO basicAuthValidationInfoDTO) throws APISecurityException {

        String[] userRoleList = basicAuthValidationInfoDTO.getUserRoleList();
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String apiElectedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();
        String httpMethod = (String) axis2MessageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
        String resourceKey = apiContext + ":" + apiVersion + ":" + apiElectedResource + ":" + httpMethod;
        Map<String, Scope> scopeMap = apiKeyValidator.retrieveScopes(tenantDomain);
        String resourceCacheKey = resourceKey + ":" + username;

        if (gatewayKeyCacheEnabled && getGatewayBasicAuthResourceCache().get(resourceCacheKey) != null &&
                basicAuthValidationInfoDTO.isCached()) {
            return true;
        }

        if (openAPI != null) {
            // retrieve the user roles related to the scope of the API resource
            List<String> resourceScopes = OpenAPIUtils.getScopesOfResource(openAPI, synCtx);
            if (resourceScopes != null && resourceScopes.size() > 0) {
                for (String resourceScope : resourceScopes) {
                    Scope scope = scopeMap.get(resourceScope);
                    if (scope != null) {
                        if (scope.getRoles().isEmpty()) {
                            log.debug("Scope " + resourceScope + " didn't have roles");
                            if (gatewayKeyCacheEnabled) {
                                getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
                            }
                            return true;
                        } else {
                            //check if the roles related to the API resource contains internal role which matches
                            // any of the role of the user
                            //check if the roles related to the API resource contains internal role which matches
                            // any of the role of the user
                            if (validateInternalUserRoles(scope.getRoles(), userRoleList)) {
                                if (gatewayKeyCacheEnabled) {
                                    getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
                                }
                                return true;
                            }
                            // check if the roles related to the API resource contains any of the role of the user
                            for (String role : userRoleList) {
                                if (scope.getRoles().contains(role)) {
                                    if (gatewayKeyCacheEnabled) {
                                        getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Basic Authentication: No scopes for the API resource: ".concat(resourceKey));
                }
                return true;
            }
        } else if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            HashMap<String, String> operationScopeMappingList = (HashMap<String, String>) synCtx
                    .getProperty(APIConstants.SCOPE_OPERATION_MAPPING);
            String[] operationList = ((String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).split(",");
            for (String operation : operationList) {
                String operationScope = operationScopeMappingList.get(operation);
                if (operationScope != null) {
                    if (scopeMap.containsKey(operationScope)) {
                        List<String> operationRoles = scopeMap.get(operationScope).getRoles();
                        boolean userHasOperationRole = false;
                        if (operationRoles.isEmpty()) {
                            userHasOperationRole = true;
                        } else {
                            for (String role : userRoleList) {
                                if (operationRoles.contains(role)) {
                                    userHasOperationRole = true;
                                    break;
                                }
                            }
                        }
                        if (!userHasOperationRole) {
                            throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
                        }

                    } else {
                        throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                    }
                }
            }
            if (gatewayKeyCacheEnabled) {
                getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
            }
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: No OpenAPI found in the gateway for the API: ".concat(apiContext)
                        .concat(":").concat(apiVersion));
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Basic Authentication: Scope validation failed for the API resource: ".concat(apiElectedResource));
        }
        throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
    }

    /**
     * This method used to validate scopes which bind with internal roles.
     *
     * @param resourceRoles allowed roles for resource
     * @param userRoles     roles of user
     * @return true if one of userRoles match with any internal role of resource scope
     */
    private boolean validateInternalUserRoles(List<String> resourceRoles, String[] userRoles) {

        for (String role : resourceRoles) {
            if (role.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                int index = role.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index > 0) {
                    String domain = role.substring(0, index);
                    if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
                        for (String userRole : userRoles) {
                            if (role.equalsIgnoreCase(userRole)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }



    /**
     * @return the resource cache
     */
    private Cache getGatewayBasicAuthResourceCache() {
        return CacheProvider.getGatewayBasicAuthResourceCache();
    }

    /**
     * @return the valid username cache
     */
    private Cache getGatewayUsernameCache() {
        return CacheProvider.getGatewayUsernameCache();
    }

    /**
     * @return the invalid username cache
     */
    private Cache getInvalidUsernameCache() {
        return CacheProvider.getInvalidUsernameCache();
    }

    /**
     * Returns the API Manager Configuration.
     *
     * @return the API Manager Configuration
     */
    private APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    /**
     * Returns the Cache object of the given name.
     *
     * @param cacheName name of the Cache
     * @return the cache object
     */
    private Cache getCacheFromCacheManager(String cacheName) {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }

    /**
     * Returns whether the gateway token cache is enabled.
     *
     * @return true if the gateway token cache is enabled
     */
    private boolean isGatewayTokenCacheEnabled() {
        try {
            APIManagerConfiguration config = getApiManagerConfiguration();
            String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(cacheEnabled);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration." +
                    " Use default configuration " + e, e);
        }
        return true;
    }
}
