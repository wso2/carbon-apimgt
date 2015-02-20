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
import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Load balance endpoint contains multiple child endpoints. It routes messages according to the
 * specified load balancing algorithm. This will assume that all immediate child endpoints are
 * identical in state (state is replicated) or state is not maintained at those endpoints. If an
 * endpoint is failing, the failed endpoint is marked as inactive and the message sent to the next
 * endpoint obtained using the load balancing algorithm. If all the endpoints have failed and a
 * parent endpoint is available, onChildEndpointFail(...) method of parent endpoint is called. If
 * a parent is not available, this will call next FaultHandler for the message context.
 */
public class LoadbalanceEndpoint extends AbstractEndpoint {

    /** Should this load balancer fail over as well? */
    private boolean failover = true;
    /** The algorithm used for selecting the next endpoint */
    private LoadbalanceAlgorithm algorithm = null;
    /** The algorithm context to hold runtime state related to the load balance algorithm */
    private AlgorithmContext algorithmContext = null;

    /**
     * List of currently available application members amongst which the load is distributed
     */
    private List<Member> activeMembers = null;

    /**
     * List of currently unavailable members
     */
    private List<Member> inactiveMembers = null;


    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        ConfigurationContext cc =
                ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
        if (!initialized) {
            super.init(synapseEnvironment);
            if (algorithmContext == null) {
                algorithmContext = new AlgorithmContext(isClusteringEnabled, cc, getName());
            }

            // if the loadbalancing algorithm implements the ManagedLifecycle interface
            // initlize the algorithm
            if (algorithm != null && algorithm instanceof ManagedLifecycle) {
                ManagedLifecycle lifecycle = (ManagedLifecycle) algorithm;
                lifecycle.init(synapseEnvironment);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        // if the loadbalancing algorithm implements the ManagedLifecycle interface
        // destroy the algorithm
        if (algorithm != null && algorithm instanceof ManagedLifecycle) {
            ManagedLifecycle lifecycle = (ManagedLifecycle) algorithm;
            lifecycle.destroy();
        }
    }

    public void send(MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Sending using Load-balance " + toString());
        }

        if (getContext().isState(EndpointContext.ST_OFF)) {
            informFailure(synCtx, SynapseConstants.ENDPOINT_LB_NONE_READY,
                    "Loadbalance endpoint : " + getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT + " - is inactive");
            return;
        }

        Endpoint endpoint = null;
        if (activeMembers == null) {
            endpoint = getNextChild(synCtx);
        }

        // evaluate the endpoint properties
        evaluateProperties(synCtx);

        if (endpoint != null) {
            // if this is not a retry
            if (synCtx.getProperty(SynapseConstants.LAST_ENDPOINT) == null) {
                // We have to build the envelop when we are supporting failover, as we
                // may have to retry this message for failover support
                if (failover) {
                    synCtx.getEnvelope().build();
                }
            } else {
                if (metricsMBean != null) {
                    // this is a retry, where we are now failing over to an active node
                    metricsMBean.reportSendingFault(SynapseConstants.ENDPOINT_LB_FAIL_OVER);
                }
            }
            synCtx.pushFaultHandler(this);
            endpoint.send(synCtx);

        } else if (activeMembers != null && !activeMembers.isEmpty()) {
            EndpointReference to = synCtx.getTo();
            LoadbalanceFaultHandler faultHandler = new LoadbalanceFaultHandler(to);
            if (failover) {
                synCtx.pushFaultHandler(faultHandler);
            }
            sendToApplicationMember(synCtx, to, faultHandler);
        } else {
            String msg = "Loadbalance endpoint : " +
                    (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                    " - no ready child endpoints";
            log.warn(msg);
            // if this is not a retry
            informFailure(synCtx, SynapseConstants.ENDPOINT_LB_NONE_READY, msg);
        }
    }

    private void sendToApplicationMember(MessageContext synCtx,
                                         EndpointReference to,
                                         LoadbalanceFaultHandler faultHandler) {
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        String transport = axis2MsgCtx.getTransportIn().getName();
        algorithm.setApplicationMembers(activeMembers);
        Member currentMember = algorithm.getNextApplicationMember(algorithmContext);
        faultHandler.setCurrentMember(currentMember);

        if (currentMember != null) {

            // URL rewrite
            if (transport.equals("http") || transport.equals("https")) {
                String address = to.getAddress();
                if (address.indexOf(":") != -1) {
                    try {
                        address = new URL(address).getPath();
                    } catch (MalformedURLException e) {
                        String msg = "URL " + address + " is malformed";
                        log.error(msg, e);
                        throw new SynapseException(msg, e);
                    }
                }
                EndpointReference epr =
                        new EndpointReference(transport + "://" + currentMember.getHostName()
                                + ":" + ("http".equals(transport) ? currentMember.getHttpPort() :
                                currentMember.getHttpsPort()) + address);
                synCtx.setTo(epr);
                if (failover) {
                    synCtx.getEnvelope().build();
                }

                AddressEndpoint endpoint = new AddressEndpoint();
                EndpointDefinition definition = new EndpointDefinition();
                endpoint.setDefinition(definition);
                endpoint.init(synCtx.getEnvironment());
                endpoint.send(synCtx);
            } else {
                log.error("Cannot load balance for non-HTTP/S transport " + transport);
            }
        } else {
            synCtx.getFaultStack().pop(); // Remove the LoadbalanceFaultHandler
            String msg = "No application members available";
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    /**
     * If this endpoint is in inactive state, checks if all immediate child endpoints are still
     * failed. If so returns false. If at least one child endpoint is in active state, sets this
     * endpoint's state to active and returns true. As this a sessionless load balancing endpoint
     * having one active child endpoint is enough to consider this as active.
     *
     * @return true if active. false otherwise.
     */
    public boolean readyToSend() {
        if (getContext().isState(EndpointContext.ST_OFF)) {
            return false;
        }

        for (Endpoint endpoint : getChildren()) {
            if (endpoint.readyToSend()) {
                if (log.isDebugEnabled()) {
                    log.debug("Load-balance " + this.toString()
                            + " has at least one endpoint at ready state");
                }
                return true;
            }
        }

        log.warn("Load-balance " + this.toString()
                + " has no endpoints at ready state to process message");

        return false;
    }


    @Override
    public void onChildEndpointFail(Endpoint endpoint, MessageContext synMessageContext) {

        logOnChildEndpointFail(endpoint, synMessageContext);
        // resend (to a different endpoint) only if we support failover
        if (failover) {
            if (((AbstractEndpoint)endpoint).isRetry(synMessageContext)) {
                if (log.isDebugEnabled()) {
                    log.debug(this + " Retry Attempt for Request with [Message ID : " +
                            synMessageContext.getMessageID() + "], [To : " +
                            synMessageContext.getTo() + "]");
                }
                send(synMessageContext);
            } else {
                String msg = "Loadbalance endpoint : " +
                        (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                        " - one of the child endpoints encounterd a non-retry error, " +
                        "not sending message to another endpoint";
                log.warn(msg);
                informFailure(synMessageContext, SynapseConstants.ENDPOINT_LB_NONE_READY, msg);
            }
        } else {
            // we are not informing this to the parent endpoint as the failure of this loadbalance
            // endpoint. there can be more active endpoints under this, and current request has
            // failed only because the currently selected child endpoint has failed AND failover is
            // turned off in this load balance endpoint. so just call the next fault handler.
            Object o = synMessageContext.getFaultStack().pop();
            if (o != null) {
                ((FaultHandler) o).handleFault(synMessageContext);
            }
        }
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public LoadbalanceAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(LoadbalanceAlgorithm algorithm) {
        if (log.isDebugEnabled()) {
            log.debug("Load-balance " + this.toString() + " will be using the algorithm "
                + algorithm.getName() + " for load distribution");
        }
        this.algorithm = algorithm;
    }

    protected Endpoint getNextChild(MessageContext synCtx) {
        return algorithm.getNextEndpoint(synCtx, algorithmContext);
    }
    
    /**
     * This FaultHandler will try to resend the message to another member if an error occurs
     * while sending to some member. This is a failover mechanism
     */
    private class LoadbalanceFaultHandler extends FaultHandler {

        private EndpointReference to;
        private Member currentMember;

        public void setCurrentMember(Member currentMember) {
            this.currentMember = currentMember;
        }

        private LoadbalanceFaultHandler(EndpointReference to) {
            this.to = to;
        }

        public void onFault(MessageContext synCtx) {
            if (currentMember == null) {
                return;
            }
            synCtx.pushFaultHandler(this);
            activeMembers.remove(currentMember); // This member has to be inactivated
            inactiveMembers.add(currentMember);
            sendToApplicationMember(synCtx, to, this);
        }
    }

    public void setMembers(List<Member> members) {
        this.activeMembers = members;
        this.inactiveMembers = new ArrayList<Member>();
    }

    public List<Member> getMembers(){
        return this.activeMembers;
    }

    public void startApplicationMembershipTimer(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MemberActivatorTask(), 1000, 500);
    }

    /**
     * The task which checks whther inactive members have become available again
     */
    private class MemberActivatorTask extends TimerTask {

        public void run() {
            try {
                for(Member member: inactiveMembers){
                    if(canConnect(member)){
                        inactiveMembers.remove(member);
                        activeMembers.add(member);
                    }
                }
            } catch (Exception ignored) {
                // Ignore all exceptions. The timer should continue to run
            }
        }

        /**
         * Before activating a member, we will try to verify whether we can connect to it
         *
         * @param member The member whose connectvity needs to be verified
         * @return true, if the member can be contacted; false, otherwise.
         */
        private boolean canConnect(Member member) {
            if(log.isDebugEnabled()){
                log.debug("Trying to connect to member " + member.getHostName() + "...");
            }
            for (int retries = 30; retries > 0; retries--) {
                try {
                    InetAddress addr = InetAddress.getByName(member.getHostName());
                    int httpPort = member.getHttpPort();
                    if(log.isDebugEnabled()){
                        log.debug("HTTP Port=" + httpPort);
                    }
                    if (httpPort != -1) {
                        SocketAddress httpSockaddr = new InetSocketAddress(addr, httpPort);
                        new Socket().connect(httpSockaddr, 10000);
                    }
                    int httpsPort = member.getHttpsPort();
                    if(log.isDebugEnabled()){
                        log.debug("HTTPS Port=" + httpPort);
                    }
                    if (httpsPort != -1) {
                        SocketAddress httpsSockaddr = new InetSocketAddress(addr, httpsPort);
                        new Socket().connect(httpsSockaddr, 10000);
                    }
                    return true;
                } catch (IOException e) {
                    if(log.isDebugEnabled()){
                        log.debug("", e);
                    }
                    String msg = e.getMessage();
                    if (msg.indexOf("Connection refused") == -1 &&
                        msg.indexOf("connect timed out") == -1) {
                        log.error("Cannot connect to member " + member, e);
                    }
                }
            }
            return false;
        }
    }
}

