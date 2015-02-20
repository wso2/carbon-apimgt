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

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.AspectConfigurationDetectionStrategy;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.StatisticsReporter;
import org.apache.synapse.mediators.MediatorFaultHandler;

/**
 * This message receiver should be configured in the Axis2 configuration as the
 * default message receiver, which will handle all incoming messages through the
 * synapse mediation
 */
public class SynapseMessageReceiver implements MessageReceiver {

    private static final Log log = LogFactory.getLog(SynapseMessageReceiver.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    public void receive(org.apache.axis2.context.MessageContext mc) throws AxisFault {

        MessageContext synCtx = MessageContextCreatorForAxis2.getSynapseMessageContext(mc);

        StatisticsReporter.reportForComponent(synCtx,
                AspectConfigurationDetectionStrategy.getAspectConfiguration(synCtx),
                ComponentType.PROXYSERVICE);

        boolean traceOn = synCtx.getMainSequence().getTraceState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (traceOrDebugOn) {
            traceOrDebug(traceOn, "Synapse received a new message for message mediation...");
            traceOrDebug(traceOn, "Received To: " +
                (mc.getTo() != null ? mc.getTo().getAddress() : "null"));
            traceOrDebug(traceOn, "SOAPAction: " +
                (mc.getSoapAction() != null ? mc.getSoapAction() : "null"));
            traceOrDebug(traceOn, "WSA-Action: " +
                (mc.getWSAAction() != null ? mc.getWSAAction() : "null"));

            if (traceOn && trace.isTraceEnabled()) {
                String[] cids = mc.getAttachmentMap().getAllContentIDs();
                if (cids != null && cids.length > 0) {
                    for (String cid : cids) {
                        trace.trace("Attachment : " + cid);
                    }
                }
                trace.trace("Envelope : " + mc.getEnvelope());
            }
        }

        // get service log for this message and attach to the message context
        Log serviceLog = LogFactory.getLog(SynapseConstants.SERVICE_LOGGER_PREFIX +
            SynapseConstants.SYNAPSE_SERVICE_NAME);
        ((Axis2MessageContext) synCtx).setServiceLog(serviceLog);

        try {
            // invoke synapse message mediation through the main sequence
            synCtx.getEnvironment().injectMessage(synCtx);

        } catch (SynapseException syne) {

            if (!synCtx.getFaultStack().isEmpty()) {
                warn(traceOn, "Executing fault handler due to exception encountered", synCtx);
                ((FaultHandler) synCtx.getFaultStack().pop()).handleFault(synCtx, syne);

            } else {
                warn(traceOn, "Exception encountered but no fault handler found - " +
                    "message dropped", synCtx);
            }
        } finally {
            StatisticsReporter.endReportForAllOnRequestProcessed(synCtx);
        }
    }

    private void traceOrDebug(boolean traceOn, String msg) {
        if (traceOn) {
            trace.info(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    private void warn(boolean traceOn, String msg, MessageContext msgContext) {
        if (traceOn) {
            trace.warn(msg);
        }
        if (log.isDebugEnabled()) {
            log.warn(msg);
        }
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().warn(msg);
        }
    }
}
