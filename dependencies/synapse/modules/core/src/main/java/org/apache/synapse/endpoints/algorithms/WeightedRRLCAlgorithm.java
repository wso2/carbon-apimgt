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

import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.PropertyInclude;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.axis2.clustering.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This is a Weighted Round Robin Least Connection algorithm.</p>
 *
 * <p> This algorithm is similar to {@link WeightedRoundRobin WeightedRoundRobin} algorithm
 * except it takes the active connections made by the endpoints in to account. Weights assinged
 * to each endpoint and these are static weights. But depending on the active connections these
 * weights are changed dynamically during the execution. </p>
 *
 * <p> Algorithm assumes that the endpoint connections to total connection ratio should be eqault
 * to endpoint weight to total weights ratio. If the ratios are different it tries to align them
 * by changing the weights dynamically.</p>
 *
 */
public class WeightedRRLCAlgorithm implements LoadbalanceAlgorithm, ManagedLifecycle {
    private static final Log log = LogFactory.getLog(WeightedRRLCAlgorithm.class);

    /**
     * Endpoints list for the round robin algorithm
     */
    private List<Endpoint> endpoints = null;
    /** Load balance endpoint */
    private Endpoint loadBalanceEndpoint = null;
    /** We keep a sorted array of endpoint states, first state will point to the
     * endpoint with the highest weight */
    private WeightedState[] list;
    /** Keep track of the current poistion we are operating on the endpointStates array */
    private int endpointCursor = 0;
    /** How many rounds should go before re-calculating the dynamic weights based
     * on number of active connections */
    private int roundsPerRecalculation = 1;
    /** How many rounds we have gone throug */
    private int currentRound = 0;
    /** total weight of the endpoints */
    private int totalWeight = 0;
    /** current connection count */
    private int totalConnections = 0;

    public static final String LB_WEIGHTED_RRLC_ROUNDS_PER_RECAL =
            "loadbalance.weightedRRLC.roundsPerRecal";
    public static final String LB_WEIGHTED_RRLC_WEIGHT = "loadbalance.weight";
    public static final String LB_WEIGHTED_RRLC_WEIGHT_MIN = LB_WEIGHTED_RRLC_WEIGHT + ".min";
    public static final String LB_WEIGHTED_RRLC_WEIGHT_MAX = LB_WEIGHTED_RRLC_WEIGHT + ".max";
    public static final int LB_WEIGHTED_RRLC_WEIGHT_SKEW = 2;    

    public void setApplicationMembers(List<Member> members) {}

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setLoadBalanceEndpoint(Endpoint endpoint) {
        this.loadBalanceEndpoint = endpoint;        
    }

    public synchronized Endpoint getNextEndpoint(MessageContext messageContext,
                                                 AlgorithmContext algorithmContext) {                
        WeightedState s = list[endpointCursor];

        // once we choose an endpoit we countinue to use that until all
        // the chances are over for this round
        if (!s.isSendsAvailable()) {
            // reset this state for this round
            s.resetPerRound();
            do {
                if (++endpointCursor == list.length) {
                    endpointCursor = 0;             
                    // if we we have gone through enough cycles to recalculate the weights based
                    // on the current connection count recalculate the current weights
                    if (++currentRound == roundsPerRecalculation) {
                        currentRound = 0;
                        // we recalculate the current weights based on the connections and weights
                        reCalcuateWeights(messageContext);
                    }
                }
                s = list[endpointCursor];
            } while (!s.isSendsAvailable());
        }

        s.chosenToSend();
        // get the endpoint correspondint to the current poistion and return it
        return endpoints.get(s.getEndpointPosition());
    }

    /**
     * Initialize the algorithm reading the configurations from the endpoints.
     */
    private void intialize() {
        // get the global properties
        if (loadBalanceEndpoint != null && loadBalanceEndpoint instanceof PropertyInclude) {
            PropertyInclude include = (PropertyInclude) loadBalanceEndpoint;

            MediatorProperty val = include.getProperty(LB_WEIGHTED_RRLC_ROUNDS_PER_RECAL);
            if (val != null) {
                roundsPerRecalculation = Integer.parseInt(val.getValue());
            }
        }

        // initialize the states list, this runs only once
        list = new WeightedState[endpoints.size()];

        int totalWeight = 0;
        for (Endpoint endpoint : endpoints) {
            if (endpoint instanceof PropertyInclude) {
                PropertyInclude include = (PropertyInclude) endpoint;
                MediatorProperty val = include.getProperty(LB_WEIGHTED_RRLC_WEIGHT);

                if (val == null) {
                    String msg = "Parameter " +
                            "loadbalance.weighted.weight should be specified for every " +
                            "endpoint in the load balance group";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
                totalWeight += Integer.parseInt(val.getValue());
            }
        }

        this.totalWeight = totalWeight;

        for (int i = 0; i < endpoints.size(); i++) {
            Endpoint e = endpoints.get(i);
            if (e instanceof PropertyInclude) {
                PropertyInclude include = (PropertyInclude) e;

                MediatorProperty weight = include.getProperty(
                        LB_WEIGHTED_RRLC_WEIGHT);

                String key;
                URL url;
                if (e instanceof AddressEndpoint) {
                    AddressEndpoint addressEndpoint = (AddressEndpoint) e;
                    try {
                        url = new URL(addressEndpoint.getDefinition().getAddress());
                    } catch (MalformedURLException e1) {
                        String msg = "Mulformed URL in address endpoint";
                        log.error(msg);
                        throw new SynapseException(msg);
                    }
                } else if (e instanceof WSDLEndpoint) {
                    WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) e;
                    try {
                        url = new URL(wsdlEndpoint.getDefinition().getAddress());
                    } catch (MalformedURLException e1) {
                        String msg = "Mulformed URL in address endpoint";
                        log.error(msg);
                        throw new SynapseException(msg);
                    }
                } else {
                    String msg = "Only AddressEndpoint and WSDLEndpoint can be used " +
                                    "with WeightedRRLCAlgorithm";
                    log.error(msg);
                    throw new SynapseException(msg);
                }

                // construct the key
                key = url.getHost() + ":" + url.getPort();

                WeightedState state = new WeightedState(
                        Integer.parseInt(weight.getValue()), i, key);

                MediatorProperty minimumWeight = include.getProperty(
                        LB_WEIGHTED_RRLC_WEIGHT_MIN);
                if (minimumWeight != null) {
                    state.setMinWeight(Integer.parseInt(minimumWeight.getValue()));
                }

                MediatorProperty maxWeight = include.getProperty(
                        LB_WEIGHTED_RRLC_WEIGHT_MAX);
                if (maxWeight != null) {
                    state.setMaxWeight(Integer.parseInt(maxWeight.getValue()));
                }

                list[i] = state;
            }
        }                   

        // sort the states according to the initial fixed weights
        Arrays.sort(list, new Comparator<WeightedState>() {
            public int compare(WeightedState o1, WeightedState o2) {
                return o2.getFixedWeight() - o1.getFixedWeight();
            }
        });
    }

    public Member getNextApplicationMember(AlgorithmContext algorithmContext) {
        // this doesn't make sense for weighted load balance algorithm
        return null;
    }

    public void reset(AlgorithmContext algorithmContext) {
        for (WeightedState state : list) {
            state.reset();
        }
    }

    public String getName() {
        return WeightedRRLCAlgorithm.class.getName();
    }

    public LoadbalanceAlgorithm clone() {
        return null;  
    }

    public int getEndpointCursor() {
        return endpointCursor;
    }

    public int getRoundsPerRecalculation() {
        return roundsPerRecalculation;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    /**
     * Recalculate the dynamic weights based on the active connection count.
     *
     * @param messageContext synapse message context
     */
    private void reCalcuateWeights(MessageContext messageContext) {
        Map connectionsMap = null;
        // fetch the connections map
        if (messageContext instanceof Axis2MessageContext) {
            Axis2MessageContext axis2MessageContext = (Axis2MessageContext) messageContext;
            org.apache.axis2.context.MessageContext msgCtx =
                    axis2MessageContext.getAxis2MessageContext();

            Object obj = msgCtx.getProperty("OPEN_CONNNECTIONS_MAP");
            if (obj != null && obj instanceof Map) {
                connectionsMap = (Map) obj;
            }
        }

        if (connectionsMap == null) {
            String msg = "Connections map not found.";
            log.error(msg);
            throw new SynapseException(msg);
        }

        for (WeightedState state : list) {
            String key = state.getKeyToConnectionCount();
            AtomicInteger integer = (AtomicInteger) connectionsMap.get(key);

            if (integer != null) {
                state.setCurrentConnectionCount(integer.get());
            } else {
                state.setCurrentConnectionCount(0);
            }

            totalConnections += state.getCurrentConnectionCount();
        }

        for (WeightedState state : list) {
            state.reCalcuateWeight();
        }
    }

    public void init(SynapseEnvironment se) {
        intialize();
    }

    public void destroy() {}

    /**
     * Simple class for holding the states about the endpoints.
     */
    private class WeightedState {
        /** this is the statics weight specified by the user */
        private int fixedWeight = 0;
        /** position of the endpoint related to this state */
        private int endpointPosition = 0;
        /** current weight of the algorithm, this is calculated based on sends through this epr */
        private int currentWeight = 1;
        /** calculated weight for this round */
        private int currentCalcWeight = 0;
        /** current connection count */
        private int currentConnectionCount = 0;
        /** minimum possible weight */
        private int minWeight = 0;
        /** maximum possible weight */
        private int maxWeight = 0;
        /** holds the key to access the connection count */
        private String keyToConnectionCount = "";

        public WeightedState(int weight, int endpointPosition, String keyToConnectionCount) {
            this.fixedWeight = weight;
            this.endpointPosition = endpointPosition;
            this.currentWeight = fixedWeight;
            this.currentCalcWeight = fixedWeight;
            this.keyToConnectionCount = keyToConnectionCount;
            this.maxWeight = fixedWeight + LB_WEIGHTED_RRLC_WEIGHT_SKEW;
            this.minWeight = fixedWeight - LB_WEIGHTED_RRLC_WEIGHT_SKEW > 0 ?
                    fixedWeight - LB_WEIGHTED_RRLC_WEIGHT_SKEW : 0;
        }

        public int getEndpointPosition() {
            return endpointPosition;
        }

        public int getFixedWeight() {
            return fixedWeight;
        }

        public boolean isSendsAvailable() {
            return currentCalcWeight > 0;
        }

        public void chosenToSend() {
            currentCalcWeight--;
        }

        public int getCurrentWeight() {
            return currentWeight;
        }

        public void setMinWeight(int minWeight) {
            this.minWeight = minWeight;
        }

        public String getKeyToConnectionCount() {
            return keyToConnectionCount;
        }

        public void setCurrentWeight(int currentWeight) {
            this.currentWeight = currentWeight;
        }

        public void setCurrentConnectionCount(int currentConnectionCount) {
            this.currentConnectionCount = currentConnectionCount;
        }

        public int getCurrentConnectionCount() {
            return currentConnectionCount;
        }

        public void setMaxWeight(int maxWeight) {
            this.maxWeight = maxWeight;
        }

        /**
         * Recalcualate the weights based on the current connection count for this set of rounds.         
         */
        public void reCalcuateWeight() {
            if (totalConnections > 0) {
                double weightRatio = (double) fixedWeight / totalWeight;
                double connectionRatio;
                if (totalConnections != 0) {
                    connectionRatio = (double) currentConnectionCount / totalConnections;
                } else {
                    connectionRatio = 0;
                }

                double diff = weightRatio - connectionRatio;
                double multiple = diff * totalConnections;
                double floor = Math.floor(multiple);

                if (floor - multiple >= -0.5) {
                    currentWeight = fixedWeight + (int) floor;
                } else {
                    currentWeight = fixedWeight + (int) Math.ceil(multiple);
                }

                if (diff < 0) {
                    // we always return the max from minWeight and calculated Current weight
                    currentWeight = minWeight > currentWeight ? minWeight : currentWeight;
                } else {
                    // we always return the min from maxWeight and calculated Current weight
                    currentWeight = maxWeight < currentWeight ? maxWeight : currentWeight;
                }
                currentCalcWeight = currentWeight;
            }
        }

        public void resetPerRound() {
            currentCalcWeight = currentWeight;
        }

        public void reset() {
            currentWeight = fixedWeight;
            currentConnectionCount = 0;
            currentCalcWeight = fixedWeight;
        }
    }
}
