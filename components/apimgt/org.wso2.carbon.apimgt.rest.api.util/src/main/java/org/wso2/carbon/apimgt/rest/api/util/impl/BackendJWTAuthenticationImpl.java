/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.OAuthTokenInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.AbstractOAuthAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.text.ParseException;

/**
 * This class is for Authenticate API requests that coming with X-JWT-Assertion header. X-JWT-Assertion header transporting
 * the backend JWT.Validating the JWT is not required.
 * */

public class BackendJWTAuthenticationImpl extends AbstractOAuthAuthenticator {

    private static final Log log = LogFactory.getLog(BackendJWTAuthenticationImpl.class);
    private boolean isRESTApiTokenCacheEnabled;
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    @Override
    public boolean authenticate(Message message) throws APIManagementException {
        RESTAPICacheConfiguration cacheConfiguration = APIUtil.getRESTAPICacheConfig();
        isRESTApiTokenCacheEnabled = cacheConfiguration.isTokenCacheEnabled();
        String accessToken = (String) message.get(RestApiConstants.JWT_TOKEN);

        if ((accessToken != null && StringUtils.countMatches(accessToken, APIConstants.DOT) != 2)) {
            log.error("Invalid JWT token. The expected token format is <header.payload.signature>");
            return false;
        }
        try {
            SignedJWTInfo signedJWTInfo = null;
            String token = (String) message.get(RestApiConstants.JWT_TOKEN);
            if (accessToken != null) {
                signedJWTInfo = getSignedJwt(accessToken);
            }

            if (signedJWTInfo != null) {
                String jwtTokenIdentifier = getJWTTokenIdentifier(signedJWTInfo);

                JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
                jwtValidationInfo.setValid(true);
                if (isRESTApiTokenCacheEnabled) {
                    getRESTAPITokenCache().put(jwtTokenIdentifier, jwtValidationInfo);
                }
                //Validating scopes
                return handleScopeValidation(message, signedJWTInfo, token);
            } else {
                log.error("Invalid Signed JWT :" + signedJWTInfo);
                return false;
            }

        } catch (ParseException e) {
            log.error("Not a JWT token. Failed to decode the token. Reason: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get signed jwt info.
     *
     * @param accessToken JWT token
     * @return SignedJWTInfo : Signed token info
     */
    @MethodStats
    private SignedJWTInfo getSignedJwt(String accessToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        return new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
    }

    /**
     * Get jti information.
     *
     * @param signedJWTInfo
     * @return String : jti
     */
    private String getJWTTokenIdentifier(SignedJWTInfo signedJWTInfo) {

        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        String jwtID = jwtClaimsSet.getJWTID();
        if (StringUtils.isNotEmpty(jwtID)) {
            return jwtID;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
    }

    /**
     * Handle scope validation
     *
     * @param accessToken   JWT token
     * @param signedJWTInfo : Signed token info
     * @param message       : cxf Message
     */
    private boolean handleScopeValidation(Message message, SignedJWTInfo signedJWTInfo, String accessToken)
            throws APIManagementException, ParseException {

        String maskedToken = message.get(RestApiConstants.MASKED_TOKEN).toString();
        OAuthTokenInfo oauthTokenInfo = new OAuthTokenInfo();
        oauthTokenInfo.setAccessToken(accessToken);
        oauthTokenInfo.setEndUserName(signedJWTInfo.getJwtClaimsSet().getSubject());
        String scopeClaim = signedJWTInfo.getJwtClaimsSet().getStringClaim(APIConstants.JwtTokenConstants.SCOPE);
        if (scopeClaim != null) {
            String orgId = RestApiUtil.resolveOrganization(message);
            String[] scopes = scopeClaim.split(APIConstants.JwtTokenConstants.SCOPE_DELIMITER);
            scopes = java.util.Arrays.stream(scopes).filter(s -> s.contains(orgId))
                    .map(s -> s.replace(APIConstants.URN_CHOREO + orgId + ":", ""))
                    .toArray(size -> new String[size]);
            oauthTokenInfo.setScopes(scopes);

            if (validateScopes(message, oauthTokenInfo)) {
                //Add the user scopes list extracted from token to the cxf message
                message.getExchange().put(RestApiConstants.USER_REST_API_SCOPES, oauthTokenInfo.getScopes());
                //If scope validation successful then set tenant name and user name to current context
                String tenantDomain = MultitenantUtils.getTenantDomain(oauthTokenInfo.getEndUserName());
                int tenantId;
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
                try {
                    String username = oauthTokenInfo.getEndUserName();
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
                        log.debug("username = " + username + "masked token " + maskedToken);
                    }
                    tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    carbonContext.setTenantDomain(tenantDomain);
                    carbonContext.setTenantId(tenantId);
                    carbonContext.setUsername(username);
                    if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        APIUtil.loadTenantConfigBlockingMode(tenantDomain);
                    }
                    return true;
                } catch (UserStoreException e) {
                    log.error("Error while retrieving tenant id for tenant domain: " + tenantDomain, e);
                }
                log.debug("Scope validation success for the token " + maskedToken);
                return true;
            }
            log.error("scopes validation failed for the token" + maskedToken);
            return false;
        }
        log.error("scopes validation failed for the token" + maskedToken);
        return false;
    }
}
