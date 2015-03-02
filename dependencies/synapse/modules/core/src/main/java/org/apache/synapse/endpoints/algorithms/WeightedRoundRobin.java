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
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.MessageContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.SynapseException;
import org.apache.synapse.PropertyInclude;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This algorithm sends messages based on the weights of the endpoints. For example we may
 * have 3 endpoints with following weights.</p>
 * <ul>
 * <li>Epr 1: 5</li>
 * <li>Epr 2: 3</li>
 * <li>Epr 3: 2</li>
 * </ul>
 * <p> This algorithm will send the first 5 messages through Epr1, next 3 messages through
 * Epr2 and next 2 messages with Epr3. Then algorithm moves again to the first endpoint
 * and cycle continues.</p>  
 */
public class WeightedRoundRobin implements LoadbalanceAlgorithm, ManagedLifecycle {

    private static final Log log = LogFactory.getLog(WeightedRoundRobin.class);

    /** We keep a sorted array of endpoint states, first state will point to the
     * endpoint with the highest weight */
    private EndpointState[] endpointStates = null;

    /** Endpoint list */
    private List<Endpoint> endpoints;

    private Endpoint loadBalanceEndpoint;

    /** Keep track of the current poistion we are operating on the endpointStates array */
    private int endpointCursor = 0;

    /** If a weight is not specified by the user, we use the default as 1 */
    private static final int DEFAULT_WEIGHT = 1;

    /** Configuration key used by the endpoints for indicating their weight */
    private static final String LOADBALANCE_WEIGHT = "loadbalance.weight";

    /** Configuration key used by the endpoints for indicating their weight */
    private static final String LOADBALANCE_ThEADLOCAL = "loadbalance.threadLocal";

    private boolean isThreadLocal = false;

    private AlgorithmThreadLocal threadedAlgorithm = null;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private WeightedRoundRobinViewMBean view;

    /** we are not supporting members */
    public void setApplicationMembers(List<Member> members) {
        throw new UnsupportedOperationException("This algorithm doesn't operate on Members");
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setLoadBalanceEndpoint(Endpoint endpoint) {
        this.loadBalanceEndpoint = endpoint;   
    }

    public Endpoint getNextEndpoint(MessageContext synapseMessageContext,
                                    AlgorithmContext algorithmContext) {

        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (!isThreadLocal) {
                synchronized (this) {
                    EndpointState state = endpointStates[endpointCursor];
                    if (state.getCurrentWeight() == 0) {
                        // reset the current state
                        state.reset();

                        // go to the next endpoint
                        if (endpointCursor == endpointStates.length - 1) {
                            endpointCursor = 0;
                        } else {
                            ++endpointCursor;
                        }

                        state = endpointStates[endpointCursor];
                    }

                    // we are about to use this endpoint, so decrement its current count
                    state.decrementCurrentWeight();

                    // return the endpoint corresponding to the current position
                    return endpoints.get(state.getEndpointPosition());
                }
            } else {
                if (threadedAlgorithm != null) {
                    Algorithm algo = threadedAlgorithm.get();

                    int position = algo.getNextEndpoint();

                    return endpoints.get(position);
                } else {
                    String msg = "Algorithm: WeightedRoundRobin algorithm not initialized properly";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
            }
        } finally {
            readLock.unlock();
        }
    }        

    public Member getNextApplicationMember(AlgorithmContext algorithmContext) {
        throw new UnsupportedOperationException("This algorithm doesn't operate on Members");
    }

    public void reset(AlgorithmContext algorithmContext) {
        for (EndpointState state : endpointStates) {
            state.reset();
        }

        endpointCursor = 0;
    }

    public String getName() {
        return WeightedRoundRobin.class.getName();
    }

    public LoadbalanceAlgorithm clone() {
        return null;
    }

    public void init(SynapseEnvironment se) {
        if (endpoints == null) {
            String msg = "Endpoints are not set, cannot initialize the algorithm";
            log.error(msg);
            throw new SynapseException(msg);
        }

        endpointStates = new EndpointState[endpoints.size()];

        for (int i = 0; i < endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get(i);
            if (!(endpoint instanceof PropertyInclude)) {
                EndpointState state = new EndpointState(i, DEFAULT_WEIGHT);
                endpointStates[i] = state;
            } else {
                MediatorProperty property =
                        ((PropertyInclude) endpoint).getProperty(LOADBALANCE_WEIGHT);
                EndpointState state;
                if (property != null) {
                    int weight = Integer.parseInt(property.getValue());

                    if (weight <= 0) {
                        String msg = "Weight must be greater than zero";
                        log.error(msg);
                        throw new SynapseException(msg);
                    }

                    state = new EndpointState(i, weight);
                } else {
                    state = new EndpointState(i, DEFAULT_WEIGHT);
                }

                endpointStates[i] = state;
            }
        }

        if (loadBalanceEndpoint instanceof PropertyInclude) {
            MediatorProperty threadLocalProperty = ((PropertyInclude) loadBalanceEndpoint).
                    getProperty(LOADBALANCE_ThEADLOCAL);

            if (threadLocalProperty != null && threadLocalProperty.getValue().equals("true")) {
                isThreadLocal = true;
            }
        }

        view = new WeightedRoundRobinView(this);

        MBeanRegistrar.getInstance().registerMBean(view, "LBAlgorithms",
                loadBalanceEndpoint.getName() != null ? loadBalanceEndpoint.getName() : "LBEpr");
    }

    public void destroy() {}

    /**
     * Implementation of the thread local.
     */
    private class AlgorithmThreadLocal extends ThreadLocal<Algorithm> {
        @Override
        protected Algorithm initialValue() {
            return new Algorithm(endpointStates);
        }
    }

    /**
     * This is a thread local implementation of the algorithm. This way, individual threads will
     * do their own weighted round robin without considering the global state of the endpoints.
     */
    private static class Algorithm {

        /**
         * We keep a sorted array of endpoint states, first state will point to the
         * endpoint with the highest weight
         */
        private EndpointState[] threadLocalEndpointStates = null;

        /**
         * Keep track of the current poistion we are operating on the endpointStates array
         */
        private int threadLocalEndpointCursor = 0;

        public Algorithm(EndpointState[] states) {
            threadLocalEndpointStates = new EndpointState[states.length];
            for (int i = 0; i < states.length; i++) {
                threadLocalEndpointStates[i] = new EndpointState(states[i].getEndpointPosition(),
                        states[i].getWeight());
            }
        }

        public int getNextEndpoint() {
            EndpointState state = threadLocalEndpointStates[threadLocalEndpointCursor];
            if (state.getCurrentWeight() == 0) {
                // reset the current state
                state.reset();

                // go to the next enpoint
                if (threadLocalEndpointCursor == threadLocalEndpointStates.length - 1) {
                    threadLocalEndpointCursor = 0;
                } else {
                    ++threadLocalEndpointCursor;
                }

                state = threadLocalEndpointStates[threadLocalEndpointCursor];
            }

            // we are about to use this endpoint, so decrement its current count
            state.decrementCurrentWeight();

            // return the endpoint corresponfing to the current poistion
            return state.getEndpointPosition();
        }
    }


    /**
     * Simple class for holding the states about the endpoints. 
     */
    private static class EndpointState {
        /** Position of the endpoint, represented by this state */
        private int endpointPosition = 0;

        /** Weight of the endpoint */
        private int weight = 0;

        /** Current weight of the endpoint */
        private int currentWeight = 0;

        public EndpointState(int endpointPosition, int weight) {
            this.endpointPosition = endpointPosition;
            this.weight = weight;
            this.currentWeight = weight;
        }

        public int getEndpointPosition() {
            return endpointPosition;
        }

        public int getWeight() {
            return weight;
        }

        public int getCurrentWeight() {
            return currentWeight;
        }

        public void decrementCurrentWeight() {
            --currentWeight;
        }

        public void reset() {
            currentWeight = weight;
        }
    }

    private void calculate() {
        // now we are going to sort
        Arrays.sort(endpointStates, new Comparator<EndpointState>() {
            public int compare(EndpointState o1, EndpointState o2) {
                return o2.getWeight() - o1.getWeight();
            }
        });
    }

    public void changeWeight(int pos, int weight) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            EndpointState state = null;
            for (EndpointState s : endpointStates) {
                if (s.getEndpointPosition() == pos) {
                    state = s;
                }
            }

            if (state == null) {
                throw new SynapseException("The specified endpoint position cannot be found");
            }

            state.weight = weight;

            calculate();

            reset(null);
        } finally {
            writeLock.unlock();
        }
    }

    public int[] getCurrentWeights() {
        int weights[] = new int[endpointStates.length];

        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1;
        }

        for (EndpointState state : endpointStates) {
            if (state.getEndpointPosition() < weights.length && state.getEndpointPosition() >= 0) {
                weights[state.getEndpointPosition()] = state.getWeight();
            }
        }

        return weights;
    }
}
