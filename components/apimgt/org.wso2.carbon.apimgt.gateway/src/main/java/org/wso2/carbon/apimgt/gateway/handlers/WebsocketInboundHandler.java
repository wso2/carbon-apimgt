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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.common.gateway.constants.HealthCheckConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.WebSocketThrottleResponseDTO;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundWebSocketProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.WebSocketProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a handler which is actually embedded to the netty pipeline which does operations such as
 * authentication and throttling for the websocket handshake and subsequent websocket frames.
 */
public class WebsocketInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);
    private WebSocketAnalyticsMetricsHandler metricsHandler;
    private WebSocketProcessor webSocketProcessor;
    private final String API_PROPERTIES = "API_PROPERTIES";
    private final String API_CONTEXT_URI = "API_CONTEXT_URI";
    private final String WEB_SC_API_UT = "api.ut.WS_SC";

    public WebsocketInboundHandler() {
        webSocketProcessor = initializeWebSocketProcessor();
        initializeDataPublisher();
    }

    public WebSocketProcessor getWebSocketProcessor() {
        return webSocketProcessor;
    }

    public WebSocketProcessor initializeWebSocketProcessor() {
        WebSocketProcessor processor = ServiceReferenceHolder.getInstance().getWebsocketProcessor();
        if (processor == null) {
            return new InboundWebSocketProcessor();
        } else {
            return processor;
        }
    }

    private void initializeDataPublisher() {
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        if (InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().containsKey(channelId)) {
            InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (APIUtil.isAnalyticsEnabled()) {
            // Resets the property since context is shared. 
            // If this property is non-zero, it means that the frame is coming from backend, to client.
            // If not, it means that the frame is coming from client, to backend.
            WebSocketUtils.setApiPropertyToChannel(ctx, Constants.BACKEND_START_TIME_PROPERTY,
                    0L);
        }
        String channelId = ctx.channel().id().asLongText();

        // This block is for the health check of the ports 8099 and 9099
        if (msg instanceof FullHttpRequest && ((FullHttpRequest) msg).headers() != null
                && !((FullHttpRequest) msg).headers().contains(HttpHeaders.UPGRADE)
                && ((FullHttpRequest) msg).uri().equals(APIConstants.WEB_SOCKET_HEALTH_CHECK_PATH)) {
            ctx.fireChannelRead(msg);
            return;
        }

        if (msg instanceof FullHttpRequest && ((FullHttpRequest) msg).headers() != null
                && !((FullHttpRequest) msg).headers().contains(HttpHeaders.UPGRADE)
                && HealthCheckConstants.HEALTH_CHECK_API_CONTEXT.equals(((FullHttpRequest) msg).uri())) {
            boolean isAllApisDeployed = GatewayUtils.isAllApisDeployed();
            if (isAllApisDeployed) {
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK);
                httpResponse.headers().set(APIConstants.HEADER_CONTENT_TYPE, "text/plain; charset=UTF-8");
                httpResponse.headers().set(APIConstants.HEADER_CONTENT_LENGTH,
                        httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
                return;
            } else {
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR);
                httpResponse.headers().set(APIConstants.HEADER_CONTENT_TYPE, "text/plain; charset=UTF-8");
                httpResponse.headers().set(APIConstants.HEADER_CONTENT_LENGTH,
                        httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
                return;
            }
        }

        InboundMessageContext inboundMessageContext;
        if (InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().containsKey(channelId)) {
            inboundMessageContext = InboundMessageContextDataHolder.getInstance()
                    .getInboundMessageContextForConnectionId(channelId);
        } else {
            inboundMessageContext = new InboundMessageContext();
            inboundMessageContext.setCtx(ctx);
            InboundMessageContextDataHolder.getInstance()
                    .addInboundMessageContextForConnection(channelId, inboundMessageContext);
        }
        inboundMessageContext.setUserIP(getRemoteIP(ctx));
        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.REQUEST_START_TIME_PROPERTY,
                    System.currentTimeMillis());
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.USER_IP_PROPERTY,
                    inboundMessageContext.getUserIP());
        }
        //check if the request is a handshake
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            populateContextHeaders(req, inboundMessageContext);
            validateCorsHeaders(ctx, req);

            InboundProcessorResponseDTO responseDTO =
                    webSocketProcessor.handleHandshake(req, ctx, inboundMessageContext);
            if (!responseDTO.isError()) {
                responseDTO = WebsocketUtil.validateDenyPolicies(inboundMessageContext);
                if (!responseDTO.isError()) {
                    setApiAuthPropertiesToChannel(ctx, inboundMessageContext);
                    setApiPropertiesMapToChannel(ctx, inboundMessageContext);
                    setApiContextUriToChannel(ctx, inboundMessageContext);
                    if (StringUtils.isNotEmpty(inboundMessageContext.getToken())) {
                        String backendJwtHeader = null;
                        JWTConfigurationDto jwtConfigurationDto = ServiceReferenceHolder.getInstance()
                                .getAPIManagerConfiguration().getJwtConfigurationDto();
                        if (jwtConfigurationDto != null) {
                            backendJwtHeader = jwtConfigurationDto.getJwtHeader();
                        }
                        if (StringUtils.isEmpty(backendJwtHeader)) {
                            backendJwtHeader = APIMgtGatewayConstants.WS_JWT_TOKEN_HEADER;
                        }
                        boolean isSSLEnabled = ctx.channel().pipeline().get("ssl") != null;
                        String prefix = null;
                        AxisConfiguration axisConfiguration = ServiceReferenceHolder.getInstance()
                                .getServerConfigurationContext().getAxisConfiguration();
                        TransportOutDescription transportOut;
                        if (isSSLEnabled) {
                            transportOut = axisConfiguration.getTransportOut(APIMgtGatewayConstants.WS_SECURED);
                        } else {
                            transportOut = axisConfiguration.getTransportOut(APIMgtGatewayConstants.WS_NOT_SECURED);
                        }
                        if (transportOut != null
                                && transportOut.getParameter(APIMgtGatewayConstants.WS_CUSTOM_HEADER) != null) {
                            prefix = String.valueOf(transportOut.getParameter(APIMgtGatewayConstants.WS_CUSTOM_HEADER)
                                    .getValue());
                        }
                        if (StringUtils.isNotEmpty(prefix)) {
                            backendJwtHeader = prefix + backendJwtHeader;
                        }
                        req.headers().set(backendJwtHeader, inboundMessageContext.getToken());
                    }
                    ctx.fireChannelRead(req);
                    publishHandshakeEvent(ctx, inboundMessageContext);
                    InboundWebsocketProcessorUtil.publishGoogleAnalyticsData(inboundMessageContext,
                            ctx.channel().remoteAddress().toString());
                } else {
                    ReferenceCountUtil.release(msg);
                    InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
                    FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.valueOf(responseDTO.getErrorCode()),
                            Unpooled.copiedBuffer(responseDTO.getErrorMessage(), CharsetUtil.UTF_8));
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                    ctx.writeAndFlush(httpResponse);
                }
            } else {
                ReferenceCountUtil.release(msg);
                InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(responseDTO.getErrorCode()),
                        Unpooled.copiedBuffer(responseDTO.getErrorMessage(), CharsetUtil.UTF_8));
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
            }
        } else if (msg instanceof CloseWebSocketFrame) {
            //remove inbound message context from data holder
            InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().remove(channelId);
            //if the inbound frame is a closed frame, throttling, analytics will not be published.
            ctx.fireChannelRead(msg);
        } else if (msg instanceof PingWebSocketFrame || msg instanceof PongWebSocketFrame) {
            //if the inbound frame is a ping/pong frame, throttling, analytics will not be published.
            ctx.fireChannelRead(msg);
        } else if (msg instanceof WebSocketFrame) {
            InboundProcessorResponseDTO responseDTO =
                    webSocketProcessor.handleRequest((WebSocketFrame) msg, inboundMessageContext);
            if (responseDTO.isError()) {
                // Release WebsocketFrame
                ReferenceCountUtil.release(msg);
                if (responseDTO.isCloseConnection()) {
                    //remove inbound message context from data holder
                    InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().remove(channelId);
                    Attribute<Object> attributes = ctx.channel().attr(AttributeKey.valueOf(API_PROPERTIES));
                    if (attributes != null) {
                        try {
                            HashMap apiProperties = (HashMap) attributes.get();
                            if (apiProperties != null && !apiProperties.containsKey(WEB_SC_API_UT)) {
                                apiProperties.put(WEB_SC_API_UT, responseDTO.getErrorCode());
                            }
                        } catch (ClassCastException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Unable to cast attributes to a map", e);
                            }
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(channelId + " -- Websocket API request [outbound] : Error while handling Outbound " +
                                "Websocket frame. Closing connection for "
                                + ctx.channel().toString());
                    }
                    handlePublishFrameErrorEvent(ctx, responseDTO);
                    ctx.writeAndFlush(new CloseWebSocketFrame(responseDTO.getErrorCode(),
                            responseDTO.getErrorMessage() + StringUtils.SPACE + "Connection closed" + "!"));
                    ctx.close();
                } else {
                    String errorMessage = responseDTO.getErrorResponseString();
                    ctx.writeAndFlush(new TextWebSocketFrame(errorMessage));
                    handlePublishFrameErrorEvent(ctx, responseDTO);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(channelId + " -- Websocket API request [inbound] : Sending Inbound Websocket frame." +
                            ctx.channel().toString());
                }
                ctx.fireChannelRead(msg);
                // publish analytics events if analytics is enabled
                if (APIUtil.isAnalyticsEnabled()) {
                    WebSocketUtils.setApiPropertyToChannel(ctx, Constants.REQUEST_END_TIME_PROPERTY,
                            System.currentTimeMillis());
                    if (msg instanceof TextWebSocketFrame) {
                        WebSocketUtils.setApiPropertyToChannel(ctx, Constants.RESPONSE_SIZE,
                                ((TextWebSocketFrame) msg).text().length());
                    }
                }
                publishPublishEvent(ctx);
            }
        }
    }

    private void handlePublishFrameErrorEvent(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
        String channelId = ctx.channel().id().asLongText();
        if (responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP) {
            if (log.isDebugEnabled()) {
                WebSocketThrottleResponseDTO throttleResponseDTO =
                        ((WebSocketThrottleResponseDTO) responseDTO.getInboundProcessorResponseError());
                log.debug(channelId + " -- Websocket API request [inbound] : Inbound WebSocket frame is throttled. "
                                  + ctx.channel().toString() + " API Context: " + throttleResponseDTO.getApiContext()
                                  + ", " + "User: " + throttleResponseDTO.getUser() + ", Reason: "
                                  + throttleResponseDTO.getThrottledOutReason());
            }
        } else if (responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_ERROR
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR) {
            if (log.isDebugEnabled()) {
                log.debug(channelId + " -- Websocket API request [inbound] : Inbound WebSocket frame failed due to " +
                        "auth error. " + ctx.channel().toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(channelId + " -- Websocket API request [inbound] : Unclassified error in Inbound WebSocket " +
                        "frame. " + ctx.channel().toString());
            }
        }
        publishPublishFrameErrorEvent(ctx, responseDTO);
    }

    /**
     * Set API auth properties to channel.
     *
     * @param ctx                   Channel context
     * @param inboundMessageContext InboundMessageContext
     */
    private void setApiAuthPropertiesToChannel(ChannelHandlerContext ctx, InboundMessageContext inboundMessageContext) {

        Map<String, Object> apiPropertiesMap = WebSocketUtils.getApiProperties(ctx);
        apiPropertiesMap.put(APIConstants.API_KEY_TYPE, inboundMessageContext.getKeyType());
        apiPropertiesMap.put(APISecurityUtils.API_AUTH_CONTEXT, inboundMessageContext.getAuthContext());
        ctx.channel().attr(WebSocketUtils.WSO2_PROPERTIES).set(apiPropertiesMap);
    }

    private void publishPublishFrameErrorEvent(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
        if (APIUtil.isAnalyticsEnabled()) {
            addErrorPropertiesToChannel(ctx, responseDTO);
            metricsHandler.handlePublish(ctx);
            removeErrorPropertiesFromChannel(ctx);
        }
    }

    protected String getRemoteIP(ChannelHandlerContext ctx) {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
    }

    private void removeErrorPropertiesFromChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_CODE);
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_MESSAGE);
    }

    private void addErrorPropertiesToChannel(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE, responseDTO.getErrorCode());
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, responseDTO.getErrorMessage());
    }

    private void populateContextHeaders(FullHttpRequest request, InboundMessageContext inboundMessageContext) {

        for (Map.Entry<String, String> headerEntry : request.headers().entries()) {
            inboundMessageContext.getRequestHeaders().put(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    private void publishPublishEvent(ChannelHandlerContext ctx) {

        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler.handlePublish(ctx);
        }
    }

    /**
     * Publish handshake event if analytics enabled.
     *
     * @param ctx                   Channel context
     * @param inboundMessageContext InboundMessageContext
     */
    private void publishHandshakeEvent(ChannelHandlerContext ctx, InboundMessageContext inboundMessageContext) {

        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.USER_AGENT_PROPERTY,
                    inboundMessageContext.getRequestHeaders().get(HttpHeaders.USER_AGENT));
            WebSocketUtils.setApiPropertyToChannel(ctx, APIConstants.API_ELECTED_RESOURCE,
                    inboundMessageContext.getMatchingResource());
            metricsHandler.handleHandshake(ctx);
        }
    }

    private void validateCorsHeaders(ChannelHandlerContext ctx, FullHttpRequest req) throws APISecurityException {
        // Current implementation supports validating only the 'origin' header

        if (!APIUtil.isCORSValidationEnabledForWS()) {
            return;
        }
        String requestOrigin = req.headers().get(HttpHeaderNames.ORIGIN);
        // Don't validate the 'origin' header if it's not present in the request
        if (requestOrigin == null) {
            return;
        }
        String allowedOrigin = assessAndGetAllowedOrigin(requestOrigin);
        if (allowedOrigin == null) {
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
            ctx.writeAndFlush(httpResponse);
            ctx.close();
            log.warn("Validation of CORS origin header failed for WS request on: " + req.uri());
            throw new APISecurityException(APISecurityConstants.CORS_ORIGIN_HEADER_VALIDATION_FAILED,
                    APISecurityConstants.CORS_ORIGIN_HEADER_VALIDATION_FAILED_MESSAGE);
        }
    }

    private String assessAndGetAllowedOrigin(String origin) {

        if (WebsocketUtil.allowedOriginsConfigured.contains("*")) {
            return "*";
        } else if (WebsocketUtil.allowedOriginsConfigured.contains(origin)) {
            return origin;
        } else if (origin != null) {
            for (String allowedOrigin : WebsocketUtil.allowedOriginsConfigured) {
                if (allowedOrigin.contains("*")) {
                    Pattern pattern = Pattern.compile(allowedOrigin.replace("*", ".*"));
                    Matcher matcher = pattern.matcher(origin);
                    if (matcher.find()) {
                        return origin;
                    }
                }
            }
        }
        return null;
    }

    private void setApiPropertiesMapToChannel(ChannelHandlerContext ctx, InboundMessageContext inboundMessageContext) {

        ctx.channel().attr(AttributeKey.valueOf(API_PROPERTIES)).set(createApiPropertiesMap(inboundMessageContext));
    }

    private void setApiContextUriToChannel(ChannelHandlerContext ctx, InboundMessageContext inboundMessageContext) {

        Map<String, String> apiContextUriMap = new HashMap<>();
        apiContextUriMap.put("apiContextUri", inboundMessageContext.getRequestPath());
        ctx.channel().attr(AttributeKey.valueOf(API_CONTEXT_URI)).set(apiContextUriMap);
    }

    private Map<String, Object> createApiPropertiesMap(InboundMessageContext inboundMessageContext) {

        Map<String, Object> apiPropertiesMap = new HashMap<>();
        API api = inboundMessageContext.getElectedAPI();
        String apiName = api.getApiName();
        String apiVersion = api.getApiVersion();
        apiPropertiesMap.put(APIMgtGatewayConstants.API, apiName);
        apiPropertiesMap.put(APIMgtGatewayConstants.VERSION, apiVersion);
        apiPropertiesMap.put(APIMgtGatewayConstants.API_VERSION, apiName + ":v" + apiVersion);
        apiPropertiesMap.put(APIMgtGatewayConstants.CONTEXT, inboundMessageContext.getApiContext());
        apiPropertiesMap.put(APIMgtGatewayConstants.API_TYPE, String.valueOf(APIConstants.ApiTypes.API));
        apiPropertiesMap.put(APIMgtGatewayConstants.HOST_NAME, APIUtil.getHostAddress());

        APIKeyValidationInfoDTO infoDTO = inboundMessageContext.getInfoDTO();
        if (infoDTO != null) {
            apiPropertiesMap.put(APIMgtGatewayConstants.CONSUMER_KEY, infoDTO.getConsumerKey());
            apiPropertiesMap.put(APIMgtGatewayConstants.USER_ID, infoDTO.getEndUserName());
            apiPropertiesMap.put(APIMgtGatewayConstants.API_PUBLISHER, infoDTO.getApiPublisher());
            apiPropertiesMap.put(APIMgtGatewayConstants.END_USER_NAME, infoDTO.getEndUserName());
            apiPropertiesMap.put(APIMgtGatewayConstants.APPLICATION_NAME, infoDTO.getApplicationName());
            apiPropertiesMap.put(APIMgtGatewayConstants.APPLICATION_ID, infoDTO.getApplicationId());
        }
        return apiPropertiesMap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Attribute<Object> attributes = ctx.channel().attr(AttributeKey.valueOf(API_PROPERTIES));
        if (cause instanceof CorruptedWebSocketFrameException && attributes != null) {
            HashMap apiProperties = (HashMap) attributes.get();
            CorruptedWebSocketFrameException corruptedWebSocketFrameException = ((CorruptedWebSocketFrameException) cause);
            apiProperties.put(WEB_SC_API_UT, corruptedWebSocketFrameException.closeStatus().code());
        }

        // Improve Websocket logging by adding API URI into log
        Attribute<Object> apiContextUriAttributes = ctx.channel().attr(AttributeKey.valueOf(API_CONTEXT_URI));
        HashMap apiContextUris = (HashMap) apiContextUriAttributes.get();
        String apiContextUri = (String) apiContextUris.get("apiContextUri");

        if (apiContextUri != null) {
            Throwable newCause = new Throwable(cause.getMessage() + " For the URI: " + apiContextUri);
            newCause.initCause(cause);
            super.exceptionCaught(ctx, newCause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
