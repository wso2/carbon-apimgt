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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * Test class for WebsocketHandler
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, DataPublisherUtil.class})
public class WebsocketHandlerTestCase {

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);

    }

    /*
    * This method tests write() when msg is not a WebSocketFrame
    * */
    @Test
    public void testWrite() throws Exception {
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        ChannelPromise channelPromise = Mockito.mock(ChannelPromise.class);
        Object msg = "msg";
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        WebsocketHandler websocketHandler = new WebsocketHandler();
        websocketHandler.write(channelHandlerContext, msg, channelPromise);
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

    }

    /*
   * This method tests write() when msg is a WebSocketFrame
   * */
    @Test
    public void testWrite1() throws Exception {
        String publisherClass = "publisherClass";
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getPublisherClass()).thenReturn(publisherClass);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        ChannelPromise channelPromise = Mockito.mock(ChannelPromise.class);
        WebSocketFrame msg = Mockito.mock(WebSocketFrame.class);
        ByteBuf content = Mockito.mock(ByteBuf.class);
        Mockito.when(msg.content()).thenReturn(content);
        WebsocketHandler websocketHandler = new WebsocketHandler() {
            @Override
            protected boolean isAllowed(ChannelHandlerContext ctx, WebSocketFrame msg) throws APIManagementException {
                return true;
            }

            @Override
            protected String getClientIp(ChannelHandlerContext ctx) {
                return "192.168.0.100";
            }
        };
        WebsocketInboundHandler websocketInboundHandler = Mockito.mock(WebsocketInboundHandler.class);
        Mockito.when(websocketInboundHandler.doThrottle(channelHandlerContext, msg)).thenReturn(true);
        websocketHandler.write(channelHandlerContext, msg, channelPromise);
        Assert.assertTrue(true);  // No error has occurred. hence test passes.
    }
}
