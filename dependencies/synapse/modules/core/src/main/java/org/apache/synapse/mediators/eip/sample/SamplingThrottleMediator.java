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

package org.apache.synapse.mediators.eip.sample;

import org.apache.axis2.Constants;
import org.apache.axis2.context.OperationContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.eip.Target;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This implements the well known <code>Sample</code> EIP (Enterprise Integration Pattern), which controls the flow
 * of messages and limit the rate at which the messages are flowing through the sampler</p>
 *
 * <p>Please note that the usage of this will require the sampler to be on the out-flow as well to correctly
 * determine & to manage the rate.</p>
 *
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class SamplingThrottleMediator extends AbstractMediator implements ManagedLifecycle {

    /** Rate at which this mediator allows the flow in TPS */
    private int samplingRate = 1;

    /** Unit time in milliseconds applied to the <code>samplingRate</code> */
    private long unitTime = 1000;

    /** Identifier is used to co-relate the in and out path samplers */
    private String id;

    /** Target to be used for mediation from the sampler */
    private Target target;

    /**
     * {@link org.apache.synapse.mediators.eip.sample.MessageQueue} implementation to be used,
     * which defaults to {@link org.apache.synapse.mediators.eip.sample.UnboundedMessageQueue}
     */
    private MessageQueue messageQueue = new UnboundedMessageQueue();

    private boolean messageQueueExplicitlySet;

    private TimerTask messageProcessor;

    public void init(SynapseEnvironment synapseEnvironment) {

        if (messageQueue.isPersistent()) {
            log.info("Loading the persisted messages if there are any to the message queue");
            messageQueue.load();
        }

        Timer samplingTimer = synapseEnvironment.getSynapseConfiguration().getSynapseTimer();
        messageProcessor = new MessageProcessor();
        log.info("Scheduling the sampling timer to invoke the message processor " +
                "at an interval of : " + unitTime);
        samplingTimer.schedule(messageProcessor, 0, unitTime);
    }

    public void destroy() {
        messageProcessor.cancel();
        if (!messageQueue.isEmpty()) {
            log.warn("There are messages on the sampling message queue, " +
                    "but the message processor has been destroyed.");
            if (messageQueue.isPersistent()) {
                if (log.isDebugEnabled()) {
                    log.debug("Persisting the messages on the message queue");
                }
                if (messageQueue.persist()) {
                    log.info("Completed persisting the messages on the message queue");
                } else {
                    log.error("Couldn't persist the messages on the message queue");
                }
            } else {
                log.warn("You are not using a persistent message queue, " +
                        "you will be loosing messages which are on the queue");
            }
        }
    }

    public boolean mediate(MessageContext messageContext) {

        SynapseLog synLog = getLog(messageContext);

        synLog.traceOrDebug("Start : Sampler mediator");
        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Message : " + messageContext.getEnvelope());
        }

        if (!messageContext.isResponse()) {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Adding the message with message id : "
                        + messageContext.getMessageID() + " into the message queue for sampling");
            }
            messageQueue.add(messageContext);
        } else {
            synLog.auditWarn("Encountered a response message which will not be sampled");
        }

        OperationContext opCtx
            = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getOperationContext();
        if (opCtx != null) {
            opCtx.setProperty(Constants.RESPONSE_WRITTEN, "SKIP");
        }

        synLog.traceOrDebug("End : Sampler mediator");
        return false;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public long getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(long unitTime) {
        this.unitTime = unitTime;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
        this.messageQueueExplicitlySet = true;
    }

    public boolean isMessageQueueExplicitlySet() {
        return messageQueueExplicitlySet;
    }

    private class MessageProcessor extends TimerTask {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Started running the message processor");
            }
            for (int i = 0; i < samplingRate && !messageQueue.isEmpty(); i++) {
                MessageContext synCtx = messageQueue.get();
                if (log.isDebugEnabled()) {
                    log.debug("Mediating the message on the message queue with message id : "
                            + synCtx.getMessageID());
                }
                target.mediate(synCtx);
            }
            if (log.isDebugEnabled()) {
                log.debug("Message processing completed for the given sampling rate");
            }
        }
    }

}
