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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.http.HttpHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContextDataHolder;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.WebSocketProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test class for WebsocketInboundHandler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ WebSocketUtils.class, InboundWebsocketProcessorUtil.class,
        APIUtil.class, WebsocketInboundHandler.class, ServiceReferenceHolder.class, WebsocketUtil.class})
public class WebsocketInboundHandlerTestCase {

    private static final String channelIdString = "11111";
    private static final String remoteIP = "192.168.0.100";
    private static final String APPLICATION_TIER = "ApplicationTier";
    private static final String APPLICATION_NAME = "ApplicationName";
    private static final String APPLICATION_ID = "1";
    private static final String TIER = "Tier";
    private static final String SUBSCRIBER = "subscriber";
    private static final String APPLICATION_CONSUMER_KEY = "NdYZFnAfUa7uST1giZrmIq8he8Ya";
    private String SUPER_TENANT_DOMAIN = "carbon.super";
    private ChannelHandlerContext channelHandlerContext;
    private WebSocketProcessor inboundWebSocketProcessor;
    private WebsocketInboundHandler websocketInboundHandler;
    private org.wso2.carbon.apimgt.keymgt.model.entity.API websocketAPI;

    @Before
    public void setup() throws Exception {
        inboundWebSocketProcessor = Mockito.mock(WebSocketProcessor.class);
        channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        Channel channel = Mockito.mock(Channel.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        ChannelId channelId = Mockito.mock(ChannelId.class);
        Mockito.when(channel.attr(AttributeKey.valueOf("API_PROPERTIES"))).thenReturn(attribute);
        Mockito.when(channel.attr(AttributeKey.valueOf("API_CONTEXT_URI"))).thenReturn(attribute);
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(channel.id()).thenReturn(channelId);
        Mockito.when(channelId.asLongText()).thenReturn(channelIdString);
        Mockito.when(channel.attr(WebSocketUtils.WSO2_PROPERTIES)).thenReturn(getChannelAttributeMap());
        PowerMockito.mockStatic(APIUtil.class);
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
            public WebSocketProcessor initializeWebSocketProcessor() {
                return inboundWebSocketProcessor;
            }
        };
        websocketAPI = new API(UUID.randomUUID().toString(), 1, "admin", "WSAPI", "1.0.0", "/wscontext", "Unlimited",
                APIConstants.API_TYPE_WS, APIConstants.PUBLISHED_STATUS, false);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.mockStatic(WebsocketUtil.class);
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

        HashMap<String, Object> apiProperties = new HashMap<>();
        PowerMockito.whenNew(HashMap.class).withAnyArguments().thenReturn(apiProperties);
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        APIKeyValidationInfoDTO infoDTO = createAPIKeyValidationInfo(websocketAPI);
        inboundMessageContext.setInfoDTO(infoDTO);
        PowerMockito.when(APIUtil.getHostAddress()).thenReturn("localhost");
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
        ServiceReferenceHolder   serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        ChannelPipeline channelPipeline = Mockito.mock(ChannelPipeline.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getServerConfigurationContext()).thenReturn(configurationContext);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);
        Mockito.when(channelHandlerContext.channel().pipeline()).thenReturn(channelPipeline);
        Mockito.when(inboundWebSocketProcessor.handleHandshake(fullHttpRequest, channelHandlerContext,
                inboundMessageContext)).thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        websocketInboundHandler.channelRead(channelHandlerContext, fullHttpRequest);
        validateApiProperties(apiProperties, infoDTO, inboundMessageContext);

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
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
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
        responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.INTERNAL_SERVER_ERROR);
        websocketInboundHandler.channelRead(channelHandlerContext, msg);
        Assert.assertFalse((InboundMessageContextDataHolder.getInstance().getInboundMessageContextMap()
                .containsKey(channelIdString)));
    }

    @Test
    public void exceptionCaughtTest() throws Exception {
        Throwable cause = new CorruptedWebSocketFrameException(WebSocketCloseStatus.MESSAGE_TOO_BIG,
                "Max frame length of 65536 has been exceeded.");
        Attribute<Object> apiPropertiesAttributes = Mockito.mock(Attribute.class);
        Attribute<Object> contextUriAttributes = Mockito.mock(Attribute.class);

        Mockito.when(channelHandlerContext.channel().attr(AttributeKey.valueOf("API_PROPERTIES")))
                .thenReturn(apiPropertiesAttributes);
        Mockito.when(channelHandlerContext.channel().attr(AttributeKey.valueOf("API_CONTEXT_URI")))
                .thenReturn(contextUriAttributes);

        HashMap apiProperties = new HashMap();
        HashMap apiContextUriAttributes = new HashMap();

        Mockito.when((HashMap)apiPropertiesAttributes.get()).thenReturn(apiProperties);
        Mockito.when((HashMap)contextUriAttributes.get()).thenReturn(apiContextUriAttributes);

        websocketInboundHandler.exceptionCaught(channelHandlerContext, cause);
        Assert.assertEquals(apiProperties.get("api.ut.WS_SC"), 1009);
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

    private void validateApiProperties(HashMap apiPropertiesMap, APIKeyValidationInfoDTO infoDTO, InboundMessageContext inboundMessageContext) {
        API electedAPI = inboundMessageContext.getElectedAPI();
        Assert.assertEquals(electedAPI.getApiName(), apiPropertiesMap.get(APIMgtGatewayConstants.API));
        Assert.assertEquals(electedAPI.getApiVersion(), apiPropertiesMap.get(APIMgtGatewayConstants.VERSION));
        Assert.assertEquals(electedAPI.getApiName() + ":v" + electedAPI.getApiVersion(),
                apiPropertiesMap.get(APIMgtGatewayConstants.API_VERSION));
        Assert.assertEquals(inboundMessageContext.getApiContext(),
                apiPropertiesMap.get(APIMgtGatewayConstants.CONTEXT));
        Assert.assertEquals(String.valueOf(APIConstants.ApiTypes.API),
                apiPropertiesMap.get(APIMgtGatewayConstants.API_TYPE));
        Assert.assertEquals(APIUtil.getHostAddress(), apiPropertiesMap.get(APIMgtGatewayConstants.HOST_NAME));
        Assert.assertEquals(infoDTO.getConsumerKey(), apiPropertiesMap.get(APIMgtGatewayConstants.CONSUMER_KEY));
        Assert.assertEquals(infoDTO.getEndUserName(), apiPropertiesMap.get(APIMgtGatewayConstants.USER_ID));
        Assert.assertEquals(infoDTO.getApiPublisher(), apiPropertiesMap.get(APIMgtGatewayConstants.API_PUBLISHER));
        Assert.assertEquals(infoDTO.getEndUserName(), apiPropertiesMap.get(APIMgtGatewayConstants.END_USER_NAME));
        Assert.assertEquals(infoDTO.getApplicationName(),
                apiPropertiesMap.get(APIMgtGatewayConstants.APPLICATION_NAME));
        Assert.assertEquals(infoDTO.getApplicationId(), apiPropertiesMap.get(APIMgtGatewayConstants.APPLICATION_ID));
    }

    private APIKeyValidationInfoDTO createAPIKeyValidationInfo(API api) {
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(true);
        info.setApplicationTier(APPLICATION_TIER);
        info.setTier(TIER);
        info.setSubscriberTenantDomain(SUPER_TENANT_DOMAIN);
        info.setSubscriber(SUBSCRIBER);
        info.setStopOnQuotaReach(true);
        info.setApiName(api.getApiName());
        info.setApplicationId(APPLICATION_ID);
        info.setType("PRODUCTION");
        info.setApiPublisher(api.getApiProvider());
        info.setApplicationName(APPLICATION_NAME);
        info.setConsumerKey(APPLICATION_CONSUMER_KEY);
        info.setEndUserName(SUBSCRIBER+"@"+SUPER_TENANT_DOMAIN);
        info.setApiTier(api.getApiTier());
        info.setEndUserToken("callerToken");
        info.setGraphQLMaxDepth(5);
        info.setGraphQLMaxComplexity(5);
        return info;
    }
}
