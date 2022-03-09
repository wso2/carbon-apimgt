/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.http.HttpHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundWebSocketProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test class for WebsocketInboundHandler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebSocketUtils.class, InboundWebsocketProcessorUtil.class})
public class WebsocketInboundHandlerTestCase {

    private static final String channelIdString = "11111";
    private static final String remoteIP = "192.168.0.100";
    private ChannelHandlerContext channelHandlerContext;
    private InboundWebSocketProcessor inboundWebSocketProcessor;
    private WebsocketInboundHandler websocketInboundHandler;
    private org.wso2.carbon.apimgt.keymgt.model.entity.API websocketAPI;

    @Before
    public void setup() throws Exception {
        inboundWebSocketProcessor = Mockito.mock(InboundWebSocketProcessor.class);
        channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        Channel channel = Mockito.mock(Channel.class);
        ChannelId channelId = Mockito.mock(ChannelId.class);
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(channel.id()).thenReturn(channelId);
        Mockito.when(channelId.asLongText()).thenReturn(channelIdString);
        Mockito.when(channel.attr(WebSocketUtils.WSO2_PROPERTIES)).thenReturn(getChannelAttributeMap());
        PowerMockito.mockStatic(WebSocketUtils.class);
        SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
        Mockito.when(channel.remoteAddress()).thenReturn(socketAddress);
        PowerMockito.when(WebSocketUtils.getApiProperties(channelHandlerContext)).thenReturn(new HashMap<>());
        APIMgtGoogleAnalyticsUtils apiMgtGoogleAnalyticsUtils = Mockito.mock(APIMgtGoogleAnalyticsUtils.class);
        PowerMockito.whenNew(APIMgtGoogleAnalyticsUtils.class).withAnyArguments()
                .thenReturn(apiMgtGoogleAnalyticsUtils);
        Mockito.doNothing().when(apiMgtGoogleAnalyticsUtils).init("carbon.super");
        Mockito.doNothing().when(apiMgtGoogleAnalyticsUtils).publishGATrackingData(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyObject());
        websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return remoteIP;
            }

            @Override
            public InboundWebSocketProcessor initializeWebSocketProcessor() {
                return inboundWebSocketProcessor;
            }
        };
        websocketAPI = new API(UUID.randomUUID().toString(), 1, "admin", "WSAPI", "1.0.0", "/wscontext", "Unlimited",
                APIConstants.API_TYPE_WS, APIConstants.PUBLISHED_STATUS, false);
    }

    @Test
    public void testWSCloseFrameResponse() throws Exception {

        Object msg = "msg";
        websocketInboundHandler.channelRead(channelHandlerContext, msg);
        Assert.assertTrue((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString))); // No error has occurred context exists in data-holder map.

        CloseWebSocketFrame closeWebSocketFrame = Mockito.mock(CloseWebSocketFrame.class);
        websocketInboundHandler.channelRead(channelHandlerContext, closeWebSocketFrame);
        Assert.assertFalse((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString))); // Closing connection. Remove context from data-holder map.
    }

    @Test
    public void testWSHandshakeResponse() throws Exception {

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        InboundMessageContextDataHolder.getInstance().addInboundMessageContextForConnection(channelIdString,
                inboundMessageContext);
        String headerName = "test-header";
        String headerValue = "test-header-value";
        String strWebSocket = "websocket";
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "ws://localhost:8080/graphql");
        fullHttpRequest.headers().set(headerName, headerValue);
        fullHttpRequest.headers().set(HttpHeaders.UPGRADE, strWebSocket);
        Mockito.when(inboundWebSocketProcessor.handleHandshake(fullHttpRequest, channelHandlerContext,
                inboundMessageContext)).thenReturn(responseDTO);
        websocketInboundHandler.channelRead(channelHandlerContext, fullHttpRequest);
        Assert.assertTrue((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString)));// No error has occurred context exists in data-holder map.
        Assert.assertEquals(inboundMessageContext.getRequestHeaders().get(headerName), headerValue);
        Assert.assertEquals(inboundMessageContext.getToken(), fullHttpRequest.headers()
                .get(APIMgtGatewayConstants.WS_JWT_TOKEN_HEADER));
        Assert.assertEquals(inboundMessageContext.getUserIP(), remoteIP);

        //error response
        responseDTO.setError(true);
        responseDTO.setErrorMessage("error");
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "ws://localhost:8080/graphql");
        Mockito.when(inboundWebSocketProcessor.handleHandshake(fullHttpRequest, channelHandlerContext,
                inboundMessageContext)).thenReturn(responseDTO);
        fullHttpRequest.headers().set(headerName, headerValue);
        fullHttpRequest.headers().set(HttpHeaders.UPGRADE, strWebSocket);
        websocketInboundHandler.channelRead(channelHandlerContext, fullHttpRequest);
        Assert.assertFalse(InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString));  // Closing connection error has occurred
        Assert.assertFalse(fullHttpRequest.headers().contains(APIMgtGatewayConstants.WS_JWT_TOKEN_HEADER));
    }

    @Test
    public void testWSFrameResponse() throws Exception {

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        InboundMessageContextDataHolder.getInstance().addInboundMessageContextForConnection(channelIdString,
                inboundMessageContext);
        ByteBuf content = Mockito.mock(ByteBuf.class);
        WebSocketFrame msg = Mockito.mock(WebSocketFrame.class);
        Mockito.when(msg.content()).thenReturn(content);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        Mockito.when(inboundWebSocketProcessor.handleRequest(msg, inboundMessageContext)).thenReturn(responseDTO);
        websocketInboundHandler.channelRead(channelHandlerContext, msg);
        Assert.assertTrue((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString)));// No error has occurred context exists in data-holder map.

        //error response
        responseDTO.setError(true);
        websocketInboundHandler.channelRead(channelHandlerContext, msg);
        Assert.assertTrue((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString)));

        //close connection error response
        responseDTO.setCloseConnection(true);
        websocketInboundHandler.channelRead(channelHandlerContext, msg);
        Assert.assertFalse((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString)));
    }

    private InboundMessageContext createWebSocketApiMessageContext() {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        inboundMessageContext.setTenantDomain("carbon.super");
        inboundMessageContext.setElectedAPI(websocketAPI);
        inboundMessageContext.setToken("test-backend-jwt-token");
        return inboundMessageContext;
    }

    private Attribute<Map<String, Object>> getChannelAttributeMap() {
        return new Attribute<Map<String, Object>>() {
            @Override
            public AttributeKey<Map<String, Object>> key() {
                return null;
            }

            @Override
            public Map<String, Object> get() {
                return null;
            }

            @Override
            public void set(Map<String, Object> stringObjectMap) {

            }

            @Override
            public Map<String, Object> getAndSet(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> setIfAbsent(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> getAndRemove() {
                return null;
            }

            @Override
            public boolean compareAndSet(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                return false;
            }

            @Override
            public void remove() {

            }
        };
    }
}
