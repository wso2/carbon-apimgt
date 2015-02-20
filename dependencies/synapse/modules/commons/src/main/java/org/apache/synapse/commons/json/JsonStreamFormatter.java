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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;

public final class JsonStreamFormatter implements MessageFormatter {
    private static final Log logger = LogFactory.getLog(JsonStreamFormatter.class.getName());

    public byte[] getBytes(MessageContext messageContext, OMOutputFormat format) throws AxisFault {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeTo(messageContext, format, baos ,true);
        return baos.toByteArray();
    }

    public String getContentType(MessageContext messageContext, OMOutputFormat format,
                                 String soapActionString) {
        String contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        String encoding = format.getCharSetEncoding();
        if (contentType == null) {
            contentType = (String) messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
        }
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }
        return contentType;
    }

    public void writeTo(MessageContext messageContext, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        if (preserve) {
            messageContext.setProperty(JsonUtil.PRESERVE_JSON_STREAM, true);
        }
        JsonUtil.writeAsJson(messageContext, out);
        if (logger.isDebugEnabled()) {
            logger.debug("#writeTo. Wrote JSON payload to output stream. MessageID: " + messageContext.getMessageID());
        }
    }

    /**
     * @deprecated Use {@link org.apache.synapse.commons.json.JsonUtil#writeAsJson(org.apache.axis2.context.MessageContext, java.io.OutputStream)}
     * @param messageContext Axis2 Message context that holds the JSON/XML payload.
     * @param out Output stream to which the payload(JSON) must be written.
     * @throws AxisFault
     */
    public static void toJson(MessageContext messageContext, OutputStream out) throws AxisFault {
        JsonUtil.writeAsJson(messageContext, out);
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL)
            throws AxisFault {
        if (logger.isDebugEnabled()) {
            logger.debug("#getTargetAddress. Not implemented. #getTargetAddress()");
        }
        return targetURL;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat,
                                   String s) {
        if (logger.isDebugEnabled()) {
            logger.debug("#formatSOAPAction. Not implemented. #formatSOAPAction()");
        }
        return null;
    }
}
