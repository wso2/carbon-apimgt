package org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake.security.oauth;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketWSClient;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import java.text.ParseException;
import java.util.List;

import static org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils.API_AUTH_CONTEXT;

public class OAuthAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    private boolean isMandatory;

    private String securityHeader;
    @Override
    public void init(SynapseEnvironment env) {

    }


    @Override
    public void destroy() {

    }

    public OAuthAuthenticator(String authorizationHeader, boolean isMandatory) {
        this.isMandatory = isMandatory;
        this.securityHeader = authorizationHeader;
    }

    @Override
    public AuthenticationResponse authenticate(MessageContext synCtx) throws APIManagementException {
        return null;
    }

    public AuthenticationResponse authenticate(RequestContextDTO requestContext)
            throws APIManagementException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    requestContext.getDomainAddress(), true);
            APIKeyValidationInfoDTO info;
            String authorizationHeader = requestContext.getMsgInfo().getHeaders().get(securityHeader);
            String[] auth = authorizationHeader.split(StringUtils.SPACE);
            List<String> keyManagerList =
                    DataHolder.getInstance().getKeyManagersFromUUID(requestContext.getApiRequestInfo().getApiId());
            if (APIConstants.CONSUMER_KEY_SEGMENT.equals(auth[0])) {
                String cacheKey;
                boolean isJwtToken = false;
                String apiKey = auth[1];
                if (WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()) {
                    requestContext.getContextHandler().setProperty(WebsocketUtil.authorizationHeader, securityHeader);
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
                        requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.SIGNED_JWT_INFO,
                                getSignedJwtInfo(apiKey));
                        String keyManager = ServiceReferenceHolder.getInstance().getJwtValidationService()
                                .getKeyManagerNameIfJwtValidatorExist((SignedJWTInfo)
                                        requestContext.getContextHandler().getProperty(
                                                APIConstants.JwtTokenConstants.SIGNED_JWT_INFO));
                        if (StringUtils.isNotEmpty(keyManager)) {
                            if (log.isDebugEnabled()){
                                log.debug("KeyManager " + keyManager + "found for authenticate token " +
                                        GatewayUtils.getMaskedToken(apiKey));
                            }
                            if (keyManagerList.contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                                    keyManagerList.contains(keyManager)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Elected KeyManager " + keyManager + "found in API level list " +
                                            String.join(",", keyManagerList));
                                }
                                isJwtToken = true;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Elected KeyManager " + keyManager + " not found in API level list " +
                                            String.join(",", keyManagerList));
                                }
                                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                        "Invalid JWT token");
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("KeyManager not found for accessToken " +
                                        GatewayUtils.getMaskedToken(apiKey));
                            }
                        }
                    } catch (ParseException e) {
                        log.debug("Not a JWT token. Failed to decode the token header.", e);
                    } catch (APIManagementException e) {
                        log.error("Error while checking validation of JWT", e);
                        return new AuthenticationResponse(true, isMandatory,
                                false, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                    } catch (APISecurityException e) {
                        return new AuthenticationResponse(false, isMandatory,
                                true, e.getErrorCode(), e.getMessage());
                    }
                }
                // Find the authentication scheme based on the token type
                if (isJwtToken) {
                    log.debug("The token was identified as a JWT token");
                    if (APIConstants.GRAPHQL_API.equals(requestContext.getApiRequestInfo())) {
                        return new AuthenticationResponse(authenticateGraphQLJWTToken(requestContext),
                                isMandatory, !authenticateGraphQLJWTToken(requestContext), 0, null);
                    } else {
                        return new AuthenticationResponse(authenticateWSJWTToken(requestContext), isMandatory,
                                !authenticateWSJWTToken(requestContext), 0, null);
                    }
                } else {
                    log.debug("The token was identified as an OAuth token");
                    //If the key have already been validated
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, requestContext.getApiRequestInfo().getContext(),
                                requestContext.getMsgInfo().getElectedResource());
                        info = WebsocketUtil.validateCache(apiKey, cacheKey);
                        if (info != null) {
                            requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.KEY_TYPE,
                                    info.getType());
                            requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.
                                    KEY_VALIDATION_INFO, info);
                            return new AuthenticationResponse(info.isAuthorized(), isMandatory, !info.isAuthorized(),
                                    0, null);
                        }
                    }
                    info = getApiKeyDataForWSClient(apiKey, requestContext.getDomainAddress(),
                            requestContext.getApiRequestInfo().getContext(),
                            requestContext.getApiRequestInfo().getVersion(), keyManagerList);
                    if (info == null || !info.isAuthorized()) {
                        return new AuthenticationResponse(false, isMandatory, true,
                                0, null);
                    }
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey,
                                requestContext.getApiRequestInfo().getContext(),
                                requestContext.getMsgInfo().getElectedResource());
                        WebsocketUtil.putCache(info, apiKey, cacheKey);
                    }
                    requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.KEY_TYPE,
                            info.getType());
                    requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.TOKEN,
                            info.getEndUserToken());
                    requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.
                            KEY_VALIDATION_INFO, info);
                    return new AuthenticationResponse(true, isMandatory, false,
                            0, null);
                }
            } else {
                return new AuthenticationResponse(false, isMandatory, true,
                        0, null);
            }
        } catch (APISecurityException e) {
            return new AuthenticationResponse(false, isMandatory, true,
                    e.getErrorCode(), e.getMessage());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public AuthenticationResponse authenticate(InboundMessageContext inboundMessageContext)
            throws APIManagementException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    inboundMessageContext.getTenantDomain(), true);
            APIKeyValidationInfoDTO info;
            String authorizationHeader = inboundMessageContext.getRequestHeaders().get(WebsocketUtil.authorizationHeader);
            String[] auth = authorizationHeader.split(StringUtils.SPACE);
            List<String> keyManagerList =
                    DataHolder.getInstance().getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid());
            if (APIConstants.CONSUMER_KEY_SEGMENT.equals(auth[0])) {
                String cacheKey;
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
                                log.debug("KeyManager " + keyManager + "found for authenticate token " +
                                        GatewayUtils.getMaskedToken(apiKey));
                            }
                            if (keyManagerList.contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                                    keyManagerList.contains(keyManager)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Elected KeyManager " + keyManager + "found in API level list " +
                                            String.join(",", keyManagerList));
                                }
                                isJwtToken = true;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Elected KeyManager " + keyManager + " not found in API level list " +
                                            String.join(",", keyManagerList));
                                }
                                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                        "Invalid JWT token");
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("KeyManager not found for accessToken " +
                                        GatewayUtils.getMaskedToken(apiKey));
                            }
                        }
                    } catch (ParseException e) {
                        log.debug("Not a JWT token. Failed to decode the token header.", e);
                    } catch (APIManagementException e) {
                        log.error("Error while checking validation of JWT", e);
                        return new AuthenticationResponse(true, isMandatory,
                                false, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                    } catch (APISecurityException e) {
                        return new AuthenticationResponse(false, isMandatory,
                                true, e.getErrorCode(), e.getMessage());
                    }
                }
                // Find the authentication scheme based on the token type
                if (isJwtToken) {
                    log.debug("The token was identified as a JWT token");
                    if (APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType())) {
                        return new AuthenticationResponse(authenticateGraphQLJWTToken(inboundMessageContext),
                                isMandatory, !authenticateGraphQLJWTToken(inboundMessageContext), 0, null);
                    } else {
                        return new AuthenticationResponse(authenticateWSJWTToken(inboundMessageContext), isMandatory,
                                !authenticateWSJWTToken(inboundMessageContext), 0, null);
                    }
                } else {
                    log.debug("The token was identified as an OAuth token");
                    //If the key have already been validated
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, inboundMessageContext.getApiContext(),
                                inboundMessageContext.getMatchingResource());
                        info = WebsocketUtil.validateCache(apiKey, cacheKey);
                        if (info != null) {
                            inboundMessageContext.setKeyType(info.getType());
                            inboundMessageContext.setInfoDTO(info);
                            return new AuthenticationResponse(info.isAuthorized(), isMandatory, !info.isAuthorized(),
                                    0, null);
                        }
                    }
                    info = getApiKeyDataForWSClient(apiKey, inboundMessageContext.getTenantDomain(),
                            inboundMessageContext.getApiContext(), inboundMessageContext.getVersion(), keyManagerList);
                    if (info == null || !info.isAuthorized()) {
                        return new AuthenticationResponse(false, isMandatory, true,
                                0, null);
                    }
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey,
                                inboundMessageContext.getApiContext(), inboundMessageContext.getMatchingResource());
                        WebsocketUtil.putCache(info, apiKey, cacheKey);
                    }
                    inboundMessageContext.setKeyType(info.getType());
                    inboundMessageContext.setToken(info.getEndUserToken());
                    inboundMessageContext.setInfoDTO(info);
                    return new AuthenticationResponse(true, isMandatory, false,
                            0, null);
                }
            } else {
                return new AuthenticationResponse(false, isMandatory, true,
                        0, null);
            }
        } catch (APISecurityException e) {
            return new AuthenticationResponse(false, isMandatory, true,
                    e.getErrorCode(), e.getMessage());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
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

    /**
     * Get Websocket API Key data from websocket client.
     *
     * @param key           API key
     * @param domain        tenant domain
     * @param apiContextUri API context
     * @param apiVersion    API version
     * @return APIKeyValidationInfoDTO
     * @throws APISecurityException if validation fails
     */
    private static APIKeyValidationInfoDTO getApiKeyDataForWSClient(String key, String domain, String apiContextUri,
                                                                    String apiVersion, List<String> keyManagers)
            throws APISecurityException {

        return new WebsocketWSClient().getAPIKeyData(apiContextUri, apiVersion, key, domain, keyManagers);
    }

    /**
     * Authenticates JWT token in incoming GraphQL subscription requests.
     *
     * @param requestContext IrequestContext
     * @return true if authenticated
     * @throws APIManagementException if an internal error occurs
     * @throws APISecurityException   if authentication fails
     */
    public static boolean authenticateGraphQLJWTToken(RequestContextDTO requestContext)
            throws APIManagementException, APISecurityException {

        AuthenticationContext authenticationContext;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestContext.getDomainAddress(),
                true);
        JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(), requestContext.getDomainAddress());
        authenticationContext = jwtValidator.
                authenticateForGraphQLSubscription((SignedJWTInfo) requestContext.getContextHandler().getProperty(
                        APIConstants.JwtTokenConstants.SIGNED_JWT_INFO),
                        requestContext.getApiRequestInfo().getContext(), requestContext.getApiRequestInfo().getVersion());
        return validateAuthenticationContext(authenticationContext, requestContext);
    }

    /**
     * Authenticates JWT token in incoming GraphQL subscription requests.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return true if authenticated
     * @throws APIManagementException if an internal error occurs
     * @throws APISecurityException   if authentication fails
     */
    public static boolean authenticateGraphQLJWTToken(InboundMessageContext inboundMessageContext)
            throws APIManagementException, APISecurityException {

        AuthenticationContext authenticationContext;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(inboundMessageContext.getTenantDomain(),
                true);
        JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(), inboundMessageContext.getTenantDomain());
        authenticationContext = jwtValidator.
                authenticateForGraphQLSubscription(inboundMessageContext.getSignedJWTInfo(),
                        inboundMessageContext.getApiContext(), inboundMessageContext.getVersion());
        return validateAuthenticationContext(authenticationContext, inboundMessageContext);
    }

    /**
     * Validates AuthenticationContext and set APIKeyValidationInfoDTO to InboundMessageContext.
     *
     * @param authenticationContext Validated AuthenticationContext
     * @param requestContext requestContext
     * @return true if authenticated
     */
    public static boolean validateAuthenticationContext(AuthenticationContext authenticationContext,
                                                        RequestContextDTO requestContext) {

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

        requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.KEY_TYPE,
                info.getType());
        requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.
                KEY_VALIDATION_INFO, info);
        requestContext.getContextHandler().setProperty(API_AUTH_CONTEXT,authenticationContext);

        requestContext.getContextHandler().setProperty(APIConstants.JwtTokenConstants.
                KEY_VALIDATION_INFO, info);
        return authenticationContext.isAuthenticated();
    }

    /**
     * Validates AuthenticationContext and set APIKeyValidationInfoDTO to InboundMessageContext.
     *
     * @param authenticationContext Validated AuthenticationContext
     * @param inboundMessageContext InboundMessageContext
     * @return true if authenticated
     */
    public static boolean validateAuthenticationContext(AuthenticationContext authenticationContext,
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

        inboundMessageContext.setKeyType(info.getType());
        inboundMessageContext.setInfoDTO(info);
        inboundMessageContext.setAuthContext(authenticationContext);
        inboundMessageContext.setInfoDTO(info);
        return authenticationContext.isAuthenticated();
    }

    /**
     * Authenticates JWT token in incoming Websocket handshake requests.
     *
     * @param requestContext requestContext
     * @return true if authenticated
     * @throws APIManagementException if an internal error occurs
     * @throws APISecurityException   if authentication fails
     */
    public static boolean authenticateWSJWTToken(RequestContextDTO requestContext)
            throws APIManagementException, APISecurityException {

        AuthenticationContext authenticationContext;
        JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(), requestContext.getDomainAddress());
        authenticationContext = jwtValidator.
                authenticateForWebSocket((SignedJWTInfo) requestContext.getContextHandler().getProperty(
                        APIConstants.JwtTokenConstants.SIGNED_JWT_INFO),
                        requestContext.getApiRequestInfo().getContext(),
                        requestContext.getApiRequestInfo().getVersion(),
                        requestContext.getMsgInfo().getElectedResource());
        return validateAuthenticationContext(authenticationContext, requestContext);
    }

    /**
     * Authenticates JWT token in incoming Websocket handshake requests.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return true if authenticated
     * @throws APIManagementException if an internal error occurs
     * @throws APISecurityException   if authentication fails
     */
    public static boolean authenticateWSJWTToken(InboundMessageContext inboundMessageContext)
            throws APIManagementException, APISecurityException {

        AuthenticationContext authenticationContext;
        JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(), inboundMessageContext.getTenantDomain());
        authenticationContext = jwtValidator.
                authenticateForWebSocket(inboundMessageContext.getSignedJWTInfo(),
                        inboundMessageContext.getApiContext(), inboundMessageContext.getVersion(),
                        inboundMessageContext.getMatchingResource());
        return validateAuthenticationContext(authenticationContext, inboundMessageContext);
    }

    @Override
    public String getChallengeString() {
        return null;
    }

    @Override
    public String getRequestOrigin() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
