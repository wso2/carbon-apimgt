package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketWSClient;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.apache.commons.logging.Log;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;

import javax.cache.Cache;
import java.text.ParseException;
import java.util.List;

public class OAuthAuthenticator implements Authenticator{

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    public OAuthAuthenticator() {
    }

    @Override
    public InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext) throws APISecurityException {

        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        boolean validateScopes = false;

        String authorizationHeader = inboundMessageContext.getRequestHeaders().get(WebsocketUtil.authorizationHeader);
        String[] auth = authorizationHeader.split(StringUtils.SPACE);
        List<String> keyManagerList =
                DataHolder.getInstance().getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid());
        if (APIConstants.CONSUMER_KEY_SEGMENT.equals(auth[0])) {
            boolean isJwtToken = false;
            String apiKey = auth[1];
            if (WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()) {
                inboundMessageContext.getHeadersToRemove().add(WebsocketUtil.authorizationHeader);
            }

            //Initial guess of a JWT token using the presence of a DOT.
            if (StringUtils.isNotEmpty(apiKey) && apiKey.contains(APIConstants.DOT)) {
                try {
                    // Check if the header part is decoded
                    if (StringUtils.countMatches(apiKey, APIConstants.DOT) != 2) {
                        log.debug("Invalid JWT token. The expected token format is <header.payload.signature>");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                    inboundMessageContext.setSignedJWTInfo(getSignedJwtInfo(apiKey));
                    String keyManager = ServiceReferenceHolder.getInstance().getJwtValidationService()
                            .getKeyManagerNameIfJwtValidatorExist(inboundMessageContext.getSignedJWTInfo());
                    if (StringUtils.isNotEmpty(keyManager)) {
                        if (log.isDebugEnabled()){
                            log.debug("KeyManager " + keyManager + "found for authenticate token " + GatewayUtils.getMaskedToken(apiKey));
                        }
                        if (keyManagerList.contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                                keyManagerList.contains(keyManager)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Elected KeyManager " + keyManager + "found in API level list " + String.join(",", keyManagerList));
                            }
                            isJwtToken = true;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Elected KeyManager " + keyManager + " not found in API level list " + String.join(",", keyManagerList));
                            }
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    "Invalid JWT token");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("KeyManager not found for accessToken " + GatewayUtils.getMaskedToken(apiKey));
                        }
                    }
                } catch (ParseException e) {
                    log.debug("Not a JWT token. Failed to decode the token header.", e);
                } catch (APIManagementException e) {
                    log.error("Error while checking validation of JWT", e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                }
            }
            // Find the authentication scheme based on the token type
            if (isJwtToken) {
                log.debug("The token was identified as a JWT token");
                inboundMessageContext.setJWTToken(true);
            }
//            boolean isGQL = APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType());
            validateScopes = !APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType());
//            boolean authenticated = !(authenticateToken(inboundMessageContext, !isGQL).isError());
//            return authenticated;
        } else {
            inboundProcessorResponseDTO.setError(true);
        }



        try {
            //validate token and subscriptions
            if (inboundMessageContext.isJWTToken()) {
                log.debug("Authentication started for JWT tokens");
                JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(),
                        inboundMessageContext.getTenantDomain());
                AuthenticationContext authenticationContext;
                String matchingResources = validateScopes ? inboundMessageContext.getMatchingResource() : null;
                authenticationContext = jwtValidator.
                        authenticateForWebSocket(inboundMessageContext.getSignedJWTInfo(),
                                inboundMessageContext.getApiContext(), inboundMessageContext.getVersion(),
                                matchingResources, validateScopes);
                if (!validateAuthenticationContext(authenticationContext, inboundMessageContext)) {
                    inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getFrameErrorDTO(
                            WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, true);
                }
            } else {
                log.debug("Authentication started for Opaque tokens");
                String apiKey;
                if (inboundMessageContext.getToken() == null) {
                    String authHeader = inboundMessageContext.getRequestHeaders().get(WebsocketUtil.authorizationHeader);
                    apiKey = getTokenFromAuthHeader(authHeader);
                } else {
                    apiKey = inboundMessageContext.getToken();
                }
                APIKeyValidationInfoDTO info;
                String cacheKey;
                //If the key have already been validated
                if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                    cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, inboundMessageContext.getApiContext(),
                            inboundMessageContext.getMatchingResource());
                    info = WebsocketUtil.validateCache(apiKey, cacheKey);
                    if (info != null) {
                        inboundMessageContext.setKeyType(info.getType());
                        inboundMessageContext.setInfoDTO(info);
                        inboundMessageContext.setToken(info.getEndUserToken());
                    } else {
                        String revokedCachedToken = (String) CacheProvider.getInvalidTokenCache().get(apiKey);
                        if (revokedCachedToken != null) {
                            // Token is revoked/invalid or expired
                            return InboundWebsocketProcessorUtil.getFrameErrorDTO(
                                    WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, true);
                        }
                    }
                }
//                List<String> keyManagerList =
//                        DataHolder.getInstance().getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid());
                info = getApiKeyDataForWSClient(apiKey, inboundMessageContext.getTenantDomain(),
                        inboundMessageContext.getApiContext(), inboundMessageContext.getVersion(), keyManagerList);
                if (info == null || !info.isAuthorized()) {
                    info.setAuthorized(false);
                }
                if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                    cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey,
                            inboundMessageContext.getApiContext(), inboundMessageContext.getMatchingResource());
                    WebsocketUtil.putCache(info, apiKey, cacheKey);
                }
                inboundMessageContext.setKeyType(info.getType());
                inboundMessageContext.setToken(info.getEndUserToken());
                inboundMessageContext.setInfoDTO(info);
                if (info.isAuthorized()) {
                    return inboundProcessorResponseDTO;
                }
                return InboundWebsocketProcessorUtil.getFrameErrorDTO(
                        WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, true);
            }
        } catch (APIManagementException e) {
            log.error(WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_MESSAGE, e);
            inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getFrameErrorDTO(
                    WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_ERROR,
                    WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_MESSAGE, true);
        } catch (APISecurityException e) {
            log.error(WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS, e);
            inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getFrameErrorDTO(
                    WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                    e.getMessage(), true);
        }
        return inboundProcessorResponseDTO;

    }

    public boolean validateAuthenticationContext(AuthenticationContext authenticationContext,
                                                        InboundMessageContext inboundMessageContext) {

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
        return authenticationContext.isAuthenticated();
    }

    private static String getTokenFromAuthHeader(String authHeader) {
        if (StringUtils.isEmpty(authHeader)) {
            return StringUtils.EMPTY;
        }
        String[] auth = authHeader.split(StringUtils.SPACE);
        if (auth.length > 1) {
            return auth[1];
        } else {
            return StringUtils.EMPTY;
        }
    }

    private static APIKeyValidationInfoDTO getApiKeyDataForWSClient(String key, String domain, String apiContextUri,
                                                                    String apiVersion, List<String> keyManagers)
            throws APISecurityException {

        return new WebsocketWSClient().getAPIKeyData(apiContextUri, apiVersion, key, domain, keyManagers);
    }

    /**
     * Get signed JWT info for access token
     *
     * @param accessToken Access token
     * @return SignedJWTInfo
     * @throws ParseException if an error occurs
     */
    private static SignedJWTInfo getSignedJwtInfo(String accessToken) throws ParseException {

        String signature = accessToken.split("\\.")[2];
        SignedJWTInfo signedJWTInfo = null;
        Cache gatewaySignedJWTParseCache = CacheProvider.getGatewaySignedJWTParseCache();
        if (gatewaySignedJWTParseCache != null) {
            Object cachedEntry = gatewaySignedJWTParseCache.get(signature);
            if (cachedEntry != null) {
                signedJWTInfo = (SignedJWTInfo) cachedEntry;
            }
            if (signedJWTInfo == null || !signedJWTInfo.getToken().equals(accessToken)) {
                SignedJWT signedJWT = SignedJWT.parse(accessToken);
                JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
                signedJWTInfo = new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
                gatewaySignedJWTParseCache.put(signature, signedJWTInfo);
            }
        } else {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            signedJWTInfo = new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
        }
        return signedJWTInfo;
    }
}
