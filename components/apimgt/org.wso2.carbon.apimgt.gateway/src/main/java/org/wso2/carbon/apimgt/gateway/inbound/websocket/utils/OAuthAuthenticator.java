package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

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
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.apache.commons.logging.Log;

import java.util.List;

public class OAuthAuthenticator implements Authenticator{

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    public OAuthAuthenticator() {
    }

    @Override
    public InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext, boolean validateScopes) {
        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
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
                List<String> keyManagerList =
                        DataHolder.getInstance().getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid());
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
}
