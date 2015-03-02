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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.SynapseBinaryDataSource;

import javax.activation.DataHandler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * Enables a message encoded using the Hessian binary protocol to be written to transport by
 * axis2/synapse and this formats the HessianDataSource to a Hessian message.
 *
 * @see org.apache.axis2.transport.MessageFormatter
 * @see org.apache.synapse.util.SynapseBinaryDataSource
 */
public class HessianMessageFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(HessianMessageFormatter.class);

    /**
     * Formats the content type to be written in to the transport
     *
     * @param msgCtxt message of which the content type has to be formatted
     * @param format fomat of the expected formatted message
     * @param soapActionString soap action of the message
     * 
     * @return contentType formatted content type as a String
     */
    public String getContentType(MessageContext msgCtxt, OMOutputFormat format,
            String soapActionString) {

        String contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        if (contentType == null) {
            contentType = HessianConstants.HESSIAN_CONTENT_TYPE;
        }

        String encoding = format.getCharSetEncoding();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        return contentType;
    }

    /**
     * Extract Hessian bytes from the received SOAP message and write it onto the wire
     *
     * @param msgCtx message from which the Hessian message has to be extracted
     * @param format message format to be written
     * @param out stream to which the message is written
     * @param preserve whether to preserve the indentations
     * 
     * @throws AxisFault in case of a failure in writing the message to the provided stream
     */
    public void writeTo(MessageContext msgCtx, OMOutputFormat format, OutputStream out,
            boolean preserve) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Start writing the Hessian message to OutputStream");
        }
        
        // Check whether the message to be written is a fault message
        if (msgCtx.getFLOW() == MessageContext.OUT_FAULT_FLOW || msgCtx.getEnvelope().hasFault()) {
            
            SOAPFault soapFault = msgCtx.getEnvelope().getBody().getFault();
            convertAndWriteHessianFault(soapFault, out);
        } else {
            
            // no differentiation between normal reply and fault (pass the original message through)
            writeHessianMessage(msgCtx, out);
        }

        if (log.isDebugEnabled()) {
            log.debug("Writing message as a Hessian message is successful");
        }
    }

    /**
     * This method is not supported because of large file handling limitations
     *
     * @param msgCtxt message which contains the Hessian message inside the HessianDataSource
     * @param format message format to be written
     * 
     * @return Hessian binary bytes of the message
     * 
     * @throws AxisFault for any invocation
     */
    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format) throws AxisFault {
        throw new AxisFault("Method not supported. Use the "
                + "HessianMessageFormatter#writeTo method instead");
    }

    /**
     * {@inheritDoc}
     * 
     * Simply returns the soapAction unchanged.
     */
    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format,
            String soapAction) {

        return soapAction;
    }

    /**
     * {@inheritDoc}
     * 
     * @return A templated URL based on the given target URL. 
     */
    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL)
            throws AxisFault {

        return URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);
    }

    /**
     * Writes the Hessian message contained in the message context to the provided output stream.
     * 
     * @param msgCtxt the message context containing the Hessian message
     * @param out the provided output stream to which the message shall be written
     * 
     * @throws AxisFault if an error occurs writing to the output stream
     */
    private void writeHessianMessage(MessageContext msgCtxt, OutputStream out) throws AxisFault {
 
        OMElement omElement = msgCtxt.getEnvelope().getBody().getFirstElement();
        SynapseBinaryDataSource synapseBinaryDataSource = extractSynapseBinaryDataSource(omElement);

        if (synapseBinaryDataSource != null) {

            InputStream inputStream = null;
            try {
                inputStream = synapseBinaryDataSource.getInputStream();
                IOUtils.copy(inputStream, out);
            } catch (IOException e) {
                handleException("Couldn't get the bytes from the HessianDataSource", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignore) {
                        log.warn("Error closing input stream.", ignore);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignore) {
                        log.warn("Error closing output stream.", ignore);
                    }
                }
            }

        } else {
            handleException("Unable to find the Hessian content in the payload");
        }
    }

    /**
     * Tries to extract the binary data source containing the Hessian message.
     * 
     * @param omElement
     * 
     * @return the binary data source containing the Hessian message or null, if the OMElement
     *         does not contain a binary datasource.
     */
    private SynapseBinaryDataSource extractSynapseBinaryDataSource(OMElement omElement) {
        
        SynapseBinaryDataSource synapseBinaryDataSource = null;
        Iterator it = omElement.getChildren();

        while (it.hasNext() && synapseBinaryDataSource == null) {

            OMNode hessianElement = (OMNode) it.next();
            if (hessianElement instanceof OMText) {

                OMText tempNode = (OMText) hessianElement;
                if (tempNode.getDataHandler() != null
                        && ((DataHandler) tempNode.getDataHandler()).getDataSource() instanceof SynapseBinaryDataSource) {

                    synapseBinaryDataSource = (SynapseBinaryDataSource) ((DataHandler) tempNode
                            .getDataHandler()).getDataSource();
                }
            }
        }

        return synapseBinaryDataSource;
    }

    /**
     * Reads details from the SOAPFault and creates a new Hessian fault using those details and
     * writes it to the output stream.
     * 
     * @param soapFault the SOAP fault to convert and write as a Hessian fault
     * @param out the output stream to write the Hessian fault to
     * 
     * @throws AxisFault if an error occurs writing the message to the output stream
     */
    private void convertAndWriteHessianFault(SOAPFault soapFault, OutputStream out) throws AxisFault {

        BufferedOutputStream faultOutStream = new BufferedOutputStream(out);

        try {
            String hessianFaultCode = "500";
            String hessianFaultMessage = "";
            String hessianFaultDetail = "";
            
            if (soapFault.getCode() != null) {
                hessianFaultCode = soapFault.getCode().getText();
            }
            
            if (soapFault.getReason() != null) {
                hessianFaultMessage = soapFault.getReason().getText();
            }

            if (soapFault.getDetail() != null) {
                hessianFaultDetail = soapFault.getDetail().getText();
            }            

            HessianUtils.writeFault(hessianFaultCode, hessianFaultMessage, hessianFaultDetail,
                    faultOutStream);
            faultOutStream.flush();

        } catch (IOException e) {
            handleException("Unalbe to write the fault as a Hessian message", e);
        } finally {
            try {
                if (faultOutStream != null) {
                    faultOutStream.close();
                }
            } catch (IOException ignore) {
                log.warn("Error closing output stream.", ignore);
            }
        }
    }

    /**
     * Logs the original exception, wrappes it in an AxisFault and rethrows it.
     * 
     * @param msg the error message
     * @param e the original exception
     * 
     * @throws AxisFault 
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
    
    /**
     * Logs an error message and throws a newly created AxisFault. 
     * 
     * @param msg the error message
     * 
     * @throws AxisFault 
     */
    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }
}
