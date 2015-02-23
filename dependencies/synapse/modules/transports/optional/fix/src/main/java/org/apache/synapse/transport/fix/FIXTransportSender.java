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

package org.apache.synapse.transport.fix;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.commons.logging.LogFactory;
import quickfix.*;
import quickfix.field.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * The FIX transport sender implementation. This implementation looks at the SOAPBody of the message
 * context to identify how the message was first received by Axis2 engine and also looks at some FIX
 * header fields to make the optimum routing decision.
 * <p/>
 * This transport sender implementation does not support forwarding FIX messages to sessions with
 * different BeginString values.When it performs a message forwarding it makes sure the forwarding
 * takes place according to the conditions specified in the 'Third Party Routing' section in the
 * FIX protocol specification.
 */
public class FIXTransportSender extends AbstractTransportSender {

    private FIXSessionFactory sessionFactory;
    private FIXOutgoingMessageHandler messageSender;
    private WorkerPool workerPool;

    public FIXTransportSender() {
        this.log = LogFactory.getLog(this.getClass());
    }

    /**
     * @param cfgCtx       the axis2 configuration context
     * @param transportOut the Out Transport description
     * @throws AxisFault on error
     */
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
        this.sessionFactory = FIXSessionFactory.getInstance(new FIXApplicationFactory(cfgCtx));
        this.workerPool = WorkerPoolFactory.getWorkerPool(
                            10, 20, 5, -1, "FIX Sender Worker thread group", "FIX-Worker");
        this.sessionFactory.setSenderThreadPool(this.workerPool);
        messageSender = new FIXOutgoingMessageHandler();
        messageSender.setSessionFactory(this.sessionFactory);
        log.info("FIX transport sender initialized...");
    }

    public void stop() {
        try {
            this.workerPool.shutdown(10000);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted while waiting for worker pool to shut down");
        }
        sessionFactory.disposeFIXInitiators();
        super.stop();
    }

    /**
     * Performs the actual sending of the message.
     *
     * @param msgCtx           the axis2 message context of the message to be sent
     * @param targetEPR        the EPR for which the message is to be sent
     * @param outTransportInfo the OutTransportInfo for the message
     * @throws AxisFault on error
     */
    public void sendMessage(MessageContext msgCtx, String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Attempting to send a FIX message, Message ID:" + msgCtx.getMessageID());
        }
        Message fixMessage = null;
        String serviceName = FIXUtils.getServiceName(msgCtx);
        String fixApplication = FIXUtils.getFixApplication(msgCtx);
        String sourceSession = FIXUtils.getSourceSession(msgCtx);
        int counter = FIXUtils.getSequenceNumber(msgCtx);

        try {
            fixMessage = FIXUtils.getInstance().createFIXMessage(msgCtx);
        } catch (IOException e) {
            handleException("Exception occurred while creating the FIX message from SOAP Envelope", e);
        }

        if (FIXConstants.FIX_ACCEPTOR.equals(fixApplication)) {
            //A message came in through an acceptor bound to a service
            if (targetEPR != null) {
                //Forward the message to the given EPR
                sendUsingEPR(targetEPR, serviceName, fixMessage, sourceSession, counter, msgCtx);
            } else if (outTransportInfo != null && outTransportInfo instanceof FIXOutTransportInfo) {
                //Send the message back to the sender
                sendUsingTrpOutInfo(outTransportInfo, serviceName, fixMessage,
                        sourceSession, counter, msgCtx);
            }
        } else if (FIXConstants.FIX_INITIATOR.equals(fixApplication)) {

            if (sendUsingAcceptorSession(serviceName, fixMessage, sourceSession, counter, msgCtx)) {
                return;
            } else if (targetEPR != null) {
                sendUsingEPR(targetEPR, serviceName, fixMessage, sourceSession, counter, msgCtx);
                return;
            }
            handleException("Unable to find a session to send the message...");

        } else {
            //A message generated in Axis2 engine or a message arrived over a different transport
            if (targetEPR != null) {
                sendUsingEPR(targetEPR, serviceName, fixMessage, sourceSession, counter, msgCtx);
            } else {
                sendUsingAcceptorSession(serviceName, fixMessage, sourceSession, counter, msgCtx);
            }
        }
    }

    private boolean isTargetValid(Map<String, String> fieldValues, SessionID targetSession,
                                 boolean beginStrValidation) {
        
        String beginString = fieldValues.get(FIXConstants.BEGIN_STRING);
        String deliverToCompID = fieldValues.get(FIXConstants.DELIVER_TO_COMP_ID);
        String deliverToSubID = fieldValues.get(FIXConstants.DELIVER_TO_SUB_ID);
        String deliverToLocationID = fieldValues.get(FIXConstants.DELIVER_TO_LOCATION_ID);

        if (beginStrValidation && !targetSession.getBeginString().equals(beginString)) {
            return false;
        } else if (!targetSession.getTargetCompID().equals(deliverToCompID)) {
            return false;
        } else if (deliverToSubID != null && !deliverToSubID.equals(targetSession.getTargetSubID())) {
            return false;
        } else if (deliverToLocationID != null && !deliverToLocationID.equals(targetSession.getTargetLocationID())) {
            return false;
        }
        return true;
    }

    /**
     * Prepares the message to be forwarded according to the conditions specified in the FIX protocol
     * specification.
     *
     * @param message     the FIX message to be forwarded
     * @param fieldValues a Map of field values for quick access
     */
    private void prepareToForwardMessage(Message message, Map<String, String> fieldValues) {
        //set OnBehalfOf* fields
        message.getHeader().setField(new OnBehalfOfCompID(fieldValues.get(FIXConstants.SENDER_COMP_ID)));
        if (fieldValues.get(FIXConstants.SENDER_SUB_ID) != null) {
            message.getHeader().setField(new OnBehalfOfSubID(fieldValues.get(FIXConstants.SENDER_SUB_ID)));
        }

        if (fieldValues.get(FIXConstants.SENDER_LOCATION_ID) != null) {
            message.getHeader().setField(new OnBehalfOfLocationID(fieldValues.get(FIXConstants.SENDER_LOCATION_ID)));
        }

        //remove additional Sender* fields and DeliverTo* fields
        message.getHeader().removeField(SenderSubID.FIELD);
        message.getHeader().removeField(SenderLocationID.FIELD);
        message.getHeader().removeField(DeliverToCompID.FIELD);
        message.getHeader().removeField(DeliverToSubID.FIELD);
        message.getHeader().removeField(DeliverToLocationID.FIELD);
    }

    /**
     * Puts DeliverToX fields in the message to enable the message to be forwarded at the destination.
     *
     * @param message   the FIX message to be forwarded
     * @param targetEPR the EPR to which the message will be sent
     */
    private void setDeliverToXFields(Message message, String targetEPR) {
        Hashtable<String, String> properties = BaseUtils.getEPRProperties(targetEPR);
        String deliverTo = properties.get(FIXConstants.DELIVER_TO_COMP_ID);
        //If a DeliverToCompID field is given in EPR put the field in the message
        if (deliverTo != null) {
            message.getHeader().setField(new DeliverToCompID(deliverTo));
            deliverTo = properties.get(FIXConstants.DELIVER_TO_SUB_ID);
            if (deliverTo != null) {
                message.getHeader().setField(new DeliverToSubID(deliverTo));
            }

            deliverTo = properties.get(FIXConstants.DELIVER_TO_LOCATION_ID);
            if (deliverTo != null) {
                message.getHeader().setField(new DeliverToLocationID(deliverTo));
            }
        }
    }

    /**
     * Puts DeliverToX fields in the message to enable the message to be forwarded at the destination.
     *
     * @param message     the FIX message to be forwarded
     * @param fieldValues the Map of field values for quick access
     */
    private void setDeliverToXFields(Message message, Map<String, String> fieldValues) {
        //Use the fields of the message to set DeliverToX fields
        String onBehalf = fieldValues.get(FIXConstants.ON_BEHALF_OF_COMP_ID);
        if (onBehalf != null) {
            message.getHeader().setField(new DeliverToCompID(onBehalf));
            onBehalf = fieldValues.get(FIXConstants.ON_BEHALF_OF_SUB_ID);
            if (onBehalf != null) {
                message.getHeader().setField(new DeliverToSubID(onBehalf));
            }

            onBehalf = fieldValues.get(FIXConstants.ON_BEHALF_OF_LOCATION_ID);
            if (onBehalf != null) {
                message.getHeader().setField(new DeliverToLocationID(onBehalf));
            }

            message.getHeader().removeField(OnBehalfOfCompID.FIELD);
            message.getHeader().removeField(OnBehalfOfSubID.FIELD);
            message.getHeader().removeField(OnBehalfOfLocationID.FIELD);
        }
    }

    /**
     * Puts DeliverToX fields in the message to enable the message to be forwarded at the destination.
     * This method retrieves the parameters from the services.xml and put them in the message as
     * DeliverToX fields. Should be used when a response message has to forwarded at the destination.
     *
     * @param message the FIX message to be forwarded
     * @param service the AxisService of the message
     */
    private void setDeliverToXFields(Message message, AxisService service) {
        Parameter param = service.getParameter(FIXConstants.FIX_RESPONSE_DELIVER_TO_COMP_ID_PARAM);
        if (param != null) {
            message.getHeader().setField(new DeliverToCompID(param.getValue().toString()));
            param = service.getParameter(FIXConstants.FIX_RESPONSE_DELIVER_TO_SUB_ID_PARAM);
            if (param != null) {
                message.getHeader().setField(new DeliverToSubID(param.getValue().toString()));
            }

            param = service.getParameter(FIXConstants.FIX_RESPONSE_DELIVER_TO_LOCATION_ID_PARAM);
            if (param != null) {
                message.getHeader().setField(new DeliverToLocationID(param.getValue().toString()));
            }
        }
    }

    /**
     * Sends a FIX message to the given EPR
     *
     * @param targetEPR   the EPR to which the message is sent to
     * @param serviceName name of the service which processed the message
     * @param fixMessage  the FIX message
     * @param srcSession  String uniquely identifying the incoming session
     * @param counter     application level sequence number of the message
     * @param msgCtx      the Axis2 MessageContext for the message
     * @return boolean value indicating the result
     * @throws AxisFault on error
     */
    private boolean sendUsingEPR(String targetEPR, String serviceName, Message fixMessage,
                                 String srcSession, int counter, MessageContext msgCtx) throws AxisFault {

        FIXOutTransportInfo fixOut = new FIXOutTransportInfo(targetEPR);
        SessionID sessionID = fixOut.getSessionID();
        Map<String, String> fieldValues = FIXUtils.getMessageForwardingParameters(fixMessage);
        String beginString = fieldValues.get(FIXConstants.BEGIN_STRING);
        String deliverToCompID = fieldValues.get(FIXConstants.DELIVER_TO_COMP_ID);

        AxisService service = cfgCtx.getAxisConfiguration().getService(serviceName);

        //match BeginString values
        if (isValidationOn(service) && beginString != null && !beginString.equals(sessionID.getBeginString())) {
            handleException("BeginString validation is on. Cannot forward messages to a session" +
                    " with a different BeginString");
         }

        if (deliverToCompID != null) {
            //message needs to be delivered
            if (!deliverToCompID.equals(sessionID.getTargetCompID())) {
                handleException("Cannot forward messages that do not have a valid DeliverToCompID field");
            } else {
                prepareToForwardMessage(fixMessage, fieldValues);
                setDeliverToXFields(fixMessage, targetEPR);
            }
        }

        if (!Session.doesSessionExist(sessionID)) {
            //try to create initiator to send the message
            sessionFactory.createFIXInitiator(targetEPR, service, sessionID);
        }

        try {
            messageSender.sendMessage(fixMessage, sessionID, srcSession, counter, msgCtx, targetEPR);
            return true;
        } catch (SessionNotFound e) {
            log.error("Error while sending the FIX message. Session " + sessionID.toString() + " does" +
                    " not exist", e);
            return false;
        }
    }

    /**
     * Sends a FIX message using the SessionID in the OutTransportInfo
     *
     * @param trpOutInfo  the TransportOutInfo for the message
     * @param fixMessage  the FIX message to be sent
     * @param srcSession  String uniquely identifying the incoming session
     * @param counter     application level sequence number of the message
     * @param serviceName name of the AxisService for the message
     * @param msgCtx      Axis2 MessageContext
     * @return boolean value indicating the result
     * @throws AxisFault on error
     */
    private boolean sendUsingTrpOutInfo(OutTransportInfo trpOutInfo, String serviceName,
                                        Message fixMessage, String srcSession, int counter,
                                        MessageContext msgCtx) throws AxisFault {

        FIXOutTransportInfo fixOut = (FIXOutTransportInfo) trpOutInfo;
        SessionID sessionID = fixOut.getSessionID();
        Map<String, String> fieldValues = FIXUtils.getMessageForwardingParameters(fixMessage);
        String beginString = fieldValues.get(FIXConstants.BEGIN_STRING);
        String deliverToCompID = fieldValues.get(FIXConstants.DELIVER_TO_COMP_ID);

        AxisService service = cfgCtx.getAxisConfiguration().getService(serviceName);

        //match BeginString values
        if (isValidationOn(service) && beginString != null && !beginString.equals(sessionID.getBeginString())) {
            handleException("BeginString validation is on. Cannot forward messages to a session" +
                    " with a different BeginString");
         }

        if (deliverToCompID != null) {
            //message needs to be delivered to some other party
            if (!deliverToCompID.equals(sessionID.getTargetCompID())) {
                handleException("Cannot forward messages that do not have a valid DeliverToCompID field");
            } else {
                prepareToForwardMessage(fixMessage, fieldValues);
                setDeliverToXFields(fixMessage, service);
            }
        } else {
            setDeliverToXFields(fixMessage, fieldValues);
        }

        try {
            messageSender.sendMessage(fixMessage, sessionID, srcSession, counter, msgCtx, null);
            return true;
        } catch (SessionNotFound e) {
            log.error("Error while sending the FIX message. Session " + sessionID.toString() + " does" +
                    " not exist", e);
            return false;
        }
    }

    /**
     * Send the message using a session in the acceptor side
     *
     * @param serviceName the service of the message
     * @param fixMessage  the FIX message to be sent
     * @param srcSession  String uniquely identifying the incoming session
     * @param counter     the application level sequence number of the message
     * @param msgCtx      Axi2 MessageContext
     * @return boolean value indicating the result
     * @throws AxisFault on error
     */
    private boolean sendUsingAcceptorSession(String serviceName, Message fixMessage, String srcSession,
                                             int counter, MessageContext msgCtx) throws AxisFault {

        Map<String, String> fieldValues = FIXUtils.getMessageForwardingParameters(fixMessage);
        String deliverToCompID = fieldValues.get(FIXConstants.DELIVER_TO_COMP_ID);

        Acceptor acceptor = sessionFactory.getAcceptor(serviceName);
        SessionID sessionID = null;

        AxisService service = cfgCtx.getAxisConfiguration().getService(serviceName);

        if (acceptor != null) {
            ArrayList<SessionID> sessions = acceptor.getSessions();
            if (sessions.size() == 1) {
                sessionID = sessions.get(0);
                if (deliverToCompID != null && !isTargetValid(fieldValues, sessionID, isValidationOn(service))) {
                    sessionID = null;
                }

            } else if (sessions.size() > 1 && deliverToCompID != null) {
                for (SessionID session : sessions) {
                    sessionID = session;
                    if (isTargetValid(fieldValues, sessionID, isValidationOn(service))) {
                        break;
                    }
                }
            }
        }

        if (sessionID != null) {
            //Found a valid session. Now forward the message...
            FIXOutTransportInfo fixOutInfo = new FIXOutTransportInfo(sessionID);
            return sendUsingTrpOutInfo(fixOutInfo, serviceName, fixMessage,
                    srcSession, counter, msgCtx);
        }
        return false;
    }

    /**
     * Checks whether BeginString validation is on for the specified
     * service.
     *
     * @param service the AxisService of the message
     * @return a boolean value indicating the validation state
     */
    private boolean isValidationOn(AxisService service) {
        Parameter validationParam = service.getParameter(FIXConstants.FIX_BEGIN_STRING_VALIDATION);
        if (validationParam != null) {
            if ("true".equals(validationParam.getValue().toString())) {
                return true;
            }
        }
        return false;
    }

    public void logOutIncomingSession(SessionID sessionID) {
        messageSender.cleanUpMessages(sessionID.toString());
    }

}
