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
package org.apache.synapse.mediators.throttle;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringFault;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.state.Replicator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.throttle.*;

/**
 * The Mediator for the throttling - Throttling will occur according to the ws-policy
 * which is specified as the key for lookup from the registry or the inline policy
 * Only support IP based throttling- Throttling can manage per IP using the throttle policy
 */

public class ThrottleMediator extends AbstractMediator implements ManagedLifecycle,
                                                                  FlowContinuableMediator {

    /* The key for getting the throttling policy - key refers to a/an [registry] entry    */
    private String policyKey = null;
    /* InLine policy object - XML  */
    private OMElement inLinePolicy = null;
    /* The reference to the sequence which will execute when access is denied   */
    private String onRejectSeqKey = null;
    /* The in-line sequence which will execute when access is denied */
    private Mediator onRejectMediator = null;
    /* The reference to the sequence which will execute when access is allowed  */
    private String onAcceptSeqKey = null;
    /* The in-line sequence which will execute when access is allowed */
    private Mediator onAcceptMediator = null;
    /* The concurrent access control group id */
    private String id;
    /* Access rate controller - limit the remote caller access*/
    private AccessRateController accessControler;
    /* ConcurrentAccessController - limit the remote callers concurrent access */
    private ConcurrentAccessController concurrentAccessController = null;
    /* The property key that used when the ConcurrentAccessController
       look up from ConfigurationContext */
    private String key;
    /* Is this env. support clustering*/
    private boolean isClusteringEnable = false;
    /* The Throttle object - holds all runtime and configuration data */
    private Throttle throttle;
    /* Lock used to ensure thread-safe creation of the throttle */
    private final Object throttleLock = new Object();
    /* Last version of dynamic policy resource*/
    private long version;

    public ThrottleMediator() {
        this.accessControler = new AccessRateController();
    }

    public void init(SynapseEnvironment se) {
        if (onAcceptMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onAcceptMediator).init(se);
        } else if (onAcceptSeqKey != null) {
            SequenceMediator onAcceptSeq =
                    (SequenceMediator) se.getSynapseConfiguration().
                            getSequence(onAcceptSeqKey);

            if (onAcceptSeq == null || onAcceptSeq.isDynamic()) {
                se.addUnavailableArtifactRef(onAcceptSeqKey);
            }
        }

        if (onRejectMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onRejectMediator).init(se);
        } else if (onRejectSeqKey != null) {
            SequenceMediator onRejectSeq =
                    (SequenceMediator) se.getSynapseConfiguration().
                            getSequence(onRejectSeqKey);

            if (onRejectSeq == null || onRejectSeq.isDynamic()) {
                se.addUnavailableArtifactRef(onRejectSeqKey);
            }
        }
    }

    public void destroy() {
        if (onAcceptMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onAcceptMediator).destroy();
        }
        if (onRejectMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onRejectMediator).destroy();
        }
    }

    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);
        boolean isResponse = synCtx.isResponse();
        ConfigurationContext cc;
        org.apache.axis2.context.MessageContext axisMC;

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Throttle mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }
        // To ensure the creation of throttle is thread safe Ã¢â‚¬â€œ It is possible create same throttle
        // object multiple times  by multiple threads.

        synchronized (throttleLock) {

            // get Axis2 MessageContext and ConfigurationContext
            axisMC = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            cc = axisMC.getConfigurationContext();

            //To ensure check for clustering environment only happens one time
            if ((throttle == null && !isResponse) || (isResponse
                    && concurrentAccessController == null)) {
                ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
                if (clusteringAgent != null &&
                        clusteringAgent.getStateManager() != null) {
                    isClusteringEnable = true;
                }
            }

            // Throttle only will be created ,if the massage flow is IN
            if (!isResponse) {
                //check the availability of the ConcurrentAccessControler
                //if this is a clustered environment
                if (isClusteringEnable) {
                    concurrentAccessController =
                            (ConcurrentAccessController) cc.getProperty(key);
                }
                // for request messages, read the policy for throttling and initialize
                if (inLinePolicy != null) {
                    // this uses a static policy
                    if (throttle == null) {  // only one time creation

                        if (synLog.isTraceTraceEnabled()) {
                            synLog.traceTrace("Initializing using static throttling policy : "
                                    + inLinePolicy);
                        }
                        try {
                            // process the policy
                            throttle = ThrottleFactory.createMediatorThrottle(
                                    PolicyEngine.getPolicy(inLinePolicy));

                            //At this point concurrent access controller definitely 'null'
                            // f the clustering is disable.
                            //For a clustered environment,it is 'null' ,
                            //if this is the first instance on the cluster ,
                            // that message mediation has occurred through this mediator.
                            if (throttle != null && concurrentAccessController == null) {
                                concurrentAccessController =
                                        throttle.getConcurrentAccessController();
                                if (concurrentAccessController != null) {
                                    cc.setProperty(key, concurrentAccessController);
                                }
                            }
                        } catch (ThrottleException e) {
                            handleException("Error processing the throttling policy", e, synCtx);
                        }
                    }

                } else if (policyKey != null) {

                    // If the policy has specified as a registry key.
                    // load or re-load policy from registry or local entry if not already available

                    Entry entry = synCtx.getConfiguration().getEntryDefinition(policyKey);
                    if (entry == null) {
                        handleException("Cannot find throttling policy using key : "
                                + policyKey, synCtx);

                    } else {
                        boolean reCreate = false;
                        // if the key refers to a dynamic resource
                        if (entry.isDynamic()) {
                            if ( (!entry.isCached() || entry.isExpired() ) && version!= entry.getVersion()) {
                                reCreate = true;
                                version = entry.getVersion();
                            }
                        }
                        if (reCreate || throttle == null) {
                            Object entryValue = synCtx.getEntry(policyKey);
                            if (entryValue == null) {
                                handleException(
                                        "Null throttling policy returned by Entry : "
                                                + policyKey, synCtx);

                            } else {
                                if (!(entryValue instanceof OMElement)) {
                                    handleException("Policy returned from key : " + policyKey +
                                            " is not an OMElement", synCtx);

                                } else {
                                    //Check for reload in a cluster environment Ã¢â‚¬â€œ
                                    // For clustered environment ,if the concurrent access controller
                                    // is not null and throttle is not null , then must reload.
                                    if (isClusteringEnable && concurrentAccessController != null
                                            && throttle != null) {
                                        concurrentAccessController = null; // set null ,
                                        // because need reload
                                    }

                                    try {
                                        // Creates the throttle from the policy
                                        throttle = ThrottleFactory.createMediatorThrottle(
                                                PolicyEngine.getPolicy((OMElement) entryValue));

                                        //For non-clustered  environment , must re-initiates
                                        //For  clustered  environment,
                                        //concurrent access controller is null ,
                                        //then must re-initiates
                                        if (throttle != null && (concurrentAccessController == null
                                                || !isClusteringEnable)) {
                                            concurrentAccessController =
                                                    throttle.getConcurrentAccessController();
                                            if (concurrentAccessController != null) {
                                                cc.setProperty(key, concurrentAccessController);
                                            } else {
                                                cc.removeProperty(key);
                                            }
                                        }
                                    } catch (ThrottleException e) {
                                        handleException("Error processing the throttling policy",
                                                e, synCtx);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // if the message flow path is OUT , then must lookp from ConfigurationContext -
                // never create ,just get the existing one
                concurrentAccessController =
                        (ConcurrentAccessController) cc.getProperty(key);
            }
        }
        //perform concurrency throttling
        boolean canAccess = doThrottleByConcurrency(isResponse, synLog);

        //if the access is success through concurrency throttle and if this is a request message
        //then do access rate based throttling
        if (throttle != null && !isResponse && canAccess) {
            canAccess = throttleByAccessRate(synCtx, axisMC, cc, synLog);
        }
        // all the replication functionality of the access rate based throttling handles by itself
        // Just replicate the current state of ConcurrentAccessController
        if (isClusteringEnable && concurrentAccessController != null) {
            if (cc != null) {
                try {
                    if (synLog.isTraceOrDebugEnabled()) {
                        synLog.traceOrDebug("Going to replicates the  " +
                                "states of the ConcurrentAccessController with key : " + key);
                    }
                    Replicator.replicate(cc);
                } catch (ClusteringFault clusteringFault) {
                    handleException("Error during the replicating  states ",
                            clusteringFault, synCtx);
                }
            }
        }
        if (canAccess) {
            if (onAcceptSeqKey != null) {
                Mediator mediator = synCtx.getSequence(onAcceptSeqKey);
                if (mediator != null) {
                    ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                    return mediator.mediate(synCtx);
                } else {
                    handleException("Unable to find onAccept sequence with key : "
                            + onAcceptSeqKey, synCtx);
                }
            } else if (onAcceptMediator != null) {
                ContinuationStackManager.addReliantContinuationState(synCtx, 0, getMediatorPosition());
                boolean result = onAcceptMediator.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;
            } else {
                return true;
            }

        } else {
            if (onRejectSeqKey != null) {
                Mediator mediator = synCtx.getSequence(onRejectSeqKey);
                if (mediator != null) {
                    ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                    return mediator.mediate(synCtx);
                } else {
                    handleException("Unable to find onReject sequence with key : "
                            + onRejectSeqKey, synCtx);
                }
            } else if (onRejectMediator != null) {
                ContinuationStackManager.addReliantContinuationState(synCtx, 1, getMediatorPosition());
                boolean result = onRejectMediator.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;
            } else {
                return false;
            }
        }

        synLog.traceOrDebug("End : Throttle mediator");
        return canAccess;
    }

    public boolean mediate(MessageContext synCtx, ContinuationState continuationState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Throttle mediator : Mediating from ContinuationState");
        }

        boolean result;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();
        if (subBranch == 0) {
            if (!continuationState.hasChild()) {
                result = ((SequenceMediator)onAcceptMediator).
                        mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) ((SequenceMediator)onAcceptMediator).
                                getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else {
            if (!continuationState.hasChild()) {
                result = ((SequenceMediator)onRejectMediator).
                        mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) ((SequenceMediator)onRejectMediator).getChild(
                                continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        }

        return result;
    }

    /**
     * Helper method that handles the concurrent access through throttle
     *
     * @param isResponse Current Message is response or not
     * @param synLog     the Synapse log to use
     * @return true if the caller can access ,o.w. false
     */
    private boolean doThrottleByConcurrency(boolean isResponse, SynapseLog synLog) {
        boolean canAcess = true;
        if (concurrentAccessController != null) {
            // do the concurrency throttling
            int concurrentLimit = concurrentAccessController.getLimit();
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Concurrent access controller for ID : " + id +
                        " allows : " + concurrentLimit + " concurrent accesses");
            }
            int available;
            if (!isResponse) {
                available = concurrentAccessController.getAndDecrement();
                canAcess = available > 0;
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Concurrency Throttle : Access " +
                            (canAcess ? "allowed" : "denied") + " :: " + available
                            + " of available of " + concurrentLimit + " connections");
                }
            } else {
                available = concurrentAccessController.incrementAndGet();
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Concurrency Throttle : Connection returned" + " :: " +
                            available + " of available of " + concurrentLimit + " connections");
                }
            }
        }
        return canAcess;
    }

    /**
     * Helper method that handles the access-rate based throttling
     *
     * @param synCtx MessageContext(Synapse)
     * @param axisMC MessageContext(Axis2)
     * @param cc     ConfigurationContext
     * @param synLog the Synapse log to use
     * @return ue if the caller can access ,o.w. false
     */
    private boolean throttleByAccessRate(MessageContext synCtx,
                                         org.apache.axis2.context.MessageContext axisMC,
                                         ConfigurationContext cc,
                                         SynapseLog synLog) {

        String callerId = null;
        boolean canAccess = true;
        //remote ip of the caller
        String remoteIP = (String) axisMC.getPropertyNonReplicable(
                org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        //domain name of the caller
        String domainName = (String) axisMC.getPropertyNonReplicable(NhttpConstants.REMOTE_HOST);

        //Using remote caller domain name , If there is a throttle configuration for
        // this domain name ,then throttling will occur according to that configuration
        if (domainName != null) {
            // do the domain based throttling
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("The Domain Name of the caller is :" + domainName);
            }
            // loads the DomainBasedThrottleContext
            ThrottleContext context
                    = throttle.getThrottleContext(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
            if (context != null) {
                //loads the DomainBasedThrottleConfiguration
                ThrottleConfiguration config = context.getThrottleConfiguration();
                if (config != null) {
                    //checks the availability of a policy configuration for  this domain name
                    callerId = config.getConfigurationKeyOfCaller(domainName);
                    if (callerId != null) {  // there is configuration for this domain name

                        //If this is a clustered env.
                        if (isClusteringEnable) {
                            context.setConfigurationContext(cc);
                            context.setThrottleId(id);
                        }

                        try {
                            //Checks for access state
                            AccessInformation accessInformation = accessControler.canAccess(context,
                                    callerId, ThrottleConstants.DOMAIN_BASE);
                            canAccess = accessInformation.isAccessAllowed();

                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Access " + (canAccess ? "allowed" : "denied")
                                        + " for Domain Name : " + domainName);
                            }

                            //In the case of both of concurrency throttling and
                            //rate based throttling have enabled ,
                            //if the access rate less than maximum concurrent access ,
                            //then it is possible to occur death situation.To avoid that reset,
                            //if the access has denied by rate based throttling
                            if (!canAccess && concurrentAccessController != null) {
                                concurrentAccessController.incrementAndGet();
                                if (isClusteringEnable) {
                                    cc.setProperty(key, concurrentAccessController);
                                }
                            }
                        } catch (ThrottleException e) {
                            handleException("Error occurred during throttling", e, synCtx);
                        }
                    }
                }
            }
        } else {
            synLog.traceOrDebug("The Domain name of the caller cannot be found");
        }

        //At this point , any configuration for the remote caller hasn't found ,
        //therefore trying to find a configuration policy based on remote caller ip
        if (callerId == null) {
            //do the IP-based throttling
            if (remoteIP == null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("The IP address of the caller cannot be found");
                }
                canAccess = true;

            } else {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("The IP Address of the caller is :" + remoteIP);
                }
                try {
                    // Loads the IPBasedThrottleContext
                    ThrottleContext context =
                            throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY);
                    if (context != null) {
                        //Loads the IPBasedThrottleConfiguration
                        ThrottleConfiguration config = context.getThrottleConfiguration();
                        if (config != null) {
                            //Checks the availability of a policy configuration for  this ip
                            callerId = config.getConfigurationKeyOfCaller(remoteIP);
                            if (callerId != null) {   // there is configuration for this ip

                                //For clustered env.
                                if (isClusteringEnable) {
                                    context.setConfigurationContext(cc);
                                    context.setThrottleId(id);
                                }
                                //Checks access state
                                AccessInformation accessInformation = accessControler.canAccess(
                                        context,
                                        callerId,
                                        ThrottleConstants.IP_BASE);

                                canAccess = accessInformation.isAccessAllowed();
                                if (synLog.isTraceOrDebugEnabled()) {
                                    synLog.traceOrDebug("Access " +
                                            (canAccess ? "allowed" : "denied")
                                            + " for IP : " + remoteIP);
                                }
                                //In the case of both of concurrency throttling and
                                //rate based throttling have enabled ,
                                //if the access rate less than maximum concurrent access ,
                                //then it is possible to occur death situation.To avoid that reset,
                                //if the access has denied by rate based throttling
                                if (!canAccess && concurrentAccessController != null) {
                                    concurrentAccessController.incrementAndGet();
                                    if (isClusteringEnable) {
                                        cc.setProperty(key, concurrentAccessController);
                                    }
                                }
                            }
                        }
                    }
                } catch (ThrottleException e) {
                    handleException("Error occurred during throttling", e, synCtx);
                }
            }
        }
        return canAccess;
    }

    /**
     * To get the policy key - The key for which will used to lookup policy from the registry
     *
     * @return String
     */

    public String getPolicyKey() {
        return policyKey;
    }

    /**
     * To set the policy key - The key for which lookup from the registry
     *
     * @param policyKey Value for picking policy from the registry
     */
    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    /**
     * getting throttle policy which has defined as InLineXML
     *
     * @return InLine Throttle Policy
     */
    public OMElement getInLinePolicy() {
        return inLinePolicy;
    }

    /**
     * setting throttle policy which has defined as InLineXML
     *
     * @param inLinePolicy Inline policy
     */
    public void setInLinePolicy(OMElement inLinePolicy) {
        this.inLinePolicy = inLinePolicy;
    }

    public String getOnRejectSeqKey() {
        return onRejectSeqKey;
    }

    public void setOnRejectSeqKey(String onRejectSeqKey) {
        this.onRejectSeqKey = onRejectSeqKey;
    }

    public Mediator getOnRejectMediator() {
        return onRejectMediator;
    }

    public void setOnRejectMediator(Mediator onRejectMediator) {
        this.onRejectMediator = onRejectMediator;
    }

    public String getOnAcceptSeqKey() {
        return onAcceptSeqKey;
    }

    public void setOnAcceptSeqKey(String onAcceptSeqKey) {
        this.onAcceptSeqKey = onAcceptSeqKey;
    }

    public Mediator getOnAcceptMediator() {
        return onAcceptMediator;
    }

    public void setOnAcceptMediator(Mediator onAcceptMediator) {
        this.onAcceptMediator = onAcceptMediator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + id + ThrottleConstants.CAC_SUFFIX;
    }

    @Override
    public boolean isContentAware() {
        return false;
    }
}
