package org.apache.synapse.format.hessian;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class to support both HessianMessageBuilder and HessianMessageFormatter tests.
 */
public class HessianTestHelper {
    
    static final String HESSIAN_DUMMY_FAULT_V1_RESPONSE = "hessianDummyFaultResponse_V1.bin";

    static final String HESSIAN_DUMMY_REQUEST = "hessianDummyRequest.bin";

    static final String HESSIAN_INCOMPLETE = "hessianIncomplete.bin";
    
    static final String CHARSET_ENCODING = "UTF-8";

    public MessageContext createAxis2MessageContext(SynapseEnvironment synEnv) throws AxisFault {
        MessageContext msgContext = new MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        if (synEnv != null) {
            axisConfig.addParameter(SynapseConstants.SYNAPSE_ENV, synEnv);
        }
        msgContext.setConfigurationContext(configContext);

        return msgContext;
    }

    public void addBodyToMessageContext(MessageContext msgContext, OMElement element) throws AxisFault { 
    
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        SOAPBody body = envelope.getBody();
        body.addChild(element);
        msgContext.setEnvelope(envelope);
    }
    
    public void addSoapFaultToMessageContext(MessageContext msgContext, String faultCode, 
            String faultReason, String faultDetail) throws AxisFault {
        
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();       
        SOAPFault soapFault = faultEnvelope.getBody().getFault();

        SOAPFaultCode soapFaultCode = factory.createSOAPFaultCode();
        soapFaultCode.setText(faultCode);
        soapFault.setCode(soapFaultCode);
        
        SOAPFaultReason soapFaultReason = factory.createSOAPFaultReason();
        soapFaultReason.setText(faultReason);
        soapFault.setReason(soapFaultReason);
        
        SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
        soapFaultDetail.setText(faultDetail);
        soapFault.setDetail(soapFaultDetail);
        
        msgContext.setEnvelope(faultEnvelope);
    }
    
    public byte[] getTestMessageAsBytes(String testMessageName) throws IOException {
        
        InputStream is = getClass().getResourceAsStream(testMessageName);
        return IOUtils.toByteArray(is);
    }
    

    public OMElement buildHessianTestMessage(String testMessageName, MessageContext msgContext)
            throws AxisFault {

        HessianMessageBuilder messageBuilder = new HessianMessageBuilder();
        InputStream is = getClass().getResourceAsStream(testMessageName);
        OMElement element = messageBuilder.processDocument(is,
                HessianConstants.HESSIAN_CONTENT_TYPE, msgContext);

        return element;
    }
}
