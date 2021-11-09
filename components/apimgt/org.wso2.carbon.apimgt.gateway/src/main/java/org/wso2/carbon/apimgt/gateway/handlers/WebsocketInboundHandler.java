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
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundWebSocketProcessor;
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
        webSocketProcessor = new InboundWebSocketProcessor();
        initializeDataPublisher();
    }

    public InboundWebSocketProcessor getWebSocketProcessor() {
        return webSocketProcessor;
    }

    private void initializeDataPublisher() {
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        InboundMessageContext inboundMessageContext;
        if (InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().containsKey(channelId)) {
            inboundMessageContext = InboundMessageContextDataHolder.getInstance()
                    .getInboundMessageContextForConnectionId(channelId);
        } else {
            inboundMessageContext = new InboundMessageContext();
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
            if (responseDTO.isError()) {
                InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(responseDTO.getErrorCode()),
                        Unpooled.copiedBuffer(responseDTO.getErrorMessage(), CharsetUtil.UTF_8));
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse);
            } else {
                ctx.fireChannelRead(msg);
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
                    ctx.writeAndFlush(new CloseWebSocketFrame(responseDTO.getErrorCode(),
                            responseDTO.getErrorMessage() + StringUtils.SPACE + "Connection closed" + "!"));
                    ctx.close();
                } else {
                    String errorMessage = responseDTO.getErrorResponseString();
                    ctx.writeAndFlush(new TextWebSocketFrame(errorMessage));
                    if (responseDTO.getErrorCode() == WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR) {
                        if (log.isDebugEnabled()) {
                            log.debug("Inbound Websocket frame is throttled. " + ctx.channel().toString());
                        }
                        publishPublishThrottledEvent(ctx);
                    }
                }
            } else {
                ctx.fireChannelRead(msg);
                // publish analytics events if analytics is enabled
                publishPublishEvent(ctx);
            }
        }
    }

    private void publishPublishThrottledEvent(ChannelHandlerContext ctx) {
        if (APIUtil.isAnalyticsEnabled()) {
            addThrottledErrorPropertiesToChannel(ctx);
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

    private void addThrottledErrorPropertiesToChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE);
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, "Message Throttled Out");
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
}
