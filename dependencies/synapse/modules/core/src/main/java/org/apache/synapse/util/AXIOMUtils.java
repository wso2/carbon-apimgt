/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.util;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

/**
 * Utility class with AXIOM helper methods.
 */
public class AXIOMUtils {
    private AXIOMUtils() {}
    
    /**
     * Get a {@link Source} backed by a given AXIOM node.
     * 
     * @param node an AXIOM node
     * @return a {@link Source} object that can be used with XSL transformers,
     *         schema validators, etc.
     */
    public static Source asSource(OMNode node) {
        // Note: Once we depend on JDK 1.6, we could also use StAXSource from JAXP 1.4.
        if (node instanceof NodeImpl) {
            return new DOMSource((NodeImpl)node);
        } else {
            return new OMSource((OMElement)node);
        }
    }

    /**
     * @param omElement OMElement which need to convert to SOAPEnvelope
     * @return SOAPEnvelope with contents of passed OMElement
     */
    public static SOAPEnvelope getSOAPEnvFromOM(OMElement omElement) {
        SOAPFactory soapFactory;
        if (omElement.getQName().getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        }

        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(omElement.getXMLStreamReader(),
                soapFactory, omElement.getQName().getNamespaceURI());
        return builder.getSOAPEnvelope();
    }
}
