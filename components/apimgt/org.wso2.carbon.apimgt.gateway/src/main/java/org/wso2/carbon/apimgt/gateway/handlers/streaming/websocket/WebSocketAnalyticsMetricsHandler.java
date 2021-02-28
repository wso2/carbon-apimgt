/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

import io.netty.channel.ChannelHandlerContext;
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.impl.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

/**
 * Handler to publish WebSocket analytics data to analytics cloud.
 */
public class WebSocketAnalyticsMetricsHandler {
    private static final String HANDSHAKE = "HANDSHAKE";
    private static final String PUBLISH = "PUBLISH";
    private static final String SUBSCRIBE = "SUBSCRIBE";

    public void handleHandshake(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, APIMgtGatewayConstants.HTTP_METHOD, HANDSHAKE);
        collectData(ctx);
    }

    public void handlePublish(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, APIMgtGatewayConstants.HTTP_METHOD, PUBLISH);
        collectData(ctx);
    }

    public void handleSubscribe(ChannelHandlerContext ctx) {
        WebSocketUtils.setApiPropertyToChannel(ctx, APIMgtGatewayConstants.HTTP_METHOD, SUBSCRIBE);
        collectData(ctx);
    }

    private void collectData(ChannelHandlerContext ctx) {
        AnalyticsDataProvider provider = new WebSocketAnalyticsDataProvider(ctx);
        GenericRequestDataCollector collector = new GenericRequestDataCollector(provider);
        collector.collectData();
    }
}
