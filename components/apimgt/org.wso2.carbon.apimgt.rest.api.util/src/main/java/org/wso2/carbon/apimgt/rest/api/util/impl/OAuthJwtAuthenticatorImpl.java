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
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.text.ParseException;
import java.net.MalformedURLException;

import com.nimbusds.jwt.util.DateUtils;
import org.wso2.carbon.apimgt.impl.jwt.*;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.apimgt.impl.APIConstants.JwtTokenConstants;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.OAuthAuthenticator;
import org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil;

import static org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil.getRestApiJWTAuthAudiences;

public class OAuthJwtAuthenticatorImpl implements OAuthAuthenticator {

    private static final Log log = LogFactory.getLog(OAuthJwtAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private boolean isRESTApiTokenCacheEnabled;
    private JWTValidator jwtValidator;
    private Map<String, TokenIssuerDto> tokenIssuers;
    private java.util.Map<String, String> audiencesList;

    public OAuthJwtAuthenticatorImpl() {
        jwtValidator = new JWTValidatorImpl();
        tokenIssuers = getTokenIssuers();
        audiencesList = getRestApiJWTAuthAudiences();
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
            SignedJWTInfo signedJWTInfo = getSignedJwt(accessToken);
            String jwtTokenIdentifier = getJWTTokenIdentifier(signedJWTInfo);
            String jwtHeader = signedJWTInfo.getSignedJWT().getHeader().toString();
            URL basePath = new URL(message.get("http.base.path").toString());

            //Validate token
            log.info("Starting JWT token validation");
            JWTValidationInfo jwtValidationInfo =
                    validateJWTToken(signedJWTInfo, jwtTokenIdentifier, accessToken, jwtHeader, basePath);
            if (jwtValidationInfo != null) {
                if (jwtValidationInfo.isValid()) {
                    if (isRESTApiTokenCacheEnabled) {
                        getRESTAPITokenCache().put(jwtTokenIdentifier, jwtValidationInfo);
                    }
                    //Validating scopes
                    log.info("JWT token validation success. starting scope validation");
                    return handleScopeValidation(message, signedJWTInfo, accessToken);
                } else {
                    log.error("Invalid JWT token :" + getMaskedToken(jwtHeader));
                    return false;
                }

            } else {
                log.error("Invalid JWT token :" + getMaskedToken(jwtHeader));
                return false;
            }
        } catch (ParseException e) {
            log.error("Not a JWT token. Failed to decode the token. Reason: " + e.getMessage());
        } catch (MalformedURLException e) {
            log.error("Malformed URL found in request path.Reason: " + e.getMessage());
        }
        return false;
    }

    /**
     * Handle scope validation
     *
     * @param accessToken   JWT token
     * @param signedJWTInfo : Signed token info
     * @param message       : cxf Message
     */
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
                    String username = oauthTokenInfo.getEndUserName(); //
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
                log.info("Scope validation success");
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
     * @param jti           jwtTokenIdentifier
     * @param signedJWTInfo signed jwt info object
     * @return JWTValidationInfo : token validated info
     */
    private JWTValidationInfo validateJWTToken(SignedJWTInfo signedJWTInfo, String jti, String accessToken,
                                               String jwtHeader, URL basePath) throws APIManagementException {

        JWTValidationInfo jwtValidationInfo;
        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();

        if (StringUtils.isNotEmpty(issuer)) {
            //validate Issuer
            List<String> audiences = signedJWTInfo.getJwtClaimsSet().getAudience();
            if (tokenIssuers != null && tokenIssuers.containsKey(issuer)) {
                jwtValidator.loadTokenIssuerConfiguration(tokenIssuers.get(issuer));
                //validate audience
                if (audiencesList != null && audiences.contains(audiencesList.get(basePath.getPath()))) {
                    if (isRESTApiTokenCacheEnabled) {
                        JWTValidationInfo tempJWTValidationInfo = (JWTValidationInfo) getRESTAPITokenCache().get(jti);
                        if (tempJWTValidationInfo != null) {
                            Boolean isExpired = checkTokenExpiration(new Date(tempJWTValidationInfo.getExpiryTime()));
                            if (isExpired) {
                                tempJWTValidationInfo.setValid(false);
                                getRESTAPITokenCache().remove(jti);
                                getRESTAPIInvalidTokenCache().put(jti, tempJWTValidationInfo);
                                log.error("JWT token validation failed. Reason: Expired Token. "
                                        + getMaskedToken(jwtHeader));
                                return tempJWTValidationInfo;
                            }
                            //check accessToken
                            if (!tempJWTValidationInfo.getRawPayload().equals(accessToken)) {
                                tempJWTValidationInfo.setValid(false);
                                getRESTAPITokenCache().remove(jti);
                                getRESTAPIInvalidTokenCache().put(jti, tempJWTValidationInfo);
                                log.error("JWT token validation failed. Reason: Invalid Token. "
                                        + getMaskedToken(jwtHeader));
                                return tempJWTValidationInfo;
                            }
                            return tempJWTValidationInfo;

                        } else if (getRESTAPIInvalidTokenCache().get(jti) != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Token retrieved from the invalid token cache. Token: "
                                        + getMaskedToken(jwtHeader));
                            }
                            return (JWTValidationInfo) getRESTAPIInvalidTokenCache().get(jti);
                        }
                    }
                    //info not in cache
                    //validate signature and exp
                    jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
                    if (jwtValidationInfo.isValid()) {
                        //valid token
                        if (isRESTApiTokenCacheEnabled) {
                            getRESTAPITokenCache().put(jti, jwtValidationInfo);
                        }
                    } else {
                        //put in invalid cache
                        if (isRESTApiTokenCacheEnabled) {
                            getRESTAPIInvalidTokenCache().put(jti, jwtValidationInfo);
                        }
                        //invalid credentials : 900901 error code
                        log.error("JWT token validation failed. Reason: Invalid Credentials. " +
                                "Make sure you have provided the correct security credentials in the token :"
                                + getMaskedToken(jwtHeader));
                    }
                } else {
                    log.error("JWT token audience validation failed. Reason: Non of the aud present "
                            + "in the JWT (" + signedJWTInfo.getJwtClaimsSet().getAudience().toString() +
                            ") matches the intended audience. (" + audiencesList.values().toString() + ")");
                    return null;
                }
            } else {
                //invalid issuer. invalid token
                log.error("JWT token issuer validation failed. Reason: Issuer present in the JWT (" + issuer
                        + ") does not match with the token issuer (" + tokenIssuers.keySet().toString() + ")");
                return null;
            }
        } else {
            log.error("Issuer is not found in the token");
            return null;
        }
        return jwtValidationInfo;
    }

    /**
     * Retrieve token issuer details from deployment.toml file.
     *
     * @return Map<String, TokenIssuerDto>
     */
    private Map<String, TokenIssuerDto> getTokenIssuers() {
        return APIMConfigUtil.getTokenIssuerMap();

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

    /**
     * Get jti information.
     *
     * @param signedJWTInfo
     * @return String : jti
     */
    private String getJWTTokenIdentifier(SignedJWTInfo signedJWTInfo) {

        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        String jwtid = jwtClaimsSet.getJWTID();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(jwtid)) {
            return jwtid;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
    }

    /**
     * Get masked JWT token.
     *
     * @param token The JWT token
     * @return String : maskedtoken
     */
    public static String getMaskedToken(String token) {

        if (token.length() >= 10) {
            return "XXXXX" + token.substring(token.length() - 10);
        } else {
            return "XXXXX" + token.substring(token.length() / 2);
        }
    }
}
