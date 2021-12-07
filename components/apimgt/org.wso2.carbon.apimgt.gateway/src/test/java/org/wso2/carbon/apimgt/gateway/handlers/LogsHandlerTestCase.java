package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.log4j.MDC;
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
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.TreeMap;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class, MDC.class, System.class})
public class LogsHandlerTestCase {

    @Before
    public void init() {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.mockStatic(MDC.class);
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getProperty("enableCorrelationLogs")).thenReturn("true");
    }

    @Test
    public void testHandleRequestOutFlow() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MC =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MC);

        // For websocket APIs
        Mockito.when(axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(null);
        PowerMockito.when(LogUtils.getTransportHeaders(messageContext)).thenReturn(null);
        PowerMockito.when(MDC.get(APIConstants.CORRELATION_ID)).thenReturn(null);
        PowerMockito.when(LogUtils.getAPIName(messageContext)).thenReturn("Chats:v1.0.0");
        PowerMockito.when(LogUtils.getAPICtx(messageContext)).thenReturn("/chats/1.0.0");
        PowerMockito.when(LogUtils.getRestMethod(messageContext)).thenReturn(null);
        PowerMockito.when(LogUtils.getElectedResource(messageContext)).thenReturn(null);
        PowerMockito.when(LogUtils.getRestReqFullPath(messageContext)).thenReturn("/chats/1.0.0/notifications");
        PowerMockito.when(messageContext.getMessageID()).thenReturn("urn:uuid:"+ UUID.randomUUID().toString());
        PowerMockito.when(LogUtils.getResourceCacheKey(messageContext)).thenReturn(null);

        LogsHandler logsHandler = new LogsHandler();
        Assert.assertTrue(logsHandler.handleResponseOutFlow(messageContext));

        // For normal APIs
        TreeMap<String, String> treeMap = new TreeMap<>();
        // since the all requested header values in the code was not available in normal rest API header object,
        // did not add any values to the map

        PowerMockito.when(axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(treeMap);
        PowerMockito.when(LogUtils.getTransportHeaders(messageContext)).thenReturn(treeMap);
        String uuid = UUID.randomUUID().toString();
        PowerMockito.when(MDC.get(APIConstants.CORRELATION_ID)).thenReturn(uuid);
        PowerMockito.when(LogUtils.getAPIName(messageContext)).thenReturn("PizzaShackAPI:v1.0.0");
        PowerMockito.when(LogUtils.getAPICtx(messageContext)).thenReturn("/pizzashack/1.0.0");
        PowerMockito.when(LogUtils.getRestMethod(messageContext)).thenReturn("GET");
        PowerMockito.when(LogUtils.getElectedResource(messageContext)).thenReturn("/menu");
        PowerMockito.when(LogUtils.getRestReqFullPath(messageContext)).thenReturn("/pizzashack/1.0.0/menu");
        PowerMockito.when(messageContext.getMessageID()).thenReturn("urn:uuid:"+uuid);
        PowerMockito.when(LogUtils.getResourceCacheKey(messageContext)).thenReturn("/pizzashack/1.0.0/1.0.0/menu:GET");

        Assert.assertTrue(logsHandler.handleResponseOutFlow(messageContext));
    }
}
