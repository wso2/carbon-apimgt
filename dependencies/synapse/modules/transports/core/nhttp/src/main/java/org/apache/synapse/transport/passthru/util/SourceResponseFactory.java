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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.SourceResponse;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import com.ibm.wsdl.extensions.http.HTTPConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SourceResponseFactory {

    public static SourceResponse create(MessageContext msgContext,
                                        SourceRequest sourceRequest,
                                        SourceConfiguration sourceConfiguration) {
        // determine the status code to be sent
        int statusCode = PassThroughTransportUtils.determineHttpStatusCode(msgContext);

        SourceResponse sourceResponse =
                new SourceResponse(sourceConfiguration, statusCode, sourceRequest);

        // set any transport headers
        Map transportHeaders = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        
        boolean forceContentLength = msgContext.isPropertyTrue(NhttpConstants.FORCE_HTTP_CONTENT_LENGTH);
	    boolean forceContentLengthCopy = msgContext.isPropertyTrue(PassThroughConstants.COPY_CONTENT_LENGTH_FROM_INCOMING);
	    
	    if (forceContentLength && forceContentLengthCopy && msgContext.getProperty(PassThroughConstants.ORGINAL_CONTEN_LENGTH) != null) {
	    	 sourceResponse.addHeader(HTTP.CONTENT_LEN, (String)msgContext.getProperty(PassThroughConstants.ORGINAL_CONTEN_LENGTH));
		}

        if (transportHeaders != null && msgContext.getProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE) != null) {
            if (msgContext.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE) != null
                    && msgContext.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE).toString().contains(PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)) {
                transportHeaders.put(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED);
            } else {
                Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
                if (pipe != null && !Boolean.TRUE.equals(msgContext.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED))) {
                    transportHeaders.put(HTTP.CONTENT_TYPE, msgContext.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE));
                }
            }
        }

        if (transportHeaders != null) {
            addResponseHeader(sourceResponse, transportHeaders);
        }else{
        	  Boolean noEntityBody = (Boolean) msgContext.getProperty(NhttpConstants.NO_ENTITY_BODY);
        	 if (noEntityBody == null || Boolean.FALSE == noEntityBody) {
        		 OMOutputFormat format = NhttpUtil.getOMOutputFormat(msgContext);
        		 transportHeaders = new HashMap();
            	 MessageFormatter messageFormatter =
                     MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
            	 if(msgContext.getProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE) == null){
            	    transportHeaders.put(HTTP.CONTENT_TYPE, messageFormatter.getContentType(msgContext, format, msgContext.getSoapAction()));
            	 }
            	 addResponseHeader(sourceResponse, transportHeaders);
             }
        	 
        }
        
 

	// Add excess response header. 
	String excessProp = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
	Map excessHeaders = (Map) msgContext.getProperty(excessProp);
	if (excessHeaders != null) {
		for (Iterator iterator = excessHeaders.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			for (String excessVal : (Collection<String>) excessHeaders.get(key)) {
				sourceResponse.addHeader(key, (String) excessVal);
			}
		}
	}

        return sourceResponse;
    }

	private static void addResponseHeader(SourceResponse sourceResponse, Map transportHeaders) {
	    for (Object entryObj : transportHeaders.entrySet()) {
	        Map.Entry entry = (Map.Entry) entryObj;
	        if (entry.getValue() != null && entry.getKey() instanceof String &&
	                entry.getValue() instanceof String) {
	            sourceResponse.addHeader((String) entry.getKey(), (String) entry.getValue());
	        }
	    }
    }
    
}
