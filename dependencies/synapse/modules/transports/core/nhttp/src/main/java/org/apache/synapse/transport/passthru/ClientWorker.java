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

package org.apache.synapse.transport.passthru;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ClientWorker implements Runnable {
    private Log log = LogFactory.getLog(ClientWorker.class);
    /** the Axis2 configuration context */
    private ConfigurationContext cfgCtx = null;
    /** the response message context that would be created */
    private org.apache.axis2.context.MessageContext responseMsgCtx = null;
    /** the HttpResponse received */
    private TargetResponse response = null;
    /** weather a body is expected or not */
    private boolean expectEntityBody = true;

    public ClientWorker(ConfigurationContext cfgCtx,
                        MessageContext outMsgCtx,
                        TargetResponse response) {
        this.cfgCtx = cfgCtx;
        this.response = response;
        this.expectEntityBody = response.isExpectResponseBody();

        Map<String,String> headers = response.getHeaders();
        Map excessHeaders = response.getExcessHeaders();

        String oriURL = headers.get(PassThroughConstants.LOCATION);

        // Special casing 302, 301,303 and 307 scenarios (respectively) in following section. Not sure whether it's the correct fix,
        // but this fix makes it possible to do http --> https redirection.
        if (oriURL != null && ((response.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) &&
                (response.getStatus() != HttpStatus.SC_MOVED_PERMANENTLY) &&
                (response.getStatus() != HttpStatus.SC_SEE_OTHER) &&
                (response.getStatus() != HttpStatus.SC_TEMPORARY_REDIRECT) )) {
            URL url;
            try {
                url = new URL(oriURL);
            } catch (MalformedURLException e) {
                log.error("Invalid URL received", e);
                return;
            }

            headers.remove(PassThroughConstants.LOCATION);
            String prfix = (String) outMsgCtx.getProperty(PassThroughConstants.SERVICE_PREFIX);
            if (prfix != null) {
                headers.put(PassThroughConstants.LOCATION, prfix + url.getFile());
            }

        }
        try {
            responseMsgCtx = outMsgCtx.getOperationContext().
                getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
            // fix for RM to work because of a soapAction and wsaAction conflict
            if (responseMsgCtx != null) {
                responseMsgCtx.setSoapAction("");
            }
        } catch (AxisFault af) {
            log.error("Error getting IN message context from the operation context", af);
            return;
        }

        if (responseMsgCtx == null) {
            if (outMsgCtx.getOperationContext().isComplete()) {
                if (log.isDebugEnabled()) {
                    log.debug("Error getting IN message context from the operation context. " +
                            "Possibly an RM terminate sequence message");
                }
                return;

            }
            responseMsgCtx = new MessageContext();
            responseMsgCtx.setOperationContext(outMsgCtx.getOperationContext());
        }
        responseMsgCtx.setProperty("PRE_LOCATION_HEADER",oriURL);
        // copy the important properties from the original message context
        responseMsgCtx.setProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION,
                outMsgCtx.getProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION));
        responseMsgCtx.setProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION,
                outMsgCtx.getProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION));

        responseMsgCtx.setServerSide(true);
        responseMsgCtx.setDoingREST(outMsgCtx.isDoingREST());
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_IN, outMsgCtx
                .getProperty(MessageContext.TRANSPORT_IN));
        responseMsgCtx.setTransportIn(outMsgCtx.getTransportIn());
        responseMsgCtx.setTransportOut(outMsgCtx.getTransportOut());

        //setting the responseMsgCtx PassThroughConstants.INVOKED_REST property to the one set inside PassThroughTransportUtils
        responseMsgCtx.setProperty(PassThroughConstants.INVOKED_REST, outMsgCtx.getProperty(PassThroughConstants.INVOKED_REST));

        // set any transport headers received
        Set<Map.Entry<String, String>> headerEntries = response.getHeaders().entrySet();
        Map<String, String> headerMap = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        for (Map.Entry<String, String> headerEntry : headerEntries) {
            headerMap.put(headerEntry.getKey(), headerEntry.getValue());
        }
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
        responseMsgCtx.setProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS, excessHeaders);

        if (response.getStatus() == 202) {
            responseMsgCtx.setProperty(AddressingConstants.
                    DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            responseMsgCtx.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.FALSE);
            responseMsgCtx.setProperty(NhttpConstants.SC_ACCEPTED, Boolean.TRUE);
        }

        responseMsgCtx.setAxisMessage(outMsgCtx.getOperationContext().getAxisOperation().
                getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
        responseMsgCtx.setOperationContext(outMsgCtx.getOperationContext());
        responseMsgCtx.setConfigurationContext(outMsgCtx.getConfigurationContext());
        responseMsgCtx.setTo(null);

        responseMsgCtx.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, response.getPipe());
        responseMsgCtx.setProperty(PassThroughConstants.PASS_THROUGH_TARGET_RESPONSE, response);
        responseMsgCtx.setProperty(PassThroughConstants.PASS_THROUGH_TARGET_CONNECTION,
                response.getConnection());
    }

    public void run() {
        if (responseMsgCtx == null) {
            return;
        }
       
        try {
            if (expectEntityBody) {
            	  String cType = response.getHeader(HTTP.CONTENT_TYPE);
                  if(cType == null){
                  	cType =  response.getHeader(HTTP.CONTENT_TYPE.toLowerCase());
                  }
                String contentType;
                if (cType != null) {
                    // This is the most common case - Most of the time servers send the Content-Type
                    contentType = cType;
                } else {
                    // Server hasn't sent the header - Try to infer the content type
                    contentType = inferContentType();
                }

                responseMsgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);

                String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
                if (charSetEnc == null) {
                    charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
                }

                responseMsgCtx.setProperty(
                        Constants.Configuration.CHARACTER_SET_ENCODING,
                        contentType.indexOf("charset") > 0 ?
                                charSetEnc : MessageContext.DEFAULT_CHAR_SET_ENCODING);
                
                responseMsgCtx.setServerSide(false);
                SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
                SOAPEnvelope envelope = fac.getDefaultEnvelope();
                try {
                    responseMsgCtx.setEnvelope(envelope);
                } catch (AxisFault axisFault) {
                    log.error("Error setting SOAP envelope", axisFault);
                }

                responseMsgCtx.setServerSide(true);
            } else {
                // there is no response entity-body
                responseMsgCtx.setProperty(PassThroughConstants.NO_ENTITY_BODY, Boolean.TRUE);
                responseMsgCtx.setEnvelope(new SOAP11Factory().getDefaultEnvelope());
            }

            // copy the HTTP status code as a message context property with the key HTTP_SC to be
            // used at the sender to set the proper status code when passing the message
            int statusCode = this.response.getStatus();
            responseMsgCtx.setProperty(PassThroughConstants.HTTP_SC, statusCode);
            responseMsgCtx.setProperty(PassThroughConstants.HTTP_SC_DESC, response.getStatusLine());
            if (statusCode >= 400) {
                responseMsgCtx.setProperty(PassThroughConstants.FAULT_MESSAGE,
                        PassThroughConstants.TRUE);
            } /*else if (statusCode == 202 && responseMsgCtx.getOperationContext().isComplete()) {
                // Handle out-only invocation scenario
                responseMsgCtx.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            }*/
            responseMsgCtx.setProperty(PassThroughConstants.NON_BLOCKING_TRANSPORT, true);

            // process response received
            try {
                AxisEngine.receive(responseMsgCtx);
            } catch (AxisFault af) {
                log.error("Fault processing response message through Axis2", af);
            }

        } catch (AxisFault af) {
            log.error("Fault creating response SOAP envelope", af);            
        }
    }

    private String inferContentType() {
        //Check whether server sent Content-Type in different case
        Map<String,String> headers = response.getHeaders();
        for(String header : headers.keySet()){
            if(HTTP.CONTENT_TYPE.equalsIgnoreCase(header)){
                return headers.get(header);
            }
        }
        String cType = response.getHeader("content-type");
        if (cType != null) {
            return cType;
        }
        cType = response.getHeader("Content-type");
        if (cType != null) {
            return cType;
        }

        // Try to get the content type from the message context
        Object cTypeProperty = responseMsgCtx.getProperty(PassThroughConstants.CONTENT_TYPE);
        if (cTypeProperty != null) {
            return cTypeProperty.toString();
        }
        // Try to get the content type from the axis configuration
        Parameter cTypeParam = cfgCtx.getAxisConfiguration().getParameter(
                PassThroughConstants.CONTENT_TYPE);
        if (cTypeParam != null) {
            return cTypeParam.getValue().toString();
        }
        // Unable to determine the content type - Return default value
        return PassThroughConstants.DEFAULT_CONTENT_TYPE;
    }

}
