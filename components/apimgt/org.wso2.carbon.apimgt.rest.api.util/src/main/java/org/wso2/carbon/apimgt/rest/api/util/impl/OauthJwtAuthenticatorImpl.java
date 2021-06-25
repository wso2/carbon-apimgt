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
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.text.ParseException;

import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import java.util.Date;
import com.nimbusds.jwt.util.DateUtils;
import org.wso2.carbon.apimgt.impl.jwt.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.rest.api.util.utils.OauthTokenInfo;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.OauthAuthenticator;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class OauthJwtAuthenticatorImpl implements OauthAuthenticator {

    private static final Log log = LogFactory.getLog(OauthJwtAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private boolean isRESTApiTokenCacheEnabled;
    private static String jti = "";
    private static String signature  = "";

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     * @throws APIManagementException when error in authentication process
     */
    public boolean authenticate(Message message) throws APIManagementException {

        RESTAPICacheConfiguration cacheConfiguration = APIUtil.getRESTAPICacheConfig();
        isRESTApiTokenCacheEnabled = cacheConfiguration.isTokenCacheEnabled();
        String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(message,
                RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);

        if (StringUtils.countMatches(accessToken, APIConstants.DOT) != 2) {
            log.debug("Invalid JWT token. The expected token format is <header.payload.signature>");
//            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
//                    "Invalid JWT token");
            return false;
        }
        try {
            signature = accessToken.split("\\.")[2];
            SignedJWTInfo signedJWTInfo = getSignedJwt(accessToken);
//            jti = getJWTTokenIdentifier(signedJWTInfo);

            JWTValidationInfo jwtValidationInfo = validateJWTToken(signedJWTInfo, accessToken);

//                if (isRESTApiTokenCacheEnabled){
//                    cacheValidationInfo(jwtValidationInfo, signedJWTInfo);
//                }
                if (jwtValidationInfo.isValid()) {
//                    //validate nbf if defined
//                    if(signedJWTInfo.getJwtClaimsSet().getNotBeforeTime() != null){
//                        Boolean checkNbf = checkTokenExpiration(signedJWTInfo.getJwtClaimsSet().getNotBeforeTime());
//                    }
                    //put the obj into cache
                    if (isRESTApiTokenCacheEnabled){
                        getRESTAPITokenCache().put(accessToken, jwtValidationInfo);
                    }

                    log.info("JWT token validation successful... validating scopes");

                    //TODO: validate scopes
                    OauthTokenInfo oauthTokenInfo = new OauthTokenInfo();
                    oauthTokenInfo.setAccessToken(accessToken);
                    oauthTokenInfo.setEndUserName(signedJWTInfo.getJwtClaimsSet().getSubject());
//                    OauthTokenInfo oauthTokenInfo = OauthOpaqueAuthenticatorImpl.getTokenMetaData(accessToken);
                    //TODO:APIConstants.JwtTokenConstants.SCOPE = scopes
                    if (signedJWTInfo.getJwtClaimsSet().getClaim(APIConstants.JwtTokenConstants.SCOPE) != null) {
                        String orgId = message.getId();
                        String[] scopes = signedJWTInfo.getJwtClaimsSet().getStringClaim(org.wso2.carbon.apimgt.impl.APIConstants.JwtTokenConstants.SCOPE)
                                .split(org.wso2.carbon.apimgt.impl.APIConstants.JwtTokenConstants.SCOPE_DELIMITER);
                        scopes = java.util.Arrays.stream(scopes)
                                .map(s -> s.split(":",2)[1])
                                .toArray(size -> new String[size]);
                        oauthTokenInfo.setScopes(scopes);
                        if(validateScopes(message,oauthTokenInfo)){
                            log.info("Scope validation successful");
                            log.info("user grant access");
                            //Add the user scopes list extracted from token to the cxf message
                            message.getExchange().put(RestApiConstants.USER_REST_API_SCOPES, oauthTokenInfo.getScopes());
                            //If scope validation successful then set tenant name and user name to current context
                            String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantUtils.getTenantDomain(oauthTokenInfo.getEndUserName());
                            int tenantId;
                            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                            org.wso2.carbon.user.core.service.RealmService realmService = (org.wso2.carbon.user.core.service.RealmService) carbonContext.getOSGiService(org.wso2.carbon.user.core.service.RealmService.class, null);
                            try {
                                String username = oauthTokenInfo.getEndUserName();
                                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                                    //when the username is an email in supertenant, it has at least 2 occurrences of '@'
                                    long count = username.chars().filter(ch -> ch == '@').count();
                                    //in the case of email, there will be more than one '@'
                                    boolean isEmailUsernameEnabled = Boolean.parseBoolean(org.wso2.carbon.utils.CarbonUtils.getServerConfiguration().
                                            getFirstProperty("EnableEmailUserName"));
                                    if (isEmailUsernameEnabled || (username.endsWith(SUPER_TENANT_SUFFIX) && count <= 1)) {
                                        username = org.wso2.carbon.utils.multitenancy.MultitenantUtils.getTenantAwareUsername(username);
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
                            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                                log.error("Error while retrieving tenant id for tenant domain: " + tenantDomain, e);
                            }
                            return true;
                        }
                        log.info("scopes validation failed");
                        return false;
                    } else {
                        log.info("scopes are not defined in the JWT token");
                        return false;
                    }
                } else if (!jwtValidationInfo.isValid()){
                    //put the obj into invalid cache
                    if (isRESTApiTokenCacheEnabled){
                        getRESTAPIInvalidTokenCache().put(accessToken, jwtValidationInfo);
                    }
                    log.debug("Invalid JWT token. The expected token format is <header.payload.signature>");
                    return false;
                }
        } catch (Exception e){
            log.debug("Not a JWT token. Failed to decode the token", e);
        }
        return false;
    }

    private void cacheValidationInfo (JWTValidationInfo jwtValidationInfo, SignedJWTInfo signedJWTInfo) {
        if(getRESTAPITokenCache().get(signature) == null){
            getRESTAPITokenCache().put(signature, signedJWTInfo);
        }
        if (jwtValidationInfo.isValid()){
            getRESTAPITokenCache().put(jti, jwtValidationInfo);
        } else if (!jwtValidationInfo.isValid()){
            //put in invalid cache
            getRESTAPIInvalidTokenCache().put(jti, jwtValidationInfo);
        }

    }

    private SignedJWTInfo getSignedJwt(String accessToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        return new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);

        }

    public JWTValidationInfo validateJWTToken(SignedJWTInfo signedJWTInfo, String accessToken) throws Exception {

        //TODO: Exception handling
        JWTValidationInfo jwtValidationInfo = null;
        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();

        if (StringUtils.isNotEmpty(issuer)) {
            if (isRESTApiTokenCacheEnabled){

                if (getRESTAPITokenCache().get(accessToken) != null) {
                    JWTValidationInfo tempJWTValidationInfo = (JWTValidationInfo) getRESTAPITokenCache().get(accessToken);
                    Boolean isExpired = checkTokenExpiration(new Date(tempJWTValidationInfo.getExpiryTime()));
                    if(isExpired){
                        getRESTAPITokenCache().remove(accessToken);
                        getRESTAPIInvalidTokenCache().put(accessToken, tempJWTValidationInfo);
                        tempJWTValidationInfo.setValid(false);
                    }
                    jwtValidationInfo = tempJWTValidationInfo;

                }  else if (getRESTAPIInvalidTokenCache().get(accessToken) != null) {

                    log.error("Invalid JWT token. ");
//                    jwtValidationInfo.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
                    jwtValidationInfo.setValid(false);
                }
            } if (jwtValidationInfo == null){
                TokenIssuerDto tokenIssuerDto = getTokenIssuerDto();
                //validate Issuer
                if(tokenIssuerDto.getIssuer() != null && issuer.equals(tokenIssuerDto.getIssuer())){
                    //validate aud
                    if(signedJWTInfo.getJwtClaimsSet().getAudience() != null
                      && signedJWTInfo.getJwtClaimsSet().getAudience().get(0).equals(tokenIssuerDto.getAudience())){
                        //validate nbf if defined
                        if(signedJWTInfo.getJwtClaimsSet().getNotBeforeTime() != null &&
                                checkTokenExpiration(signedJWTInfo.getJwtClaimsSet().getNotBeforeTime())){
                            //validate signature and exp with JWTValidator
                            JWTValidator jwtValidator = (JWTValidator) Class.forName("org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl").newInstance();
                            jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
                            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
                        } else {
                            jwtValidationInfo.setValid(false);
                            //invalid nbf. invalid token
                        }
                    } else {
                        jwtValidationInfo.setValid(false);
                        //invalid aud
                    }

                } else {
                    jwtValidationInfo.setValid(false);
                    //invalid issuer. invalid token
                }
            }
            return jwtValidationInfo;
        }

        jwtValidationInfo.setValid(false);
        jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
        return jwtValidationInfo;
    }

    private String getJWTTokenIdentifier(SignedJWTInfo signedJWTInfo) {

        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        String jwtid = jwtClaimsSet.getJWTID();
        if (StringUtils.isNotEmpty(jwtid)) {
            return jwtid;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
    }


    private TokenIssuerDto getTokenIssuerDto (){
//        config = getApiManagerConfiguration();
//        return config.getJwtConfigurationDto().getTokenIssuerDtoMap().get(issuer);
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto();
        APIManagerConfiguration apiManagerConfiguration = getApiManagerConfiguration();
        if (apiManagerConfiguration != null){
            tokenIssuerDto.setIssuer(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI + "JwtAuth.iss"));
            tokenIssuerDto.setAudience(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI + "JwtAuth.aud"));
            tokenIssuerDto.setAlias(apiManagerConfiguration.getFirstProperty(APIConstants.API_RESTAPI + "JwtAuth.alias"));
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

        long timestampSkew = getTimeStampSkewInSeconds();
        Date now = new Date();
        return  DateUtils.isBefore(tokenExp, now, timestampSkew);
    }

    protected long getTimeStampSkewInSeconds() {

        return OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();
    }
    //TODO: check for method visibility (public, private static ..etc)
    }


