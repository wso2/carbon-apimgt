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

package org.apache.synapse.handler.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.synapse.Mediator;
import org.apache.synapse.handler.HandlerConstants;

/**
 * This is a helper class to get the loggin done in both in and out handlers
 */
public class HandlerUtil {

    /**
     * Helper util method to get the logging done whenever injecting the message into synapse
     *
     * @param log - Log appender to be used to append logs
     * @param messageContext - MessageContext which will be logged
     */
    public static void doHandlerLogging(Log log, MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("Synapse handler received a new message for message mediation...");
            log.debug("Received To: " + (messageContext.getTo() != null ?
                    messageContext.getTo().getAddress() : "null"));
            log.debug("SOAPAction: " + (messageContext.getSoapAction() != null ?
                    messageContext.getSoapAction() : "null"));
            log.debug("WSA-Action: " + (messageContext.getWSAAction() != null ?
                    messageContext.getWSAAction() : "null"));
            String[] cids = messageContext.getAttachmentMap().getAllContentIDs();
            if (cids != null && cids.length > 0) {
                for (int i = 0; i < cids.length; i++) {
                    log.debug("Attachment : " + cids[i]);
                }
            }
            log.debug("Body : \n" + messageContext.getEnvelope());
        }
    }

    public static boolean mediateInMessage(Log log, MessageContext messageContext,
                                           org.apache.synapse.MessageContext synCtx)
            throws AxisFault {

        AxisService service = messageContext.getAxisService();
        if (service != null) {
            Parameter inMediationParam
                    = service.getParameter(HandlerConstants.IN_SEQUENCE_PARAM_NAME);
            if (inMediationParam != null && inMediationParam.getValue() != null) {
                if (inMediationParam.getValue() instanceof Mediator) {
                    Mediator inMessageSequence = (Mediator) inMediationParam.getValue();
                    return inMessageSequence.mediate(synCtx);
                } else if (inMediationParam.getValue() instanceof String) {
                    Mediator inMessageSequence = synCtx.getConfiguration().getSequence(
                            (String) inMediationParam.getValue());
                    return inMessageSequence.mediate(synCtx);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("The provided in message mediation " +
                                "sequence is not a proper mediator");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the incoming mediation for the service "
                            + service.getName());
                }
            }
        } else {
            String message = "Couldn't find the Service for the associated message with id "
                    + messageContext.getMessageID();
            log.error(message);
            throw new AxisFault(message);
        }
        return true;
    }

    public static boolean mediateOutMessage(Log log, MessageContext messageContext,
                                           org.apache.synapse.MessageContext synCtx)
            throws AxisFault {

        AxisService service = messageContext.getAxisService();
        if (service != null) {
            Parameter inMediationParam
                    = service.getParameter(HandlerConstants.OUT_SEQUENCE_PARAM_NAME);
            if (inMediationParam != null && inMediationParam.getValue() != null) {
                if (inMediationParam.getValue() instanceof Mediator) {
                    Mediator inMessageSequence = (Mediator) inMediationParam.getValue();
                    return inMessageSequence.mediate(synCtx);
                } else if (inMediationParam.getValue() instanceof String) {
                    Mediator inMessageSequence = synCtx.getConfiguration().getSequence(
                            (String) inMediationParam.getValue());
                    return inMessageSequence.mediate(synCtx);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("The provided out message mediation " +
                                "sequence is not a proper mediator");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the outgoing mediation for the service "
                            + service.getName());
                }
            }
        } else {
            String message = "Couldn't find the Service for the associated message with id "
                    + messageContext.getMessageID();
            log.error(message);
            throw new AxisFault(message);
        }
        return true;
    }
}
