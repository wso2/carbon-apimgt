/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.utils.TransportHeaderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TransportHeaderUtil.class})
public class TransportHeaderUtilTest {
    private List<String> standardResponseHeaders = new ArrayList<>();
    private List<String> preserveHeaders = new ArrayList<>();
    private Map<String, String> requestHeaders = new TreeMap();
    private MessageContext messageContext;

    @Before
    public void init() {
        standardResponseHeaders.add("respHeader1");
        standardResponseHeaders.add("respHeader2");
        requestHeaders.put("key", "val");
        requestHeaders.put("key", "val");
        PowerMockito.mockStatic(TransportHeaderUtil.class);
        messageContext = Mockito.mock(Axis2MessageContext.class);
    }

    @Test
    public void removeExcessTransportHeadersWhenExcessHeadersNull() {
        PowerMockito.when(TransportHeaderUtil.getExcessTransportHeaders(messageContext)).thenReturn(null);
        try {
            TransportHeaderUtil.removeExcessTransportHeadersFromList(messageContext, this.standardResponseHeaders);
        } catch (NullPointerException e) {
            Assert.fail("Unexpected NullPointerException is thrown when excessTransportHeaders map is null when "
                                            + "trying to remove ExcessTransportHeadersFromList");
        }
    }

    @Test
    public void removeTransportHeadersFromListWhenTransportHeadersNull() {
        PowerMockito.when(TransportHeaderUtil.getTransportHeaders(messageContext)).thenReturn(null);
        try {
            TransportHeaderUtil.removeTransportHeadersFromList(messageContext, this.standardResponseHeaders);
        } catch (NullPointerException e) {
            Assert.fail("Unexpected NullPointerException is thrown when transportHeaders map is null when "
                                            + "trying to remove TransportHeadersFromList");
        }
    }

    @Test
    public void removeRequestHeadersFromNullResponseHeadersMap() {
        try {
            TransportHeaderUtil.removeRequestHeadersFromResponseHeaders(requestHeaders, null, preserveHeaders);
        } catch (NullPointerException e) {
            Assert.fail("Unexpected NullPointerException is thrown when trying to remove request headers from null "
                                            + "map of response headers");
        }
    }
}

