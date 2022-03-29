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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class WebsocketHandler extends CombinedChannelDuplexHandler<WebsocketInboundHandler, WebsocketOutboundHandler> {

    private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);
    private WebSocketAnalyticsMetricsHandler metricsHandler;

    public WebsocketHandler() {
        this(new WebsocketInboundHandler(), new WebsocketOutboundHandler());
    }

    public WebsocketHandler(WebsocketInboundHandler websocketInboundHandler,
            WebsocketOutboundHandler websocketOutboundHandler) {
        super(websocketInboundHandler, websocketOutboundHandler);
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        String channelId = ctx.channel().id().asLongText();
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

        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.REQUEST_START_TIME_PROPERTY,
                    System.currentTimeMillis());
        }

        if ((msg instanceof CloseWebSocketFrame) || (msg instanceof PongWebSocketFrame)) {
            //remove inbound message context from data holder
            InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap().remove(channelId);
            //if the inbound frame is a closed frame, throttling, analytics will not be published.
            outboundHandler().write(ctx, msg, promise);
        } else if (msg instanceof WebSocketFrame) {
            InboundProcessorResponseDTO responseDTO = inboundHandler().getWebSocketProcessor().handleResponse(
                    (WebSocketFrame) msg, inboundMessageContext);
            if (responseDTO.isError()) {
                if (responseDTO.isCloseConnection()) {
                    InboundMessageContextDataHolder.getInstance().removeInboundMessageContextForConnection(channelId);
                    if (log.isDebugEnabled()) {
                        log.debug("Error while handling Outbound Websocket frame. Closing connection for "
                                + ctx.channel().toString());
                    }
                    handleSubscribeFrameErrorEvent(ctx, responseDTO);
                    outboundHandler().write(ctx, new CloseWebSocketFrame(responseDTO.getErrorCode(),
                            responseDTO.getErrorMessage() + StringUtils.SPACE + "Connection closed" + "!"), promise);
                    outboundHandler().flush(ctx);
                    outboundHandler().close(ctx, promise);
                } else {
                    handleSubscribeFrameErrorEvent(ctx, responseDTO);
                    String errorMessage = responseDTO.getErrorResponseString();
                    outboundHandler().write(ctx, new TextWebSocketFrame(errorMessage), promise);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Sending Outbound Websocket frame." + ctx.channel().toString());
                }
                outboundHandler().write(ctx, msg, promise);
                // publish analytics events if analytics is enabled
                publishSubscribeEvent(ctx);
            }
        } else {
            outboundHandler().write(ctx, msg, promise);
        }
    }

    private void handleSubscribeFrameErrorEvent(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
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
        publishSubscribeFrameErrorEvent(ctx, responseDTO);
    }

    private void publishSubscribeEvent(ChannelHandlerContext ctx) {
        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler.handleSubscribe(ctx);
        }
    }

    private void publishSubscribeFrameErrorEvent(ChannelHandlerContext ctx, InboundProcessorResponseDTO responseDTO) {
        if (APIUtil.isAnalyticsEnabled()) {
            addErrorPropertiesToChannel(ctx, responseDTO);
            metricsHandler.handleSubscribe(ctx);
            removeErrorPropertiesFromChannel(ctx);
        }
    }

    private void addErrorPropertiesToChannel(ChannelHandlerContext ctx,
            InboundProcessorResponseDTO responseDTO) {
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE, responseDTO.getErrorCode());
        WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, responseDTO.getErrorMessage());
    }

    private void removeErrorPropertiesFromChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_CODE);
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_MESSAGE);
    }
}
