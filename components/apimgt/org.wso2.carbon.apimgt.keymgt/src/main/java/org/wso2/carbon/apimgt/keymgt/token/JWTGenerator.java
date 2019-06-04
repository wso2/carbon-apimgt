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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.keymgt.MethodStats;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

@MethodStats
public class JWTGenerator extends AbstractJWTGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);


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

        String subscriber = validationContext.getValidationInfoDTO().getSubscriber();
        String applicationName = validationContext.getValidationInfoDTO().getApplicationName();
        String applicationId = validationContext.getValidationInfoDTO().getApplicationId();
        String tier = validationContext.getValidationInfoDTO().getTier();
        String endUserName = validationContext.getValidationInfoDTO().getEndUserName();
        String keyType = validationContext.getValidationInfoDTO().getType();
        String userType = validationContext.getValidationInfoDTO().getUserType();
        String applicationTier = validationContext.getValidationInfoDTO().getApplicationTier();
        String enduserTenantId = String.valueOf(APIUtil.getTenantId(endUserName));
        Application application = getApplicationbyId(Integer.parseInt(applicationId));
        String uuid = null;
        Map<String, String> appAttributes = null;
        if (application != null) {
            appAttributes = application.getApplicationAttributes();
            uuid = application.getUUID();
        }
        Map<String, String> claims = new LinkedHashMap<String, String>(20);

        claims.put("iss", API_GATEWAY_ID);
        claims.put("exp", String.valueOf(expireIn));
        claims.put(dialect + "/subscriber", subscriber);
        claims.put(dialect + "/applicationid", applicationId);
        claims.put(dialect + "/applicationname", applicationName);
        claims.put(dialect + "/applicationtier", applicationTier);
        claims.put(dialect + "/apicontext", validationContext.getContext());
        claims.put(dialect + "/version", validationContext.getVersion());
        claims.put(dialect + "/tier", tier);
        claims.put(dialect + "/keytype", keyType);
        claims.put(dialect + "/usertype", userType);
        claims.put(dialect + "/enduser", APIUtil.getUserNameWithTenantSuffix(endUserName));
        claims.put(dialect + "/enduserTenantId", enduserTenantId);
        claims.put(dialect + "/applicationUUId", uuid);
        try {
            if (appAttributes != null && !appAttributes.isEmpty()) {
                String stringAppAttributes = new ObjectMapper().writeValueAsString(appAttributes);
                claims.put(dialect + "/applicationAttributes", stringAppAttributes);
            }

        } catch (JsonProcessingException e) {
            log.error("Error in converting Map to String");
        }

        return claims;
    }

    @Override
    public Map<String, String> populateCustomClaims(TokenValidationContext validationContext)
            throws APIManagementException {
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            //fix for https://github.com/wso2/product-apim/issues/4112
            String accessToken = validationContext.getAccessToken();
            AuthorizationGrantCacheKey cacheKey = new AuthorizationGrantCacheKey(accessToken);

            Map<String, String> customClaims = getClaimsFromCache(cacheKey);
            if (isNotEmpty(customClaims)) {
                if (log.isDebugEnabled()) {
                    log.debug("The custom claims are retrieved from AuthorizationGrantCache for user : " +
                            validationContext.getValidationInfoDTO().getEndUserName());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Custom claims are not available in the AuthorizationGrantCache. Hence will be " +
                            "retrieved from the user store for user : " +
                            validationContext.getValidationInfoDTO().getEndUserName());
                }
            }
            // If claims are not found in AuthorizationGrantCache, they will be retrieved from the userstore.
            String tenantAwareUserName = validationContext.getValidationInfoDTO().getEndUserName();

            try {
                int tenantId = APIUtil.getTenantId(tenantAwareUserName);

                if (tenantId != -1) {
                    UserStoreManager manager = ServiceReferenceHolder.getInstance().
                            getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

                    String tenantDomain = MultitenantUtils.getTenantDomain(tenantAwareUserName);
                    String[] split = tenantAwareUserName.split(tenantDomain);

                    if (split.length != 1) {
                        log.error("Could not extract username without tenant domain for: " + tenantAwareUserName);
                        return null;
                    }

                    String username = split[0].substring(0, split[0].length() - 1);

                    if (manager.isExistingUser(username)) {
                        customClaims.putAll(claimsRetriever.getClaims(username));
                        return customClaims;
                    } else {
                        if (!customClaims.isEmpty()) {
                            return customClaims;
                        } else {
                            log.warn("User " + tenantAwareUserName + " cannot be found by user store manager");
                        }
                    }
                } else {
                    log.error("Tenant cannot be found for username: " + tenantAwareUserName);
                }
            } catch (APIManagementException e) {
                log.error("Error while retrieving claims ", e);
            } catch (UserStoreException e) {
                log.error("Error while retrieving user store ", e);
            }
        }
        return null;
    }
    protected Map<String, String> getClaimsFromCache(AuthorizationGrantCacheKey cacheKey) {

        AuthorizationGrantCacheEntry cacheEntry = AuthorizationGrantCache.getInstance().getValueFromCacheByToken(cacheKey);
        if (cacheEntry == null) {
            return new HashMap<String, String>();
        }
        Map<ClaimMapping, String> userAttributes = cacheEntry.getUserAttributes();
        Map<String, String> userClaims = new HashMap<String, String>();
        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            userClaims.put(entry.getKey().getRemoteClaim().getClaimUri(), entry.getValue());
        }
        return userClaims;
    }
}
