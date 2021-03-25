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
import io.netty.util.AttributeKey;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.Resource;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.mediators.builtin.SendMediator;
import org.apache.synapse.mediators.filters.FilterMediator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Util methods related to WebSocket APIs.
 */
public class WebSocketUtils {
    public static final AttributeKey<Map<String, Object>> WSO2_PROPERTIES = AttributeKey.valueOf("WSO2_PROPERTIES");

    /**
     * Gets the uri-template of the endpoint, which has been specified in the given resource.
     * @param resource  API Resource
     * @param synCtx    MessageContext
     * @return          URI template
     */
    public static String getEndpointUrl(Resource resource, MessageContext synCtx) {
        Optional<Mediator> filterMediator = resource.getInSequence().getList().stream()
                .filter(m -> m instanceof FilterMediator).findFirst();
        if (filterMediator.isPresent()) {
            Optional<Mediator> sendMediator = ((FilterMediator) filterMediator.get()).getList().stream()
                    .filter(m -> m instanceof SendMediator).findFirst();
            if (sendMediator.isPresent()) {
                Endpoint endpoint = ((SendMediator) sendMediator.get()).getEndpoint();
                if (endpoint instanceof IndirectEndpoint) {
                    String endpointKey = ((IndirectEndpoint) endpoint).getKey();
                    Endpoint directEndpoint = synCtx.getConfiguration().getEndpoint(endpointKey);
                    if (directEndpoint instanceof HTTPEndpoint) {
                        return ((HTTPEndpoint) synCtx.getConfiguration().getEndpoint(endpointKey))
                                .getUriTemplate().getTemplate();
                    }
                }
            }
        }
        return null; // Ideally we won't reach here
    }

    public static Object getPropertyFromChannel(String key, ChannelHandlerContext ctx) {
        Object prop = ctx.channel().attr(WSO2_PROPERTIES).get();
        if (prop != null) {
            Map<String, Object> properties = (Map<String, Object>) prop;
            return properties.get(key);
        }
        return null;
    }

    public static Map<String, Object> getApiProperties(ChannelHandlerContext ctx) {
        Object prop = ctx.channel().attr(WSO2_PROPERTIES).get();
        if (prop != null) {
            return (Map<String, Object>) prop;
        }
        return new HashMap<>();
    }

    public static void setApiPropertyToChannel(ChannelHandlerContext ctx, String key, Object value) {
        if (key != null && value != null) {
            Map<String, Object> properties = getApiProperties(ctx);
            properties.put(key, value);
            ctx.channel().attr(WSO2_PROPERTIES).set(properties);
        }
    }

    public static void removeApiPropertyFromChannel(ChannelHandlerContext ctx, String key) {
        if (key != null) {
            Object prop = ctx.channel().attr(WSO2_PROPERTIES).get();
            if (prop != null) {
                Map<String, Object> properties = (Map<String, Object>) prop;
                properties.remove(key);
                ctx.channel().attr(WSO2_PROPERTIES).set(properties);
            }
        }
    }
}
