/*
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

package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.config.PassThroughConfiguration;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RelayUtils {

   	private static final Log log = LogFactory.getLog(RelayUtils.class);

    private static final DeferredMessageBuilder messageBuilder = new DeferredMessageBuilder();

    private static volatile Handler addressingInHandler = null;
    private static boolean noAddressingHandler = false;
    
    private static Boolean forcePTBuild = null;
    

    static{
    	if(forcePTBuild == null){
           forcePTBuild =PassThroughConfiguration.getInstance().getBooleanProperty(PassThroughConstants.FORCE_PASSTHROUGH_BUILDER);
           if(forcePTBuild ==null){
             forcePTBuild =true;
           }
        //this to keep track ignore the builder operation eventhough content level is enable.
        }
    }

	public static void buildMessage(org.apache.axis2.context.MessageContext msgCtx) throws IOException,
            XMLStreamException {

        buildMessage(msgCtx, false);
    }

    public static void buildMessage(MessageContext messageContext, boolean earlyBuild) throws IOException,
            XMLStreamException {

        final Pipe pipe = (Pipe) messageContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
		if (pipe != null &&
		    !Boolean.TRUE.equals(messageContext.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED)) &&
		    forcePTBuild) {
			InputStream in = pipe.getInputStream();
        	
        	builldMessage(messageContext, earlyBuild, in);
            return;
        }
    }

	public static void builldMessage(MessageContext messageContext, boolean earlyBuild, InputStream in) throws IOException, AxisFault {
	    //
	    BufferedInputStream bufferedInputStream= (BufferedInputStream) messageContext.getProperty(PassThroughConstants.BUFFERED_INPUT_STREAM);
	    if(bufferedInputStream != null){
	    	try{
	    	  bufferedInputStream.reset();
	    	  bufferedInputStream.mark(0);
	    	}catch (Exception e) {
	    		//just ignore the error
			}
          
	    }else{
	    		bufferedInputStream =new BufferedInputStream(in);
		    	 //TODO: need to handle properly for the moment lets use around 100k buffer.
			    bufferedInputStream.mark(128 * 1024);
		    	messageContext.setProperty(PassThroughConstants.BUFFERED_INPUT_STREAM, bufferedInputStream);
		  }
	   
	    OMElement element = null;
        try {
            element = messageBuilder.getDocument(messageContext,
                                                 bufferedInputStream != null ? bufferedInputStream : in);
        } catch (Exception e) {
            messageContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            handleException("Error while building Passthrough stream", e);
        }
        if (element != null) {
	        messageContext.setEnvelope(TransportUtils.createSOAPEnvelope(element));
	        messageContext.setProperty(DeferredMessageBuilder.RELAY_FORMATTERS_MAP,
	                messageBuilder.getFormatters());
	        messageContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED,
	                Boolean.TRUE);

	        if (!earlyBuild) {
	            processAddressing(messageContext);
	        }
	    }
	    return;
    }

    private static void processAddressing(MessageContext messageContext) throws AxisFault {
        if (noAddressingHandler) {
            return;
        } else if (addressingInHandler == null) {
            synchronized (messageBuilder) {
                if (addressingInHandler == null) {
                    AxisConfiguration axisConfig = messageContext.getConfigurationContext().
                            getAxisConfiguration();
                    List<Phase> phases = axisConfig.getInFlowPhases();
                    boolean handlerFound = false;
                    for (Phase phase : phases) {
                        if ("Addressing".equals(phase.getName())) {
                            List<Handler> handlers = phase.getHandlers();
                            for (Handler handler : handlers) {
                                if ("AddressingInHandler".equals(handler.getName())) {
                                    addressingInHandler = handler;
                                    handlerFound = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    if (!handlerFound) {
                        noAddressingHandler = true;
                        return;
                    }
                }
            }
        }

        messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_IN_MESSAGES, "false");
        
        Object disableAddressingForOutGoing = null;
        if(messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES) != null){
        	disableAddressingForOutGoing = messageContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        }
        addressingInHandler.invoke(messageContext);
        
        if(disableAddressingForOutGoing !=null){
        	messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, disableAddressingForOutGoing);
        }

        if (messageContext.getAxisOperation() == null) {
            return;
        }

        String mepString = messageContext.getAxisOperation().getMessageExchangePattern();

        if (isOneWay(mepString)) {
            Object requestResponseTransport = messageContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null) {

                Boolean disableAck = getDisableAck(messageContext);
                if (disableAck == null || disableAck.booleanValue() == false) {
                    ((RequestResponseTransport) requestResponseTransport).acknowledgeMessage(messageContext);
                }
            }
        } else if (AddressingHelper.isReplyRedirected(messageContext) && AddressingHelper.isFaultRedirected(messageContext)) {
            if (mepString.equals(WSDL2Constants.MEP_URI_IN_OUT)
                    || mepString.equals(WSDL2Constants.MEP_URI_IN_OUT)
                    || mepString.equals(WSDL2Constants.MEP_URI_IN_OUT)) {
                // OR, if 2 way operation but the response is intended to not use the response channel of a 2-way transport
                // then we don't need to keep the transport waiting.

                Object requestResponseTransport = messageContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
                if (requestResponseTransport != null) {

                    // We should send an early ack to the transport whenever possible, but some modules need
                    // to use the back channel, so we need to check if they have disabled this code.
                    Boolean disableAck = getDisableAck(messageContext);

                    if (disableAck == null || disableAck.booleanValue() == false) {
                        ((RequestResponseTransport) requestResponseTransport).acknowledgeMessage(messageContext);
                    }

                }
            }
        }
    }

    private static Boolean getDisableAck(MessageContext msgContext) throws AxisFault {
       // We should send an early ack to the transport whenever possible, but some modules need
       // to use the back channel, so we need to check if they have disabled this code.
       Boolean disableAck = (Boolean) msgContext.getProperty(Constants.Configuration.DISABLE_RESPONSE_ACK);
       if(disableAck == null) {
          disableAck = (Boolean) (msgContext.getAxisService() != null ? msgContext.getAxisService().getParameterValue(Constants.Configuration.DISABLE_RESPONSE_ACK) : null);
       }

       return disableAck;
    }

    private static boolean isOneWay(String mepString) {
        return (mepString.equals(WSDL2Constants.MEP_URI_IN_ONLY)
                || mepString.equals(WSDL2Constants.MEP_URI_IN_ONLY)
                || mepString.equals(WSDL2Constants.MEP_URI_IN_ONLY));
    }

    /**
     * Perform an error log message to all logs @ ERROR and throws a AxisFault
     *
     * @param msg the log message
     * @param e an Exception encountered
     * @throws AxisFault
     */
    private static void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}

}
