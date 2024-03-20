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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.OAuthTokenInfo;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.JwtTokenConstants;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.AbstractOAuthAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.security.cert.X509Certificate;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * This OAuthJwtAuthenticatorImpl class specifically implemented for API Manager store and publisher rest APIs'
 * JWT based authentication.
 */
public class OAuthJwtAuthenticatorImpl extends AbstractOAuthAuthenticator {

    private static final Log log = LogFactory.getLog(OAuthJwtAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private static final String OIDC_IDP_ENTITY_ID = "IdPEntityId";
    public static final String JWKS_URI = "jwksUri";
    private boolean isRESTApiTokenCacheEnabled;
    private Map<String, TokenIssuerDto> tokenIssuers;

    public OAuthJwtAuthenticatorImpl() {
        tokenIssuers = getTokenIssuers();
    }

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     */
    @Override
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
            String maskedToken = message.get(RestApiConstants.MASKED_TOKEN).toString();
            URL basePath = new URL(message.get(APIConstants.BASE_PATH).toString());

            //Validate token
            log.debug("Starting JWT token validation " + maskedToken);
            JWTValidationInfo jwtValidationInfo =
                    validateJWTToken(signedJWTInfo, jwtTokenIdentifier, accessToken, maskedToken, basePath);
            if (jwtValidationInfo != null) {
                if (jwtValidationInfo.isValid()) {
                    if (isRESTApiTokenCacheEnabled) {
                        getRESTAPITokenCache().put(jwtTokenIdentifier, jwtValidationInfo);
                    }
                    //Validating scopes
                    return handleScopeValidation(message, signedJWTInfo, accessToken);
                } else {
                    log.error("Invalid JWT token :" + maskedToken);
                    return false;
                }

            } else {
                log.error("Invalid JWT token :" + maskedToken);
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
            throws APIManagementException, ParseException {

        String maskedToken = message.get(RestApiConstants.MASKED_TOKEN).toString();
        OAuthTokenInfo oauthTokenInfo = new OAuthTokenInfo();
        oauthTokenInfo.setAccessToken(accessToken);
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (signedJWTInfo.getJwtClaimsSet().getClaim(JwtTokenConstants.USER_DOMAIN) != null) {
            tenantDomain = (String) signedJWTInfo.getJwtClaimsSet().getClaim(JwtTokenConstants.USER_DOMAIN);
        } 
        log.debug("Tenant domain for user " + tenantDomain);
        oauthTokenInfo.setEndUserName(signedJWTInfo.getJwtClaimsSet().getSubject() + "@" + tenantDomain);
        oauthTokenInfo.setConsumerKey(signedJWTInfo.getJwtClaimsSet().getStringClaim(JWTConstants.AUTHORIZED_PARTY));
        String scopeClaim = signedJWTInfo.getJwtClaimsSet().getStringClaim(JwtTokenConstants.SCOPE);
        if (scopeClaim != null) {
            String orgId = RestApiUtil.resolveOrganization(message);
            String[] scopes = scopeClaim.split(JwtTokenConstants.SCOPE_DELIMITER);
            oauthTokenInfo.setScopes(scopes);
            Map<String, Object> authContext = RestApiUtil.addToJWTAuthenticationContext(message);
            String basePath = (String) message.get(RestApiConstants.BASE_PATH);
            String version = (String) message.get(RestApiConstants.API_VERSION);
            authContext.put(RestApiConstants.URI_TEMPLATES, RestApiCommonUtil.getURITemplatesForBasePath(basePath
                    + version));

            if (RestApiCommonUtil.validateScopes(authContext, oauthTokenInfo)) {
                //Add the user scopes list extracted from token to the cxf message
                message.getExchange().put(RestApiConstants.USER_REST_API_SCOPES, oauthTokenInfo.getScopes());
                //If scope validation successful then set tenant name and user name to current context

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
                    message.put(RestApiConstants.AUTH_TOKEN_INFO, oauthTokenInfo);
                    message.put(RestApiConstants.SUB_ORGANIZATION, tenantDomain);
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
     * Validate the JWT token.
     *
     * @param jti           jwtTokenIdentifier
     * @param signedJWTInfo signed jwt info object
     * @return JWTValidationInfo : token validated info
     */
    @MethodStats
    private JWTValidationInfo validateJWTToken(SignedJWTInfo signedJWTInfo, String jti, String accessToken,
                                               String maskedToken, URL basePath) throws APIManagementException {

        JWTValidationInfo jwtValidationInfo;
        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();

        if (StringUtils.isNotEmpty(issuer)) {
            //validate Issuer
            JWTValidator jwtValidator;
            try {
                jwtValidator = validateAndGetJWTValidatorForIssuer(signedJWTInfo.getJwtClaimsSet());
            } catch (APIManagementException e) {
                log.error(e.getMessage(), e);
                return null;
            }
            if (isRESTApiTokenCacheEnabled) {
                JWTValidationInfo tempJWTValidationInfo = (JWTValidationInfo) getRESTAPITokenCache().get(jti);
                if (tempJWTValidationInfo != null) {
                    boolean isExpired = checkTokenExpiration(new Date(tempJWTValidationInfo.getExpiryTime()));
                    if (isExpired) {
                        tempJWTValidationInfo.setValid(false);
                        getRESTAPITokenCache().remove(jti);
                        getRESTAPIInvalidTokenCache().put(jti, tempJWTValidationInfo);
                        log.error("JWT token validation failed. Reason: Expired Token. " + maskedToken);
                        return tempJWTValidationInfo;
                    }
                    //check accessToken
                    if (!tempJWTValidationInfo.getRawPayload().equals(accessToken)) {
                        tempJWTValidationInfo.setValid(false);
                        getRESTAPITokenCache().remove(jti);
                        getRESTAPIInvalidTokenCache().put(jti, tempJWTValidationInfo);
                        log.error("JWT token validation failed. Reason: Invalid Token. " + maskedToken);
                        return tempJWTValidationInfo;
                    }
                    return tempJWTValidationInfo;

                } else if (getRESTAPIInvalidTokenCache().get(jti) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the invalid token cache. Token: " + maskedToken);
                    }
                    return (JWTValidationInfo) getRESTAPIInvalidTokenCache().get(jti);
                }
            }
            //info not in cache. validate signature and exp
            if (jwtValidator != null) {
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
                            + maskedToken);
                }
            } else {
                log.error("JWT token issuer validation failed. Reason: Cannot find a JWTValidator for the " +
                        "issuer present in the JWT: " + issuer);
                return null;
            }

        } else {
            log.error("Issuer is not found in the token " + maskedToken);
            return null;
        }
        return jwtValidationInfo;
    }

    /**
     * Retrieve JWT Validator for the given issuer.
     *
     * @param issuer       Issuer from the token
     * @return JWTValidator implementation for the given issuer.
     */
    private JWTValidator getJWTValidator(String issuer) {

        return APIMConfigUtil.getJWTValidatorMap().get(issuer);
    }

    /**
     * Validate issuer in the token against the registered token issuers/default key manager issuer.
     *
     * @param jwtClaimsSet JWT Claim set from the token
     * @return if issuer validation fails or success
     * @throws APIManagementException if an error occurs during validation
     */
    private JWTValidator validateAndGetJWTValidatorForIssuer(JWTClaimsSet jwtClaimsSet)
            throws APIManagementException {

        String tokenIssuer = jwtClaimsSet.getIssuer();
        if (tokenIssuers != null && !tokenIssuers.isEmpty()) {
            if (tokenIssuers.containsKey(tokenIssuer)) {
                return getJWTValidator(tokenIssuer);
            }
            throw new APIManagementException("JWT token issuer validation failed. Reason: Issuer present in the JWT ("
                    + tokenIssuer + ") does not match with the token issuer (" + tokenIssuers.keySet() + ")");
        }
        String residentTenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
        if (jwtClaimsSet.getClaim(JwtTokenConstants.APP_DOMAIN) != null) {
            residentTenantDomain = (String) jwtClaimsSet.getClaim(JwtTokenConstants.APP_DOMAIN);
        } 
        log.debug("Tenant domain for residant IDP " + residentTenantDomain);
        IdentityProvider residentIDP = validateAndGetResidentIDPForIssuer(residentTenantDomain, tokenIssuer);
        if (residentIDP == null) {
            //invalid issuer. invalid token
            throw new APIManagementException("JWT token issuer validation failed. Reason: Resident Identity Provider "
                    + "cannot be found for the organization: " + residentTenantDomain);
        }
        JWTValidator jwtValidator = new JWTValidatorImpl();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto(tokenIssuer);
        if (residentIDP.getCertificate() != null) {
            X509Certificate certificate = APIUtil.retrieveCertificateFromContent(residentIDP.getCertificate());
            if (certificate == null) {
                throw new APIManagementException("JWT token issuer validation failed. Reason: Certificate for resident"
                        + " identity provider is not in base64 encoded format in organization: "
                        + residentTenantDomain);
            }
            tokenIssuerDto.setCertificate(certificate);

        } else {
            throw new APIManagementException("JWT token issuer validation failed. Reason: Certificate for resident" +
                    " identity provider cannot be found for the organization: " + residentTenantDomain);
        }
        jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
        return jwtValidator;
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
        String jwtID = jwtClaimsSet.getJWTID();
        if (StringUtils.isNotEmpty(jwtID)) {
            return jwtID;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
    }


    /**
     * Validate issuer and get resident Identity Provider.
     *
     * @param tenantDomain tenant Domain
     * @param jwtIssuer    issuer extracted from assertion
     * @return resident Identity Provider
     * @throws APIManagementException if an error occurs while retrieving resident idp issuer
     */
    private IdentityProvider validateAndGetResidentIDPForIssuer(String tenantDomain, String jwtIssuer)
            throws APIManagementException {

        String issuer = StringUtils.EMPTY;
        IdentityProvider residentIdentityProvider;
        try {
            residentIdentityProvider = IdentityProviderManager.getInstance().getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            String errorMsg = String.format("Error while getting Resident Identity Provider of '%s' tenant.",
                    tenantDomain);
            throw new APIManagementException(errorMsg, e);
        }
        FederatedAuthenticatorConfig[] fedAuthnConfigs = residentIdentityProvider.getFederatedAuthenticatorConfigs();
        FederatedAuthenticatorConfig oauthAuthenticatorConfig =
                IdentityApplicationManagementUtil.getFederatedAuthenticator(fedAuthnConfigs,
                        IdentityApplicationConstants.Authenticator.OIDC.NAME);
        if (oauthAuthenticatorConfig != null) {
            issuer = IdentityApplicationManagementUtil.getProperty(oauthAuthenticatorConfig.getProperties(),
                    OIDC_IDP_ENTITY_ID).getValue();
        }
        return jwtIssuer.equals(issuer) ? residentIdentityProvider : null;
    }
}
