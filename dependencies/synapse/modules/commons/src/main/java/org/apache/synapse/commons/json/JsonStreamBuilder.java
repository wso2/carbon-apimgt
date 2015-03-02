/**
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

package org.apache.synapse.commons.json;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public final class JsonStreamBuilder implements Builder {

    private static final Log logger = LogFactory.getLog(JsonStreamBuilder.class.getName());

    public OMElement processDocument(InputStream inputStream, String s,
                                     MessageContext messageContext) throws AxisFault {
        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();

        if (inputStream != null) {
            OMElement element = JsonUtil.newJsonPayload(messageContext, inputStream, false, false);
            if (element != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("#processDocument. Built JSON payload from JSON stream. MessageID: " + messageContext.getMessageID());
                }
                return element;
            }
        } else {
            EndpointReference endpointReference = messageContext.getTo();
            if (endpointReference == null) {
                logger.error("#processDocument. Cannot build payload without a valid EPR. MessageID: " + messageContext.getMessageID());
                throw new AxisFault("Cannot build payload without a valid EPR.");
            }
            String requestURL;
            try {
                requestURL = URIEncoderDecoder.decode(endpointReference.getAddress());
            } catch (UnsupportedEncodingException e) {
                logger.error("#processDocument. Could not decode request URL. MessageID: " + messageContext.getMessageID());
                throw new AxisFault("Could not decode request URL.", e);
            }
            String jsonString;
            int index;
            //As the message is received through GET, check for "=" sign and consider the second
            //half as the incoming JSON message
            if ((index = requestURL.indexOf('=')) > 0) {
                jsonString = requestURL.substring(index + 1);
                messageContext.setProperty(Constants.JSON_STRING, jsonString);
                ByteArrayInputStream is = new ByteArrayInputStream(jsonString.getBytes());
                return processDocument(is, s, messageContext);
            } else {
                messageContext.setProperty(Constants.JSON_STRING, null);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#processDocument. No JSON payload found in request. MessageID: " + messageContext.getMessageID());
        }
        return envelope;
    }
}
