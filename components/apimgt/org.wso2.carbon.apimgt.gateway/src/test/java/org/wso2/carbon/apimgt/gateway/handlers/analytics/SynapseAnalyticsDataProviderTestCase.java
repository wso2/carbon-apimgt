
/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

public class SynapseAnalyticsDataProviderTestCase {

    @Test
    public void testMetricsWhenMessageContextPropertiesAreNull() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        AnalyticsCustomDataProvider analyticsCustomDataProvider = Mockito.mock(AnalyticsCustomDataProvider.class);
        SynapseAnalyticsDataProvider synapseAnalyticsDataProvider = new SynapseAnalyticsDataProvider(messageContext,
                analyticsCustomDataProvider);
        Mockito.when((messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS))).thenReturn(null);
        Mockito.when((messageContext.getProperty(SynapseConstants.HTTP_SC))).thenReturn(null);
        Mockito.when((messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY))).thenReturn(null);
        Mockito.when((messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY))).thenReturn(null);
        Assert.assertEquals(APIMgtGatewayConstants.DUMMY_ENDPOINT_ADDRESS,
                synapseAnalyticsDataProvider.getTarget().getDestination());
        Assert.assertEquals(200, synapseAnalyticsDataProvider.getProxyResponseCode());
        Assert.assertEquals(0, synapseAnalyticsDataProvider.getBackendLatency());
        Assert.assertEquals(0, synapseAnalyticsDataProvider.getResponseMediationLatency());
    }
}
