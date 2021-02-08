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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.websocket.WebSocketApiException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.service.APIKeyValidationService;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

import static org.junit.Assert.fail;
import static org.wso2.carbon.apimgt.gateway.handlers.websocket.WebSocketApiConstants.WS_ENDPOINT_NAME;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_KEY_VALIDATOR_WS_CLIENT;

/**
 * Test class for WebsocketInboundHandler
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebsocketInboundHandler.class, MultitenantUtils.class, DataPublisherUtil.class,
        UsageComponent.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class, Caching.class,
        APISecurityUtils.class, WebsocketUtil.class, ThrottleDataPublisher.class, APIUtil.class, RegistryService.class,
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class})
@PowerMockIgnore("javax.net.ssl.SSLContext")
public class WebsocketInboundHandlerTestCase {
    private String TENANT_URL = "https://localhost/t/abc.com/1.0";
    private String SUPER_TENANT_URL = "https://localhost/abc/1.0";
    private String TENANT_DOMAIN = "abc.com";
    private String SUPER_TENANT_DOMAIN = "carbon.super";
    private String AUTHORIZATION = "Authorization: 587hfbt4i8ydno87ywq";
    private String USER_AGENT = "Mozilla";
    private String TOKEN_CACHE_EXPIRY = "900";
    private String API_KEY_VALIDATOR_URL = "https://localhost:9000/";
    private String API_KEY_VALIDATOR_USERNAME = "IsharaC";
    private String API_KEY_VALIDATOR_PASSWORD = "abc123";
    private String CACHE_KEY = "587hfbt4i8ydno87ywq:https://localhost/t/abc.com/1.0";
    //    private String GATEWAY_TOKEN_CACHE_ENABLED = "true";
    private String API_KEY = "587hfbt4i8ydno87ywq";
    private ChannelHandlerContext channelHandlerContext;
    private FullHttpRequest fullHttpRequest;
    private APIManagerConfiguration apiManagerConfiguration;
    private HttpHeaders headers;
    private Cache gatewayCache;
    private RegistryService registryService;
    private ServiceReferenceHolder serviceReferenceHolder;

    @Before
    public void setup() throws RegistryException {
        System.setProperty("carbon.home", "jhkjn");

        channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        fullHttpRequest = Mockito.mock(FullHttpRequest.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(DataPublisherUtil.class);
        PowerMockito.mockStatic(UsageComponent.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(Caching.class);

        Mockito.mock(APIMgtUsageDataBridgeDataPublisher.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        ServiceReferenceHolder serviceReferenceHolder;
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration =
                Mockito.mock(APIManagerAnalyticsConfiguration.class);
        gatewayCache = Mockito.mock(Cache.class);
        headers = Mockito.mock(HttpHeaders.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        CacheBuilder cacheBuilder = Mockito.mock(CacheBuilder.class);
        PowerMockito.when(UsageComponent.getAmConfigService()).thenReturn(apiManagerConfigurationService);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        PowerMockito.when(cacheManager.createCacheBuilder(APIConstants.GATEWAY_KEY_CACHE_NAME)).thenReturn(cacheBuilder);
        PowerMockito.when(DataPublisherUtil.getHostAddress()).thenReturn("192.168.0.1:18000");
        APIMgtUsageDataBridgeDataPublisher apiMgtUsageDataBridgeDataPublisher = Mockito.mock(APIMgtUsageDataBridgeDataPublisher.class);
        Mockito.when(fullHttpRequest.getUri()).thenReturn(TENANT_URL);
        Mockito.when(fullHttpRequest.headers()).thenReturn(headers);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.USER_AGENT)).thenReturn(USER_AGENT);
        Mockito.when(fullHttpRequest.headers()).thenReturn(headers);
        Mockito.when(apiManagerConfigurationService.getAPIAnalyticsConfiguration()).thenReturn(apiManagerAnalyticsConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY)).thenReturn("900");
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        CacheConfiguration.Duration duration = new CacheConfiguration.Duration(TimeUnit.SECONDS,
                Long.parseLong(TOKEN_CACHE_EXPIRY));
        Mockito.when(gatewayCache.get(API_KEY)).thenReturn("fhgvjhhhjkghj");
        Mockito.when(gatewayCache.get(CACHE_KEY)).thenReturn(null);
        Mockito.when(cacheManager.getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME)).thenReturn(gatewayCache);
        Mockito.when(cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.MODIFIED, duration)).thenReturn(cacheBuilder);
        Mockito.when(cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.ACCESSED, duration)).thenReturn(cacheBuilder);
        Mockito.when(cacheBuilder.setStoreByValue(false)).thenReturn(cacheBuilder);
        Mockito.when(cacheBuilder.build()).thenReturn(gatewayCache);
        PowerMockito.doNothing().when(apiMgtUsageDataBridgeDataPublisher).init();
        PowerMockito.mockStatic(ThrottleDataPublisher.class);
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        PowerMockito.when(ThrottleDataPublisher.getDataPublisher()).thenReturn(dataPublisher);
        PowerMockito.mockStatic(WebsocketUtil.class);

        PowerMockito.mockStatic(RegistryService.class);
        registryService = Mockito.mock(RegistryService.class);


    }

    /*
    * Tests channelRead method for tenant when msg is FullHttpRequest
    * */
    @Test
    public void testChannelRead() throws AxisFault {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        //test when the request is a handshake
        WebsocketInboundHandler websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }
        };
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        FullHttpRequest fullHttpRequest = Mockito.mock(FullHttpRequest.class);
        try {
            websocketInboundHandler.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected exception is not thrown. Hence test fails.");
        } catch (Exception e) {
//            test for exception
        }
        Mockito.when(fullHttpRequest.getUri()).thenReturn(TENANT_URL);
        Mockito.when(fullHttpRequest.headers()).thenReturn(headers);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.USER_AGENT)).thenReturn(USER_AGENT);
        Mockito.when(fullHttpRequest.headers()).thenReturn(headers);
        WebsocketInboundHandler websocketInboundHandler1 = new WebsocketInboundHandler() {

            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }

            @Override
            protected String getInboundName(ChannelHandlerContext ctx) {
                return WS_ENDPOINT_NAME;
            }

            @Override
            protected String getMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req) {
                return "";
            }

            @Override
            protected void setApiPropertiesToChannel(ChannelHandlerContext ctx) {
                // do nothing
            }

            @Override
            protected void setUris(FullHttpRequest req) {
                // do nothing
            }

            @Override
            protected void setTenantDomain(String uri) {
                // do nothing
            }
        };
        CacheConfiguration.Duration duration = new CacheConfiguration.Duration(TimeUnit.SECONDS,
                Long.parseLong(TOKEN_CACHE_EXPIRY));
        Mockito.when(gatewayCache.get(API_KEY)).thenReturn("fhgvjhhhjkghj");
        Mockito.when(gatewayCache.get(CACHE_KEY)).thenReturn(null);
        PowerMockito.when(MultitenantUtils.getTenantDomainFromUrl(TENANT_URL)).thenReturn(TENANT_DOMAIN);
        Whitebox.setInternalState(websocketInboundHandler1, "fullRequestPath", "dummy/url");
        //test for Invalid Credentials error
        try {
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown (Invalid Credentials)");
        } catch (Exception e) {
            if (e instanceof APISecurityException) {
                Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
            } else {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).
                thenReturn(API_KEY_VALIDATOR_URL);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).
                thenReturn(API_KEY_VALIDATOR_USERNAME);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).
                thenReturn(API_KEY_VALIDATOR_PASSWORD);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 587hfbt4i8ydno87ywq");

        //test when CONSUMER_KEY_SEGMENT is not present
        Mockito.when(headers.contains(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(true);
        try {
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown(Error while accessing backend services for API key " +
                    "validation");
        } catch (Exception e) {
            if (e instanceof APISecurityException) {
                Assert.assertTrue(e.getMessage().startsWith("Error while accessing backend services for API key " +
                        "validation"));
            } else {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }

        // Test when api key validation client type is invalid it should throw Invalid Credentials Exception
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
        Mockito.when(gatewayCache.get(CACHE_KEY)).thenReturn(apiKeyValidationInfoDTO);
        PowerMockito.mockStatic(APISecurityUtils.class);
        PowerMockito.when(APISecurityUtils.getKeyValidatorClientType()).thenReturn("invalid");
        Mockito.when(headers.contains(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(true);

        ConfigurationContext ctx = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        APIKeyValidationService apiKeyValidationService = Mockito.mock(APIKeyValidationService.class);
        WebsocketWSClient websocketWSClient = Mockito.mock(WebsocketWSClient.class);
        try {
            PowerMockito.when(websocketWSClient.getAPIKeyData(TENANT_URL, "1.0", "587hfbt4i8ydno87ywq", "abc.com"))
                    .thenReturn(apiKeyValidationInfoDTO);
            PowerMockito.whenNew(WebsocketWSClient.class).withNoArguments().thenReturn(websocketWSClient);
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown (Invalid Credentials) when KeyValidatorClientType is provided.");
        } catch (Exception e) {
            if (e instanceof APISecurityException) {
                Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
            } else {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }

    }

    /*
   * Tests channelRead method for tenant when msg is WebSocketFrame
   * */
    @Test
    public void testChannelRead1() throws Exception {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        //test when the request is a handshake
        WebsocketInboundHandler websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }
        };
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        WebSocketFrame webSocketFrame = Mockito.mock(WebSocketFrame.class);
        CacheConfiguration.Duration duration = new CacheConfiguration.Duration(TimeUnit.SECONDS,
                Long.parseLong(TOKEN_CACHE_EXPIRY));
        Mockito.when(gatewayCache.get(API_KEY)).thenReturn("fhgvjhhhjkghj");
        Mockito.when(gatewayCache.get(CACHE_KEY)).thenReturn(null);

        ByteBuf content = Mockito.mock(ByteBuf.class);
        Mockito.when(webSocketFrame.content()).thenReturn(content);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
        //test for happy path
        websocketInboundHandler.channelRead(channelHandlerContext, webSocketFrame);
    }

    /*
    * Tests channelRead method for super tenant
    * */
    @Test
    public void testChannelReadForSuperTenant() throws Exception {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        //test when the request is a handshake
        WebsocketInboundHandler websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }
        };
        PowerMockito.when(MultitenantUtils.getTenantDomainFromUrl(SUPER_TENANT_URL)).thenReturn(SUPER_TENANT_DOMAIN);
        try {
            websocketInboundHandler.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected exception is not thrown. Hence test fails.");
        } catch (Exception e) {
//            test for exception
        }

        WebsocketInboundHandler websocketInboundHandler1 = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }

            @Override
            protected String getInboundName(ChannelHandlerContext ctx) {
                return WS_ENDPOINT_NAME;
            }

            @Override
            protected String getMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req) {
                return "";
            }

            @Override
            protected void setUris(FullHttpRequest req) {
                // do nothing
            }

            @Override
            protected void setTenantDomain(String uri) {
                // do nothing
            }
        };
        //test for Invalid Credentials error
        try {
            Whitebox.setInternalState(websocketInboundHandler1, "fullRequestPath", "dummy/url");
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown (Invalid Credentials)");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
        }

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).
                thenReturn(API_KEY_VALIDATOR_URL);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).
                thenReturn(API_KEY_VALIDATOR_USERNAME);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).
                thenReturn(API_KEY_VALIDATOR_PASSWORD);
        Mockito.when(headers.get(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 587hfbt4i8ydno87ywq");

        //test when CONSUMER_KEY_SEGMENT is not present
        Mockito.when(headers.contains(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(true);
        try {
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown(Error while accessing backend services for API key " +
                    "validation");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while accessing backend services for API key " +
                    "validation"));
        }
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
        Mockito.when(apiKeyValidationInfoDTO.isAuthorized()).thenReturn(false);
        PowerMockito.when(WebsocketUtil.isGatewayTokenCacheEnabled()).thenReturn(true);
        PowerMockito.when(WebsocketUtil.getAccessTokenCacheKey(API_KEY, TENANT_URL, "")).thenReturn(CACHE_KEY);
        PowerMockito.when(WebsocketUtil.validateCache(API_KEY, CACHE_KEY)).thenReturn(apiKeyValidationInfoDTO);
        PowerMockito.when(WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()).thenReturn(true);
//        PowerMockito.when(WebsocketUtil.validateCache(api)).thenReturn(true);
        // Test when api key validation client type is invalid it should throw Invalid Credentials Exception
//        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
        Mockito.when(gatewayCache.get(CACHE_KEY)).thenReturn(apiKeyValidationInfoDTO);
        PowerMockito.mockStatic(APISecurityUtils.class);
        PowerMockito.when(APISecurityUtils.getKeyValidatorClientType()).thenReturn("invalid");
        ConfigurationContext ctx = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        APIKeyValidationService apiKeyValidationServiceStub = Mockito.mock(APIKeyValidationService.class);
        WebsocketWSClient websocketWSClient = Mockito.mock(WebsocketWSClient.class);
        try {
            PowerMockito.when(
                    websocketWSClient.getAPIKeyData(SUPER_TENANT_URL, "1.0", "587hfbt4i8ydno87ywq", "carbon.super"))
                    .thenReturn(apiKeyValidationInfoDTO);
            PowerMockito.whenNew(WebsocketWSClient.class).withAnyArguments().thenReturn(websocketWSClient);
            websocketInboundHandler1.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown (Invalid Credentials) when KeyValidatorClientType is provided.");

        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
        }
        WebsocketInboundHandler websocketInboundHandler2 = new WebsocketInboundHandler() {
            APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();

            @Override
            protected APIKeyValidationInfoDTO getApiKeyDataForWSClient(String key, String domain, String apiContextUri,
                                                                       String apiVersion) {
                return info;
            }

            @Override
            protected String getInboundName(ChannelHandlerContext ctx) {
                return WS_ENDPOINT_NAME;
            }

            @Override
            protected String getMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req) {
                return "";
            }

            @Override
            protected void setUris(FullHttpRequest req) throws URISyntaxException {
                // do nothing
            }

            @Override
            protected void setTenantDomain(String uri) {
                // do nothing
            }
        };

        // keyValidatorClientType = wsclient
        PowerMockito.when(APISecurityUtils.getKeyValidatorClientType()).thenReturn(API_KEY_VALIDATOR_WS_CLIENT);
        PowerMockito.when(WebsocketUtil.isGatewayTokenCacheEnabled()).thenReturn(false);

        try {
            Whitebox.setInternalState(websocketInboundHandler1,"fullRequestPath", "dummy/url");
            websocketInboundHandler2.channelRead(channelHandlerContext, fullHttpRequest);
            fail("Expected APISecurityException is not thrown (Invalid Credentials)");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
        }

        WebsocketInboundHandler websocketInboundHandler3 = new WebsocketInboundHandler() {
            APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();

            @Override
            protected APIKeyValidationInfoDTO getApiKeyDataForWSClient(String key, String domain, String apiKey,
                                                                       String tenantDomain)
                    throws APISecurityException {
                info.setAuthorized(true);
                info.setApiName("Phoneverify*1.0");
                info.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
                return info;
            }

            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }

            @Override
            protected String getInboundName(ChannelHandlerContext ctx) {
                return WS_ENDPOINT_NAME;
            }

            @Override
            protected String getMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req)
                    throws WebSocketApiException, AxisFault, URISyntaxException {
                return "";
            }

            @Override
            protected void setApiPropertiesToChannel(ChannelHandlerContext ctx){
                // do nothing
            }

            @Override
            protected void setUris(FullHttpRequest req) throws URISyntaxException {
                // do nothing
            }

            @Override
            protected void setTenantDomain(String uri) {
                // do nothing
            }
        };
        PowerMockito.mockStatic(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder serviceReferenceHolder
                = Mockito.mock(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);
        PowerMockito.when(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance())
                .thenReturn(serviceReferenceHolder);

        Channel channel = Mockito.mock(Channel.class);
        SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(channel.remoteAddress()).thenReturn(socketAddress);
        String configKey = "/apimgt/statistics/ga-config.xml";
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry()).thenReturn(userRegistry);
        Resource resource = Mockito.mock(Resource.class);
        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode("<test>ishara</test>").array());
        Mockito.when(resource.getContentStream()).thenReturn(is);
        Mockito.when(userRegistry.get(configKey)).thenReturn(resource);

        APIMgtGoogleAnalyticsUtils apiMgtGoogleAnalyticsUtils = Mockito.mock(APIMgtGoogleAnalyticsUtils.class);
        Mockito.doNothing().when(apiMgtGoogleAnalyticsUtils).init("");
        PowerMockito.whenNew(APIMgtGoogleAnalyticsUtils.class).withAnyArguments().thenReturn(apiMgtGoogleAnalyticsUtils);

        Whitebox.setInternalState(websocketInboundHandler1,"fullRequestPath", "dummy/url");
        websocketInboundHandler3.channelRead(channelHandlerContext, fullHttpRequest);
        PowerMockito.when(WebsocketUtil.isGatewayTokenCacheEnabled()).thenReturn(false);
        websocketInboundHandler3.channelRead(channelHandlerContext, fullHttpRequest);

        //When gateway token cache is enabled
        PowerMockito.when(WebsocketUtil.isGatewayTokenCacheEnabled()).thenReturn(true);
        Mockito.when(apiKeyValidationInfoDTO.isAuthorized()).thenReturn(true);

        websocketInboundHandler3.channelRead(channelHandlerContext, fullHttpRequest);

        //when Bearer token is not provided Invalid Credentials exception should be thrown
        Mockito.when(headers.get(org.apache.http.HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION);
        try {
            websocketInboundHandler3.channelRead(channelHandlerContext, fullHttpRequest);
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid Credentials"));
        }

    }

    /*
    *  Test for doThrottle() happy path
    *
    * */
    @Test
    public void testDoThrottle() throws APIManagementException {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        //todo
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        WebSocketFrame webSocketFrame = Mockito.mock(WebSocketFrame.class);
        WebsocketInboundHandler websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }
        };
        ByteBuf content = Mockito.mock(ByteBuf.class);
        Mockito.when(webSocketFrame.content()).thenReturn(content);

        websocketInboundHandler.doThrottle(channelHandlerContext, webSocketFrame);
    }


    /*
    *  Test for NumberFormatException throws when remoteIP is mis formatted
    *
    * */
    @Test
    public void testDoThrottle1() {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        //todo
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        WebSocketFrame webSocketFrame = Mockito.mock(WebSocketFrame.class);
        WebsocketInboundHandler websocketInboundHandler = new WebsocketInboundHandler() {
            @Override
            protected String getRemoteIP(ChannelHandlerContext ctx) {
                return "localhost";
            }
        };
        try {
            websocketInboundHandler.doThrottle(channelHandlerContext, webSocketFrame);
            fail("Expected NumberFormatException is not thrown.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NumberFormatException);
        }
    }
}
