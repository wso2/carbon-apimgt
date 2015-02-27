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

import org.apache.axiom.om.OMException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.statistics.ErrorLogFactory;
import org.apache.synapse.aspects.statistics.StatisticsReporter;
import org.apache.synapse.carbonext.TenantInfoConfigurator;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.FailoverEndpoint;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.apache.synapse.util.ResponseAcceptEncodingProcessor;

import java.util.Stack;
import java.util.Timer;

/**
 * This is the message receiver that receives the responses for outgoing messages sent out
 * by Synapse. It holds a callbackStore that maps the [unique] messageID of each message to
 * a callback object that gets executed on timeout or when a response is received (before timeout)
 *
 * The AnonymousServiceFactory uses this MessageReceiver for all Anonymous services created by it.
 * This however - effectively - is a singleton class
 */
public class SynapseCallbackReceiver extends CallbackReceiver {

    private static final Log log = LogFactory.getLog(SynapseCallbackReceiver.class);

    /** This is the synchronized callbackStore that maps outgoing messageID's to callback objects */
//    private final Map<String, AxisCallback> callbackStore;  // will made thread safe in the constructor

    /**
     * Create the *single* instance of this class that would be used by all anonymous services
     * used for outgoing messaging.
     * @param synCfg the Synapse configuration
     * @param contextInformation server runtime information
     */
    public SynapseCallbackReceiver(SynapseConfiguration synCfg,
                                   ServerContextInformation contextInformation) {

//        callbackStore = Collections.synchronizedMap(new HashMap<String, AxisCallback>());

        // create the Timer object and a TimeoutHandler task
        TimeoutHandler timeoutHandler = new TimeoutHandler(callbackStore, contextInformation);
        
        Timer timeOutTimer = synCfg.getSynapseTimer();
        long timeoutHandlerInterval = SynapseConfigUtils.getTimeoutHandlerInterval();

        // schedule timeout handler to run every n seconds (n : specified or defaults to 15s)
        timeOutTimer.schedule(timeoutHandler, 0, timeoutHandlerInterval);
    }

    public int getCallbackCount() {
        return callbackStore.size();
    }

    public void addCallback(String MsgID, AxisCallback callback) {
        callbackStore.put(MsgID, callback);
        if (log.isDebugEnabled()) {
            log.debug("Callback added. Total callbacks waiting for : " + callbackStore.size());
        }
    }

    /**
     * Everytime a response message is received this method gets invoked. It will then select
     * the outgoing *Synapse* message context for the reply we received, and determine what action
     * to take at the Synapse level
     *
     * @param messageCtx the Axis2 message context of the reply received
     * @throws AxisFault
     */
    public void receive(MessageContext messageCtx) throws AxisFault {

        String messageID = null;

        /**
         * In an Out-only scenario if the client receives a HTTP 202 accepted we need to
         * remove the call back/s registered for that request.
         * This if will check weather this is a message sent in a that scenario and remove the callback
         */
        if (messageCtx.getProperty(NhttpConstants.HTTP_202_RECEIVED) != null && "true".equals(
                messageCtx.getProperty(NhttpConstants.HTTP_202_RECEIVED))) {
            if (callbackStore.containsKey(messageCtx.getMessageID())) {
                callbackStore.remove(messageCtx.getMessageID());
                if (log.isDebugEnabled()) {
                    log.debug("CallBack registered with Message id : " + messageCtx.getMessageID() +
                            " removed from the " +
                            "callback store since we got an accepted Notification");
                }
            }

            return;
        }


        if (messageCtx.getOptions() != null && messageCtx.getOptions().getRelatesTo() != null) {
            // never take a chance with a NPE at this stage.. so check at each level :-)
            Options options = messageCtx.getOptions();
            if (options != null) {
                RelatesTo relatesTo = options.getRelatesTo();
                if (relatesTo != null) {
                    messageID = relatesTo.getValue();
                }
            }
        } else if (messageCtx.getProperty(SandeshaClientConstants.SEQUENCE_KEY) == null) {
            messageID = (String) messageCtx.getProperty(SynapseConstants.RELATES_TO_FOR_POX);
        }

        if (messageID != null) {
            AsyncCallback callback = (AsyncCallback)callbackStore.remove(messageID);
            if (log.isDebugEnabled()) {
                log.debug("Callback removed for request message id : " + messageID +
                        ". Pending callbacks count : " + callbackStore.size());
            }

            RelatesTo[] relates = messageCtx.getRelationships();
            if (relates != null && relates.length > 1) {
                // we set a relates to to the response message so that if WSA is not used, we
                // could still link back to the original message. But if WSA was used, this
                // gets duplicated, and we should remove it
                removeDuplicateRelatesTo(messageCtx, relates);
            }
            
            if (callback != null) {
                handleMessage(messageID, messageCtx, ((AsyncCallback) callback).getSynapseOutMsgCtx(),
                        (AsyncCallback)callback);
                
            } else {
                // TODO invoke a generic synapse error handler for this message
                log.warn("Synapse received a response for the request with message Id : " +
                        messageID + " But a callback is not registered (anymore) to process this response");
            }

        } else if (!messageCtx.isPropertyTrue(NhttpConstants.SC_ACCEPTED)){
            // TODO invoke a generic synapse error handler for this message
            log.warn("Synapse received a response message without a message Id");
        }
    }

    /**
     * Handle the response or error (during a failed send) message received for an outgoing request
     *
     * @param messageID        Request message ID
     * @param response         the Axis2 MessageContext that has been received and has to be handled
     * @param synapseOutMsgCtx the corresponding (outgoing) Synapse MessageContext for the above
     *                         Axis2 MC, that holds Synapse specific information such as the error
     *                         handler stack and local properties etc.
     * @throws AxisFault       if the message cannot be processed
     */
    private void handleMessage(String messageID ,MessageContext response,
        org.apache.synapse.MessageContext synapseOutMsgCtx, AsyncCallback callback) throws AxisFault {
        // apply the tenant information to the out message context
        TenantInfoConfigurator configurator = synapseOutMsgCtx.getEnvironment()
                .getTenantInfoConfigurator();
        if (configurator != null) {
            configurator.applyTenantInfo(synapseOutMsgCtx);
        }
        Object o = response.getProperty(SynapseConstants.SENDING_FAULT);
        if (o != null && Boolean.TRUE.equals(o)) {

            StatisticsReporter.reportFaultForAll(synapseOutMsgCtx,
                    ErrorLogFactory.createErrorLog(response));
            // there is a sending fault. propagate the fault to fault handlers.

            Stack faultStack = synapseOutMsgCtx.getFaultStack();
            if (faultStack != null && !faultStack.isEmpty()) {

                // if we have access to the full synapseOutMsgCtx.getEnvelope(), then let
                // it flow with the error details. Else, replace its envelope with the
                // fault envelope
                try {
                    synapseOutMsgCtx.getEnvelope().build();
                } catch (OMException x) {
                    synapseOutMsgCtx.setEnvelope(response.getEnvelope());
                }

                Exception e = (Exception) response.getProperty(SynapseConstants.ERROR_EXCEPTION);

                synapseOutMsgCtx.setProperty(SynapseConstants.SENDING_FAULT, Boolean.TRUE);
                synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_CODE,
                    response.getProperty(SynapseConstants.ERROR_CODE));
                synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_MESSAGE,
                    response.getProperty(SynapseConstants.ERROR_MESSAGE));
                synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                    response.getProperty(SynapseConstants.ERROR_DETAIL));
                synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, e);

                if (log.isDebugEnabled()) {
                    log.debug("[Failed Request Message ID : " + messageID + "]" +
                            " [New to be Retried Request Message ID : " +
                            synapseOutMsgCtx.getMessageID() + "]");
                }  
                
                int errorCode = (Integer)response.getProperty(SynapseConstants.ERROR_CODE);

                //If a timeout has occured and the timeout action of the callback is to discard the message
                if(errorCode == SynapseConstants.NHTTP_CONNECTION_TIMEOUT &&
                   callback.getTimeOutAction() == SynapseConstants.DISCARD){
                        //Do not execute any fault sequences. Discard message
                        if(log.isWarnEnabled()){
                            log.warn("Synapse timed out for the request with Message ID : " + messageID +
                                      ". Ignoring fault handlers since the timeout action is DISCARD");
                        }
                        faultStack.removeAllElements();
                }
                else{
                    ((FaultHandler) faultStack.pop()).handleFault(synapseOutMsgCtx, null);
                }
            }

        } else {

            // there can always be only one instance of an Endpoint in the faultStack of a message
            // if the send was successful, so remove it before we proceed any further
            Stack faultStack = synapseOutMsgCtx.getFaultStack();

            Endpoint successfulEndpoint=null;
            if (faultStack != null && !faultStack.isEmpty()
                && faultStack.peek() instanceof Endpoint) {
                successfulEndpoint = (Endpoint) faultStack.pop();
            }

            if (log.isDebugEnabled()) {
                log.debug("Synapse received an asynchronous response message");
                log.debug("Received To: " +
                        (response.getTo() != null ? response.getTo().getAddress() : "null"));
                log.debug("SOAPAction: " +
                        (response.getSoapAction() != null ? response.getSoapAction() : "null"));
                log.debug("WSA-Action: " +
                        (response.getWSAAction() != null ? response.getWSAAction() : "null"));
                String[] cids = response.getAttachmentMap().getAllContentIDs();
                if (cids != null && cids.length > 0) {
                    for (String cid : cids) {
                        log.debug("Attachment : " + cid);
                    }
                }
                log.debug("Body : \n" + response.getEnvelope());
            }
            MessageContext axisOutMsgCtx =
                    ((Axis2MessageContext) synapseOutMsgCtx).getAxis2MessageContext();

            //Processes 'Accept-Encoding'
            ResponseAcceptEncodingProcessor.process(response, axisOutMsgCtx);

            response.setServiceContext(null);
            response.setOperationContext(axisOutMsgCtx.getOperationContext());
            response.setAxisMessage(axisOutMsgCtx.getAxisOperation().getMessage(
                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE));

            // set properties on response
            response.setServerSide(true);
            response.setProperty(SynapseConstants.ISRESPONSE_PROPERTY, Boolean.TRUE);
            response.setProperty(MessageContext.TRANSPORT_OUT,
                    axisOutMsgCtx.getProperty(MessageContext.TRANSPORT_OUT));
            response.setProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
                    axisOutMsgCtx.getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO));
            response.setTransportIn(axisOutMsgCtx.getTransportIn());
            response.setTransportOut(axisOutMsgCtx.getTransportOut());

            // If request is REST assume that the response is REST too
            response.setDoingREST(axisOutMsgCtx.isDoingREST());
            if (axisOutMsgCtx.isDoingMTOM()) {
                response.setDoingMTOM(true);
                response.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }
            if (axisOutMsgCtx.isDoingSwA()) {
                response.setDoingSwA(true);
                response.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }

            // when axis2 receives a soap message without addressing headers it users
            // DISABLE_ADDRESSING_FOR_OUT_MESSAGES property to keep it and hence avoid addressing
            // headers on the response. this causes a problem for synapse if the original message
            // it receivs (from client) has addressing and the synaspse service invocation has not
            // engage addressing. in this case when synapse receives the response from the server
            // addessing In handler dissable addressing since that response does not have addressing
            // headers. synapse sends the response to its orignal client using the same message
            // context. Then this response does not have addressing headers since it already
            // disable. to avoid this we need to set the DISABLE_ADDRESSING_FOR_OUT_MESSAGES
            // property state to original state.
            if (axisOutMsgCtx.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES) != null) {
                
                response.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                        axisOutMsgCtx.getProperty(
                                AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
            } else {
                response.removeProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            }

            Object messageType = axisOutMsgCtx.getProperty(
                    org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
            if (!HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(messageType)) {
                 // copy the message type property that's used by the out message to the
                 // response message
                response.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                    messageType);
            }

            // compare original received message (axisOutMsgCtx) soap version with the response
            // if they are different change to original version 
            if(axisOutMsgCtx.isSOAP11() != response.isSOAP11()) {
            	if(axisOutMsgCtx.isSOAP11()) {
            		SOAPUtils.convertSOAP12toSOAP11(response);
            	} else {
            		SOAPUtils.convertSOAP11toSOAP12(response);
            	}
            }

            if (axisOutMsgCtx.getMessageID() != null) {
                response.setRelationships(
                        new RelatesTo[]{new RelatesTo(axisOutMsgCtx.getMessageID())});
            }

            response.setReplyTo(axisOutMsgCtx.getReplyTo());
            response.setFaultTo(axisOutMsgCtx.getFaultTo());

            if (axisOutMsgCtx.isPropertyTrue(NhttpConstants.IGNORE_SC_ACCEPTED)) {
                response.setProperty(NhttpConstants.FORCE_SC_ACCEPTED, Constants.VALUE_TRUE);
            }

            // axis2 client options still contains properties such as policy files used in
            // outgoing request. Need to remove those.
            removeUnwantedClientOptions(response);

            // create the synapse message context for the response
            Axis2MessageContext synapseInMessageContext =
                    new Axis2MessageContext(
                            response,
                            synapseOutMsgCtx.getConfiguration(),
                            synapseOutMsgCtx.getEnvironment());
            synapseInMessageContext.setResponse(true);

            Object obj = synapseOutMsgCtx.getProperty(SynapseConstants.FORCE_ERROR_PROPERTY);
            String errorOnSOAPFault = (String) obj;

            if (Constants.VALUE_TRUE.equals(errorOnSOAPFault) && successfulEndpoint != null) {

                if(log.isDebugEnabled()){
                    log.debug("FORCE_ERROR_ON_SOAP_FAULT is true, checking for SOAPFault");
                }
                
                try {
                    RelayUtils.buildMessage(((Axis2MessageContext) synapseInMessageContext).getAxis2MessageContext(),true);
                } catch (Exception e) {
                   // handleException("Error while building message", e, synapseInMessageContext);
                }

                if ((synapseInMessageContext.getEnvelope() != null) && synapseInMessageContext.getEnvelope().hasFault()) {
                
                    if(log.isDebugEnabled()){
                        log.debug("SOAPFault found in response message, forcing endpoint "+
                                successfulEndpoint.getName()+" to fail");
                    }
                    
                    //setup new pipe configuration..if failure happens (this will be setup as the source writer and during the TargetContext
                    //clean up operation the writer will be reset and pull to the buffer
                	MessageContext axis2OUTMC =((Axis2MessageContext) synapseOutMsgCtx).getAxis2MessageContext();
                    NHttpServerConnection conn = (NHttpServerConnection) axis2OUTMC.getProperty("pass-through.Source-Connection");
					if (conn != null) {
						SourceConfiguration sourceConfiguration = (SourceConfiguration) axis2OUTMC.getProperty("PASS_THROUGH_SOURCE_CONFIGURATION");
						Pipe pipe = new Pipe(conn, sourceConfiguration.getBufferFactory().getBuffer(), "source",
						                     sourceConfiguration);
						axis2OUTMC.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, pipe);
					}
                    
                    
                   
                    StatisticsReporter.reportFaultForAll(synapseOutMsgCtx,
                            ErrorLogFactory.createErrorLog(response));
                    synapseOutMsgCtx.setProperty(SynapseConstants.SENDING_FAULT, Boolean.TRUE);
                    synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_CODE, SynapseConstants.ENDPOINT_CUSTOM_ERROR);
                    
                    boolean failOver =false;
                    if(successfulEndpoint instanceof AbstractEndpoint){
                    	Endpoint endpoint =((AbstractEndpoint)successfulEndpoint).getParentEndpoint();
                    	if(endpoint != null && (endpoint instanceof FailoverEndpoint)){
                    		failOver =true;
                    	}
                    }
                   
                    if(failOver){
                    	 //we may required to handle same message for failover cases only other than that 
                    	 //should treat based on the incoming message
                    	 ((FaultHandler) successfulEndpoint).handleFault(synapseOutMsgCtx, null);
                    }else{
                    	faultStack = synapseOutMsgCtx.getFaultStack();
						if (faultStack != null) {
							synapseInMessageContext.getFaultStack().addAll(faultStack);
							((FaultHandler) successfulEndpoint).handleFault(synapseInMessageContext,
							                                                null);
						}
                    }
                    return;
                } else {
                    successfulEndpoint.onSuccess();
                }

            } else if(successfulEndpoint != null) {
                successfulEndpoint.onSuccess();
            }

            synapseInMessageContext.setTo(
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
            synapseInMessageContext.setTracingState(synapseOutMsgCtx.getTracingState());

            // set the properties of the original MC to the new MC

            for (Object key : synapseOutMsgCtx.getPropertyKeySet()) {
                synapseInMessageContext.setProperty(
                        (String) key, synapseOutMsgCtx.getProperty((String) key));
            }

            // Copy SequenceCallStack from original MC to the new MC
            Boolean isContinuationCall =
                                    (Boolean) synapseOutMsgCtx.getProperty(SynapseConstants.CONTINUATION_CALL);
            if (isContinuationCall != null && isContinuationCall) {

                // Set the message direction
                if (!synapseOutMsgCtx.isResponse()) {
                    synapseInMessageContext.setResponse(false);
                }

                Stack<ContinuationState> seqContinuationStates =
                        synapseOutMsgCtx.getContinuationStateStack();
                for (int i = 0; i < seqContinuationStates.size(); i++) {
                    synapseInMessageContext.pushContinuationState(seqContinuationStates.get(i));
                }
            }

            // If this response is related to session affinity endpoints -Server initiated session
            Dispatcher dispatcher =
                    (Dispatcher) synapseOutMsgCtx.getProperty(
                            SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_DISPATCHER);
            if (dispatcher != null && dispatcher.isServerInitiatedSession()) {
                dispatcher.updateSession(synapseInMessageContext);
            }

            StatisticsReporter.reportForAllOnResponseReceived(synapseInMessageContext);
            
            // send the response message through the synapse mediation flow
            try {
                synapseOutMsgCtx.getEnvironment().injectMessage(synapseInMessageContext);
            } catch (SynapseException syne) {
                Stack stack = synapseInMessageContext.getFaultStack();
                if (stack != null &&
                        !stack.isEmpty()) {
                    ((FaultHandler) stack.pop()).handleFault(synapseInMessageContext, syne);
                } else {
                    log.error("Synapse encountered an exception, " +
                            "No error handlers found - [Message Dropped]\n" + syne.getMessage());
                }
            }
        }
    }

    /**
     * Properties in client options such as inbound and outbound policy files used for outgoing
     * request may still be present. These need to be removed before sending the response as
     * they are no longer required.
     *
     * @param msgCtx Axis2 MessageContext
     */
    private void removeUnwantedClientOptions(MessageContext msgCtx) {
        Options clientOptions = msgCtx.getOptions().getParent().getParent();
        clientOptions.setProperty(SynapseConstants.RAMPART_OUT_POLICY, null);
        clientOptions.setProperty(SynapseConstants.RAMPART_IN_POLICY, null);
    }


    /**
     * It is possible for us (Synapse) to cause the creation of a duplicate relatesTo as we
     * try to hold onto the outgoing message ID even for POX messages using the relates to
     * Now once we get a response, make sure we remove any trace of this before we proceed any
     * further
     * @param mc the message context from which a possibly duplicated relatesTo should be removed
     * @param relates the existing relatedTo array of the message
     */
    private void removeDuplicateRelatesTo(MessageContext mc, RelatesTo[] relates) {

        int insertPos = 0;
        RelatesTo[] newRelates = new RelatesTo[relates.length];

        for (RelatesTo current : relates) {
            boolean found = false;
            for (int j = 0; j < newRelates.length && j < insertPos; j++) {
                if (newRelates[j].equals(current) ||
                        newRelates[j].getValue().equals(current.getValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newRelates[insertPos++] = current;
            }
        }

        RelatesTo[] trimmedRelates = new RelatesTo[insertPos];
        System.arraycopy(newRelates, 0, trimmedRelates, 0, insertPos);
        mc.setRelationships(trimmedRelates);
    }

}
