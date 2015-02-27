/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.synapse.core.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.custommonkey.xmlunit.XMLTestCase;

public class SOAPConversionTest extends XMLTestCase {

    public void testSOAP11To12Conversion() throws Exception {
        MessageContext msgCtx = TestUtils.getAxis2MessageContext("<test/>", null).
                getAxis2MessageContext();
        msgCtx.setEnvelope(getSOAP11Envelope());
        SOAPUtils.convertSOAP11toSOAP12(msgCtx);

        assertXMLEqual(getSOAP12Envelope().toString(), msgCtx.getEnvelope().toString());
    }

    public void testSOAP12To11Conversion() throws Exception {
        MessageContext msgCtx = TestUtils.getAxis2MessageContext("<test/>", null).
                getAxis2MessageContext();
        msgCtx.setEnvelope(getSOAP12Envelope());
        SOAPUtils.convertSOAP12toSOAP11(msgCtx);

        assertXMLEqual(getSOAP11Envelope().toString(), msgCtx.getEnvelope().toString());
    }

    private SOAPEnvelope getSOAP11Envelope() {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(createPayload(fac));
        populateHeader(envelope, fac);
        return envelope;
    }

    private SOAPEnvelope getSOAP12Envelope() {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(createPayload(fac));
        populateHeader(envelope, fac);
        return envelope;
    }

    private void populateHeader(SOAPEnvelope envelope, SOAPFactory fac) {
        OMNamespace ns = fac.createOMNamespace("http://custom.header.com", "syn");
        envelope.getHeader().addHeaderBlock("Foo", ns);
        envelope.getHeader().addHeaderBlock("Bar", ns);
    }

    private OMElement createPayload(SOAPFactory fac) {
        OMNamespace ns = fac.createOMNamespace("http://samples.services", "m0");
        OMElement getQuote = fac.createOMElement("getQuote", ns);
        OMElement request = fac.createOMElement("request", ns);
        request.setText("IBM");
        getQuote.addChild(request);
        return getQuote;
    }
}
