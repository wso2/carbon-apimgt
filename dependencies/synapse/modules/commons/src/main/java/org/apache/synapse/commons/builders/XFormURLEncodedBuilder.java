package org.apache.synapse.commons.builders;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MultipleEntryHashMap;

import javax.xml.namespace.QName;
import java.io.InputStream;

/**
 * Synapse specific message builder for "application/x-www-form-urlencoded" content type. This
 * builder extends the functionality provided by the Axis2 builder.
 */
public class XFormURLEncodedBuilder implements Builder {

    private org.apache.axis2.builder.XFormURLEncodedBuilder
            xformAxis2Builder = new org.apache.axis2.builder.XFormURLEncodedBuilder();

    private static final QName XFORM_FIRST_ELEMENT = new QName("xformValues");

    public OMElement processDocument(InputStream inputStream, String s,
                                     MessageContext messageContext) throws AxisFault {
        // first process the input stream using the Axis2 builder
        SOAPEnvelope soapEnv = (SOAPEnvelope) xformAxis2Builder.processDocument(inputStream,
                s, messageContext);

        // when this is a POST request, if the body of the soap envelope is empty and the parameter
        // map is there, build a dummy soap body which contains all the parameters coming in.
        SOAPBody body = soapEnv.getBody();
        String httpMethod = (String) messageContext.getProperty(HTTPConstants.HTTP_METHOD);
        if (body.getFirstElement() == null && HTTPConstants.HTTP_METHOD_POST.equals(httpMethod) &&
                messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP) != null) {
            MultipleEntryHashMap map = (MultipleEntryHashMap) messageContext
                    .getProperty(Constants.REQUEST_PARAMETER_MAP);
            SOAPFactory soapFactory = getSOAPFactory(messageContext);
            OMElement bodyFirstChild = soapFactory
                    .createOMElement(XFORM_FIRST_ELEMENT, body);
            BuilderUtil.createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild, map);
        }
        return soapEnv;
    }

    private SOAPFactory getSOAPFactory(MessageContext messageContext) throws AxisFault {
        SOAPFactory soapFactory;
        AxisEndpoint axisEndpoint = (AxisEndpoint) messageContext
                .getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (axisEndpoint != null) {
            AxisBinding axisBinding = axisEndpoint.getBinding();
            String soapVersion =
                    (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            soapFactory = getSOAPFactory(soapVersion);
        } else {
            soapFactory = getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }
        return soapFactory;
    }

    private SOAPFactory getSOAPFactory(String nsURI) throws AxisFault {
        if (nsURI == null) {
            return OMAbstractFactory.getSOAP12Factory();
        }
        else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }

}
