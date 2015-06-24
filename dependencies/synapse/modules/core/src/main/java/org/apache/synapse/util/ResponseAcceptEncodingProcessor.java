/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/contrib/src/main/java/org/apache/http/contrib/compress/ResponseGzipCompress.java $
 * $Revision: 558111 $
 * $Date: 2007-07-21 01:31:50 +0530 (Sat, 21 Jul 2007) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.synapse.util;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.passthru.PassThroughConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Process the AcceptEncoding in the received Message Context. This uses the headers in the
 * received message context to decide the content encoding that client expects.
 */
public class ResponseAcceptEncodingProcessor {

    private static final Log log = LogFactory.getLog(ResponseAcceptEncodingProcessor.class);

    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String GZIP_CODEC = "gzip";

    public static void process(final MessageContext response, final MessageContext request) {

        if (response == null) {
            throw new IllegalArgumentException("Response Message Context cannot be null");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request Message context cannot be null");
        }

        Object o = request.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (o != null && o instanceof Map) {
            Map headers = (Map) o;

            String encode = (String) headers.get(ACCEPT_ENCODING);
            if (encode != null) {

                //If message  contains 'Accept-Encoding' header and  if it's value is 'qzip'
                if (GZIP_CODEC.equals(encode)) {

                    Object obj = response.getProperty(MessageContext.TRANSPORT_HEADERS);
                    Map responseHeaders;
                    
                    if (obj != null && obj instanceof Map) {
                        responseHeaders = (Map) obj;
                    } else {
                        responseHeaders = new HashMap();
                        response.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Sets the 'Content-Encoding' header as ' " + GZIP_CODEC + " '");
                    }

                    //WE CAN'T BLINDLY SAY RESPONSE IS IN GZIP IF WE SENT ACCEPT = gzip !!!
                    //responseHeaders.put(HTTP.CONTENT_ENCODING, GZIP_CODEC);

                    //Transport sender can decide to enable gzip compression by looking at this property
                    response.setProperty(PassThroughConstants.REQUEST_ACCEPTS_GZIP,"true");

                }
                //if there are any type for 'Accept-Encoding' , those should go here
            }
        }
    }
}
