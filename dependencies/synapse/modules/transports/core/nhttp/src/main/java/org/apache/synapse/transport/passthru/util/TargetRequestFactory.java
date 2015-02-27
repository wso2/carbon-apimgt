/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpVersion;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.TargetRequest;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
//import org.apache.axis2.util.MessageProcessorSelector;

public class TargetRequestFactory {
    
	private static Log log = LogFactory.getLog(TargetRequestFactory.class);

    public static TargetRequest create(MessageContext msgContext,
                                       HttpRoute route, 
                                       TargetConfiguration configuration) throws AxisFault {
        try {
            String httpMethod = (String) msgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD);
            if (httpMethod == null) {
                httpMethod = "POST";
            }

            // basic request
            Boolean noEntityBody = (Boolean) msgContext.getProperty(PassThroughConstants.NO_ENTITY_BODY);
            
            if(msgContext.getEnvelope().getBody().getFirstElement() != null){
            	noEntityBody  =false;
            }

            EndpointReference epr = PassThroughTransportUtils.getDestinationEPR(msgContext);
            URL url = new URL(epr.getAddress());
            TargetRequest request = new TargetRequest(configuration, route, url, httpMethod,
                    noEntityBody == null || !noEntityBody);

            // headers
            PassThroughTransportUtils.removeUnwantedHeaders(msgContext,
                    configuration.isPreserveServerHeader(),
                    configuration.isPreserveUserAgentHeader());


            Object o = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (o != null && o instanceof Map) {
                Map headers = (Map) o;
                for (Object entryObj : headers.entrySet()) {
                    Map.Entry entry = (Map.Entry) entryObj;
                    if (entry.getValue() != null && entry.getKey() instanceof String &&
                            entry.getValue() instanceof String) {
                        if (!HTTPConstants.HEADER_HOST.equalsIgnoreCase((String) entry.getKey())) {
                            request.addHeader((String) entry.getKey(), (String) entry.getValue());
                        }else {
                            if(msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER) != null) {
                            	request.addHeader((String) (String) entry.getKey(),
                                        (String)msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER));
                            }
                        }
                    }
                }
            }

            String cType = getContentType(msgContext);
            if (cType != null && (!httpMethod.equals("GET") && !httpMethod.equals("DELETE"))) {
                request.addHeader(HTTP.CONTENT_TYPE, cType);
            }

            // version
            String forceHttp10 = (String) msgContext.getProperty(PassThroughConstants.FORCE_HTTP_1_0);
            if ("true".equals(forceHttp10)) {
                request.setVersion(HttpVersion.HTTP_1_0);
            }

            // keep alive
            String noKeepAlie = (String) msgContext.getProperty(PassThroughConstants.NO_KEEPALIVE);
            if ("true".equals(noKeepAlie)) {
                request.setKeepAlive(false);
            }

            // port
            int port = url.getPort();
            request.setPort(port != -1 ? port : 80);

            // chunk
            String disableChunking = (String) msgContext.getProperty(
                    PassThroughConstants.DISABLE_CHUNKING);
            if ("true".equals(disableChunking)) {
                request.setChunk(false);
            }

            // full url
            String fullUrl = (String) msgContext.getProperty(PassThroughConstants.FULL_URI);
            if ("true".equals(fullUrl)) {
                request.setFullUrl(true);                
            }
            
            // Add excess respsonse header.
            String excessProp = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
            Map excessHeaders = (Map) msgContext.getProperty(excessProp);
            if (excessHeaders != null) {
                    for (Iterator iterator = excessHeaders.keySet().iterator(); iterator.hasNext();) {
                            String key = (String) iterator.next();
                            for (String excessVal : (Collection<String>) excessHeaders.get(key)) {
                                    request.addHeader(key, (String) excessVal);
                            }
                    }
            }

            return request;
        } catch (MalformedURLException e) {
            handleException("Invalid to address" + msgContext.getTo().getAddress(), e);
        }

        return null;
    }

    private static String getContentType(MessageContext msgCtx) throws AxisFault {
        MessageFormatter formatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
        OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgCtx);
        
        if (formatter != null) {
            String contentType= formatter.getContentType(msgCtx, format, msgCtx.getSoapAction());
          //keep the formatter information to prevent multipart boundary override (this will be the content writing to header)
            msgCtx.setProperty(PassThroughConstants.MESSAGE_OUTPUT_FORMAT, format);
            return contentType;
            
        } else {
            String contentType = (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
            if (contentType != null) {
                return contentType;
            } else {
                return new SOAPMessageFormatter().getContentType(
                        msgCtx, format,  msgCtx.getSoapAction());
            }
        }
    }

    /**
     * Throws an AxisFault if an error occurs at this level
     * @param s a message describing the error
     * @param e original exception leads to the error condition
     * @throws org.apache.axis2.AxisFault wrapping the original exception
     */
    private static void handleException(String s, Exception e) throws AxisFault {
        log.error(s, e);
        throw new AxisFault(s, e);
    }
}
