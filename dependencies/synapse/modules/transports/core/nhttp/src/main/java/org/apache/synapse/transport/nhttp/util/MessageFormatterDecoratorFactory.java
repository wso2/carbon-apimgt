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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;

import java.util.Map;

/**
 * Factory for getting Decorator to Message Formatter . This decorator is needed when extending
 * message formatter  by adding decoration functionality
 */
public class MessageFormatterDecoratorFactory {

    private static final Log log = LogFactory.getLog(MessageFormatterDecoratorFactory.class);

    private static final String GZIP_CODEC = "gzip";

    public static MessageFormatter createMessageFormatterDecorator(MessageContext msgContext) {

        if (msgContext == null) {
            throw new IllegalArgumentException("Message Context cannot be null");
        }

        try {
            // Get message formatter based on the content type
            MessageFormatter formatter = MessageProcessorSelector.getMessageFormatter(msgContext);

            Object o = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (o != null && o instanceof Map) {
                Map headers = (Map) o;

                String encode = (String) headers.get(HTTP.CONTENT_ENCODING);
                if (encode != null) {

                    //If message  contains 'Accept-Encoding' header and  if it's value is 'qzip'
                    if (GZIP_CODEC.equals(encode)) {
                        formatter = new GzipMessageFormatterDecorator(formatter);
                    }
                    //if there are any type for 'Accept-Encoding' , those should go here

                }
            }
            return formatter;

        } catch (AxisFault axisFault) {
            String msg = "Cannot find a suitable MessageFormatter : " + axisFault.getMessage();
            log.error(msg, axisFault);
        }

        return null;

    }
}
