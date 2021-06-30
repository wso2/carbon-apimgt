/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.api.API;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.RESTDispatcher;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import javax.cache.Cache;

import static org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants.URL_SEPARATOR;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants.WS_ENDPOINT_NAME;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants.WS_SECURED_ENDPOINT_NAME;

/**
 * This is a handler which is actually embedded to the netty pipeline which does operations such as
 * authentication and throttling for the websocket handshake and subsequent websocket frames.
 */
public class WebsocketInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);
    private String tenantDomain;
    private String fullRequestPath;
    private String requestPath; // request path without query param section
    private String version;
    private APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
    private io.netty.handler.codec.http.HttpHeaders headers = new DefaultHttpHeaders();
    private String token;
    private String apiContext;
    private String inboundName;
    private String apiName;
    private String keyType;
    private API api;
    private String electedRoute;
    private AuthenticationContext authContext;
    private WebSocketAnalyticsMetricsHandler metricsHandler;
    private org.wso2.carbon.apimgt.keymgt.model.entity.API electedAPI;

    public WebsocketInboundHandler() {
        initializeDataPublisher();
    }

    private void initializeDataPublisher() {
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.REQUEST_START_TIME_PROPERTY,
                    System.currentTimeMillis());
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.USER_IP_PROPERTY, getRemoteIP(ctx));
        }

        //check if the request is a handshake
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;

            setUris(req);
            inboundName = getInboundName(ctx);
            setTenantDomain(requestPath);
            String matchingResource = getMatchingResource(ctx, req);

            String useragent = req.headers().get(HttpHeaders.USER_AGENT);

            // '-' is used for empty values to avoid possible errors in DAS side.
            // Required headers are stored one by one as validateOAuthHeader()
            // removes some of the headers from the request
            useragent = useragent != null ? useragent : "-";
            headers.add(HttpHeaders.USER_AGENT, useragent);

            boolean isOAuthHeaderValid;
            try {
                isOAuthHeaderValid = validateOAuthHeader(req, matchingResource);
            } catch (APISecurityException e) {
                if (APIUtil.isAnalyticsEnabled()) {
                    publishHandshakeAuthErrorEvent(ctx, e.getMessage());
                }
                throw e;
            }

            if (isOAuthHeaderValid) {
                setApiAuthPropertiesToChannel(ctx);
                if (StringUtils.isNotEmpty(token)) {
                    ((FullHttpRequest) msg).headers().set(APIMgtGatewayConstants.WS_JWT_TOKEN_HEADER, token);
                }
                ctx.fireChannelRead(msg);
                if (APIUtil.isAnalyticsEnabled()) {
                    WebSocketUtils.setApiPropertyToChannel(ctx,
                            org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.USER_AGENT_PROPERTY, useragent);
                    WebSocketUtils.setApiPropertyToChannel(ctx, APIConstants.API_ELECTED_RESOURCE, matchingResource);
                    publishHandshakeEvent(ctx);
                }

                // publish google analytics data
                GoogleAnalyticsData.DataBuilder gaData = new GoogleAnalyticsData.DataBuilder(null, null, null, null)
                        .setDocumentPath(fullRequestPath).setDocumentHostName(DataPublisherUtil.getHostAddress())
                        .setSessionControl("end").setCacheBuster(APIMgtGoogleAnalyticsUtils.getCacheBusterId())
                        .setIPOverride(ctx.channel().remoteAddress().toString());
                APIMgtGoogleAnalyticsUtils gaUtils = new APIMgtGoogleAnalyticsUtils();
                gaUtils.init(tenantDomain);
                gaUtils.publishGATrackingData(gaData, req.headers().get(HttpHeaders.USER_AGENT),
                                              headers.get(HttpHeaders.AUTHORIZATION));
            } else {
                String errorMessage = APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE;
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                            HttpResponseStatus.UNAUTHORIZED,
                                                                            Unpooled.copiedBuffer(errorMessage,
                                                                                                  CharsetUtil.UTF_8));
                httpResponse.headers().set("content-type", "text/plain; charset=UTF-8");
                httpResponse.headers().set("content-length", httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
                if (APIUtil.isAnalyticsEnabled()) {
                    publishHandshakeAuthErrorEvent(ctx, APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Authentication Failure for the websocket context: " + apiContext);
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                               APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
        } else if ((msg instanceof CloseWebSocketFrame) || (msg instanceof PingWebSocketFrame)) {
            //if the inbound frame is a closed frame, throttling, analytics will not be published.
            ctx.fireChannelRead(msg);
        } else if (msg instanceof WebSocketFrame) {

            boolean isAllowed = doThrottle(ctx, (WebSocketFrame) msg);

            if (isAllowed) {
                ctx.fireChannelRead(msg);
                String clientIp = getRemoteIP(ctx);
                // publish analytics events if analytics is enabled
                if (APIUtil.isAnalyticsEnabled()) {
                    publishPublishEvent(ctx);
                }
            } else {
                ctx.writeAndFlush(new TextWebSocketFrame("Websocket frame throttled out"));
                if (APIUtil.isAnalyticsEnabled()) {
                    publishPublishThrottledEvent(ctx);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Inbound Websocket frame is throttled. " + ctx.channel().toString());
                }
            }
        }
    }

    /**
     * Extract full request path from the request and update.
     *
     * @param req Request object
     * @throws URISyntaxException if something is wrong while parsing the uri
     */
    protected void setUris(FullHttpRequest req) throws URISyntaxException {

        fullRequestPath = req.uri();
        URI uriTemp = new URI(fullRequestPath);

        requestPath = new URI(uriTemp.getScheme(), uriTemp.getAuthority(), uriTemp.getPath(), null,
                                  uriTemp.getFragment()).toString();
        if (requestPath.endsWith(URL_SEPARATOR)) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        if (log.isDebugEnabled()) {
            log.debug("Websocket API fullRequestPath = " + fullRequestPath);
        }
    }

    protected void setTenantDomain(String uri) {

        if (uri.contains("/t/")) {
            tenantDomain = MultitenantUtils.getTenantDomainFromUrl(uri);
        } else {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
    }

    /**
     * Authenticate request
     *
     * @param req              Full Http Request
     * @param matchingResource resource template matching invocation
     * @return whether authenticated or not
     * @throws APISecurityException if authentication fails
     */
    private boolean validateOAuthHeader(FullHttpRequest req, String matchingResource) throws APISecurityException,
            APIManagementException  {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            APIKeyValidationInfoDTO info;
            if (!req.headers().contains(HttpHeaders.AUTHORIZATION)) {
                QueryStringDecoder decoder = new QueryStringDecoder(fullRequestPath);
                Map<String, List<String>> requestMap = decoder.parameters();
                if (requestMap.containsKey(APIConstants.AUTHORIZATION_QUERY_PARAM_DEFAULT)) {
                    req.headers().add(HttpHeaders.AUTHORIZATION, APIConstants.CONSUMER_KEY_SEGMENT + ' '
                                    + requestMap.get(APIConstants.AUTHORIZATION_QUERY_PARAM_DEFAULT).get(0));
                    removeTokenFromQuery(requestMap);
                    req.setUri(fullRequestPath);
                } else {
                    log.error("No Authorization Header or access_token query parameter present");
                    return false;
                }
            }
            String authorizationHeader = req.headers().get(HttpHeaders.AUTHORIZATION);
            headers.add(HttpHeaders.AUTHORIZATION, authorizationHeader);
            String[] auth = authorizationHeader.split(" ");
            if (APIConstants.CONSUMER_KEY_SEGMENT.equals(auth[0])) {
                String cacheKey;
                boolean isJwtToken = false;
                String apiKey = auth[1];
                if (WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()) {
                    req.headers().remove(HttpHeaders.AUTHORIZATION);
                }

                //Initial guess of a JWT token using the presence of a DOT.

                SignedJWTInfo signedJWTInfo = null;
                if (StringUtils.isNotEmpty(apiKey) && apiKey.contains(APIConstants.DOT)) {
                    try {
                        // Check if the header part is decoded
                        if (StringUtils.countMatches(apiKey, APIConstants.DOT) != 2) {
                            log.debug("Invalid JWT token. The expected token format is <header.payload.signature>");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                                           "Invalid JWT token");
                        }
                        signedJWTInfo = getSignedJwtInfo(apiKey);
                        String keyManager = ServiceReferenceHolder.getInstance().getJwtValidationService()
                                .getKeyManagerNameIfJwtValidatorExist(signedJWTInfo);
                        if (StringUtils.isNotEmpty(keyManager)) {
                            isJwtToken = true;
                        }
                    } catch (ParseException e) {
                        log.debug("Not a JWT token. Failed to decode the token header.", e);
                    } catch (APIManagementException e) {
                        log.error("error while check validation of JWt", e);
                        throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                                       APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                    }
                }
                // Find the authentication scheme based on the token type
                if (isJwtToken) {
                    log.debug("The token was identified as a JWT token");

                    AuthenticationContext authenticationContext = new JWTValidator(new APIKeyValidator(), tenantDomain).
                            authenticateForWebSocket(signedJWTInfo, apiContext, version, matchingResource);
                    if (authenticationContext == null || !authenticationContext.isAuthenticated()) {
                        return false;
                    }
                    // The information given by the AuthenticationContext is set to an APIKeyValidationInfoDTO object
                    // so to feed information analytics and throttle data publishing
                    info = new APIKeyValidationInfoDTO();
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

                    keyType = info.getType();
                    infoDTO = info;
                    authContext = authenticationContext;
                    return authenticationContext.isAuthenticated();
                } else {
                    log.debug("The token was identified as an OAuth token");
                    //If the key have already been validated
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, apiContext, matchingResource);
                        info = WebsocketUtil.validateCache(apiKey, cacheKey);
                        if (info != null) {
                            keyType = info.getType();
                            infoDTO = info;
                            return info.isAuthorized();
                        }
                    }
                    info = getApiKeyDataForWSClient(apiKey, tenantDomain, apiContext, version);
                    if (info == null || !info.isAuthorized()) {
                        return false;
                    }
                    if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
                        cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, apiContext, matchingResource);
                        WebsocketUtil.putCache(info, apiKey, cacheKey);
                    }
                    keyType = info.getType();
                    token = info.getEndUserToken();
                    infoDTO = info;
                    return true;
                }
            } else {
                return false;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    protected String getInboundName(ChannelHandlerContext ctx) {
        return ctx.channel().pipeline().get("ssl") != null ? WS_SECURED_ENDPOINT_NAME : WS_ENDPOINT_NAME;
    }

    protected void setApiPropertiesToChannel(ChannelHandlerContext ctx) {
        Map<String, Object> apiPropertiesMap = WebSocketUtils.getApiProperties(ctx);
        apiPropertiesMap.put(RESTConstants.SYNAPSE_REST_API, apiName);
        apiPropertiesMap.put(RESTConstants.PROCESSED_API, api);
        apiPropertiesMap.put(RESTConstants.REST_API_CONTEXT, apiContext);
        apiPropertiesMap.put(RESTConstants.SYNAPSE_REST_API_VERSION, version);
        ctx.channel().attr(WebSocketUtils.WSO2_PROPERTIES).set(apiPropertiesMap);
    }

    protected void setApiAuthPropertiesToChannel(ChannelHandlerContext ctx) {
        Map<String, Object> apiPropertiesMap = WebSocketUtils.getApiProperties(ctx);
        apiPropertiesMap.put(APIConstants.API_KEY_TYPE, keyType);
        apiPropertiesMap.put(APISecurityUtils.API_AUTH_CONTEXT, authContext);
        ctx.channel().attr(WebSocketUtils.WSO2_PROPERTIES).set(apiPropertiesMap);
    }

    protected APIKeyValidationInfoDTO getApiKeyDataForWSClient(String key, String domain, String apiContextUri,
                                                               String apiVersion) throws APISecurityException {

        return new WebsocketWSClient().getAPIKeyData(apiContextUri, apiVersion, key, domain);
    }

    /**
     * Checks if the request is throttled
     *
     * @param ctx ChannelHandlerContext
     * @return false if throttled
     * @throws APIManagementException
     */
    public boolean doThrottle(ChannelHandlerContext ctx, WebSocketFrame msg) {

        String applicationLevelTier = infoDTO.getApplicationTier();
        String apiLevelTier = infoDTO.getApiTier();
        String subscriptionLevelTier = infoDTO.getTier();
        String resourceLevelTier = apiLevelTier;
        String authorizedUser;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(infoDTO.getSubscriberTenantDomain())) {
            authorizedUser = infoDTO.getSubscriber() + "@" + infoDTO.getSubscriberTenantDomain();
        } else {
            authorizedUser = infoDTO.getSubscriber();
        }
        String apiName = infoDTO.getApiName();
        String apiVersion = version;
        String appTenant = infoDTO.getSubscriberTenantDomain();
        String apiTenant = tenantDomain;
        String appId = infoDTO.getApplicationId();
        String applicationLevelThrottleKey = appId + ":" + authorizedUser;
        String apiLevelThrottleKey = apiContext + ":" + apiVersion;
        String resourceLevelThrottleKey = apiLevelThrottleKey;
        String subscriptionLevelThrottleKey = appId + ":" + apiContext + ":" + apiVersion;
        String messageId = UIDGenerator.generateURNString();
        String remoteIP = getRemoteIP(ctx);
        if (log.isDebugEnabled()) {
            log.debug("Remote IP address : " + remoteIP);
        }
        if (remoteIP.indexOf(":") > 0) {
            remoteIP = remoteIP.substring(1, remoteIP.indexOf(":"));
        }
        JSONObject jsonObMap = new JSONObject();
        Utils.setRemoteIp(jsonObMap, remoteIP);
        jsonObMap.put(APIThrottleConstants.MESSAGE_SIZE, msg.content().capacity());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            boolean isThrottled = WebsocketUtil.isThrottled(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
                                                            applicationLevelThrottleKey);
            if (isThrottled) {
                return false;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Object[] objects =
                new Object[] { messageId, applicationLevelThrottleKey, applicationLevelTier, apiLevelThrottleKey,
                        apiLevelTier, subscriptionLevelThrottleKey, subscriptionLevelTier, resourceLevelThrottleKey,
                        resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant, apiTenant, appId, apiName,
                        jsonObMap.toString() };
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(
                "org.wso2.throttle.request.stream:1.0.0", System.currentTimeMillis(), null, null, objects);
        if (ServiceReferenceHolder.getInstance().getThrottleDataPublisher() == null) {
            log.error("Cannot publish events to traffic manager because ThrottleDataPublisher "
                              + "has not been initialised");
            return true;
        }
        ServiceReferenceHolder.getInstance().getThrottleDataPublisher().getDataPublisher().tryPublish(event);
        return true;
    }

    protected String getRemoteIP(ChannelHandlerContext ctx) {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
    }

    /**
     * Publish request event to analytics server
     *
     * @param clientIp       client's IP Address
     * @param isThrottledOut request is throttled out or not
     */
    public void publishRequestEvent(String clientIp, boolean isThrottledOut) {
        //ignore data publishing
    }

    private void publishHandshakeEvent(ChannelHandlerContext ctx) {
        metricsHandler.handleHandshake(ctx);
    }

    private void publishPublishEvent(ChannelHandlerContext ctx) {
        metricsHandler.handlePublish(ctx);
    }

    public void publishSubscribeEvent(ChannelHandlerContext ctx) {
        metricsHandler.handleSubscribe(ctx);
    }

    private void publishResourceNotFoundEvent(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.RESOURCE_NOT_FOUND_ERROR_CODE);
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE,
                "No matching resource found to dispatch the request");
        metricsHandler.handleHandshake(ctx);
        removeErrorPropertiesFromChannel(ctx);
    }

    private void publishHandshakeAuthErrorEvent(ChannelHandlerContext ctx, String errorMessage) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, errorMessage);
        metricsHandler.handleHandshake(ctx);
        removeErrorPropertiesFromChannel(ctx);
    }

    private void publishPublishThrottledEvent(ChannelHandlerContext ctx) {
        addThrottledErrorPropertiesToChannel(ctx);
        metricsHandler.handlePublish(ctx);
        removeErrorPropertiesFromChannel(ctx);
    }

    public void publishSubscribeThrottledEvent(ChannelHandlerContext ctx) {
        addThrottledErrorPropertiesToChannel(ctx);
        metricsHandler.handleSubscribe(ctx);
        removeErrorPropertiesFromChannel(ctx);
    }

    private void addThrottledErrorPropertiesToChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE);
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, "Message Throttled Out");
    }

    private void removeErrorPropertiesFromChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_CODE);
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_MESSAGE);
    }

    private void removeTokenFromQuery(Map<String, List<String>> parameters) {
        StringBuilder queryBuilder = new StringBuilder(fullRequestPath.substring(0, fullRequestPath.indexOf('?') + 1));

        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (!APIConstants.AUTHORIZATION_QUERY_PARAM_DEFAULT.equals(entry.getKey())) {
                queryBuilder.append(entry.getKey()).append('=').append(entry.getValue().get(0)).append('&');
            }
        }

        // remove trailing '?' or '&' from the built string
        fullRequestPath = queryBuilder.substring(0, queryBuilder.length() - 1);
    }

    private SignedJWTInfo getSignedJwtInfo(String accessToken) throws ParseException {

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

    private MessageContext getMessageContext(String tenantDomain) throws AxisFault, URISyntaxException {

        MessageContext synCtx = WebsocketUtil.getSynapseMessageContext(tenantDomain);
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        msgCtx.setIncomingTransportName(new URI(fullRequestPath).getScheme());
        msgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, fullRequestPath);
        return synCtx;
    }

    protected String getMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req)
            throws WebSocketApiException, AxisFault, URISyntaxException {

        MessageContext synCtx = getMessageContext(tenantDomain);
        api = getApi(fullRequestPath, synCtx);
        if (api == null) {
            handleError(ctx, "No matching API found to dispatch the request");
            return null;
        }
        reConstructFullUriWithVersion(req, synCtx);
        apiContext = api.getContext();
        Resource selectedResource = null;
        Utils.setSubRequestPath(api, synCtx);
        Set<Resource> acceptableResources = new LinkedHashSet<>(Arrays.asList(api.getResources()));
        if (!acceptableResources.isEmpty()) {
            for (RESTDispatcher dispatcher : ApiUtils.getDispatchers()) {
                Resource resource = dispatcher.findResource(synCtx, acceptableResources);
                if (resource != null) {
                    selectedResource = resource;
                    if (APIUtil.isAnalyticsEnabled()) {
                        WebSocketUtils.setApiPropertyToChannel(ctx, APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS,
                                WebSocketUtils.getEndpointUrl(resource, synCtx));
                    }
                    break;
                }
            }
        }
        setApiPropertiesToChannel(ctx);
        if (selectedResource == null) {
            if (APIUtil.isAnalyticsEnabled()) {
                publishResourceNotFoundEvent(ctx);
            }
            handleError(ctx, "No matching resource found to dispatch the request");
        }
        String resource = selectedResource.getDispatcherHelper().getString();
        if (log.isDebugEnabled()) {
            log.info("Selected resource for API dispatch : " + resource);
        }
        return resource;
    }

    private void reConstructFullUriWithVersion(FullHttpRequest req, MessageContext synCtx) {
        fullRequestPath = fullRequestPath.replace(electedRoute, electedAPI.getContext());
        req.setUri(fullRequestPath);
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axis2MsgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, fullRequestPath);
        if (log.isDebugEnabled()) {
            log.debug("Updated full request uri : " + fullRequestPath);
        }
    }

    private void handleError(ChannelHandlerContext ctx, String error) throws WebSocketApiException {

        log.error(error, new Throwable());
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                    HttpResponseStatus.BAD_REQUEST,
                                                                    Unpooled.copiedBuffer(error, CharsetUtil.UTF_8));
        httpResponse.headers().set("content-type", "text/plain; charset=UTF-8");
        httpResponse.headers().set("content-length", httpResponse.content().readableBytes());
        ctx.writeAndFlush(httpResponse);
        throw new WebSocketApiException(error);
    }

    /**
     * Get the name of the matching api for the request path.
     *
     * @param requestPath The request path
     * @param synCtx      The Synapse Message Context
     * @return String The api name
     */
    private API getApi(String requestPath, MessageContext synCtx) {
        TreeMap<String, org.wso2.carbon.apimgt.keymgt.model.entity.API> selectedAPIS =
                Utils.getSelectedAPIList(requestPath, tenantDomain);
        if (selectedAPIS.size() > 0) {
            String selectedPath = selectedAPIS.firstKey();
            org.wso2.carbon.apimgt.keymgt.model.entity.API selectedAPI = selectedAPIS.get(selectedPath);
            API api = synCtx.getEnvironment().getSynapseConfiguration()
                    .getAPI(GatewayUtils.getQualifiedApiName(selectedAPI.getApiName(),
                            selectedAPI.getApiVersion()));
            version = selectedAPI.getApiVersion();
            apiName = selectedAPI.getApiName();
            electedRoute = selectedPath;
            electedAPI = selectedAPI;
            return api;
        }
        return null;
    }
}
