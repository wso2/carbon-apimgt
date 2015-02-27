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

package org.apache.synapse.continuation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SequenceType;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;

/**
 * This is the utility class which manages ContinuationState Stack.
 * <p/>
 * All operations for the stack done by mediators are done through this manager class in order to
 * easily control the operations from a central place.
 *
 */
public class ContinuationStackManager {

    private static Log log = LogFactory.getLog(ContinuationStackManager.class);

    /**
     * Add new SeqContinuationState to the stack.
     * This should be done when branching to a new Sequence
     *
     * @param synCtx  Message Context
     * @param seqName Name of the branching sequence
     * @param seqType Sequence Type
     */
    public static void addSeqContinuationState(MessageContext synCtx, String seqName,
                                               SequenceType seqType) {
        if (synCtx.isContinuationEnabled() && !SequenceType.ANON.equals(seqType)) {
            //ignore Anonymous type sequences
            synCtx.pushContinuationState(new SeqContinuationState(seqType, seqName));
        }
    }

    /**
     * Remove top SeqContinuationState from the stack.
     * This should be done when returning from a Sequence branch.
     *
     * @param synCtx Message Context
     */
    public static void removeSeqContinuationState(MessageContext synCtx, SequenceType seqType) {
        if (synCtx.isContinuationEnabled() && !synCtx.getContinuationStateStack().isEmpty()) {
            if (!SequenceType.ANON.equals(seqType)) {
                synCtx.getContinuationStateStack().pop();
            } else {
                removeReliantContinuationState(synCtx);
            }
        }
    }

    /**
     * Update SeqContinuationState with the current mediator position in the sequence.
     * SeqContinuationState should be updated when branching to a new flow
     * using a FlowContinuableMediator
     *
     * @param synCtx Message Context
     */
    public static void updateSeqContinuationState(MessageContext synCtx, int position) {
        if (synCtx.isContinuationEnabled()) {
            ContinuationState seqContState = synCtx.getContinuationStateStack().peek();
            seqContState.getLeafChild().setPosition(position);
        }
    }

    /**
     * Add a ReliantContinuationState to the top SeqContinuationState in the stack.
     * This should be done when branching to a sub branch using FlowContinuableMediators
     * except Sequence Mediator
     *
     * @param synCtx    Message Context
     * @param subBranch Sub branch id
     */
    public static void addReliantContinuationState(MessageContext synCtx, int subBranch,
                                                   int position) {
        if (synCtx.isContinuationEnabled()) {
            ContinuationState seqContState = synCtx.getContinuationStateStack().peek();
            seqContState.getLeafChild().setPosition(position);
            seqContState.addLeafChild(new ReliantContinuationState(subBranch));
        }
    }

    /**
     * Remove a ReliantContinuationState from the top SeqContinuationState in the stack.
     * This should be done when returning back from a sub branch of a FlowContinuableMediator.
     *
     * @param synCtx MessageContext
     */
    public static void removeReliantContinuationState(MessageContext synCtx) {
        if (synCtx.isContinuationEnabled()) {
            ContinuationState seqContState = synCtx.getContinuationStateStack().peek();
            seqContState.removeLeafChild();
        }
    }

    /**
     * Get a clone of a SeqContinuationState
     *
     * @param oriSeqContinuationState original SeqContinuationState
     * @return cloned SeqContinuationState
     */
    public static SeqContinuationState getClonedSeqContinuationState(
            SeqContinuationState oriSeqContinuationState) {

        SeqContinuationState clone =
                new SeqContinuationState(oriSeqContinuationState.getSeqType(),
                                         oriSeqContinuationState.getSeqName());
        clone.setPosition(oriSeqContinuationState.getPosition());
        if (oriSeqContinuationState.hasChild()) {
            clone.setChildContState(getClonedReliantContState(
                    oriSeqContinuationState.getChildContState()));
        }
        return clone;
    }

    /*
     * Get a clone of the ReliantContinuationState
     */
    private static ReliantContinuationState getClonedReliantContState(
            org.apache.synapse.ContinuationState continuationState) {

        ReliantContinuationState oriConstState =
                (ReliantContinuationState) continuationState;
        ReliantContinuationState clone =
                new ReliantContinuationState(oriConstState.getSubBranch());

        clone.setPosition(oriConstState.getPosition());
        if (oriConstState.hasChild()) {
            clone.setChildContState(
                    getClonedReliantContState(oriConstState.getChildContState()));
        }
        return clone;
    }

    /**
     * Remove all ContinuationStates from ContinuationState Stack
     * @param synCtx MessageContext
     */
    public static void clearStack(MessageContext synCtx) {
        if (synCtx.isContinuationEnabled()) {
            synCtx.getContinuationStateStack().clear();
        }
    }

    /**
     * Retrieve the sequence from Continuation state which message should be injected to.
     *
     * @param seqContState SeqContinuationState which contain the sequence information
     * @param synCtx       message context
     * @return sequence which message should be injected to
     */
    public static SequenceMediator retrieveSequence(MessageContext synCtx,
                                                    SeqContinuationState seqContState) {
        SequenceMediator sequence = null;

        switch (seqContState.getSeqType()) {
            case NAMED: {
                sequence = (SequenceMediator) synCtx.getSequence(seqContState.getSeqName());
                if (sequence == null) {
                    // This can happen only if someone delete the sequence while running
                    handleException("Sequence : " + seqContState.getSeqName() + " not found");
                }
                break;
            }
            case PROXY_INSEQ: {
                String proxyName = (String) synCtx.getProperty(SynapseConstants.PROXY_SERVICE);
                ProxyService proxyService = synCtx.getConfiguration().getProxyService(proxyName);
                if (proxyService != null) {
                    sequence = proxyService.getTargetInLineInSequence();
                } else {
                    handleException("Proxy Service :" + proxyName + " not found");
                }
                break;
            }
            case API_INSEQ: {
                String apiName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API);
                String resourceName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_RESOURCE);
                API api = synCtx.getEnvironment().getSynapseConfiguration().getAPI(apiName);
                if (api != null) {
                    Resource resource = api.getResource(resourceName);
                    if (resource != null) {
                        sequence = resource.getInSequence();
                    } else {
                        handleException("Resource : " + resourceName + " not found");
                    }
                } else {
                    handleException("REST API : " + apiName + " not found");
                }
                break;
            }
            case PROXY_OUTSEQ: {
                String proxyName = (String) synCtx.getProperty(SynapseConstants.PROXY_SERVICE);
                ProxyService proxyService = synCtx.getConfiguration().getProxyService(proxyName);
                if (proxyService != null) {
                    sequence = proxyService.getTargetInLineOutSequence();
                } else {
                    handleException("Proxy Service :" + proxyName + " not found");
                }
                break;
            }
            case API_OUTSEQ: {
                String apiName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API);
                String resourceName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_RESOURCE);
                API api = synCtx.getEnvironment().getSynapseConfiguration().getAPI(apiName);
                if (api != null) {
                    Resource resource = api.getResource(resourceName);
                    if (resource != null) {
                        sequence = resource.getOutSequence();
                    } else {
                        handleException("Resource : " + resourceName + " not found");
                    }
                } else {
                    handleException("REST API : " + apiName + " not found");
                }
                break;
            }
            case PROXY_FAULTSEQ: {
                String proxyName = (String) synCtx.getProperty(SynapseConstants.PROXY_SERVICE);
                ProxyService proxyService = synCtx.getConfiguration().getProxyService(proxyName);
                if (proxyService != null) {
                    sequence = proxyService.getTargetInLineFaultSequence();
                } else {
                    handleException("Proxy Service :" + proxyName + " not found");
                }
                break;
            }
            case API_FAULTSEQ: {
                String apiName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API);
                String resourceName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_RESOURCE);
                API api = synCtx.getEnvironment().getSynapseConfiguration().getAPI(apiName);
                if (api != null) {
                    Resource resource = api.getResource(resourceName);
                    if (resource != null) {
                        sequence = resource.getFaultSequence();
                    } else {
                        handleException("Resource : " + resourceName + " not found");
                    }
                } else {
                    handleException("REST API : " + apiName + " not found");
                }
                break;
            }
        }

        return sequence;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
