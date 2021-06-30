package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiConstants;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GatewayUtils.class, Axis2Sender.class})
public class APIStatusHandlerTest {
    @Before
    public void init() {
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.mockStatic(Axis2Sender.class);
    }

    @Test
    public void testHandleRequest() throws Exception {
        API api = Mockito.mock(API.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        APIStatusHandler apiStatusHandler = new APIStatusHandler();
        Mockito.when(api.getStatus()).thenReturn(APIConstants.CREATED);
        Mockito.doNothing().when(messageContext).setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.CREATED);
        Assert.assertTrue(apiStatusHandler.handleRequest(messageContext));
        Mockito.verify(messageContext, Mockito.times(1)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.CREATED);
    }

    @Test
    public void testHandleRequestBlocked() throws Exception {
        API api = Mockito.mock(API.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mediator mediator = Mockito.mock(Mediator.class);
        Mockito.when(messageContext.getSequence(APISecurityConstants.API_BLOCKED_SEQUENCE)).thenReturn(mediator);
        Mockito.when(mediator.mediate(messageContext)).thenReturn(true);
        Mockito.doNothing().when(messageContext).setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.BLOCKED);
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", messageContext);
        APIStatusHandler apiStatusHandler = new APIStatusHandler();
        Mockito.when(api.getStatus()).thenReturn(APIConstants.BLOCKED);
        Assert.assertFalse(apiStatusHandler.handleRequest(messageContext));
        Mockito.verify(messageContext, Mockito.times(1)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.BLOCKED);
        Mockito.verify(mediator, Mockito.times(1)).mediate(messageContext);

    }

    @Test
    public void testHandleRequestBlockedWithoutMediation() throws Exception {
        API api = Mockito.mock(API.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mediator mediator = Mockito.mock(Mediator.class);
        Mockito.when(mediator.mediate(messageContext)).thenReturn(true);
        Mockito.doNothing().when(messageContext).setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.BLOCKED);
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", messageContext);
        APIStatusHandler apiStatusHandler = new APIStatusHandler();
        Mockito.when(api.getStatus()).thenReturn(APIConstants.BLOCKED);
        Assert.assertFalse(apiStatusHandler.handleRequest(messageContext));
        Mockito.verify(messageContext, Mockito.times(1)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.BLOCKED);
        Mockito.verify(mediator, Mockito.times(0)).mediate(messageContext);
    }

    @Test
    public void testHandleRequestPrototyped() throws Exception {
        API api = Mockito.mock(API.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mediator mediator = Mockito.mock(Mediator.class);
        Mockito.when(mediator.mediate(messageContext)).thenReturn(true);
        Mockito.doNothing().when(messageContext).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.PROTOTYPED);
        Mockito.doNothing().when(messageContext).setProperty(APIConstants.API_KEY_TYPE,
                APIConstants.API_KEY_TYPE_PRODUCTION);
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        APIStatusHandler apiStatusHandler = new APIStatusHandler();
        Mockito.when(api.getStatus()).thenReturn(APIConstants.PROTOTYPED);
        Assert.assertTrue(apiStatusHandler.handleRequest(messageContext));
        Mockito.verify(messageContext, Mockito.times(1)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.PROTOTYPED);
        Mockito.verify(messageContext, Mockito.times(1)).setProperty(APIConstants.API_KEY_TYPE,
                APIConstants.API_KEY_TYPE_PRODUCTION);
    }

    @Test
    public void testHandleRequestWhenApiNotFound() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(null);
        APIStatusHandler apiStatusHandler = new APIStatusHandler();
        Assert.assertTrue(apiStatusHandler.handleRequest(messageContext));
    }


}