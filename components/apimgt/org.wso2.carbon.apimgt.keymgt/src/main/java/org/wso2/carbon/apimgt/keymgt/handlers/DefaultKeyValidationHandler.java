/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultKeyValidationHandler extends AbstractKeyValidationHandler {

    private static final Log log = LogFactory.getLog(DefaultKeyValidationHandler.class);

    public DefaultKeyValidationHandler() {

        log.info(this.getClass().getName() + " Initialised");
    }

    @Override
    public boolean validateToken(TokenValidationContext validationContext) throws APIKeyMgtException {
        // If validationInfoDTO is taken from cache, validity of the cached infoDTO is checked with each request.
        if (validationContext.isCacheHit()) {
            APIKeyValidationInfoDTO infoDTO = validationContext.getValidationInfoDTO();

            // TODO: This should only happen in GW
            boolean tokenExpired = APIUtil.isAccessTokenExpired(infoDTO);
            if (tokenExpired) {
                infoDTO.setAuthorized(false);
                infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                log.debug("Token " + validationContext.getAccessToken() + " expired.");
                return false;
            } else {
                return true;
            }
        }
        if (StringUtils.isEmpty(validationContext.getAccessToken())) {
            APIKeyValidationInfoDTO infoDTO = validationContext.getValidationInfoDTO();
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            log.debug("Token Not available");
            return false;
        }

        try {
            AccessTokenInfo tokenInfo = getAccessTokenInfo(validationContext);
            if (tokenInfo == null) {
                return false;
            }
            // Setting TokenInfo in validationContext. Methods down in the chain can use TokenInfo.
            validationContext.setTokenInfo(tokenInfo);
            //TODO: Eliminate use of APIKeyValidationInfoDTO if possible

            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            validationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);

            if (!tokenInfo.isTokenValid()) {
                apiKeyValidationInfoDTO.setAuthorized(false);
                if (tokenInfo.getErrorcode() > 0) {
                    apiKeyValidationInfoDTO.setValidationStatus(tokenInfo.getErrorcode());
                } else {
                    apiKeyValidationInfoDTO.setValidationStatus(APIConstants
                            .KeyValidationStatus.API_AUTH_GENERAL_ERROR);
                }
                return false;
            }
            apiKeyValidationInfoDTO.setKeyManager(tokenInfo.getKeyManager());
            apiKeyValidationInfoDTO.setAuthorized(tokenInfo.isTokenValid());
            apiKeyValidationInfoDTO.setEndUserName(tokenInfo.getEndUserName());
            apiKeyValidationInfoDTO.setConsumerKey(tokenInfo.getConsumerKey());
            apiKeyValidationInfoDTO.setIssuedTime(tokenInfo.getIssuedTime());
            apiKeyValidationInfoDTO.setValidityPeriod(tokenInfo.getValidityPeriod());

            if (tokenInfo.getScopes() != null) {
                Set<String> scopeSet = new HashSet<String>(Arrays.asList(tokenInfo.getScopes()));
                apiKeyValidationInfoDTO.setScopes(scopeSet);
            }
            return tokenInfo.isTokenValid();
        } catch (APIManagementException e) {
            log.error("Error while obtaining Token Metadata from Authorization Server", e);
            throw new APIKeyMgtException("Error while obtaining Token Metadata from Authorization Server");
        }
    }

    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext.isCacheHit()) {
            return true;
        }
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validationContext.getValidationInfoDTO();

        if (apiKeyValidationInfoDTO == null) {
            throw new APIKeyMgtException("Key Validation information not set");
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String httpVerb = validationContext.getHttpVerb();
        String[] scopes;
        Set<String> scopesSet = apiKeyValidationInfoDTO.getScopes();
        StringBuilder scopeList = new StringBuilder();

        if (scopesSet != null && !scopesSet.isEmpty()) {
            scopes = scopesSet.toArray(new String[scopesSet.size()]);
            if (log.isDebugEnabled() && scopes != null) {
                for (String scope : scopes) {
                    scopeList.append(scope);
                    scopeList.append(",");
                }
                scopeList.deleteCharAt(scopeList.length() - 1);
                log.debug("Scopes allowed for token : " + validationContext.getAccessToken() + " : "
                        + scopeList.toString());
            }
        }

        String resourceList = validationContext.getMatchingResource();
        List<String> resourceArray;
        if ((APIConstants.GRAPHQL_QUERY.equalsIgnoreCase(validationContext.getHttpVerb()))
                || (APIConstants.GRAPHQL_MUTATION.equalsIgnoreCase(validationContext.getHttpVerb()))
                || (APIConstants.GRAPHQL_SUBSCRIPTION.equalsIgnoreCase(validationContext.getHttpVerb()))) {
            resourceArray = new ArrayList<>(Arrays.asList(resourceList.split(",")));
        } else {
            resourceArray = new ArrayList<>(Arrays.asList(resourceList));
        }

        String actualVersion = validationContext.getVersion();
        //Check if the api version has been prefixed with _default_
        if (actualVersion != null && actualVersion.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            //Remove the prefix from the version.
            actualVersion = actualVersion.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }
        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        API api = tenantSubscriptionStore.getApiByContextAndVersion(validationContext.getContext(),
                actualVersion);
        boolean scopesValidated = false;
        if (api != null) {

            for (String resource : resourceArray) {
                List<URLMapping> resources = api.getResources();
                URLMapping urlMapping = null;
                for (URLMapping mapping : resources) {
                    if (Objects.equals(mapping.getHttpMethod(), httpVerb) || "WS".equalsIgnoreCase(api.getApiType())) {
                        if (isResourcePathMatching(resource, mapping)) {
                            urlMapping = mapping;
                            break;
                        }
                    }
                }
                if (urlMapping != null) {
                    if (urlMapping.getScopes().size() == 0) {
                        scopesValidated = true;
                        continue;
                    }
                    List<String> mappingScopes = urlMapping.getScopes();
                    boolean validate = false;
                    for (String scope : mappingScopes) {
                        if (scopesSet.contains(scope)) {
                            scopesValidated = true;
                            validate = true;
                            break;
                        }
                    }
                    if (!validate && urlMapping.getScopes().size() > 0) {
                        scopesValidated = false;
                        break;
                    }
                }
            }
        }
        if (!scopesValidated) {
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
        }
        return scopesValidated;
    }

    private AccessTokenInfo getAccessTokenInfo(TokenValidationContext validationContext)
            throws APIManagementException {

        Object cachedAccessTokenInfo =
                CacheProvider.createIntrospectionCache().get(validationContext.getAccessToken());
        if (cachedAccessTokenInfo != null) {
            log.debug("AccessToken available in introspection Cache.");
            return (AccessTokenInfo) cachedAccessTokenInfo;
        }
        String electedKeyManager = null;
        // Obtaining details about the token.
        if (StringUtils.isNotEmpty(validationContext.getTenantDomain())) {
            Map<String, KeyManagerDto>
                    tenantKeyManagers = KeyManagerHolder
                    .getGlobalAndTenantKeyManagers(validationContext.getTenantDomain());
            KeyManager keyManagerInstance = null;
            if (tenantKeyManagers.values().size() == 1) {
                log.debug("KeyManager count is 1");
                Map.Entry<String, KeyManagerDto> entry = tenantKeyManagers.entrySet().iterator().next();
                if (entry != null) {
                    KeyManagerDto keyManagerDto = entry.getValue();
                    if (keyManagerDto != null && (validationContext.getKeyManagers()
                            .contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                            validationContext.getKeyManagers().contains(keyManagerDto.getName()))) {
                        if (log.isDebugEnabled()){
                            log.debug("KeyManager " + keyManagerDto.getName() + " Available in API level KM list " + String.join(",", validationContext.getKeyManagers()));
                        }
                        if (keyManagerDto.getKeyManager() != null
                                && keyManagerDto.getKeyManager().canHandleToken(validationContext.getAccessToken())) {
                            if (log.isDebugEnabled()){
                                log.debug("KeyManager " + keyManagerDto.getName() + " can handle the token");
                            }
                            keyManagerInstance = keyManagerDto.getKeyManager();
                            electedKeyManager = entry.getKey();
                        }
                    }
                }
            } else if (tenantKeyManagers.values().size() > 1) {
                log.debug("KeyManager count is > 1");
                if (validationContext.getKeyManagers()
                        .contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS)) {
                    if (log.isDebugEnabled()){
                        log.debug("API level KeyManagers contains " + APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS);
                    }
                    for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
                        if (keyManagerDtoEntry.getValue().getKeyManager() != null &&
                                keyManagerDtoEntry.getValue().getKeyManager()
                                        .canHandleToken(validationContext.getAccessToken())) {
                            if (log.isDebugEnabled()){
                                log.debug("KeyManager " + keyManagerDtoEntry.getValue().getName() + " can handle the token");
                            }
                            keyManagerInstance = keyManagerDtoEntry.getValue().getKeyManager();
                            electedKeyManager = keyManagerDtoEntry.getKey();
                            break;
                        }
                    }
                } else {
                    for (String selectedKeyManager : validationContext.getKeyManagers()) {
                        KeyManagerDto keyManagerDto = tenantKeyManagers.get(selectedKeyManager);
                        if (keyManagerDto != null && keyManagerDto.getKeyManager() != null &&
                                keyManagerDto.getKeyManager().canHandleToken(validationContext.getAccessToken())) {
                            if (log.isDebugEnabled()){
                                log.debug("KeyManager " + keyManagerDto.getName() + " can handle the token");
                            }
                            keyManagerInstance = keyManagerDto.getKeyManager();
                            electedKeyManager = selectedKeyManager;
                            break;
                        }
                    }
                }
            }

            if (keyManagerInstance != null) {
                log.debug("KeyManager instance available to validate token.");
                AccessTokenInfo tokenInfo = keyManagerInstance.getTokenMetaData(validationContext.getAccessToken());
                if (tokenInfo == null) {
                    return null;
                }
                tokenInfo.setKeyManager(electedKeyManager);
                CacheProvider.getGatewayIntrospectCache().put(validationContext.getAccessToken(), tokenInfo);
                return tokenInfo;
            } else {
                AccessTokenInfo tokenInfo = new AccessTokenInfo();
                tokenInfo.setTokenValid(false);
                tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                log.debug("KeyManager not available to authorize token.");
                return tokenInfo;
            }
        }
        return null;
    }

    private boolean isResourcePathMatching(String resourceString, URLMapping urlMapping) {

        String resource = resourceString.trim();
        String urlPattern = urlMapping.getUrlPattern().trim();

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

}
