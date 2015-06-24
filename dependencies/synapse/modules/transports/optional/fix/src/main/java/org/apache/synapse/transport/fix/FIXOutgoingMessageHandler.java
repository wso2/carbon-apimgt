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

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXOutgoingMessageHandler makes sure that messages are delivered in the order they were received by
 * a FIX acceptor. In case the message arrived over a different transport still this class will try to
 * put the messages in correct order based on the counter value of the message.
 */
public class FIXOutgoingMessageHandler {

    private static final Log log = LogFactory.getLog(FIXOutgoingMessageHandler.class);

    private Map<String, Integer> countersMap;
    private Map<String, Map<Integer,Object[]>> messagesMap;
    private FIXSessionFactory sessionFactory;

    public FIXOutgoingMessageHandler() {
        countersMap = new ConcurrentHashMap<String, Integer>();
        messagesMap = new ConcurrentHashMap<String, Map<Integer,Object[]>>();
    }

    public void setSessionFactory(FIXSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Performs the actual send operation on the message. Tries to send the messages in the order they
     * arrived over the FIX transport
     *
     * @param message the FIX message to be sent
     * @param targetSession ID of the target FIXSession
     * @param sourceSession String that uniquely identifies the incoming session
     * @param counter application level sequence number of the message
     * @param msgCtx Axis2 MessageContext for the outgoing message
     * @param targetEPR the target EPR to forward the message
     *
     * @throws SessionNotFound on error
     */
    public synchronized void sendMessage(Message message, SessionID targetSession, String sourceSession,
                            int counter, MessageContext msgCtx, String targetEPR) throws SessionNotFound {

        boolean ignoreOrder = "true".equals(msgCtx.getProperty(FIXConstants.FIX_IGNORE_ORDER));
        if (sourceSession != null && counter != -1 && !ignoreOrder) {

            int expectedValue;
            if (countersMap.containsKey(sourceSession)) {
                expectedValue = countersMap.get(sourceSession);
            } else {
                //create new entries in the respective Maps
                //counter starts at 1
                countersMap.put(sourceSession, 1);
                messagesMap.put(sourceSession, new ConcurrentHashMap<Integer,Object[]>());
                expectedValue = 1;
            }

            if (expectedValue == counter) {
                sendToTarget(msgCtx, targetEPR, message, targetSession);
                if (FIXConstants.DEFAULT_COUNTER_UPPER_LIMIT == expectedValue) {
                    if (log.isDebugEnabled()) {
                        log.debug("Outgoing request counter rolled over for the session: " +
                                sourceSession + " (from " + expectedValue + ")");
                    }
                    expectedValue = 1;
                }
                countersMap.put(sourceSession, ++expectedValue);
                sendQueuedMessages(expectedValue, sourceSession);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Source session: " + sourceSession + " - Expected sequence number (" +
                            expectedValue + ") does not match with the actual sequence number (" +
                            counter + "). Holding the message back for later delivery.");
                }

                //save the message to be sent later...
                Map<Integer,Object[]> messages = messagesMap.get(sourceSession);
                Object[] obj = new Object[] { message, targetSession, msgCtx, targetEPR } ;
                messages.put(counter, obj);
            }
        }
        else {
            //insufficient information to send the messages in order...
            // send it right away...
            sendToTarget(msgCtx, targetEPR, message, targetSession);
        }
    }

    /**
     * Sends the FIX message to the given target session. If MessageContext and the target EPR
     * are not null then save the outgoing MessageContext in the FIX application to handle the
     * response.
     *
     * @param msgCtx the Axis2 MessageContext of the outgoing message
     * @param targetEPR the target EPR to send the message
     * @param message the FIX message
     * @param sessionID the ID of the target FIX session
     *
     * @throws SessionNotFound on error
     */
    private void sendToTarget(MessageContext msgCtx, String targetEPR, Message message,
                              SessionID sessionID) throws SessionNotFound {
        if (msgCtx != null && targetEPR != null) {
            FIXIncomingMessageHandler messageHandler = (FIXIncomingMessageHandler) sessionFactory.
                    getApplication(targetEPR);
            if (messageHandler != null) {
                messageHandler.setOutgoingMessageContext(msgCtx);
            }
        }
        Session.sendToTarget(message, sessionID);
    }

    /**
     * Sends any messages in the queues. Maintains the order of the messages.
     *
     * @param expectedValue expected counter value
     * @param session source FIX session
     *
     * @throws SessionNotFound on error
     */
    private void sendQueuedMessages(int expectedValue, String session) throws SessionNotFound {
        Map<Integer, Object[]> messages = messagesMap.get(session);
        Object[] obj = messages.get(expectedValue);
        while (obj != null) {
            Message message = (Message) obj[0];
            SessionID sessionID = (SessionID) obj[1];
            MessageContext msgCtx = null;
            String targetEPR = null;
            if (obj[2] != null) {
                msgCtx = (MessageContext) obj[2];
                targetEPR = (String) obj[3];
            }

            if (log.isDebugEnabled()) {
                log.debug("Source session: " + session + " - Sending the previously queued message " +
                        "with the sequence number: " + expectedValue);
            }
            sendToTarget(msgCtx, targetEPR, message, sessionID);
            messages.remove(expectedValue);
            if (FIXConstants.DEFAULT_COUNTER_UPPER_LIMIT == expectedValue) {
                if (log.isDebugEnabled()) {
                    log.debug("Outgoing request counter rolled over for the session: " + session +
                        " (from " + expectedValue + ")");
                }
                expectedValue = 1;
            }
            obj = messages.get(++expectedValue);
        }
        messagesMap.put(session, messages);
        countersMap.put(session, expectedValue);
    }

    public void cleanUpMessages(String session) {
        if (countersMap.containsKey(session)) {
            int expectedValue = countersMap.get(session);
            Map<Integer,  Object[]> messages = messagesMap.get(session);
            while (!messages.isEmpty()) {
                Object[] obj = messages.get(expectedValue);
                if (obj != null) {
                    Message message = (Message) obj[0];
                    SessionID sessionID = (SessionID) obj[1];

                    if (log.isDebugEnabled()) {
                        log.debug("Source session: " + session + " - Flushing the previously queued " +
                                "message with the sequence number: " + expectedValue);
                    }

                    try {
                        Session.sendToTarget(message, sessionID);
                    } catch (SessionNotFound ignore) { }

                    messages.remove(expectedValue);
                }
                expectedValue++;
            }
            messagesMap.remove(session);
            countersMap.remove(session);
        }
    }
}