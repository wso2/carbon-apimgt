/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.WebAppAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplateException;

import java.util.*;
import javax.cache.Cache;

/**
 * This web app authenticator class specifically implemented for API Manager store and publisher rest APIs
 * This will not be able to use as generic authenticator.
 */
public class WebAppAuthenticatorImpl implements WebAppAuthenticator {

    private static final Log log = LogFactory.getLog(WebAppAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     * @throws APIManagementException when error in authentication process
     */
    public boolean authenticate(Message message) throws APIManagementException {
        boolean retrievedFromInvalidTokenCache = false;
        boolean retrievedFromTokenCache = false;
        String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(message,
                RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);
        AccessTokenInfo tokenInfo = null;

        RESTAPICacheConfiguration cacheConfiguration = APIUtil.getRESTAPICacheConfig();
        //validate the token from cache if it is enabled
        if (cacheConfiguration.isTokenCacheEnabled()) {
            tokenInfo = (AccessTokenInfo)getRESTAPITokenCache().get(accessToken);
            if (tokenInfo != null) {
                if (isAccessTokenExpired(tokenInfo)) {
                    tokenInfo.setTokenValid(false);
                    //remove the token from token cache and put the token into invalid token cache
                    // when the access token is expired
                    getRESTAPIInvalidTokenCache().put(accessToken, tokenInfo);
                    getRESTAPITokenCache().remove(accessToken);
                    log.error(RestApiConstants.ERROR_TOKEN_EXPIRED);
                    return false;
                } else {
                    retrievedFromTokenCache = true;
                }
            } else {
                //if the token doesn't exist in the valid token cache, then check it in the invalid token cache
                tokenInfo = (AccessTokenInfo)getRESTAPIInvalidTokenCache().get(accessToken);
                if (tokenInfo != null) {
                    retrievedFromInvalidTokenCache = true;
                }
            }
        }

        // if the tokenInfo is null, then only retrieve the token information from the database
        try {
            if (tokenInfo == null) {
                tokenInfo = getTokenMetaData(accessToken);
            }
        } catch (APIManagementException e) {
            log.error("Error while retrieving token information for token: " + accessToken, e);
        }

        // if we got valid access token we will proceed with next
        if (tokenInfo != null && tokenInfo.isTokenValid()) {
            if (cacheConfiguration.isTokenCacheEnabled() && !retrievedFromTokenCache) {
                //put the token info into token cache
                getRESTAPITokenCache().put(accessToken, tokenInfo);
            }

            // If token is valid then we have to do other validations and set user and tenant to carbon context.
            // Scope validation should come here.
            // If access token is valid then we will perform scope check for given resource.
            if (validateScopes(message, tokenInfo)) {
                //Add the user scopes list extracted from token to the cxf message
                message.getExchange().put(RestApiConstants.USER_REST_API_SCOPES, tokenInfo.getScopes());
                //If scope validation successful then set tenant name and user name to current context
                String tenantDomain = MultitenantUtils.getTenantDomain(tokenInfo.getEndUserName());
                int tenantId;
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
                try {
                    String username = tokenInfo.getEndUserName();
                    if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        //when the username is an email in supertenant, it has at least 2 occurrences of '@'
                        long count = username.chars().filter(ch -> ch == '@').count();
                        //in the case of email, there will be more than one '@'
                        boolean isEmailUsernameEnabled = Boolean.parseBoolean(CarbonUtils.getServerConfiguration().
                                getFirstProperty("EnableEmailUserName"));
                        if (isEmailUsernameEnabled || (username.endsWith(SUPER_TENANT_SUFFIX) && count <= 1)) {
                            username = MultitenantUtils.getTenantAwareUsername(username);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("username = " + username);
                    }
                    tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    carbonContext.setTenantDomain(tenantDomain);
                    carbonContext.setTenantId(tenantId);
                    carbonContext.setUsername(username);
                    if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        APIUtil.loadTenantConfigBlockingMode(tenantDomain);
                    }
                    message.getExchange().put(RestApiConstants.LOGGED_IN_USERNAME, username);
                    return true;
                } catch (UserStoreException e) {
                    log.error("Error while retrieving tenant id for tenant domain: " + tenantDomain, e);
                }
            } else {
                log.error(RestApiConstants.ERROR_SCOPE_VALIDATION_FAILED);
            }
        } else {
            log.error(RestApiConstants.ERROR_TOKEN_INVALID);
            if (cacheConfiguration.isTokenCacheEnabled() && !retrievedFromInvalidTokenCache) {
                getRESTAPIInvalidTokenCache().put(accessToken, tokenInfo);
            }
        }
        return false;
    }

    /**
     * @param message   CXF message to be validate
     * @param tokenInfo Token information associated with incoming request
     * @return return true if we found matching scope in resource and token information
     * else false(means scope validation failed).
     */
    private boolean validateScopes(Message message, AccessTokenInfo tokenInfo) {
        String basePath = (String) message.get(Message.BASE_PATH);
        // path is obtained from Message.REQUEST_URI instead of Message.PATH_INFO, as Message.PATH_INFO contains
        // decoded values of request parameters
        String path = (String) message.get(Message.REQUEST_URI);
        String verb = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String resource = path.substring(basePath.length() - 1);
        String[] scopes = tokenInfo.getScopes();

        String version = (String) message.get(RestApiConstants.API_VERSION);

        //get all the URI templates of the REST API from the base path
        Set<URITemplate> uriTemplates = RestApiUtil.getURITemplatesForBasePath(basePath + version);
        if (uriTemplates.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No matching scopes found for request with path: " + basePath
                        + ". Skipping scope validation.");
            }
            return true;
        }

        for (Object template : uriTemplates.toArray()) {
            org.wso2.uri.template.URITemplate templateToValidate = null;
            Map<String, String> var = new HashMap<String, String>();
            //check scopes with what we have
            String templateString = ((URITemplate) template).getUriTemplate();
            try {
                templateToValidate = new org.wso2.uri.template.URITemplate(templateString);
            } catch (URITemplateException e) {
                log.error("Error while creating URI Template object to validate request. Template pattern: " +
                        templateString, e);
            }
            if (templateToValidate != null && templateToValidate.matches(resource, var) && scopes != null
                    && verb != null && verb.equalsIgnoreCase(((URITemplate) template).getHTTPVerb())) {
                message.getExchange().put(APIConstants.SWAGGER_OPERATION_ID, ((URITemplate) template).getOperationId());
                message.getExchange().put(APIConstants.SWAGGER_X_RECORD_HISTORY,
                        ((URITemplate) template).isRecordHistory());
                message.getExchange().put(APIConstants.SWAGGER_X_RECORD_HISTORY_PAYLOAD,
                        ((URITemplate) template).isRecordHistoryPayload());
                for (String scope : scopes) {
                    Scope scp = ((URITemplate) template).getScope();
                    if (scp != null) {
                        if (scope.equalsIgnoreCase(scp.getKey())) {
                            //we found scopes matches
                            if (log.isDebugEnabled()) {
                                log.debug("Scope validation successful for access token: " +
                                        tokenInfo.getAccessToken() + " with scope: " + scp.getKey() +
                                        " for resource path: " + path + " and verb " + verb);
                            }
                            return true;
                        }
                    } else if (!((URITemplate) template).retrieveAllScopes().isEmpty()) {
                        List<Scope> scopesList = ((URITemplate) template).retrieveAllScopes();
                        for (Scope scpObj : scopesList) {
                            if (scope.equalsIgnoreCase(scpObj.getKey())) {
                                //we found scopes matches
                                if (log.isDebugEnabled()) {
                                    log.debug("Scope validation successful for access token: " +
                                            tokenInfo.getAccessToken() + " with scope: " + scpObj.getKey() +
                                            " for resource path: " + path + " and verb " + verb);
                                }
                                return true;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + resource + " and verb "
                                    + verb + " . So consider as anonymous permission and let request to continue.");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Cache getRESTAPITokenCache() {
        return CacheProvider.getRESTAPITokenCache();
    }

    private Cache getRESTAPIInvalidTokenCache() {
        return CacheProvider.getRESTAPIInvalidTokenCache();
    }

    private boolean isAccessTokenExpired (AccessTokenInfo accessTokenInfo) {
        APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
        infoDTO.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        infoDTO.setIssuedTime(accessTokenInfo.getIssuedTime());
        return APIUtil.isAccessTokenExpired(infoDTO);
    }

    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken token = requestDTO.new OAuth2AccessToken();

        token.setIdentifier(accessToken);
        token.setTokenType("bearer");
        requestDTO.setAccessToken(token);

        OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] contextParams =
                new OAuth2TokenValidationRequestDTO.TokenValidationContextParam[1];
        requestDTO.setContext(contextParams);

        OAuth2ClientApplicationDTO clientApplicationDTO = findOAuthConsumerIfTokenIsValid(requestDTO);
        OAuth2TokenValidationResponseDTO responseDTO = clientApplicationDTO.getAccessTokenValidationResponse();

        if (!responseDTO.isValid()) {
            tokenInfo.setTokenValid(responseDTO.isValid());
            log.error("Invalid OAuth Token : " + responseDTO.getErrorMsg());
            tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            return tokenInfo;
        }

        tokenInfo.setTokenValid(responseDTO.isValid());
        tokenInfo.setEndUserName(responseDTO.getAuthorizedUser());
        tokenInfo.setConsumerKey(clientApplicationDTO.getConsumerKey());

        // Convert Expiry Time to milliseconds.
        if (responseDTO.getExpiryTime() == Long.MAX_VALUE) {
            tokenInfo.setValidityPeriod(Long.MAX_VALUE);
        } else {
            tokenInfo.setValidityPeriod(responseDTO.getExpiryTime() * 1000L);
        }

        tokenInfo.setIssuedTime(System.currentTimeMillis());
        tokenInfo.setScope(responseDTO.getScope());

        // If token has am_application_scope, consider the token as an Application token.
        String[] scopes = responseDTO.getScope();
        String applicationTokenScope = getConfigurationElementValue(APIConstants.APPLICATION_TOKEN_SCOPE);

        if (scopes != null && applicationTokenScope != null && !applicationTokenScope.isEmpty()) {
            if (Arrays.asList(scopes).contains(applicationTokenScope)) {
                tokenInfo.setApplicationToken(true);
            }
        }

        return tokenInfo;
    }

    /**
     * Returns the OAuth application details if the token is valid
     * @param requestDTO Token validation request
     * @return
     */
    protected OAuth2ClientApplicationDTO findOAuthConsumerIfTokenIsValid(OAuth2TokenValidationRequestDTO requestDTO) {
        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
        return oAuth2TokenValidationService.findOAuthConsumerIfTokenIsValid(requestDTO);
    }
    /**
     * Returns the value of the provided APIM configuration element.
     *
     * @param property APIM configuration element name
     * @return APIM configuration element value
     */
    protected String getConfigurationElementValue(String property) {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getFirstProperty(property);
    }
}
