package org.wso2.carbon.apimgt.gateway.handlers;

import com.google.common.net.HttpHeaders;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl;
import org.apache.axiom.soap.impl.dom.SOAPHeaderImpl;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Test class for Utils
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class, ServiceReferenceHolder.class, Axis2Sender.class, OMAbstractFactory.class})
public class UtilsTestCase {
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgCntxt;
    private Map headers;

    @Before
    public void setup() {

        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME)).thenReturn("1506576365");
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(Axis2Sender.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        try {
            PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", messageContext);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception is thrown while setup Utils test cases");
        }

        headers = Mockito.mock(Map.class);
    }

    @Test
    public void testSendFault() {
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Utils.sendFault(messageContext, 200);
    }

    @Test
    public void testSetFaultPayload() {
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(messageContext.getProperty("error_message_type"))
                .thenReturn("error_message_type");
        OMElement omElement = Mockito.mock(OMElement.class);
        SOAPEnvelope soapEnvelope = Mockito.mock(SOAPEnvelope.class);
        SOAPBody soapBody = Mockito.mock(SOAPBody.class);
        Mockito.when(messageContext.getEnvelope()).thenReturn(soapEnvelope);
        Mockito.when(soapEnvelope.getBody()).thenReturn(soapBody);
        Utils.setFaultPayload(messageContext, omElement);
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

        //when acceptType != null
        Mockito.when(headers.get(HttpHeaders.ACCEPT)).thenReturn("text/html");
        Utils.setFaultPayload(messageContext, omElement);
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

    }

    @Test
    public void testHasAccessTokenExpired() {
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
        Assert.assertTrue(Utils.hasAccessTokenExpired(apiKeyValidationInfoDTO));

        //test for active access token by seting timestamp to 1027/10/13 0:9:18 GMT
        Mockito.when(apiKeyValidationInfoDTO.getIssuedTime()).thenReturn(1823386158000L);
        Mockito.when(apiKeyValidationInfoDTO.getEndUserToken()).thenReturn("endusertoken");
        Assert.assertFalse(Utils.hasAccessTokenExpired(apiKeyValidationInfoDTO));

        //test when accessTokenDO.getValidityPeriod() == Long.MAX_VALUE
        Mockito.when(apiKeyValidationInfoDTO.getValidityPeriod()).thenReturn(Long.MAX_VALUE);
        Assert.assertFalse(Utils.hasAccessTokenExpired(apiKeyValidationInfoDTO));

    }

    @Test
    public void testGetRequestPath() {
        Assert.assertEquals("/abcd", Utils.getRequestPath(messageContext, "/ishara/abcd",
                "/ishara", "1.0"));

        // if version strategy is url
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn("url");
        Assert.assertEquals("/abcd", Utils.getRequestPath(messageContext, "/ishara/1.0/abcd",
                "/ishara", "1.0"));

    }

    @Test
    public void testSend() {
        Utils.send(messageContext, 200);
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

    }

    @Test
    public void testSetSOAPFault() {
        SOAPEnvelope soapEnvelope = Mockito.mock(SOAPEnvelope.class);
        SOAPHeader soapHeader = Mockito.mock(SOAPHeader.class);
        Iterator iterator = Mockito.mock(Iterator.class);
        OMNode soapHeaderBlock = Mockito.mock(OMNode.class);
        Mockito.when(messageContext.getMessageID()).thenReturn("123");
        Mockito.when(messageContext.getEnvelope()).thenReturn(soapEnvelope);
        Mockito.when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        Mockito.when(soapHeader.examineAllHeaderBlocks()).thenReturn(iterator);
        Mockito.when(iterator.hasNext()).thenReturn(true, false);
//        Mockito.when(iterator.next()).thenReturn(omelement);
        Mockito.when(iterator.next()).thenReturn(soapHeaderBlock);
        Utils.setSOAPFault(messageContext, "", "code", "detail");
        Assert.assertTrue(true);  // No error has occurred. hence test passes.
        
        //when messageContext.isSOAP11() is true
        Mockito.when(messageContext.isSOAP11()).thenReturn(true);
        Utils.setSOAPFault(messageContext, "", "code", "detail");
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

        // when messageContext.getFaultTo() != null
        EndpointReference endpointReference = Mockito.mock(EndpointReference.class);
        Mockito.when(messageContext.getFaultTo()).thenReturn(endpointReference);
        Utils.setSOAPFault(messageContext, "", "code", "detail");
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

        // when messageContext.getFaultTo() != null
        Mockito.when(messageContext.getFaultTo()).thenReturn(endpointReference);
        Utils.setSOAPFault(messageContext, "", "code", "detail");
        Assert.assertTrue(true);  // No error has occurred. hence test passes.

    }
}
