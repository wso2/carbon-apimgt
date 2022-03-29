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
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundWebSocketProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * This is a handler which is actually embedded to the netty pipeline which does operations such as
 * authentication and throttling for the websocket handshake and subsequent websocket frames.
 */
public class WebsocketInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);
    private WebSocketAnalyticsMetricsHandler metricsHandler;
    private InboundWebSocketProcessor webSocketProcessor;

    public WebsocketInboundHandler() {
        webSocketProcessor = initializeWebSocketProcessor();
        initializeDataPublisher();
    }

    public InboundWebSocketProcessor getWebSocketProcessor() {
        return webSocketProcessor;
    }

    public InboundWebSocketProcessor initializeWebSocketProcessor() {
        return new InboundWebSocketProcessor();
    }

    private void initializeDataPublisher() {
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String channelId = ctx.channel().id().asLongText();

        // This block is for the health check of the ports 8099 and 9099
        if (msg instanceof FullHttpRequest && ((FullHttpRequest) msg).headers() != null
                && !((FullHttpRequest) msg).headers().contains(HttpHeaders.UPGRADE)
                && ((FullHttpRequest) msg).uri().equals(APIConstants.WEB_SOCKET_HEALTH_CHECK_PATH)) {
            ctx.fireChannelRead(msg);
            return;
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
            InboundProcessorResponseDTO responseDTO =
                    webSocketProcessor.handleHandshake(req, ctx, inboundMessageContext);
            if (!responseDTO.isError()) {
                setApiAuthPropertiesToChannel(ctx, inboundMessageContext);
                if (StringUtils.isNotEmpty(inboundMessageContext.getToken())) {
                    req.headers().set(APIMgtGatewayConstants.WS_JWT_TOKEN_HEADER, inboundMessageContext.getToken());
                }
                ctx.fireChannelRead(req);
                publishHandshakeEvent(ctx, inboundMessageContext);
                InboundWebsocketProcessorUtil.publishGoogleAnalyticsData(inboundMessageContext,
                        ctx.channel().remoteAddress().toString());
            } else {
                InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(responseDTO.getErrorCode()),
                        Unpooled.copiedBuffer(responseDTO.getErrorMessage(), CharsetUtil.UTF_8));
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
            }
        } else if ((msg instanceof CloseWebSocketFrame) || (msg instanceof PingWebSocketFrame)) {
            //remove inbound message context from data holder
            InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().remove(channelId);
            //if the inbound frame is a closed frame, throttling, analytics will not be published.
            ctx.fireChannelRead(msg);
        } else if (msg instanceof WebSocketFrame) {
            InboundProcessorResponseDTO responseDTO =
                    webSocketProcessor.handleRequest((WebSocketFrame) msg, inboundMessageContext);
            if (responseDTO.isError()) {
                if (responseDTO.isCloseConnection()) {
                    //remove inbound message context from data holder
                    InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().remove(channelId);
                    if (log.isDebugEnabled()) {
                        log.debug("Error while handling Outbound Websocket frame. Closing connection for "
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
                    log.debug("Sending Inbound Websocket frame." + ctx.channel().toString());
                }
                ctx.fireChannelRead(msg);
                // publish analytics events if analytics is enabled
                publishPublishEvent(ctx);
            }
        }
    }

    private void handlePublishFrameErrorEvent(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
        if (responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_COMPLEX
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP) {
            if (log.isDebugEnabled()) {
                log.debug("Inbound WebSocket frame is throttled. " + ctx.channel().toString());
            }
        } else if (responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_ERROR
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS
                || responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR) {
            if (log.isDebugEnabled()) {
                log.debug("Inbound WebSocket frame failed due to auth error. " + ctx.channel().toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unclassified error in Inbound WebSocket frame. " + ctx.channel().toString());
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
}
