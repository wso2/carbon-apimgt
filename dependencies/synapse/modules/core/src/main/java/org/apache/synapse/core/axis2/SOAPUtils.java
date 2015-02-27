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

package org.apache.synapse.core.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.MessageHelper;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

public class SOAPUtils {

    private static final Log log = LogFactory.getLog(SOAPUtils.class);

    /**
     * Converts the SOAP version of the message context.  Creates a new envelope of the given SOAP
     * version, copy headers and bodies from the old envelope and sets the new envelope to the same
     * message context.
     *
     * @param axisOutMsgCtx  messageContext where version conversion is done
     * @param soapVersionURI either org.apache.axis2.namespace.Constants.URI_SOAP12_ENV or
     *                       org.apache.axis2.namespace.Constants.URI_SOAP11_ENV
     * @throws AxisFault in case of an error in conversion
     */
    public static void convertSoapVersion(org.apache.axis2.context.MessageContext axisOutMsgCtx,
        String soapVersionURI) throws AxisFault {

        if (org.apache.axis2.namespace.Constants.URI_SOAP12_ENV.equals(soapVersionURI)) {
            convertSOAP11toSOAP12(axisOutMsgCtx);
        } else if (org.apache.axis2.namespace.Constants.URI_SOAP11_ENV.equals(soapVersionURI)) {
            convertSOAP12toSOAP11(axisOutMsgCtx);
        } else {
            throw new SynapseException("Invalid soapVersionURI:" + soapVersionURI);
        }
    }

    private static String SOAP_ATR_ACTOR = "actor";
    private static String SOAP_ATR_ROLE = "role";
    private static String SOAP_ATR_MUST_UNDERSTAND = "mustUnderstand";

    /**
     * Converts the version of the the message context to 1.2.
     * <br />
     * <b>Message Changes:</b>
     * <ol>
     *     <li>Convert envelope, header elements</li>
     *     <li>For each header block convert attribute actor to role</li>
     *     <li>For each header block convert mustUnderstand value type</li>
     *     <li>For each header block remove 1.1 namespaced other attributes</li>
     * </ol>
     *
     * <b>Fault Changes:</b>
     * <ol>
     *     <li>Convert fault element</li>
     *     <li>faultcode to Fault/Code</li>
     *     <li>faultstring to First Fault/Reason/Text with lang=en</li>
     * </ol>
     *
     * @param axisOutMsgCtx message context to be converted
     * @throws AxisFault incase conversion process fails
     */
    public static void convertSOAP11toSOAP12(
        org.apache.axis2.context.MessageContext axisOutMsgCtx) throws AxisFault {

        if(log.isDebugEnabled()) {
            log.debug("convert SOAP11 to SOAP12");
        }

        SOAPEnvelope clonedOldEnv = MessageHelper.cloneSOAPEnvelope(axisOutMsgCtx.getEnvelope());

        SOAPFactory soap12Factory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope newEnvelope  = soap12Factory.getDefaultEnvelope();

        if (clonedOldEnv.getHeader() != null) {
            Iterator itr = clonedOldEnv.getHeader().getChildren();
            while (itr.hasNext()) {
                OMNode omNode = (OMNode) itr.next();

                if (omNode instanceof SOAPHeaderBlock) {
                    SOAPHeaderBlock soapHeader = (SOAPHeaderBlock) omNode;
                    SOAPHeaderBlock newSOAPHeader = soap12Factory.createSOAPHeaderBlock(
                        soapHeader.getLocalName(), soapHeader.getNamespace());
                    Iterator allAttributes = soapHeader.getAllAttributes();

                    while(allAttributes.hasNext()) {
                        OMAttribute attr = (OMAttribute) allAttributes.next();
                        if(attr.getNamespace() != null
                            && SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                            attr.getNamespace().getNamespaceURI())) {
                            String attrName = attr.getLocalName();

                            if(SOAP_ATR_ACTOR.equals(attrName)) {
                                OMAttribute newAtr = omNode.getOMFactory().createOMAttribute(
                                    SOAP_ATR_ROLE, newEnvelope.getNamespace(),
                                    attr.getAttributeValue());
                                newSOAPHeader.addAttribute(newAtr);

                            } else if(SOAP_ATR_MUST_UNDERSTAND.equals(attrName)) {
                                boolean isMustUnderstand = soapHeader.getMustUnderstand();
                                newSOAPHeader.setMustUnderstand(isMustUnderstand);

                            } else {
                                log.warn("removed unsupported attribute from SOAP 1.1 " +
                                    "namespace when converting to SOAP 1.2:" + attrName);
                            }

                        } else {
                            newSOAPHeader.addAttribute(attr);
                        }

                        Iterator itrChildren = soapHeader.getChildren();
                        while (itrChildren.hasNext()) {
                            OMNode node = (OMNode) itrChildren.next();
                            itrChildren.remove();
                            newSOAPHeader.addChild(node);
                        }

                        newEnvelope.getHeader().addChild(newSOAPHeader);
                    } // while(allAttributes.hasNext())

                } else {
                    itr.remove();
                    newEnvelope.getHeader().addChild(omNode);
                }

            } // while (itr.hasNext())

        } // if (clonedOldEnv.getHeader() != null)

        if (clonedOldEnv.getBody() != null) {

            Iterator itrBodyChildren = clonedOldEnv.getBody().getChildren();
            while (itrBodyChildren.hasNext()) {
                OMNode omNode = (OMNode) itrBodyChildren.next();

                if (omNode != null && omNode instanceof SOAPFault) {

                    SOAPFault soapFault = (SOAPFault) omNode;
                    SOAPFault newSOAPFault = soap12Factory.createSOAPFault();
                    newEnvelope.getBody().addChild(newSOAPFault);
                    // get the existing envelope
                    SOAPFaultCode code = soapFault.getCode();
                    if(code != null) {
                        SOAPFaultCode newSOAPFaultCode = soap12Factory.createSOAPFaultCode();
                        newSOAPFault.setCode(newSOAPFaultCode);

                        QName s11Code = code.getTextAsQName();
                        if (s11Code != null) {
                            // get the corresponding SOAP12 fault code
                            // for the provided SOAP11 fault code
                            SOAPFaultValue newSOAPFaultValue
                                    = soap12Factory.createSOAPFaultValue(newSOAPFaultCode);
                            newSOAPFaultValue.setText(getMappingSOAP12Code(s11Code));
                        }

                    }

                    SOAPFaultReason reason = soapFault.getReason();
                    if(reason != null) {
                        SOAPFaultReason newSOAPFaultReason
                                = soap12Factory.createSOAPFaultReason(newSOAPFault);
                        String reasonText = reason.getText();
                        if(reasonText != null) {
                            SOAPFaultText newSOAPFaultText
                                    = soap12Factory.createSOAPFaultText(newSOAPFaultReason);
                            newSOAPFaultText.setLang("en"); // hard coded
                            newSOAPFaultText.setText(reasonText);
                        }
                        newSOAPFault.setReason(newSOAPFaultReason);
                    }

                    SOAPFaultDetail detail = soapFault.getDetail();
                    if(detail != null) {
                        SOAPFaultDetail newSOAPFaultDetail
                                = soap12Factory.createSOAPFaultDetail(newSOAPFault);
                        Iterator<OMElement> iter = detail.getAllDetailEntries();
                        while (iter.hasNext()) {
                            OMElement detailEntry = iter.next();
                            iter.remove();
                            newSOAPFaultDetail.addDetailEntry(detailEntry);
                        }
                         newSOAPFault.setDetail(newSOAPFaultDetail);
                    }

                } else {
                    itrBodyChildren.remove();
                    newEnvelope.getBody().addChild(omNode);

                } // if (omNode instanceof SOAPFault)

            } // while (itrBodyChildren.hasNext())

        } //if (clonedOldEnv.getBody() != null)

        axisOutMsgCtx.setEnvelope(newEnvelope);
    }

    /**
     * Converts the version of the the message context to 1.1.
     * <br />
     * <b>Message Changes:</b>
     * <ol>
     *     <li>Convert envelope, header elements</li>
     *     <li>For each header block convert attribute role to actor</li>
     *     <li>For each header block convert mustUnderstand value type</li>
     *     <li>For each header block remove 1.2 namespaced other attributes</li>
     * </ol>
     *
     * <b>Fault Changes:</b>
     * <ol>
     *     <li>Convert fault element</li>
     *     <li>Fault/Code to faultcode</li>
     *     <li>First Fault/Reason/Text to faultstring</li>
     * </ol>
     * @param axisOutMsgCtx message context to be converted
     * @throws AxisFault in case of an error in conversion
     */
    public static void convertSOAP12toSOAP11(
        org.apache.axis2.context.MessageContext axisOutMsgCtx) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("convert SOAP12 to SOAP11");
        }

        SOAPEnvelope clonedOldEnv = MessageHelper.cloneSOAPEnvelope(axisOutMsgCtx.getEnvelope());

        SOAPFactory soap11Factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope newEnvelope  = soap11Factory.getDefaultEnvelope();
        if (clonedOldEnv.getHeader() != null) {
            Iterator itr = clonedOldEnv.getHeader().getChildren();
            while (itr.hasNext()) {
                OMNode omNode = (OMNode) itr.next();

                if (omNode instanceof SOAPHeaderBlock) {
                    SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) omNode;
                    SOAPHeaderBlock newSOAPHeader = soap11Factory.createSOAPHeaderBlock(
                        soapHeaderBlock.getLocalName(), soapHeaderBlock.getNamespace());

                    Iterator allAttributes = soapHeaderBlock.getAllAttributes();

                    while(allAttributes.hasNext()) {
                        OMAttribute attr = (OMAttribute) allAttributes.next();
                        if (attr.getNamespace() != null
                            && SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                            attr.getNamespace().getNamespaceURI())) {
                            String attrName = attr.getLocalName();

                            if (SOAP_ATR_ROLE.equals(attrName)) {
                                OMAttribute newAtr = omNode.getOMFactory().createOMAttribute(
                                    SOAP_ATR_ACTOR, newEnvelope.getNamespace(),
                                    attr.getAttributeValue());
                                newSOAPHeader.addAttribute(newAtr);

                            } else if(SOAP_ATR_MUST_UNDERSTAND.equals(attrName)) {
                                boolean isMustUnderstand = soapHeaderBlock.getMustUnderstand();
                                newSOAPHeader.setMustUnderstand(isMustUnderstand);

                            } else {
                                log.warn("removed unsupported attribute from SOAP 1.2 " +
                                    "namespace when converting to SOAP 1.1:" + attrName);
                            }

                        } else {
                            newSOAPHeader.addAttribute(attr);
                        }

                        Iterator itrChildren = soapHeaderBlock.getChildren();
                        while (itrChildren.hasNext()) {
                            OMNode node = (OMNode) itrChildren.next();
                            itrChildren.remove();
                            newSOAPHeader.addChild(node);
                        }

                        newEnvelope.getHeader().addChild(newSOAPHeader);
                    }

                } else {
                    itr.remove();
                    newEnvelope.getHeader().addChild(omNode);
                }
            }
        }

        if (clonedOldEnv.getBody() != null) {
            if (clonedOldEnv.hasFault()) {
                SOAPFault soapFault = clonedOldEnv.getBody().getFault();
                SOAPFault newSOAPFault = soap11Factory.createSOAPFault();
                newEnvelope.getBody().addChild(newSOAPFault);

                SOAPFaultCode code = soapFault.getCode();
                if(code != null) {
                    SOAPFaultCode newSOAPFaultCode
                            = soap11Factory.createSOAPFaultCode(newSOAPFault);

                    SOAPFaultValue value = code.getValue();
                    if(value != null) {
                        // get the corresponding SOAP12 fault code
                        // for the provided SOAP11 fault code
                        soap11Factory.createSOAPFaultValue(newSOAPFaultCode);
                        if(value.getTextAsQName() != null) {
                            newSOAPFaultCode.setText(
                                    getMappingSOAP11Code(value.getTextAsQName()));
                        }
                    }
                }

                SOAPFaultReason reason = soapFault.getReason();
                if(reason != null) {
                    SOAPFaultReason newSOAPFaultReason
                            = soap11Factory.createSOAPFaultReason(newSOAPFault);

                    List allSoapTexts = reason.getAllSoapTexts();
                    Iterator iterAllSoapTexts = allSoapTexts.iterator();
                    if (iterAllSoapTexts.hasNext()) {
                        SOAPFaultText soapFaultText = (SOAPFaultText) iterAllSoapTexts.next();
                        iterAllSoapTexts.remove();
                        newSOAPFaultReason.setText(soapFaultText.getText());
                    }
                }

                SOAPFaultDetail detail = soapFault.getDetail();
                if(detail != null) {
                    SOAPFaultDetail newSOAPFaultDetail
                            = soap11Factory.createSOAPFaultDetail(newSOAPFault);
                    Iterator<OMElement> iter = detail.getAllDetailEntries();
                    while (iter.hasNext()) {
                        OMElement detailEntry = iter.next();
                        iter.remove();
                        newSOAPFaultDetail.addDetailEntry(detailEntry);
                    }
                    newSOAPFault.setDetail(newSOAPFaultDetail);
                }

            } else {
                Iterator itr = clonedOldEnv.getBody().getChildren();
                while (itr.hasNext()) {
                    OMNode omNode = (OMNode) itr.next();
                    if (omNode != null) {
                        itr.remove();
                        newEnvelope.getBody().addChild(omNode);
                    }
                }
            }

        }
        
        axisOutMsgCtx.setEnvelope(newEnvelope);
    }

    /**********************************************************************
     *                     Fault code conversions                         *
     **********************************************************************/

    private static final QName S11_FAULTCODE_VERSIONMISMATCH = new QName(
            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, "VersionMismatch",
            SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    private static final QName S12_FAULTCODE_VERSIONMISMATCH = new QName(
            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "VersionMismatch",
            SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

    private static final QName S11_FAULTCODE_MUSTUNDERSTAND = new QName(
            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, "MustUnderstand",
            SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    private static final QName S12_FAULTCODE_MUSTUNDERSTAND = new QName(
            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "MustUnderstand",
            SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

    private static final QName S11_FAULTCODE_CLIENT = new QName(
            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, "Client",
            SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    private static final QName S12_FAULTCODE_SENDER = new QName(
            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "Sender",
            SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

    private static final QName S11_FAULTCODE_SERVER = new QName(
            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, "Server",
            SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    private static final QName S12_FAULTCODE_RECEIVER = new QName(
            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "Receiver",
            SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

    private static final QName S12_FAULTCODE_DATAENCODINGUNKNOWN = new QName(
            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "DataEncodingUnknown",
            SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

    private static QName getMappingSOAP12Code(QName soap11Code) {
        if (S11_FAULTCODE_VERSIONMISMATCH.equals(soap11Code)) {
            return S12_FAULTCODE_VERSIONMISMATCH;
        } else if (S11_FAULTCODE_MUSTUNDERSTAND.equals(soap11Code)) {
            return S12_FAULTCODE_MUSTUNDERSTAND;
        } else if (S11_FAULTCODE_CLIENT.equals(soap11Code)) {
            return S12_FAULTCODE_SENDER;
        } else if (S11_FAULTCODE_SERVER.equals(soap11Code)) {
            return S12_FAULTCODE_RECEIVER;
        } else {
            log.warn("An unidentified SOAP11 FaultCode encountered, returning a blank QName");
            return new QName("", "");
        }
    }

    private static QName getMappingSOAP11Code(QName soap12Code) {
        if (S12_FAULTCODE_VERSIONMISMATCH.equals(soap12Code)) {
            return S11_FAULTCODE_VERSIONMISMATCH;
        } else if (S12_FAULTCODE_MUSTUNDERSTAND.equals(soap12Code)) {
            return S11_FAULTCODE_MUSTUNDERSTAND;
        } else if (S12_FAULTCODE_SENDER.equals(soap12Code)) {
            return S11_FAULTCODE_SERVER;
        } else if (S12_FAULTCODE_RECEIVER.equals(soap12Code)) {
            return S11_FAULTCODE_SERVER;
        } else if (S12_FAULTCODE_DATAENCODINGUNKNOWN.equals(soap12Code)) {
            log.debug("There is no matching SOAP11 code value for SOAP12 fault code " +
                    "DataEncodingUnknown, returning a blank QName");
            return new QName("");
        } else {
            log.warn("An unidentified SOAP11 FaultCode encountered, returning a blank QName");
            return new QName("");
        }
    }
}
