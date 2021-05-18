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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.MethodStats;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.claim.mgt.ClaimManagementException;
import org.wso2.carbon.claim.mgt.ClaimManagerHandler;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
        String usernameWithoutTenantDomain = MultitenantUtils.getTenantAwareUsername(endUserName);
        Map<String, String> claims = new LinkedHashMap<String, String>(20);
        // dialect is either empty or '/' do not append a backslash. otherwise append a backslash '/'
        if (!"".equals(dialect) && !"/".equals(dialect)) {
            dialect = dialect + "/";
        } 
        claims.put("iss", API_GATEWAY_ID);
        claims.put("exp", String.valueOf(expireIn));
        claims.put("iat", String.valueOf(currentTime));
        claims.put("sub", usernameWithoutTenantDomain);
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

        Map<String, String> customClaims = new HashMap<String, String>();
        Map<String, Object> properties = new HashMap<String, Object>();

        String accessToken = validationContext.getAccessToken();
        if (accessToken != null) {
            properties.put(APIConstants.KeyManager.ACCESS_TOKEN, accessToken);
        }

        String username = validationContext.getValidationInfoDTO().getEndUserName();
        int tenantId = APIUtil.getTenantId(username);

        String dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getJwtConfigurationDto().getConsumerDialectUri();
        if (!StringUtils.isEmpty(dialectURI)) {
            properties.put(APIConstants.KeyManager.CLAIM_DIALECT, dialectURI);
            String keymanagerName = validationContext.getValidationInfoDTO().getKeyManager();
            KeyManager keymanager = KeyManagerHolder.getKeyManagerInstance(APIUtil.getTenantDomainFromTenantId(tenantId),
                    keymanagerName);
            customClaims = keymanager.getUserClaims(username, properties);
            if (log.isDebugEnabled()) {
                log.debug("Retrieved claims :" + customClaims);
            }
        }
        
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            customClaims.putAll(claimsRetriever.getClaims(username));
        }
        return customClaims;
    }

    protected Map<String, String> convertClaimMap(Map<ClaimMapping, String> userAttributes, String username)
            throws APIManagementException {

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> userClaimsCopy = new HashMap<>();
        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            Claim claimObject = entry.getKey().getLocalClaim();
            if (claimObject == null) {
                claimObject = entry.getKey().getRemoteClaim();
            }
            userClaims.put(claimObject.getClaimUri(), entry.getValue());
            userClaimsCopy.put(claimObject.getClaimUri(), entry.getValue());
        }

        String convertClaimsFromOIDCtoConsumerDialect =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.CONVERT_CLAIMS_TO_CONSUMER_DIALECT);

        if (convertClaimsFromOIDCtoConsumerDialect != null &&
                !Boolean.parseBoolean(convertClaimsFromOIDCtoConsumerDialect)) {
            return userClaims;
        }

        int tenantId = APIUtil.getTenantId(username);
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        String dialect;
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            dialect = claimsRetriever.getDialectURI(username);
        } else {
            dialect = getDialectURI();
        }

        Map<String, String> configuredDialectToCarbonClaimMapping = null; // (key) configuredDialectClaimURI -> (value)
        // carbonClaimURI
        Map<String, String> carbonToOIDCclaimMapping = null; // (key) carbonClaimURI ->  value (oidcClaimURI)

        Set<String> claimUris = new HashSet<String>(userClaims.keySet());
        try {
            carbonToOIDCclaimMapping =
                    new ClaimMetadataHandler().getMappingsMapFromOtherDialectToCarbon(OIDC_DIALECT_URI, claimUris,
                            tenantDomain, true);
            configuredDialectToCarbonClaimMapping =
                    ClaimManagerHandler.getInstance().getMappingsMapFromCarbonDialectToOther(dialect,
                            carbonToOIDCclaimMapping.keySet(), tenantDomain);
        } catch (ClaimMetadataException e) {
            String error = "Error while mapping claims from Carbon dialect to " + OIDC_DIALECT_URI + " dialect";
            throw new APIManagementException(error, e);
        } catch (ClaimManagementException e) {
            String error = "Error while mapping claims from configured dialect to Carbon dialect";
            throw new APIManagementException(error, e);
        }

        for (Map.Entry<String, String> oidcClaimValEntry : userClaims.entrySet()) {
            for (Map.Entry<String, String> carbonToOIDCEntry : carbonToOIDCclaimMapping.entrySet()) {
                if (oidcClaimValEntry.getKey().equals(carbonToOIDCEntry.getValue())) {
                    for (Map.Entry<String, String> configuredToCarbonEntry : configuredDialectToCarbonClaimMapping.entrySet()) {
                        if (configuredToCarbonEntry.getValue().equals(carbonToOIDCEntry.getKey())) {
                            userClaimsCopy.remove(oidcClaimValEntry.getKey());
                            userClaimsCopy.put(configuredToCarbonEntry.getKey(), oidcClaimValEntry.getValue());
                        }
                    }
                }
            }
        }

        return userClaimsCopy;
    }
}
