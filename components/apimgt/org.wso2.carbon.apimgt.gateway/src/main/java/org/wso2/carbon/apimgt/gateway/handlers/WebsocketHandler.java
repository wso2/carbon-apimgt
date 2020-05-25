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
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class WebsocketHandler extends CombinedChannelDuplexHandler<WebsocketInboundHandler, WebsocketOutboundHandler> {

    private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);

    public WebsocketHandler() {
        super(new WebsocketInboundHandler(), new WebsocketOutboundHandler());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if ((msg instanceof CloseWebSocketFrame) || (msg instanceof PongWebSocketFrame)) {
            //if the inbound frame is a closed frame, throttling, analytics will not be published.
            outboundHandler().write(ctx, msg, promise);

        } else if (msg instanceof WebSocketFrame) {
            if (isAllowed(ctx, (WebSocketFrame) msg)) {
                outboundHandler().write(ctx, msg, promise);
                // publish analytics events if analytics is enabled
                if (APIUtil.isAnalyticsEnabled()) {
                    String clientIp = getClientIp(ctx);
                    inboundHandler().publishRequestEvent(clientIp, true);
                }
            } else {
                if (log.isDebugEnabled()){
                    log.debug("Outbound Websocket frame is throttled. " + ctx.channel().toString());
                }
            }
        } else {
            outboundHandler().write(ctx, msg, promise);
        }
    }

    protected boolean isAllowed(ChannelHandlerContext ctx, WebSocketFrame msg) throws APIManagementException {
        return inboundHandler().doThrottle(ctx, msg);
    }

    protected String getClientIp(ChannelHandlerContext ctx) {
        return inboundHandler().getRemoteIP(ctx);
    }
}
