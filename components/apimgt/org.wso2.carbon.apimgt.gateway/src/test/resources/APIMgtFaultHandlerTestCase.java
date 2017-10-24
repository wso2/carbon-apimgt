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

package org.wso2.carbon.apimgt.gateway.handlers.handlers;

import org.apache.synapse.MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtFaultHandler;

/**
 * Test class for APIMgtFaultHandler
 */
public class APIMgtFaultHandlerTestCase {

    @Test
    public void testMediate() throws Exception {
        APIMgtFaultHandler apiMgtFaultHandler = createAPIMgtFaultHandler(false, false);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        //Test for analytics disable path
        Assert.assertTrue(apiMgtFaultHandler.mediate(messageContext));

        APIMgtFaultHandler apiMgtFaultHandler1 = createAPIMgtFaultHandler(false, true);
        //Test for analytics enabled and  skipEventReceiverConnection disabled path
        Assert.assertTrue(apiMgtFaultHandler1.mediate(messageContext));

        //Test for analytics enabled and  skipEventReceiverConnection en
        // abled path
        APIMgtFaultHandler apiMgtFaultHandler2 = createAPIMgtFaultHandler(true, true);
        Assert.assertTrue(apiMgtFaultHandler2.mediate(messageContext));

        //Test for analytics enabled and  skipEventReceiverConnection disabled path
        APIMgtFaultHandler apiMgtFaultHandler3 = createAPIMgtFaultHandler(true, false);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME)).thenReturn("564321");
        apiMgtFaultHandler3.isContentAware();
        // Test for test mediate is not failed even an exception is thrown
        Assert.assertTrue(apiMgtFaultHandler3.mediate(messageContext));

    }

    private APIMgtFaultHandler createAPIMgtFaultHandler(final boolean isAnalyticsEnabled, final boolean isSkipEventReceiverConnection) {
        return new APIMgtFaultHandler() {
            @Override
            protected void initDataPublisher() {
                enabled = isAnalyticsEnabled;
                skipEventReceiverConnection = isSkipEventReceiverConnection;
            }

            @Override
            protected String getTenantDomainFromRequestURL(String fullRequestPath) {
                return "carbon.super";
            }
        };
    }
}
