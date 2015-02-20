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

package org.apache.synapse.endpoints.algorithms;

import org.apache.axis2.clustering.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import sun.security.action.LoadLibraryAction;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the implementation of the round robin load balancing algorithm. It simply iterates
 * through the endpoint list one by one for until an active endpoint is found.
 */
public class RoundRobin implements LoadbalanceAlgorithm {

    private static final Log log = LogFactory.getLog(RoundRobin.class);

    /**
     * Endpoints list for the round robin algorithm
     */
    private List endpoints = null;

    private List<Member> members;

    private final Lock lock = new ReentrantLock();

    public RoundRobin() {

    }

    public RoundRobin(List endpoints) {
        this.endpoints = endpoints;
    }

    public void setApplicationMembers(List<Member> members) {
        this.members = members;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setLoadBalanceEndpoint(Endpoint endpoint) {
    }

    /**
     * Choose an active endpoint using the round robin algorithm. If there are no active endpoints
     * available, returns null.
     *
     * @param synCtx           MessageContext instance which holds all per-message properties
     * @param algorithmContext The context in which holds run time states related to the algorithm
     * @return endpoint to send the next message
     */
    public Endpoint getNextEndpoint(MessageContext synCtx, AlgorithmContext algorithmContext) {

        Endpoint nextEndpoint;
        int attempts = 0;
        synchronized (algorithmContext) {
            int currentEPR = algorithmContext.getCurrentEndpointIndex();
            do {
                // two successive clients could get the same endpoint if not synchronized.
                nextEndpoint = (Endpoint) endpoints.get(currentEPR);

                if (currentEPR == endpoints.size() - 1) {
                    currentEPR = 0;
                } else {
                    currentEPR++;
                }
                algorithmContext.setCurrentEndpointIndex(currentEPR);

                attempts++;
                if (attempts > endpoints.size()) {
                    return null;
                }

            } while (!nextEndpoint.readyToSend());
        }

        return nextEndpoint;
    }

    public Member getNextApplicationMember(AlgorithmContext algorithmContext) {
        if (members.size() == 0) {
            return null;
        }
        Member current = null;
        lock.lock();
        try {
            int currentMemberIndex = algorithmContext.getCurrentEndpointIndex();
            if (currentMemberIndex >= members.size()) {
                currentMemberIndex = 0;
            }
            int index = members.size();
            do {
                current = members.get(currentMemberIndex);
                if (currentMemberIndex == members.size() - 1) {
                    currentMemberIndex = 0;
                } else {
                    currentMemberIndex++;
                }
                index--;
            } while (current.isSuspended() && index > 0);
            algorithmContext.setCurrentEndpointIndex(currentMemberIndex);
            if (log.isDebugEnabled()) {
                log.debug("Members       : " + members.size());
                log.debug("Current member: " + current);
            }

        } finally {
            lock.unlock();
        }
        return current;
    }

    @Override
    public LoadbalanceAlgorithm clone() {
        return new RoundRobin();
    }

    public void reset(AlgorithmContext algorithmContext) {
        if (log.isDebugEnabled()) {
            log.debug("Resetting the Round Robin loadbalancing algorithm ...");
        }
        synchronized (algorithmContext) {
            algorithmContext.setCurrentEndpointIndex(0);
        }
    }

    public String getName() {
        return "RoundRobin";
    }
}
