/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.apache.cxf.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.apimgt.rest.api.util.utils.OAuthTokenInfo;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.user.api.UserStoreException;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;

import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;

import java.util.Date;

import com.nimbusds.jwt.util.DateUtils;
import org.wso2.carbon.apimgt.impl.jwt.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.apimgt.impl.APIConstants.JwtTokenConstants;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.OAuthAuthenticator;

public class OAuthJwtAuthenticatorImpl implements OAuthAuthenticator {

    private static final Log log = LogFactory.getLog(OAuthJwtAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private boolean isRESTApiTokenCacheEnabled;
    private JWTValidator jwtValidator;
    private TokenIssuerDto tokenIssuerDto;

    public OAuthJwtAuthenticatorImpl() {
        jwtValidator = new JWTValidatorImpl();
        tokenIssuerDto = getTokenIssuerDto();
        jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
    }

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     */
    public boolean authenticate(Message message) throws APIManagementException {

        RESTAPICacheConfiguration cacheConfiguration = APIUtil.getRESTAPICacheConfig();
        isRESTApiTokenCacheEnabled = cacheConfiguration.isTokenCacheEnabled();
        String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(message,
                RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);

        if (StringUtils.countMatches(accessToken, APIConstants.DOT) != 2) {
            log.error("Invalid JWT token. The expected token format is <header.payload.signature>");
            return false;
        }
        try {
            SignedJWTInfo signedJWTInfo;

            if (isRESTApiTokenCacheEnabled) {
                signedJWTInfo = (SignedJWTInfo) getRESTAPITokenCache().get(accessToken);
                if (signedJWTInfo != null) {
                    Boolean isExpired = checkTokenExpiration(signedJWTInfo.getJwtClaimsSet().getExpirationTime());
                    if (isExpired) {
                        getRESTAPITokenCache().remove(accessToken);
                        getRESTAPIInvalidTokenCache().put(accessToken, signedJWTInfo);
                        return false;
                    }
                    return handleScopeValidation(message, signedJWTInfo, accessToken);

                } else if (getRESTAPIInvalidTokenCache().get(accessToken) != null) {
                    log.debug("Invalid JWT token. ");
                    return false;
                } else {
                    signedJWTInfo = getSignedJwt(accessToken);
                }
            } else {
                signedJWTInfo = getSignedJwt(accessToken);
            }
            if (validateJWTToken(signedJWTInfo, accessToken)) {
                if (isRESTApiTokenCacheEnabled) {
                    getRESTAPITokenCache().put(accessToken, signedJWTInfo);
                }
                log.info("validating scopes");
                //Validating scopes
                return handleScopeValidation(message, signedJWTInfo, accessToken);
            } else {
                //put the obj into invalid cache
                if (isRESTApiTokenCacheEnabled) {
                    getRESTAPIInvalidTokenCache().put(accessToken, signedJWTInfo);
                }
                log.error("Invalid JWT token");
                return false;
            }
        } catch (ParseException e) {
            log.error("Not a JWT token. Failed to decode the token. Reason: " + e.getMessage());
        }
        return false;
    }

    /**
     * Handle scope validation and .
     *
     * @param accessToken   JWT token
     * @param signedJWTInfo : Signed token info
     */
    //take message as 1st para
    private boolean handleScopeValidation(Message message, SignedJWTInfo signedJWTInfo, String accessToken)
            throws ParseException {

        OAuthTokenInfo oauthTokenInfo = new OAuthTokenInfo();
        oauthTokenInfo.setAccessToken(accessToken);
        oauthTokenInfo.setEndUserName(signedJWTInfo.getJwtClaimsSet().getSubject());

        if (signedJWTInfo.getJwtClaimsSet().getClaim(APIConstants.JwtTokenConstants.SCOPE) != null) {

            String orgId = message.get(RestApiConstants.ORGANIZATION).toString();
            String[] scopes = signedJWTInfo.getJwtClaimsSet().getStringClaim(JwtTokenConstants.SCOPE)
                    .split(JwtTokenConstants.SCOPE_DELIMITER);
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
                        log.debug("username = " + username);
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
                return true;
            }
            log.error("scopes validation failed");
            return false;
        }
        log.error("scopes validation failed");
        return false;
    }

    /**
     * Get signed jwt info.
     *
     * @param accessToken JWT token
     * @return SignedJWTInfo : Signed token info
     */
    private SignedJWTInfo getSignedJwt(String accessToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        return new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
    }

    /**
     * Validate the JWT token.
     *
     * @param accessToken   JWT token
     * @param signedJWTInfo signed jwt info object
     * @return JWTValidationInfo : token validated info
     */
    private boolean validateJWTToken(SignedJWTInfo signedJWTInfo, String accessToken) throws APIManagementException {

        JWTValidationInfo jwtValidationInfo;
        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();

        if (StringUtils.isNotEmpty(issuer)) {
            //validate Issuer
            if (tokenIssuerDto.getIssuer() != null && issuer.equals(tokenIssuerDto.getIssuer())) {
                //validate aud
                if (signedJWTInfo.getJwtClaimsSet().getAudience() != null) {
                    if (signedJWTInfo.getJwtClaimsSet().getAudience().contains(tokenIssuerDto.getAudience())) {
                        //validate signature and exp with JWTValidator
                        jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
                        return jwtValidationInfo.isValid();
                    } else {
                        //invalid audience
                        log.error("JWT token audience validation failed. Reason: Non of the aud present "
                                + "in the JWT (" + signedJWTInfo.getJwtClaimsSet().getAudience().toString() +
                                ") matches the intended audience. (" + tokenIssuerDto.getAudience() + ")");
                        return false;
                    }
                }
            } else {
                //invalid issuer. invalid token
                log.error("JWT token issuer validation failed. Reason: Issuer present in the JWT (" + issuer
                        + ") does not match with the token issuer (" + tokenIssuerDto.getIssuer() + ")");
                return false;
            }
        }
        log.error("Issuer is not found in the token");
        return false;
    }

    /**
     * Retrieve token issuer details from deployment.toml file.
     *
     * @return TokenIssuerDto
     */
    private TokenIssuerDto getTokenIssuerDto() {
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto();
        APIManagerConfiguration apiManagerConfiguration = getApiManagerConfiguration();
        if (apiManagerConfiguration != null) {
            tokenIssuerDto.setIssuer(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI
                    + APIConstants.JWT_ISSUER));
            tokenIssuerDto.setAudience(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI
                    + APIConstants.JWT_AUDIENCE));
            tokenIssuerDto.setAlias(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI
                    + APIConstants.JWT_CERT_ALIAS));
        }
        return tokenIssuerDto;
    }

    /**
     * @return APIManagerConfiguration
     */
    private static APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param tokenExp The ExpiryTime of the JWT token
     * @return
     */
    private boolean checkTokenExpiration(Date tokenExp) {
        Date now = new Date();
        return DateUtils.isBefore(tokenExp, now, RestApiConstants.TIMESTAMP_SKEW_INSECONDS);
    }
}
