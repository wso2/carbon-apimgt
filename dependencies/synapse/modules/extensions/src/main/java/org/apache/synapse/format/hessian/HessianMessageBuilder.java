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

package org.apache.synapse.format.hessian;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.SynapseBinaryDataSource;

import javax.activation.DataHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Enables a message encoded using the Hessian binary protocol to be received by axis2/synapse
 * and this builds the HessianDataSource to represent the hessian message inside the SOAP info-set
 *
 * @see org.apache.axis2.builder.Builder
 * @see org.apache.synapse.util.SynapseBinaryDataSource
 */
public class HessianMessageBuilder implements Builder {

    private static final Log log = LogFactory.getLog(HessianMessageBuilder.class);

    /**
     * Returns an OMElement from a Hessian encoded message
     *
     * @param inputStream stream containing the Hessian message to be built
     * @param contentType content type of the message
     * @param messageContext message to which the hessian message has to be attached
     * @return OMElement containing Hessian data handler keeping the message
     * @throws AxisFault in case of a failure in building the hessian message
     *
     * @see org.apache.axis2.builder.Builder#processDocument(java.io.InputStream,
     * String, org.apache.axis2.context.MessageContext)
     */
    public OMElement processDocument(final InputStream inputStream, final String contentType,
            final MessageContext messageContext) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Start building the hessian message in to a HessianDataSource");
        }

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(HessianConstants.HESSIAN_NAMESPACE_URI,
                HessianConstants.HESSIAN_NS_PREFIX);
        OMElement element = factory.createOMElement(
                HessianConstants.HESSIAN_ELEMENT_LOCAL_NAME, ns);

        try {

            Parameter synEnv = messageContext.getConfigurationContext().getAxisConfiguration()
                    .getParameter(SynapseConstants.SYNAPSE_ENV);

            PushbackInputStream pis = detectAndMarkMessageFault(messageContext, inputStream);

            DataHandler dataHandler;
            if (synEnv != null && synEnv.getValue() != null) {
                dataHandler = new DataHandler(new SynapseBinaryDataSource(pis, contentType,
                        (SynapseEnvironment) synEnv.getValue()));
            } else {
                // add Hessian data inside a data handler
                dataHandler = new DataHandler(new SynapseBinaryDataSource(pis, contentType));
            }
            OMText textData = factory.createOMText(dataHandler, true);
            element.addChild(textData);
            
            // indicate that message faults shall be handled as http 200
            messageContext.setProperty(NhttpConstants.FAULTS_AS_HTTP_200, NhttpConstants.TRUE);

        } catch (IOException e) {
            String msg = "Unable to create the HessianDataSource";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Building the hessian message using HessianDataSource is successful");
        }

        return element;
    }

    /**
     * Reads the first four bytes of the inputstream to detect whether the message represents a
     * fault message. Once a fault message has been detected, a property used to mark fault messages
     * is stored in the Axis2 message context. The implementaton uses a PushbackInputStream to be
     * able to put those four bytes back at the end of processing.
     *
     * @param   messageContext  the Axis2 message context
     * @param   inputStream     the inputstream to read the Hessian message
     *
     * @return  the wrapped (pushback) input stream
     *
     * @throws  IOException  if an I/O error occurs
     */
    private PushbackInputStream detectAndMarkMessageFault(final MessageContext messageContext,
            final InputStream inputStream) throws IOException {

        int bytesToRead = 4;
        PushbackInputStream pis = new PushbackInputStream(inputStream, bytesToRead);
        byte[] headerBytes = new byte[bytesToRead];
        int n = pis.read(headerBytes);

        // checking fourth byte for fault marker
        if (n == bytesToRead) {
            if (headerBytes[bytesToRead - 1] == HessianConstants.HESSIAN_V1_FAULT_IDENTIFIER
                    || headerBytes[bytesToRead - 1] == HessianConstants.HESSIAN_V2_FAULT_IDENTIFIER) {
                messageContext.setProperty(BaseConstants.FAULT_MESSAGE, SynapseConstants.TRUE);
                if (log.isDebugEnabled()) {
                    log.debug("Hessian fault detected, marking in Axis2 message context");
                }
            }
            pis.unread(headerBytes);
        } else if (n > 0) {
                byte[] bytesRead = new byte[n];
                System.arraycopy(headerBytes, 0, bytesRead, 0, n);
                pis.unread(bytesRead);
        }
        return pis;
    }
}