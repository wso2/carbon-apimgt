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

package org.apache.synapse.core.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.statistics.StatisticsReporter;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.MessageHelper;
import org.apache.synapse.util.POXUtils;

import java.util.Map;

/**
 * This class helps the Axis2SynapseEnvironment implement the send method
 */
public class Axis2Sender {

    private static final Log log = LogFactory.getLog(Axis2Sender.class);

    /**
     * Send a message out from the Synapse engine to an external service
     * @param endpoint the endpoint definition where the message should be sent
     * @param synapseInMessageContext the Synapse message context
     */
    public static void sendOn(EndpointDefinition endpoint,
        org.apache.synapse.MessageContext synapseInMessageContext) {

        try {
            Axis2FlexibleMEPClient.send(
                // The endpoint where we are sending to
                endpoint,
                // The Axis2 Message context of the Synapse MC
                synapseInMessageContext);

            if (synapseInMessageContext.isResponse()) {
                // report stats for any component at response sending check point
                StatisticsReporter.reportForAllOnResponseSent(synapseInMessageContext);
            }

        } catch (Exception e) {
            handleException("Unexpected error during sending message out", e);
        }
    }

    /**
     * Send a response back to a client of Synapse
     * @param smc the Synapse message context sent as the response
     */
    public static void sendBack(org.apache.synapse.MessageContext smc) {

        MessageContext messageContext = ((Axis2MessageContext) smc).getAxis2MessageContext();

        // if this is a dummy 202 Accepted message meant only for the http/s transports
        // prevent it from going into any other transport sender
        if (messageContext.isPropertyTrue(NhttpConstants.SC_ACCEPTED) &&
            messageContext.getTransportOut() != null &&
            !messageContext.getTransportOut().getName().startsWith(Constants.TRANSPORT_HTTP)) {
                return;
        }

        // fault processing code
        if (messageContext.isDoingREST() && messageContext.isFault()) {
            POXUtils.convertSOAPFaultToPOX(messageContext);
        }

        try {
            messageContext.setProperty(SynapseConstants.ISRESPONSE_PROPERTY, Boolean.TRUE);
            // check if addressing is already engaged for this message.
            // if engaged we should use the addressing enabled Configuration context.            

            if (AddressingHelper.isReplyRedirected(messageContext) &&
                    !messageContext.getReplyTo().hasNoneAddress()) {

                messageContext.setTo(messageContext.getReplyTo());
                messageContext.setReplyTo(null);
                messageContext.setWSAAction("");
                messageContext.setSoapAction("");
                messageContext.setProperty(
                        NhttpConstants.IGNORE_SC_ACCEPTED, Constants.VALUE_TRUE);
                messageContext.setProperty(
                        AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            }

            if (messageContext.getEnvelope().hasFault()
                    && AddressingHelper.isFaultRedirected(messageContext)
                    && !messageContext.getFaultTo().hasNoneAddress()) {

                messageContext.setTo(messageContext.getFaultTo());
                messageContext.setFaultTo(null);
                messageContext.setWSAAction("");
                messageContext.setSoapAction("");
                messageContext.setProperty(
                        NhttpConstants.IGNORE_SC_ACCEPTED, Constants.VALUE_TRUE);
                messageContext.setProperty(
                        AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            }

            String preserveAddressingProperty = (String) smc.getProperty(
                    SynapseConstants.PRESERVE_WS_ADDRESSING);
            if (preserveAddressingProperty != null &&
                    Boolean.parseBoolean(preserveAddressingProperty)) {
                /*Avoiding duplicate addressing headers*/
                messageContext.setProperty(AddressingConstants.REPLACE_ADDRESSING_HEADERS, "true");
                messageContext.setMessageID(smc.getMessageID());
            } else {
                MessageHelper.removeAddressingHeaders(messageContext);
                messageContext.setMessageID(UIDGenerator.generateURNString());
            }

            // determine weather we need to preserve the processed headers
            String preserveHeaderProperty = (String) smc.getProperty(
                    SynapseConstants.PRESERVE_PROCESSED_HEADERS);
            if (preserveHeaderProperty == null || !Boolean.parseBoolean(preserveHeaderProperty)) {
                // remove the processed headers
                MessageHelper.removeProcessedHeaders(messageContext,
                        (preserveAddressingProperty != null &&
                                Boolean.parseBoolean(preserveAddressingProperty)));
            }

            // temporary workaround for https://issues.apache.org/jira/browse/WSCOMMONS-197
            if (messageContext.isEngaged(SynapseConstants.SECURITY_MODULE_NAME) &&
                    messageContext.getEnvelope().getHeader() == null) {
                SOAPFactory fac = messageContext.isSOAP11() ?
                    OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();
                fac.createSOAPHeader(messageContext.getEnvelope());
            }

            Axis2FlexibleMEPClient.clearSecurtityProperties(messageContext.getOptions());

           // report stats for any component at response sending check point
            StatisticsReporter.reportForAllOnResponseSent(smc);

            AxisEngine.send(messageContext);

        } catch (AxisFault e) {
            handleException(getResponseMessage(messageContext), e);
        }
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private static String getResponseMessage(MessageContext msgContext){
    	StringBuilder sb = new StringBuilder();
    	try{
        	
            String strEndpoint = (String)msgContext.getProperty(NhttpConstants.ENDPOINT_PREFIX);
            if(strEndpoint != null){
            	sb.append(strEndpoint + ", ");
            }    	
            Map<String, Object> mHeader = (Map<String, Object>)msgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if(mHeader != null){
                for(String strKey:mHeader.keySet()){
                	sb.append(strKey + ":"+mHeader.get(strKey).toString()+",");
                }    
            }
             sb.append(msgContext.getEnvelope().toString());
    		sb.append(" Unexpected error sending message back");
    	}catch(Exception e){
    		sb.append(" Unexpected error sending message back");
    	}
    	return sb.toString();
    }

}
