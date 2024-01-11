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
