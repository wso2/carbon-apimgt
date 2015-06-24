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
package org.apache.synapse.transport.nhttp.util;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

/**
 * This is the decorator for message formatter and it is need because there isn't any mechanism
 * that can process the message before formatting is occurred.  For handle 'Accept-Encoding' some
 * kind of this is  required. This decorator encapsulates the message formatter and add the whatever
 * functionally when as required without effecting message formatter functionality.
 * This enable to serialize message in Gzip format .
 */

public class GzipMessageFormatterDecorator implements MessageFormatter {

    private static final Log log = LogFactory.getLog(GzipMessageFormatterDecorator.class);

    /* The encapsulated message formatter instance */
    private MessageFormatter messageFormatter;

    public GzipMessageFormatterDecorator(MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    public byte[] getBytes(MessageContext messageContext, OMOutputFormat format) throws AxisFault {
        return messageFormatter.getBytes(messageContext, format);
    }

    public void writeTo(MessageContext messageContext, OMOutputFormat format, OutputStream outputStream, boolean preserve) throws AxisFault {

        try {

            // Writes message as an GZIP stream
            if (log.isDebugEnabled()) {
                log.debug("Serialize message in to a GZIP stream");
            }

            OutputStream out = new GZIPOutputStream(outputStream);
            messageFormatter.writeTo(messageContext, format, out, preserve);

            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                handleException("Error closing message stream", e);
            }

        } catch (IOException e) {
            handleException("Error getting GZIP output stream", e);
        }

    }

    public String getContentType(MessageContext messageContext, OMOutputFormat format, String soapAction) {
        return messageFormatter.getContentType(messageContext, format, soapAction);
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL) throws AxisFault {
        return messageFormatter.getTargetAddress(messageContext, format, targetURL);
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format, String soapAction) {
        return messageFormatter.formatSOAPAction(messageContext, format, soapAction);
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
