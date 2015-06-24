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
package org.apache.synapse.endpoints;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.DefaultGroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.ServiceLoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.apache.synapse.endpoints.dispatch.SALSessions;
import org.apache.synapse.endpoints.dispatch.SessionInformation;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents a dynamic load balance endpoint. The application membership is not static,
 * but discovered through some mechanism such as using a GCF
 */
public class ServiceDynamicLoadbalanceEndpoint extends DynamicLoadbalanceEndpoint {

    private static final Log log = LogFactory.getLog(ServiceDynamicLoadbalanceEndpoint.class);

    /**
     * Axis2 based membership handler which handles members in multiple clustering domains
     */
    private ServiceLoadBalanceMembershipHandler slbMembershipHandler;

    /**
     * Key - host, Value - domain
     */
    private Map<String, String> hostDomainMap;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (!initialized) {
            super.init(synapseEnvironment);
            ConfigurationContext cfgCtx =
                    ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
            ClusteringAgent clusteringAgent = cfgCtx.getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent == null) {
                throw new SynapseException("Axis2 ClusteringAgent not defined in axis2.xml");
            }
            // Add the Axis2 GroupManagement agents
            for (String domain : hostDomainMap.values()) {
                if (clusteringAgent.getGroupManagementAgent(domain) == null) {
                    clusteringAgent.addGroupManagementAgent(new DefaultGroupManagementAgent(), domain);
                }
            }
            slbMembershipHandler = new ServiceLoadBalanceMembershipHandler(hostDomainMap,
                                                                           getAlgorithm(),
                                                                           cfgCtx,
                                                                           isClusteringEnabled,
                                                                           getName());

            // Initialize the SAL Sessions if already has not been initialized.
            SALSessions salSessions = SALSessions.getInstance();
            if (!salSessions.isInitialized()) {
                salSessions.initialize(isClusteringEnabled, cfgCtx);
            }
            initialized = true;
            log.info("ServiceDynamicLoadbalanceEndpoint initialized");
        }
    }

    public ServiceDynamicLoadbalanceEndpoint(Map<String, String> hostDomainMap,
                                             LoadbalanceAlgorithm algorithm) {

        this.hostDomainMap = hostDomainMap;
        setAlgorithm(algorithm);
    }

    public LoadBalanceMembershipHandler getLbMembershipHandler() {
        return slbMembershipHandler;
    }

    public Map<String, String> getHostDomainMap() {
        return Collections.unmodifiableMap(hostDomainMap);
    }

    public void send(MessageContext synCtx) {
        setCookieHeader(synCtx);
        //TODO: Refactor Session Aware LB dispatching code

        // Check whether a valid session for session aware dispatching is available
        Member currentMember = null;
        SessionInformation sessionInformation = null;
        if (isSessionAffinityBasedLB()) {
            // first check if this session is associated with a session. if so, get the endpoint
            // associated for that session.
            sessionInformation =
                    (SessionInformation) synCtx.getProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

            currentMember = (Member) synCtx.getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER);

            if (sessionInformation == null && currentMember == null) {
                sessionInformation = dispatcher.getSession(synCtx);
                if (sessionInformation != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Current session id : " + sessionInformation.getId());
                    }

                    currentMember = sessionInformation.getMember();
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER, currentMember);
                    // This is for reliably recovery any session information if while response is getting ,
                    // session information has been removed by cleaner.
                    // This will not be a cost as  session information a not heavy data structure
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION, sessionInformation);
                }
            }

        }

        // Dispatch request the relevant member
        String targetHost = getTargetHost(synCtx);
        ConfigurationContext configCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();
        if (slbMembershipHandler.getConfigurationContext() == null) {
            slbMembershipHandler.setConfigurationContext(configCtx);
        }
        ServiceDynamicLoadbalanceFaultHandlerImpl faultHandler = new ServiceDynamicLoadbalanceFaultHandlerImpl();
        faultHandler.setHost(targetHost);
        setupTransportHeaders(synCtx);
        if (sessionInformation != null && currentMember != null) {
            //send message on current session
            sessionInformation.updateExpiryTime();
            sendToApplicationMember(synCtx, currentMember, faultHandler, false);
        } else {
            // prepare for a new session
            currentMember = slbMembershipHandler.getNextApplicationMember(targetHost);
            if (currentMember == null) {
                String msg = "No application members available";
                log.error(msg);
                throw new SynapseException(msg);
            }
            sendToApplicationMember(synCtx, currentMember, faultHandler, true);
        }
    }

    private String getTargetHost(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map<String, String> headers =
                (Map<String, String>) axis2MessageContext.
                        getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String address = headers.get(HTTP.TARGET_HOST);
        synCtx.setProperty("LB_REQUEST_HOST", address); // Need to set with the port
        if (address.contains(":")) {
            address = address.substring(0, address.indexOf(":"));
        }
        return address;
    }

    /**
     * This FaultHandler will try to resend the message to another member if an error occurs
     * while sending to some member. This is a failover mechanism
     */
    private class ServiceDynamicLoadbalanceFaultHandlerImpl extends DynamicLoadbalanceFaultHandler {

        private EndpointReference to;
        private Member currentMember;
        private Endpoint currentEp;
        private String host;

        private static final int MAX_RETRY_COUNT = 5;

        // ThreadLocal variable to keep track of how many times this fault handler has been
        // called
        private ThreadLocal<Integer> callCount = new ThreadLocal<Integer>() {
            protected Integer initialValue() {
                return 0;
            }
        };

        public void setHost(String host) {
            this.host = host;
        }

        public void setCurrentMember(Member currentMember) {
            this.currentMember = currentMember;
        }

        public void setTo(EndpointReference to) {
            this.to = to;
        }

        private ServiceDynamicLoadbalanceFaultHandlerImpl() {
        }

        public void onFault(MessageContext synCtx) {
            if (currentMember == null) {
                return;
            }
            currentMember.suspend(10000);     // TODO: Make this configurable.
            log.info("Suspended member " + currentMember + " for 10s");

            // Prevent infinite retrying to failed members
            callCount.set(callCount.get() + 1);
            if (callCount.get() >= MAX_RETRY_COUNT) {
                return;
            }

            //cleanup endpoint if exists
            if (currentEp != null) {
                currentEp.destroy();
            }
            Integer errorCode = (Integer) synCtx.getProperty(SynapseConstants.ERROR_CODE);
            if (errorCode != null) {
                if (errorCode.equals(NhttpConstants.CONNECTION_FAILED) ||
                    errorCode.equals(NhttpConstants.CONNECT_CANCEL) ||
                    errorCode.equals(NhttpConstants.CONNECT_TIMEOUT)) {
                    // Try to resend to another member
                    Member newMember = slbMembershipHandler.getNextApplicationMember(host);
                    if (newMember == null) {
                        String msg = "No application members available";
                        log.error(msg);
                        throw new SynapseException(msg);
                    }
                    log.info("Failed over to " + newMember);
                    synCtx.setTo(to);
                    if (isSessionAffinityBasedLB()) {
                        //We are sending the this message on a new session,
                        // hence we need to remove previous session information
                        Set pros = synCtx.getPropertyKeySet();
                        if (pros != null) {
                            pros.remove(SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);
                        }
                    }
                    try {
                        Thread.sleep(1000);  // Sleep for sometime before retrying
                    } catch (InterruptedException ignored) {
                    }
                    sendToApplicationMember(synCtx, newMember, this, true);
                } else if (errorCode.equals(NhttpConstants.SND_IO_ERROR_SENDING) ||
                           errorCode.equals(NhttpConstants.CONNECTION_CLOSED)) {
                    // TODO: Envelope is consumed
                }
            }
            // We cannot failover since we are using binary relay
        }

        public void setCurrentEp(Endpoint currentEp) {
            this.currentEp = currentEp;
        }
    }
}
