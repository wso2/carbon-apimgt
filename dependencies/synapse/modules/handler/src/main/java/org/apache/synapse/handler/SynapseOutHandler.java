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

package org.apache.synapse.handler;

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.synapse.handler.util.HandlerUtil;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.SynapseException;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.SynapseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the OutHandler which injects the Axis2MC in to Synapse after converting the message
 * context to a SynapseMC
 */
public class SynapseOutHandler extends AbstractHandler {

    /**
     * Log variable which will be used for logging
     */
    private static final Log log = LogFactory.getLog(SynapseOutHandler.class);

    /**
     * This method will inject the message into Synapse after creating the SynapseMC from the
     * Axis2MC and after the mediation if synapse lets the message to flow through this will let
     * the message to flow and if not aborts the message
     *
     * @param messageContext - Axis2MC to be mediated using Synapse
     * @return InvocationResponse.CONTINUE if Synapse lets the message to flow and
     *  InvocationResponse.ABORT if not
     * @throws AxisFault - incase of a failure in mediation of initiation of the mediation
     */
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        HandlerUtil.doHandlerLogging(log, messageContext);

        org.apache.synapse.MessageContext synCtx
                = MessageContextCreatorForAxis2.getSynapseMessageContext(messageContext);

        // handles the incoming and outgoing behaviours in client and server sides
        if (messageContext.isServerSide()) {
            synCtx.setProperty(SynapseConstants.RESPONSE, Boolean.TRUE);
            synCtx.setResponse(true);
            synCtx.setProperty(SynapseConstants.RESPONSE, Boolean.TRUE);
            synCtx.setResponse(true);
            try {
                // if synapse says ok let the message to flow through
                if (HandlerUtil.mediateOutMessage(log, messageContext, synCtx)) {
                    return InvocationResponse.CONTINUE;
                } else {
                    // if not abort the further processings
                    log.debug("Synapse has decided to abort the message:\n" + synCtx);
                    return InvocationResponse.ABORT;
                }
            } catch (SynapseException syne) {
                // todo : invoke the fault sequence
            }
        } else {
            synCtx.setProperty(SynapseConstants.RESPONSE, Boolean.FALSE);
            synCtx.setResponse(false);
            try {
                // if synapse says ok let the message to flow through
                if (HandlerUtil.mediateInMessage(log, messageContext, synCtx)) {
                    return InvocationResponse.CONTINUE;
                } else {
                    // if not abort the further processings
                    log.debug("Synapse has decided to abort the message:\n" + synCtx);
                    return InvocationResponse.ABORT;
                }
            } catch (SynapseException syne) {
                // todo : invoke the fault sequence
            }
        }

        try {
            // if synapse says ok let the message to flow through
            if (synCtx.getEnvironment().injectMessage(synCtx)) {
                return InvocationResponse.CONTINUE;
            } else {
                // if not abort the further processings
                log.debug("Synapse has decided to abort the message:\n" + synCtx.getEnvelope());
                return InvocationResponse.ABORT;
            }
        } catch (SynapseException syne) {
            if (!synCtx.getFaultStack().isEmpty()) {
                ((FaultHandler) synCtx.getFaultStack().pop()).handleFault(synCtx, syne);
            } else {
                log.error("Synapse encountered an exception, " +
                        "No error handlers found.\n" + syne.getMessage());
                throw new AxisFault("Synapse encountered an error." + syne);
            }
        }

        // general case should let the message flow
        return InvocationResponse.CONTINUE;
    }
}
