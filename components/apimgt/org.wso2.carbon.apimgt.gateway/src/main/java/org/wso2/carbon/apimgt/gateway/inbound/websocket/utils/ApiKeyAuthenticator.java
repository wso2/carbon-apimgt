package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.cache.Cache;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

public class ApiKeyAuthenticator {

    private static final Log log = LogFactory.getLog(ApiKeyAuthenticator.class);
    private static Boolean jwtGenerationEnabled = null;
    private static AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator = null;
    private static ExtendedJWTConfigurationDto jwtConfigurationDto = null;
    private static Boolean isGatewayTokenCacheEnabled = null;
    private static volatile long ttl = -1L;

    public static InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext) throws APISecurityException {

        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();

        if (log.isDebugEnabled()) {
            log.info("ApiKey Authentication initialized");
        }

        try {
            // Extract apikey from the request while removing it from the msg context.
            String apiKey = extractApiKey(inboundMessageContext);
            JWTTokenPayloadInfo payloadInfo = null;

            if (jwtConfigurationDto == null) {
                jwtConfigurationDto =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
            }

            if (jwtGenerationEnabled == null && jwtConfigurationDto != null) {
                jwtGenerationEnabled = jwtConfigurationDto.isEnabled();
            }


            if (apiMgtGatewayJWTGenerator == null && jwtConfigurationDto != null) {
                apiMgtGatewayJWTGenerator = ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                        .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
            }

            String tenantDomain = GatewayUtils.getTenantDomain();
            int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

            if (jwtGenerationEnabled != null && jwtGenerationEnabled) {
                // Set certificate to jwtConfigurationDto
                if (jwtConfigurationDto.isTenantBasedSigningEnabled()) {
                    jwtConfigurationDto.setPublicCert(SigningUtil.getPublicCertificate(tenantId));
                    jwtConfigurationDto.setPrivateKey(SigningUtil.getSigningKey(tenantId));
                } else {
                    jwtConfigurationDto.setPublicCert(ServiceReferenceHolder.getInstance().getPublicCert());
                    jwtConfigurationDto.setPrivateKey(ServiceReferenceHolder.getInstance().getPrivateKey());
                }
                // Set ttl to jwtConfigurationDto
                jwtConfigurationDto.setTtl(org.wso2.carbon.apimgt.impl.utils.GatewayUtils.getTtl());

                //setting the jwt configuration dto
                apiMgtGatewayJWTGenerator.setJWTConfigurationDto(jwtConfigurationDto);
            }

            String splitToken[] = apiKey.split("\\.");
            JWSHeader decodedHeader;
            JWTClaimsSet payload;
            SignedJWT signedJWT;
            String tokenIdentifier, certAlias;
            if (splitToken.length != 3) {
                log.error("Api Key does not have the format {header}.{payload}.{signature} ");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            signedJWT = SignedJWT.parse(apiKey);
            payload = signedJWT.getJWTClaimsSet();
            decodedHeader = signedJWT.getHeader();
            tokenIdentifier = payload.getJWTID();
            // Check if the decoded header contains type as 'JWT'.
            if (!JOSEObjectType.JWT.equals(decodedHeader.getType())) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key token type. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key token type.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            if (!GatewayUtils.isAPIKey(payload)) {
                log.error("Invalid Api Key. Internal Key Sent");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

            }
            if (decodedHeader.getKeyID() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key. Could not find alias in header. Api Key: " +
                            GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key. Could not find alias in header");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } else {
                certAlias = decodedHeader.getKeyID();
            }

            String apiContext = inboundMessageContext.getApiContext();
            String apiVersion = inboundMessageContext.getVersion();
            String matchingResource = inboundMessageContext.getMatchingResource();
            String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenIdentifier, apiContext, apiVersion,
                    matchingResource, null);
            boolean isVerified = false;

            // Validate from cache
            if (isGatewayTokenCacheEnabled == null) {
                isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
            }

            if (isGatewayTokenCacheEnabled) {
                String cacheToken = (String) getGatewayApiKeyCache().get(tokenIdentifier);
                if (cacheToken != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the Api Key cache.");
                    }
                    if (getGatewayApiKeyDataCache().get(cacheKey) != null) {
                        // Token is found in the key cache
                        payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
                        String accessToken = payloadInfo.getAccessToken();
                        if (!accessToken.equals(apiKey)) {
                            isVerified = false;
                        } else {
                            isVerified = true;
                        }
                    }
                } else if (getInvalidGatewayApiKeyCache().get(tokenIdentifier) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the invalid Api Key cache. Api Key: " +
                                GatewayUtils.getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid Api Key." + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                } else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                                getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid API Key. " + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            "Invalid API Key");
                }
            } else {
                if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                                getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            "Invalid JWT token");
                }
            }

            // Not found in cache or caching disabled
            if (!isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key not found in the cache.");
                }
                try {
                    signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                    payload = signedJWT.getJWTClaimsSet();
                } catch (JSONException | IllegalArgumentException | ParseException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]), e);
                    }
                    log.error("Invalid JWT token. Failed to decode the Api Key body.");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                }
                try {
                    isVerified = GatewayUtils.verifyTokenSignature(signedJWT, certAlias);
                } catch (APISecurityException e) {
                    if (e.getErrorCode() == APISecurityConstants.API_AUTH_INVALID_CREDENTIALS) {
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    } else {
                        throw e;
                    }
                }
                if (isGatewayTokenCacheEnabled) {
                    // Add token to tenant token cache
                    if (isVerified) {
                        getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                    } else {
                        getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                    }

                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        try {
                            // Start super tenant flow
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                            // Add token to super tenant token cache
                            if (isVerified) {
                                getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            } else {
                                getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            }
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                    }
                }
            }

            // If Api Key signature is verified
            if (isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key signature is verified.");
                }
                if (isGatewayTokenCacheEnabled && payloadInfo != null) {
                    // Api Key is found in the key cache
                    payload = payloadInfo.getPayload();
                    if (isJwtTokenExpired(payload)) {
                        getGatewayApiKeyCache().remove(tokenIdentifier);
                        getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                    validateAPIKeyRestrictions(payload, inboundMessageContext);
                } else {
                    // Retrieve payload from ApiKey
                    if (log.isDebugEnabled()) {
                        log.debug("ApiKey payload not found in the cache.");
                    }
                    if (payload == null) {
                        try {
                            signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                            payload = signedJWT.getJWTClaimsSet();
                        } catch (JSONException | IllegalArgumentException | ParseException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid ApiKey. ApiKey: " + GatewayUtils.getMaskedToken(splitToken[0]));
                            }
                            log.error("Invalid Api Key. Failed to decode the Api Key body.");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                        }
                    }
                    if (isJwtTokenExpired(payload)) {
                        if (isGatewayTokenCacheEnabled) {
                            getGatewayApiKeyCache().remove(tokenIdentifier);
                            getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        }
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                    validateAPIKeyRestrictions(payload, inboundMessageContext);
                    if (isGatewayTokenCacheEnabled) {
                        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                        jwtTokenPayloadInfo.setPayload(payload);
                        jwtTokenPayloadInfo.setAccessToken(apiKey);
                        getGatewayApiKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
                    }
                }
                net.minidev.json.JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, false);
                if (log.isDebugEnabled()) {
                    log.debug("Api Key authentication successful.");
                }

                String endUserToken = null;
                String contextHeader = null;
                if (jwtGenerationEnabled) {
                    SignedJWTInfo signedJWTInfo = new SignedJWTInfo(apiKey, signedJWT, payload);
                    JWTValidationInfo jwtValidationInfo = getJwtValidationInfo(signedJWTInfo);
                    JWTInfoDto jwtInfoDto = GatewayUtils.generateJWTInfoDto(api, jwtValidationInfo, null, inboundMessageContext);
                    endUserToken = generateAndRetrieveBackendJWTToken(tokenIdentifier, jwtInfoDto);
                    contextHeader = getContextHeader();
                }

                AuthenticationContext authenticationContext = GatewayUtils
                        .generateAuthenticationContext(tokenIdentifier, payload, api, null, endUserToken, null);
                if (!validateAuthenticationContext(inboundMessageContext, authenticationContext, contextHeader)) {
                    return getErrorInboundProcessorResponseDTO(inboundProcessorResponseDTO, true,
                            WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, true);
                }
                if (log.isDebugEnabled()) {
                    log.debug("User is authorized to access the resource using Api Key.");
                }
                inboundProcessorResponseDTO.setErrorCode(0);
                inboundProcessorResponseDTO.setErrorMessage(null);
                return inboundProcessorResponseDTO;
            }

            if (log.isDebugEnabled()) {
                log.debug("Api Key signature verification failure. Api Key: " +
                        GatewayUtils.getMaskedToken(splitToken[0]));
            }
            log.error("Invalid Api Key. Signature verification failed.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

        } catch (APISecurityException e) {
            return getErrorInboundProcessorResponseDTO(inboundProcessorResponseDTO, true,
                    e.getErrorCode(), e.getMessage(), true);
        } catch (ParseException e) {
            log.error("Error while parsing API Key", e);
            return getErrorInboundProcessorResponseDTO(inboundProcessorResponseDTO, true,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, true);
        } catch (APIManagementException e) {
            log.error("Error while setting public cert/private key for backend jwt generation", e);
            return getErrorInboundProcessorResponseDTO(inboundProcessorResponseDTO, true,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, true);
        }
    }

    private static InboundProcessorResponseDTO getErrorInboundProcessorResponseDTO(InboundProcessorResponseDTO inboundProcessorResponseDTO,
                                                                            boolean isError, int errorCode, String errorMessage,
                                                                            boolean closeConnection) {
        inboundProcessorResponseDTO.setError(isError);
        inboundProcessorResponseDTO.setErrorCode(errorCode);
        inboundProcessorResponseDTO.setErrorMessage(errorMessage);
        inboundProcessorResponseDTO.setCloseConnection(closeConnection);
        return inboundProcessorResponseDTO;
    }

    private static void validateAPIKeyRestrictions(JWTClaimsSet payload, InboundMessageContext inboundMessageContext) throws APISecurityException {

        String permittedIPList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP) != null) {
            permittedIPList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP);
        }

        if (StringUtils.isNotEmpty(permittedIPList)) {
            // Validate client IP against permitted IPs
            String clientIP = inboundMessageContext.getUserIP();
            if (StringUtils.isNotEmpty(clientIP)) {
                for (String restrictedIP : permittedIPList.split(",")) {
                    if (APIUtil.isIpInNetwork(clientIP, restrictedIP.trim())) {
                        // Client IP is allowed
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    String apiContext = inboundMessageContext.getApiContext();
                    String apiVersion = inboundMessageContext.getVersion();

                    if (StringUtils.isNotEmpty(clientIP)) {
                        log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                " is not permitted for client with IP: " + clientIP);
                    }
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "Access forbidden for the invocations");
            }
        }

        String permittedRefererList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER) != null) {
            permittedRefererList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER);
        }

        if (StringUtils.isNotEmpty(permittedRefererList)) {
            // Validate http referer against the permitted referrers
            String referer = inboundMessageContext.getRequestHeaders().get("Referer");
            if (StringUtils.isNotEmpty(referer)) {
                for (String restrictedReferer : permittedRefererList.split(",")) {
                    String restrictedRefererRegExp = restrictedReferer.trim()
                            .replace("*", "[^ ]*");
                    if (referer.matches(restrictedRefererRegExp)) {
                        // Referer is allowed
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    String apiContext = inboundMessageContext.getApiContext();
                    String apiVersion = inboundMessageContext.getVersion();

                    if (StringUtils.isNotEmpty(referer)) {
                        log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                " is not permitted for referer: " + referer);
                    }
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "Access forbidden for the invocations");
            } else {
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "Access forbidden for the invocations");
            }
        }
    }

    private static String generateAndRetrieveBackendJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto)
            throws APISecurityException {

        String endUserToken = null;
        boolean valid = false;
        String jwtTokenCacheKey =
                jwtInfoDto.getApiContext().concat(":").concat(jwtInfoDto.getVersion()).concat(":").concat(tokenSignature);
        if (isGatewayTokenCacheEnabled) {
            Object token = getGatewayApiKeyCache().get(jwtTokenCacheKey);
            if (token != null) {
                endUserToken = (String) token;
                String[] splitToken = ((String) token).split("\\.");
                JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                long exp = payload.getLong("exp");
                long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                valid = (exp - System.currentTimeMillis() > timestampSkew);
            }
            if (StringUtils.isEmpty(endUserToken) || !valid) {
                try {
                    endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
                    getGatewayApiKeyCache().put(jwtTokenCacheKey, endUserToken);
                } catch (JWTGeneratorException e) {
                    log.error("Error while Generating Backend JWT", e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
                }
            }
        } else {
            try {
                endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
            } catch (JWTGeneratorException e) {
                log.error("Error while Generating Backend JWT", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        return endUserToken;
    }

    private static String extractApiKey(InboundMessageContext inboundMessageContext) throws APISecurityException {

        String apiKey;

        //check headers to get apikey
        Map headers = inboundMessageContext.getRequestHeaders();
        if (headers != null) {
            apiKey = (String) headers.get(APIConstants.API_KEY_HEADER_QUERY_PARAM);
            if (apiKey != null) {
                //Remove apikey header from the request
                if (WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()) {
                    inboundMessageContext.getHeadersToRemove().add(APIConstants.API_KEY_HEADER_QUERY_PARAM);
                }
                return apiKey.trim();
            }
        }
        //check query params to get apikey
        apiKey = inboundMessageContext.getApiKeyFromQueryParams();
        if (StringUtils.isNotBlank(apiKey)) {
            //Remove apikey query param from the full request path
            inboundMessageContext.setFullRequestPath(removeApiKeyFromQueryParameters(inboundMessageContext.getFullRequestPath(), apiKey));
            return apiKey.trim();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Api Key Authentication failed: Header or Query parameter with the name '"
                        .concat(APIConstants.API_KEY_HEADER_QUERY_PARAM).concat("' was not found."));
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
        }
    }

    private static String removeApiKeyFromQueryParameters(String queryParam, String apiKey) {
        queryParam = queryParam.replace("?apikey=" + apiKey, "?");
        queryParam = queryParam.replace("&apikey=" + apiKey, "");
        queryParam = queryParam.replace("?&", "?");
        if (queryParam.lastIndexOf("?") == (queryParam.length() - 1)) {
            queryParam = queryParam.replace("?", "");
        }
        return queryParam;
    }

    private static boolean validateAuthenticationContext(InboundMessageContext inboundMessageContext, AuthenticationContext authenticationContext, String contextHeader) {

        if (authenticationContext == null || !authenticationContext.isAuthenticated()) {
            return false;
        }
        // The information given by the AuthenticationContext is set to an APIKeyValidationInfoDTO object
        // so to feed information analytics and throttle data publishing
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(authenticationContext.isAuthenticated());
        info.setApplicationTier(authenticationContext.getApplicationTier());
        info.setTier(authenticationContext.getTier());
        info.setSubscriberTenantDomain(authenticationContext.getSubscriberTenantDomain());
        info.setSubscriber(authenticationContext.getSubscriber());
        info.setStopOnQuotaReach(authenticationContext.isStopOnQuotaReach());
        info.setApiName(authenticationContext.getApiName());
        info.setApplicationId(authenticationContext.getApplicationId());
        info.setType(authenticationContext.getKeyType());
        info.setApiPublisher(authenticationContext.getApiPublisher());
        info.setApplicationName(authenticationContext.getApplicationName());
        info.setConsumerKey(authenticationContext.getConsumerKey());
        info.setEndUserName(authenticationContext.getUsername());
        info.setApiTier(authenticationContext.getApiTier());
        info.setGraphQLMaxDepth(authenticationContext.getGraphQLMaxDepth());
        info.setGraphQLMaxComplexity(authenticationContext.getGraphQLMaxComplexity());
        info.setEndUserToken(authenticationContext.getCallerToken());

        inboundMessageContext.setKeyType(info.getType());
        inboundMessageContext.setInfoDTO(info);
        inboundMessageContext.setAuthContext(authenticationContext);
        inboundMessageContext.setInfoDTO(info);
        inboundMessageContext.setToken(info.getEndUserToken());

        if (contextHeader != null && authenticationContext.getCallerToken() != null) {
            inboundMessageContext.getRequestHeaders().put(contextHeader, authenticationContext.getCallerToken());
        }

        return authenticationContext.isAuthenticated();
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param payload The payload of the JWT token
     * @return returns true if the JWT token is expired
     */
    private static boolean isJwtTokenExpired(JWTClaimsSet payload) {

        int timestampSkew = (int) OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();

        DefaultJWTClaimsVerifier jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier();
        jwtClaimsSetVerifier.setMaxClockSkew(timestampSkew);
        try {
            jwtClaimsSetVerifier.verify(payload);
            if (log.isDebugEnabled()) {
                log.debug("Token is not expired. User: " + payload.getSubject());
            }
        } catch (BadJWTException e) {
            if ("Expired JWT".equals(e.getMessage())) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getSubject());
        }
        return false;
    }

    private static JWTValidationInfo getJwtValidationInfo(SignedJWTInfo signedJWTInfo) {
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setClaims(new HashMap<>(signedJWTInfo.getJwtClaimsSet().getClaims()));
        jwtValidationInfo.setUser(signedJWTInfo.getJwtClaimsSet().getSubject());
        return jwtValidationInfo;
    }

    private static String getContextHeader() {
        APIManagerConfiguration apimConf = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        JWTConfigurationDto jwtConfigDto = apimConf.getJwtConfigurationDto();
        return jwtConfigDto.getJwtHeader();
    }

    //first level cache
    private static Cache getGatewayApiKeyCache() {
        return CacheProvider.getGatewayApiKeyCache();
    }

    private static Cache getInvalidGatewayApiKeyCache() {
        return CacheProvider.getInvalidGatewayApiKeyCache();
    }

    //second level cache
    private static Cache getGatewayApiKeyDataCache() {
        return CacheProvider.getGatewayApiKeyDataCache();
    }

}
