/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.*;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.*;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;

import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeferredMessageBuilder {

    private static Log log = LogFactory.getLog(DeferredMessageBuilder.class);

    private Map<String, Builder> builders = new HashMap<String, Builder>();
    private Map<String, MessageFormatter> formatters = new HashMap<String, MessageFormatter>();

    public final static String RELAY_FORMATTERS_MAP = "__RELAY_FORMATTERS_MAP";
    public final static String FORCED_RELAY_FORMATTER = "__FORCED_RELAY_FORMATTER";

    public DeferredMessageBuilder() {
        // first initialize with the default builders
        builders.put("multipart/related", new MIMEBuilder());
        builders.put("application/soap+xml", new SOAPBuilder());
        builders.put("text/xml", new SOAPBuilder());
        builders.put("application/xop+xml", new MTOMBuilder());
        builders.put("application/xml", new ApplicationXMLBuilder());
        builders.put("application/x-www-form-urlencoded",
                new XFormURLEncodedBuilder());

        // initialize the default formatters
        formatters.put("application/x-www-form-urlencoded", new XFormURLEncodedFormatter());
        formatters.put("multipart/form-data", new MultipartFormDataFormatter());
        formatters.put("application/xml", new ApplicationXMLFormatter());
        formatters.put("text/xml", new SOAPMessageFormatter());
        formatters.put("application/soap+xml", new SOAPMessageFormatter());
    }

    public Map<String, Builder> getBuilders() {
        return builders;
    }

    public void addBuilder(String contentType, Builder builder) {
        builders.put(contentType, builder);
    }

    public void addFormatter(String contentType, MessageFormatter messageFormatter) {
        formatters.put(contentType, messageFormatter);
    }

    public Map<String, MessageFormatter> getFormatters() {
        return formatters;
    }

    public OMElement getDocument(MessageContext msgCtx, InputStream in) throws
            XMLStreamException, IOException {
   	  
    	
    	String contentType = (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
    	String _contentType = getContentType(contentType, msgCtx);
 	    in = HTTPTransportUtils.handleGZip(msgCtx, in);

    	AxisConfiguration configuration =
    		  msgCtx.getConfigurationContext().getAxisConfiguration();
      Parameter useFallbackParameter = configuration.getParameter(Constants.Configuration.USE_DEFAULT_FALLBACK_BUILDER);
     
      boolean useFallbackBuilder = false;
      
      if (useFallbackParameter !=null){
      	useFallbackBuilder = JavaUtils.isTrueExplicitly(useFallbackParameter.getValue(),useFallbackBuilder);
      }
    	
    	OMElement element = null;
        Builder builder;
        if (contentType != null) {
            // loading builder from externally..
            //builder = configuration.getMessageBuilder(_contentType,useFallbackBuilder);
            builder = MessageProcessorSelector.getMessageBuilder(_contentType, msgCtx);
            if (builder != null) {
                try {
                    /*try {
                        throw new Exception("Building message");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    element = builder.processDocument(in,contentType, msgCtx);
                } catch (AxisFault axisFault) {
                    log.error("Error building message", axisFault);
                    throw axisFault;
                }
            }
        }

        if (element == null) {
            if (msgCtx.isDoingREST()) {
                try {
                    element = BuilderUtil.getPOXBuilder(in, null).getDocumentElement();
                } catch (XMLStreamException e) {
                    log.error("Error building message using POX Builder", e);
                    throw e;
                }
            } else {
                // switch to default
                builder = new SOAPBuilder();
                try {
                    element = builder.processDocument(in, contentType, msgCtx);
                } catch (AxisFault axisFault) {
                    log.error("Error building message using SOAP builder");
                    throw axisFault;
                }
            }
        }

        // build the soap headers and body
        if (element instanceof SOAPEnvelope) {
            SOAPEnvelope env = (SOAPEnvelope) element;
            env.hasFault();
        }

        //setting up original contentType (resetting the content type)
        if(contentType != null && !contentType.isEmpty()){
         msgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
        }
        return element;
    }

    private Builder getBuilderForContentType(String contentType) {
        String type;
        int index = contentType.indexOf(';');
        if (index > 0) {
            type = contentType.substring(0, index);
        } else {
            type = contentType;
        }

        Builder builder = builders.get(type);

        if (builder == null) {
            builder = builders.get(type.toLowerCase());
        }

        if (builder == null) {
            Iterator<Map.Entry<String, Builder>> iterator = builders.entrySet().iterator();
            while (iterator.hasNext() && builder == null) {
                Map.Entry<String, Builder> entry = iterator.next();
                String key = entry.getKey();
                if (contentType.matches(key)) {
                    builder = entry.getValue();
                }
            }
        }
        return builder;
    }

    public static Builder createBuilder(String className) throws AxisFault {
        try {
            Class c = Class.forName(className);
            Object o = c.newInstance();
            if (o instanceof Builder) {
                return (Builder) o;
            }
        } catch (ClassNotFoundException e) {
            handleException("Builder class not found :" +
                    className, e);
        } catch (IllegalAccessException e) {
            handleException("Cannot initiate Builder class :" +
                    className, e);
        } catch (InstantiationException e) {
            handleException("Cannot initiate Builder class :" +
                    className, e);
        }
        return null;
    }

    public static MessageFormatter createFormatter(String className) throws AxisFault {
        try {
            Class c = Class.forName(className);
            Object o = c.newInstance();
            if (o instanceof MessageFormatter) {
                return (MessageFormatter) o;
            }
        } catch (ClassNotFoundException e) {
            handleException("MessageFormatter class not found :" +
                    className, e);
        } catch (IllegalAccessException e) {
            handleException("Cannot initiate MessageFormatter class :" +
                    className, e);
        } catch (InstantiationException e) {
            handleException("Cannot initiate MessageFormatter class :" +
                    className, e);
        }
        return null;
    }

    private static void handleException(String message, Exception e) throws AxisFault {
        log.error(message, e);
        throw new AxisFault(message, e);
    }

    /**
     * This method is from org.apache.axis2.transport.TransportUtils - it was hack placed in Axis2 Transport to enable
     * responses with text/xml to be processed using the ApplicationXMLBuilder (which is technically wrong, it should be
     * the duty of the backend service to send the correct content type, which makes the most sense (refer RFC 1049),
     * alas, tis not the way of the World).
     * @param contentType
     * @param msgContext
     * @return  MIME content type.
     */
    public static String getContentType(String contentType, MessageContext msgContext) {
        String type;
        int index = contentType.indexOf(';');
        if (index > 0) {
            type = contentType.substring(0, index);
        } else {
            int commaIndex = contentType.indexOf(',');
            if (commaIndex > 0) {
                type = contentType.substring(0, commaIndex);
            } else {
                type = contentType;
            }
        }
        // Some services send REST responses as text/xml. We should convert it to
        // application/xml if its a REST response, if not it will try to use the SOAPMessageBuilder.
        // isDoingREST should already be properly set by HTTPTransportUtils.initializeMessageContext
        if (null != msgContext.getProperty(PassThroughConstants.INVOKED_REST)
                && msgContext.getProperty(PassThroughConstants.INVOKED_REST).equals(true)
                && HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(type)) {
                    type = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }
        return type;
    }
}