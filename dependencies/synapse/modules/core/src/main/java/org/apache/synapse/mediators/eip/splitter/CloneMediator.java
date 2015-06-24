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

package org.apache.synapse.mediators.eip.splitter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.OperationContext;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.eip.EIPConstants;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.util.MessageHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This mediator will clone the message into multiple messages and mediate as specified in the
 * target elements. A target specifies or refers to a sequence or an endpoint, and optionally
 * specifies an Action and/or To address to be set to the cloned message. The number of cloned
 * messages created is the number of targets specified
 */
public class CloneMediator extends AbstractMediator implements ManagedLifecycle,
                                                               FlowContinuableMediator {

    /**
     * Continue processing the parent message or not?
     * (i.e. message which is subjected to cloning)
     */
    private boolean continueParent = false;

    /** the list of targets to which cloned copies of the message will be given for mediation */
    private List<Target> targets = new ArrayList<Target>();

    private String id = null;

    private boolean sequential = false;

    /** Reference to the synapse environment */
    private SynapseEnvironment synapseEnv;

    /**
     * This will implement the mediate method of the Mediator interface and will provide the
     * functionality of cloning message into the specified targets and mediation
     *
     * @param synCtx - MessageContext which is subjected to the cloning
     * @return boolean true if this needs to be further mediated (continueParent=true)
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Clone mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        // get the targets list, clone the message for the number of targets and then
        // mediate the cloned messages using the targets
        Iterator<Target> iter = targets.iterator();
        int i = 0;
        while (iter.hasNext()) {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Submitting " + (i+1) + " of " + targets.size() +
                    " messages for " + (isSequential() ? "sequential processing" : "parallel processing"));
            }

            MessageContext clonedMsgCtx = getClonedMessageContext(synCtx, i++, targets.size());
            ContinuationStackManager.addReliantContinuationState(clonedMsgCtx, i - 1, getMediatorPosition());
            iter.next().mediate(clonedMsgCtx);
        }

        // if the continuation of the parent message is stopped from here set the RESPONSE_WRITTEN
        // property to SKIP to skip the blank http response 
        OperationContext opCtx
            = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getOperationContext();
        if (!continueParent && opCtx != null) {
            opCtx.setProperty(Constants.RESPONSE_WRITTEN, "SKIP");
        }

        // finalize tracing and debugging
        synLog.traceOrDebug("End : Clone mediator");

        // if continue parent is true mediators after the clone will be called for the further
        // mediation of the message which is subjected for clonning (parent message)
        return continueParent;
    }

    public boolean mediate(MessageContext synCtx,
                           ContinuationState continuationState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Clone mediator : Mediating from ContinuationState");
        }

        boolean result;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();

        if (!continuationState.hasChild()) {
            result = targets.get(subBranch).getSequence().
                    mediate(synCtx, continuationState.getPosition() + 1);
        } else {
            FlowContinuableMediator mediator =
                    (FlowContinuableMediator) targets.get(subBranch).getSequence().
                    getChild(continuationState.getPosition());
            result = mediator.mediate(synCtx, continuationState.getChildContState());
        }
        return result;
    }

    /**
     * clone the provided message context as a new message, and mark as the messageSequence'th
     * message context of a total of messageCount messages
     *
     * @param synCtx          - MessageContext which is subjected to the cloning
     * @param messageSequence - the position of this message of the cloned set
     * @param messageCount    - total of cloned copies
     *
     * @return MessageContext the cloned message context
     */
    private MessageContext getClonedMessageContext(MessageContext synCtx, int messageSequence,
                                                   int messageCount) {

        MessageContext newCtx = null;
        try {
        	
            newCtx = MessageHelper.cloneMessageContext(synCtx);
            // Set isServerSide property in the cloned message context
            ((Axis2MessageContext) newCtx).getAxis2MessageContext().setServerSide(
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext().isServerSide());
            if (id != null) {
                // set the parent correlation details to the cloned MC -
                //                              for the use of aggregation like tasks
                newCtx.setProperty(EIPConstants.AGGREGATE_CORRELATION + "." + id,
                        synCtx.getMessageID());
                // set the property MESSAGE_SEQUENCE to the MC for aggregation purposes
                newCtx.setProperty(EIPConstants.MESSAGE_SEQUENCE + "." + id,
                        String.valueOf(messageSequence) + EIPConstants.MESSAGE_SEQUENCE_DELEMITER +
                                messageCount);
            } else {
                newCtx.setProperty(EIPConstants.MESSAGE_SEQUENCE,
                        String.valueOf(messageSequence) + EIPConstants.MESSAGE_SEQUENCE_DELEMITER +
                                messageCount);
            }
        } catch (AxisFault axisFault) {
            handleException("Error cloning the message context", axisFault, synCtx);
        }

        return newCtx;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //                        Getters and Setters                                        //
    ///////////////////////////////////////////////////////////////////////////////////////

    public boolean isContinueParent() {
        return continueParent;
    }

    public void setContinueParent(boolean continueParent) {
        this.continueParent = continueParent;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public void addTarget(Target target) {
        this.targets.add(target);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public void init(SynapseEnvironment se) {

        synapseEnv = se;
        for (Target target : targets) {
            ManagedLifecycle seq = target.getSequence();
            if (seq != null) {
                seq.init(se);
            } else if (target.getSequenceRef() != null) {
                SequenceMediator targetSequence =
                        (SequenceMediator) se.getSynapseConfiguration().
                                getSequence(target.getSequenceRef());

                if (targetSequence == null || targetSequence.isDynamic()) {
                    se.addUnavailableArtifactRef(target.getSequenceRef());
                }
            }
            Endpoint endpoint = target.getEndpoint();
            if (endpoint != null) {
                endpoint.init(se);
            }
        }
    }

    public void destroy() {

        for (Target target : targets) {
            ManagedLifecycle seq = target.getSequence();
            if (seq != null) {
                seq.destroy();
            } else if (target.getSequenceRef() != null) {
                SequenceMediator targetSequence =
                        (SequenceMediator) synapseEnv.getSynapseConfiguration().
                                getSequence(target.getSequenceRef());

                if (targetSequence == null || targetSequence.isDynamic()) {
                    synapseEnv.removeUnavailableArtifactRef(target.getSequenceRef());
                }
            }
            Endpoint endpoint = target.getEndpoint();
            if (endpoint != null) {
                endpoint.destroy();
            }
        }
    }

}
