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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 *
 */
public class POXUtils {

    private static final Log log = LogFactory.getLog(POXUtils.class);

    public static void convertSOAPFaultToPOX(MessageContext msgCtx) {

        SOAPBody body = msgCtx.getEnvelope().getBody();
        SOAPFault fault = body.getFault();
        if (fault != null) {

            OMFactory fac = msgCtx.getEnvelope().getOMFactory();
            OMElement faultPayload = fac.createOMElement(new QName("Exception"));

            if (fault.getDetail() != null && !fault.getDetail().getText().equals("")) {

                String faultDetail = fault.getDetail().getText();

                if (log.isDebugEnabled()) {
                    log.debug("Setting the fault detail : " + faultDetail + " as athe POX Fault");
                }

                OMElement om = getOMFromXML(faultDetail);
                if(om == null) {
                    faultPayload.setText(faultDetail);
                }
                else  {
                    faultPayload.addChild(om);
                }

            } else if (fault.getReason() != null && !fault.getReason().getText().equals("")) {

                String faultReasonValue = fault.getReason().getText();

                if (log.isDebugEnabled()) {
                    log.debug("Setting the fault reason : "
                        + faultReasonValue + " as athe POX Fault");
                }
                faultPayload.setText(faultReasonValue);

            } else if (log.isDebugEnabled()) {
                
                log.debug("Couldn't find the fault detail or reason to compose POX Fault");
            }

            if (body.getFirstElement() != null) {
                body.getFirstElement().detach();
            }

            msgCtx.setProcessingFault(true);

            if (log.isDebugEnabled()) {

                String msg = "Original SOAP Message : " + msgCtx.getEnvelope().toString() +
                    "POXFault Message created : " + faultPayload.toString();
                log.debug(msg);
                
                if (log.isTraceEnabled()) {
                    log.trace(msg);
                }
            }

            body.addChild(faultPayload);
        }
    }

    private static OMElement getOMFromXML(String text) {

        int escape = StringUtils.countMatches(text, "<!") +
                StringUtils.countMatches(text, "<?");
        int numOfLTWithS = StringUtils.countMatches(text, "</");
        int numOfGTWithS = StringUtils.countMatches(text, "/>");
        int numOfLT = StringUtils.countMatches(text, "<") - escape;

        //Should satisfy the conditions if an XML
        if(numOfLT == 0 || ((numOfLTWithS * 2 + numOfGTWithS) != numOfLT)) {
            return null;
        }

        try {
            OMElement om = AXIOMUtil.stringToOM(text);
            return om;
        } catch (XMLStreamException ignore) {
            // means not a xml
            return null;
        } catch (OMException ignore) {
            // means not a xml
            return null;
        }
    }
}
