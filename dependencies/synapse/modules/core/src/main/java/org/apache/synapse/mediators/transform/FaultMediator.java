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

package org.apache.synapse.mediators.transform;

import org.apache.axiom.om.*;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * This transforms the current message instance into a SOAP Fault message. The
 * SOAP version for the fault message could be explicitly specified. Else if the
 * original message was SOAP 1.1 the fault will also be SOAP 1.1 else, SOAP 1.2
 *
 * This class exposes methods to set SOAP 1.1 and 1.2 fault elements and uses
 * these as required.
 *
 * Directs the fault messages' "To" EPR to the "FaultTo" or the "ReplyTo" or to
 * null of the original SOAP message
 */
public class FaultMediator extends AbstractMediator {

    public static final String WSA_ACTION = "Action";
    /** Make a SOAP 1.1 fault */
    public static final int SOAP11 = 1;
    /** Make a SOAP 1.2 fault */
    public static final int SOAP12 = 2;
    /** Make a POX fault */
    public static final int POX = 3;
    /** Holds the SOAP version to be used to make the fault, if specified */
    private int soapVersion;
    /** Whether to mark the created fault as a response or not */
    private boolean markAsResponse = true;
    /** Whether it is required to serialize the response attribute or not */
    private boolean serializeResponse = false;

    // -- fault elements --
    /** The fault code QName to be used */
    private QName faultCodeValue = null;
    /** An XPath expression that will give the fault code QName at runtime */
    private SynapseXPath faultCodeExpr = null;
    /** The fault reason to be used */
    private String faultReasonValue = null;
    /** An XPath expression that will give the fault reason string at runtime */
    private SynapseXPath faultReasonExpr = null;
    /** The fault node URI to be used */
    private URI faultNode = null;
    /** The fault role URI to be used - if applicable */
    private URI faultRole = null;
    /** The fault detail to be used */
    private String faultDetail = null;
    /** An XPath expression that will give the fault code QName at runtime */    
    private SynapseXPath faultDetailExpr = null;    
    /** array of fault detail elements */
    private final List<OMElement> faultDetailElements = new ArrayList<OMElement>();

    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Fault mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        switch (soapVersion) {
            case SOAP11:
                makeSOAPFault(synCtx, SOAP11, synLog);
                break;
            case SOAP12:
                makeSOAPFault(synCtx, SOAP12, synLog);
                break;
            case POX:
                makePOXFault(synCtx, synLog);
                break;

            default : {
                // if this is a POX or REST message then make a POX fault
                if (synCtx.isDoingPOX() || synCtx.isDoingGET()) {

                    makePOXFault(synCtx, synLog);

                } else {

                    // determine from current message's SOAP envelope namespace
                    SOAPEnvelope envelop = synCtx.getEnvelope();
                    if (envelop != null) {

                        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                            envelop.getNamespace().getNamespaceURI())) {
                            makeSOAPFault(synCtx, SOAP12, synLog);

                        } else {
                            makeSOAPFault(synCtx, SOAP11, synLog);
                        }

                    } else {
                        // default to SOAP 11
                        makeSOAPFault(synCtx, SOAP11, synLog);
                    }
                }
            }
        }
        
		final Pipe pipe =
		                  (Pipe) (((Axis2MessageContext) synCtx)).getAxis2MessageContext()
		                                                         .getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
		if (pipe != null) {
			// cleaning the OUTPUT PIPE with older references
			// if there is a [protocal violation when sending out message etc.]
			pipe.getBuffer().clear();
			pipe.resetOutputStream();
		}

        // if the message has to be marked as a response mark it as response
        if (markAsResponse) {
            synCtx.setResponse(true);
            synCtx.setTo(synCtx.getReplyTo());
        }
        
        return true;
    }

    private void makePOXFault(MessageContext synCtx, SynapseLog synLog) {

        OMFactory fac = synCtx.getEnvelope().getOMFactory();
        OMElement faultPayload = fac.createOMElement(new QName("Exception"));

        if (faultDetail != null) {

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Setting the fault detail : "
                    + faultDetail + " as the POX Fault");
            }

            faultPayload.setText(faultDetail);

        } else if (faultDetailExpr != null) {

            String faultDetail = faultDetailExpr.stringValueOf(synCtx);

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Setting the fault detail : "
                        + faultDetail + " as the POX Fault");
            }

            faultPayload.setText(faultDetail);
            
        } else if (faultReasonValue != null) {

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Setting the fault reason : "
                    + faultReasonValue + " as the POX Fault");
            }

            faultPayload.setText(faultReasonValue);

        } else if (faultReasonExpr != null) {

            String faultReason = faultReasonExpr.stringValueOf(synCtx);
            faultPayload.setText(faultReason);

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Setting the fault reason : "
                    + faultReason + " as the POX Fault");
            }
        }

        SOAPBody body = synCtx.getEnvelope().getBody();
        if (body != null) {

            if (body.getFirstElement() != null) {
                body.getFirstElement().detach();
            }

            synCtx.setFaultResponse(true);
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProcessingFault(true);

            if (synLog.isTraceOrDebugEnabled()) {
                String msg = "Original SOAP Message : " + synCtx.getEnvelope().toString() +
                    "POXFault Message created : " + faultPayload.toString();
                synLog.traceTrace(msg);
                if (log.isTraceEnabled()) {
                    log.trace(msg);
                }
            }
            
            body.addChild(faultPayload);
        }
    }

    /**
     * Actual transformation of the current message into a fault message
     * @param synCtx the current message context
     * @param soapVersion SOAP version of the resulting fault desired
     * @param synLog the Synapse log to use
     */
    private void makeSOAPFault(MessageContext synCtx, int soapVersion,
        SynapseLog synLog) {

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Creating a SOAP "
                    + (soapVersion == SOAP11 ? "1.1" : "1.2") + " fault");
        }

        // get the correct SOAP factory to be used
        SOAPFactory factory = (soapVersion == SOAP11 ?
                OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory());

        // create the SOAP fault document and envelope
        OMDocument soapFaultDocument = factory.createOMDocument();
        SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();
        soapFaultDocument.addChild(faultEnvelope);

        // create the fault element  if it is need
        SOAPFault fault = faultEnvelope.getBody().getFault();
        if (fault == null) {
            fault = factory.createSOAPFault();
        }

        // populate it
        setFaultCode(synCtx, factory, fault, soapVersion);
        setFaultReason(synCtx, factory, fault, soapVersion);
        setFaultNode(factory, fault);
        setFaultRole(factory, fault);
        setFaultDetail(synCtx, factory, fault);

        // set the all headers of original SOAP Envelope to the Fault Envelope
        if (synCtx.getEnvelope() != null) {
            SOAPHeader soapHeader = synCtx.getEnvelope().getHeader();
            if (soapHeader != null) {
                for (Iterator iter = soapHeader.examineAllHeaderBlocks(); iter.hasNext();) {
                    Object o = iter.next();
                    if (o instanceof SOAPHeaderBlock) {
                        SOAPHeaderBlock header = (SOAPHeaderBlock) o;
                        faultEnvelope.getHeader().addChild(header);
                    } else if (o instanceof OMElement) {
                        faultEnvelope.getHeader().addChild((OMElement) o);
                    }
                }
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            String msg =
                "Original SOAP Message : " + synCtx.getEnvelope().toString() +
                "Fault Message created : " + faultEnvelope.toString();
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace(msg);
            }
            if (log.isTraceEnabled()) {
                log.trace(msg);
            }
        }

        // overwrite current message envelope with new fault envelope
        try {
            synCtx.setEnvelope(faultEnvelope);
        } catch (AxisFault af) {
            handleException("Error replacing current SOAP envelope " +
                    "with the fault envelope", af, synCtx);
        }

        if (synCtx.getFaultTo() != null) {
            synCtx.setTo(synCtx.getFaultTo());
        } else if (synCtx.getReplyTo() != null) {
            synCtx.setTo(synCtx.getReplyTo());
        } else {
            synCtx.setTo(null);
        }

        // set original messageID as relatesTo
        if(synCtx.getMessageID() != null) {
            RelatesTo relatesTo = new RelatesTo(synCtx.getMessageID());
            synCtx.setRelatesTo(new RelatesTo[] { relatesTo });
        }

        synLog.traceOrDebug("End : Fault mediator");
    }

    private void setFaultCode(MessageContext synCtx, SOAPFactory factory, SOAPFault fault, int soapVersion) {

        QName fault_code = null;

        if (faultCodeValue == null && faultCodeExpr == null) {
            handleException("A valid fault code QName value or expression is required", synCtx);
        } else if (faultCodeValue != null) {
            fault_code = faultCodeValue;
        } else {
            String codeStr =  faultCodeExpr.stringValueOf(synCtx);
            fault_code = new QName(fault.getNamespace().getNamespaceURI(),codeStr);
        }

        SOAPFaultCode code = factory.createSOAPFaultCode();
        switch (soapVersion) {
            case SOAP11:
                code.setText(fault_code);
                break;
            case SOAP12:
                SOAPFaultValue value = factory.createSOAPFaultValue(code);
                value.setText(fault_code);
                break;
        }
        fault.setCode(code);
    }

    private void setFaultReason(MessageContext synCtx, SOAPFactory factory, SOAPFault fault, int soapVersion) {
        String reasonString = null;

        if (faultReasonValue == null && faultReasonExpr == null) {
            handleException("A valid fault reason value or expression is required", synCtx);
        } else if (faultReasonValue != null) {
            reasonString = faultReasonValue;
        } else {
            reasonString = faultReasonExpr.stringValueOf(synCtx);
        }

        SOAPFaultReason reason = factory.createSOAPFaultReason();
        switch(soapVersion) {
            case SOAP11:
                reason.setText(reasonString);
                break;
            case SOAP12:
                SOAPFaultText text = factory.createSOAPFaultText();
                text.setText(reasonString);
                text.setLang("en");
                reason.addSOAPText(text);
                break;
        }
        fault.setReason(reason);
    }

    private void setFaultNode(SOAPFactory factory, SOAPFault fault) {
        if (faultNode != null) {
            SOAPFaultNode soapfaultNode = factory.createSOAPFaultNode();
            soapfaultNode.setNodeValue(faultNode.toString());
            fault.setNode(soapfaultNode);
        }
    }

    private void setFaultRole(SOAPFactory factory, SOAPFault fault) {
        if (faultRole != null) {
            SOAPFaultRole soapFaultRole = factory.createSOAPFaultRole();
            soapFaultRole.setRoleValue(faultRole.toString());
            fault.setRole(soapFaultRole);
        }
    }

    private void setFaultDetail(MessageContext synCtx, SOAPFactory factory, SOAPFault fault) {
        if (faultDetail != null) {
            SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
            soapFaultDetail.setText(faultDetail);
            fault.setDetail(soapFaultDetail);
        } else if (faultDetailExpr != null) {
            SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
            Object result = null;
            try {
                result = faultDetailExpr.evaluate(synCtx);
            } catch (JaxenException e) {
                handleException("Evaluation of the XPath expression " + this.toString() +
                        " resulted in an error", e);
            }
            if (result instanceof List) {
                List list = (List) result;
                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        soapFaultDetail.addChild((OMNode) obj);
                    }
                }
            } else {
                soapFaultDetail.setText(faultDetailExpr.stringValueOf(synCtx));
            }
            fault.setDetail(soapFaultDetail);
        } else if (!faultDetailElements.isEmpty()) {
            SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
            for (OMElement faultDetailElement : faultDetailElements) {
                soapFaultDetail.addChild(faultDetailElement.cloneOMElement());
            }
            fault.setDetail(soapFaultDetail);
        } else if (fault.getDetail() != null) {
            // work around for a rampart issue in the following thread
            // http://www.nabble.com/Access-to-validation-error-message-tf4498668.html#a13284520
            fault.getDetail().detach();
        }
    }

    public int getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(int soapVersion) {
        this.soapVersion = soapVersion;
    }

    public boolean isMarkAsResponse() {
        return markAsResponse;
    }

    public void setMarkAsResponse(boolean markAsResponse) {
        this.markAsResponse = markAsResponse;
    }

    public boolean isSerializeResponse() {
        return serializeResponse;
    }

    public void setSerializeResponse(boolean serializeResponse) {
        this.serializeResponse = serializeResponse;
    }

    public QName getFaultCodeValue() {
        return faultCodeValue;
    }

    public void setFaultCodeValue(QName faultCodeValue) {

        if (soapVersion == SOAP11) {
            this.faultCodeValue = faultCodeValue;

        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                faultCodeValue.getNamespaceURI()) &&
                (SOAP12Constants.FAULT_CODE_DATA_ENCODING_UNKNOWN.equals(
                        faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_RECEIVER.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_SENDER.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_VERSION_MISMATCH.equals(
                                faultCodeValue.getLocalPart())) ) {

            this.faultCodeValue = faultCodeValue;

        } else {
            handleException("Invalid Fault code value for a SOAP 1.2 fault : " + faultCodeValue);
        }
    }

    public SynapseXPath getFaultCodeExpr() {
        return faultCodeExpr;
    }

    public void setFaultCodeExpr(SynapseXPath faultCodeExpr) {
        this.faultCodeExpr = faultCodeExpr;
    }

    public String getFaultReasonValue() {
        return faultReasonValue;
    }

    public void setFaultReasonValue(String faultReasonValue) {
        this.faultReasonValue = faultReasonValue;
    }

    public SynapseXPath getFaultReasonExpr() {
        return faultReasonExpr;
    }

    public void setFaultReasonExpr(SynapseXPath faultReasonExpr) {
        this.faultReasonExpr = faultReasonExpr;
    }

    public URI getFaultNode() {
        return faultNode;
    }

    public void setFaultNode(URI faultNode) {
        if (soapVersion == SOAP11) {
            handleException("A fault node does not apply to a SOAP 1.1 fault");
        }
        this.faultNode = faultNode;
    }

    public URI getFaultRole() {
        return faultRole;
    }

    public void setFaultRole(URI faultRole) {
        this.faultRole = faultRole;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    public SynapseXPath getFaultDetailExpr() {
        return faultDetailExpr;
    }

    public void setFaultDetailExpr(SynapseXPath faultDetailExpr) {
        this.faultDetailExpr = faultDetailExpr;
    }

    public List<OMElement> getFaultDetailElements() {
        return faultDetailElements;
    }

    public void addFaultDetailElement(OMElement element) {
        faultDetailElements.add(element);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private void handleException(String msg, Throwable e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
