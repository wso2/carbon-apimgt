/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.keymgt.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.Nullable;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.MethodStats;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.claim.mgt.ClaimManagementException;
import org.wso2.carbon.claim.mgt.ClaimManagerHandler;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@MethodStats
public class JWTGenerator extends AbstractJWTGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);
    private static final String OIDC_DIALECT_URI = "http://wso2.org/oidc/claim";

    @Override
    public Map<String, String> populateStandardClaims(TokenValidationContext validationContext)
            throws APIManagementException {

        //generating expiring timestamp
        long currentTime = System.currentTimeMillis();
        long expireIn = currentTime + getTTL() * 1000;

        String dialect;
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            dialect = claimsRetriever.getDialectURI(validationContext.getValidationInfoDTO().getEndUserName());
        } else {
            dialect = getDialectURI();
        }

        // dialect is either empty or '/' do not append a backslash. otherwise append a backslash '/'
        if (!"".equals(dialect) && !"/".equals(dialect)) {
            dialect = dialect + "/";
        }
        String subscriber = validationContext.getValidationInfoDTO().getSubscriber();
        String applicationName = validationContext.getValidationInfoDTO().getApplicationName();
        String applicationId = validationContext.getValidationInfoDTO().getApplicationId();
        String tier = validationContext.getValidationInfoDTO().getTier();
        String endUserName = validationContext.getValidationInfoDTO().getEndUserName();
        String keyType = validationContext.getValidationInfoDTO().getType();
        String userType = validationContext.getValidationInfoDTO().getUserType();
        String applicationTier = validationContext.getValidationInfoDTO().getApplicationTier();
        String enduserTenantId = String.valueOf(APIUtil.getTenantId(endUserName));
        String apiName = validationContext.getValidationInfoDTO().getApiName();
        Application application =
                getApplicationById(validationContext.getValidationInfoDTO().getSubscriberTenantDomain(),
                        Integer.parseInt(applicationId));
        String uuid = null;
        Map<String, String> appAttributes = null;
        if (application != null) {
            appAttributes = application.getAttributes();
            uuid = application.getUUID();
        }
        Map<String, String> claims = new LinkedHashMap<String, String>(20);

        claims.put("iss", API_GATEWAY_ID);
        claims.put("exp", String.valueOf(expireIn));
        claims.put(dialect + "subscriber", subscriber);
        claims.put(dialect + "applicationid", applicationId);
        claims.put(dialect + "applicationname", applicationName);
        claims.put(dialect + "applicationtier", applicationTier);
        claims.put(dialect + "apiname", apiName);
        claims.put(dialect + "apicontext", validationContext.getContext());
        claims.put(dialect + "version", validationContext.getVersion());
        claims.put(dialect + "tier", tier);
        claims.put(dialect + "keytype", keyType);
        claims.put(dialect + "usertype", userType);
        claims.put(dialect + "enduser", APIUtil.getUserNameWithTenantSuffix(endUserName));
        claims.put(dialect + "enduserTenantId", enduserTenantId);
        claims.put(dialect + "applicationUUId", uuid);
        try {
            if (appAttributes != null && !appAttributes.isEmpty()) {
                String stringAppAttributes = new ObjectMapper().writeValueAsString(appAttributes);
                claims.put(dialect + "applicationAttributes", stringAppAttributes);
            }

        } catch (JsonProcessingException e) {
            log.error("Error in converting Map to String");
        }

        return claims;
    }

    @Override
    public Map<String, String> populateCustomClaims(TokenValidationContext validationContext)
            throws APIManagementException {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        JWTConfigurationDto jwtConfigurationDto = apiManagerConfiguration.getJwtConfigurationDto();
        Map<String, String> customClaims = new HashMap<>();
        String username = validationContext.getValidationInfoDTO().getEndUserName();
        int tenantId = APIUtil.getTenantId(username);
        if (jwtConfigurationDto.isEnableUserClaims()) {
            String accessToken = validationContext.getAccessToken();
            Map<String, String> claims = getClaims(username, accessToken, tenantId,
                    jwtConfigurationDto.getConsumerDialectUri(),
                    validationContext.getValidationInfoDTO().getKeyManager());
            customClaims.putAll(claims);
        }
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            customClaims.putAll(claimsRetriever.getClaims(username));
        }
        return customClaims;
    }

    private Map<String, String> getClaims(String username, String accessToken, int tenantId, String dialectURI,
                                          String keyManager)
            throws APIManagementException {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (apiManagerConfiguration.isJWTClaimCacheEnabled()) {
            String cacheKey = username.concat("_").concat(String.valueOf(tenantId));
            Object claims = CacheProvider.getJWTClaimCache().get(cacheKey);
            if (claims instanceof Map) {
                return (Map<String, String>) claims;
            }
            if (claims == null) {
                synchronized (this.getClass().getName().concat(cacheKey).intern()) {
                    claims = CacheProvider.getJWTClaimCache().get(cacheKey);
                    if (claims instanceof Map) {
                        return (Map<String, String>) claims;
                    }
                    Map<String, String> claimsFromKeyManager = getClaimsFromKeyManager(username, accessToken,
                            tenantId, dialectURI, keyManager);
                    if (claimsFromKeyManager != null) {
                        CacheProvider.getJWTClaimCache().put(cacheKey, claimsFromKeyManager);
                        return claimsFromKeyManager;
                    }
                }
            }
        } else {
            Map<String, String> tempClaims = getClaimsFromKeyManager(username, accessToken, tenantId, dialectURI,
                    keyManager);
            if (tempClaims != null) return tempClaims;
        }
        return new HashMap<>();
    }

    private Map<String, String> getClaimsFromKeyManager(String username, String accessToken, int tenantId,
                                                               String dialectURI, String keyManager) throws APIManagementException {

        Map<String, Object> properties = new HashMap<>();
        if (accessToken != null) {
            properties.put(APIConstants.KeyManager.ACCESS_TOKEN, accessToken);
        }
        if (!StringUtils.isEmpty(dialectURI)) {
            properties.put(APIConstants.KeyManager.CLAIM_DIALECT, dialectURI);
            KeyManager keymanager = KeyManagerHolder
                    .getKeyManagerInstance(APIUtil.getTenantDomainFromTenantId(tenantId), keyManager);
            if (keymanager != null) {
                Map<String, String> tempClaims = keymanager.getUserClaims(username, properties);
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved claims :" + tempClaims);
                }
                if (tempClaims != null) {
                    return tempClaims;
                }
            }
        }
        return null;
    }
}
