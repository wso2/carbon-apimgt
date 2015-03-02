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
import org.apache.synapse.MessageContext;
import org.apache.synapse.endpoints.Endpoint;

import java.util.List;

/**
 * All load balance algorithms must implement this interface. Implementations of this interface can
 * be registered in LoadbalanceManagers.
 */
public interface LoadbalanceAlgorithm extends Cloneable {

    /**
     * Set the application members
     *
     * @param members The application members
     */
    void setApplicationMembers(List<Member> members);

    /**
     * Set the endpoints
     *
     * @param endpoints The endpoints
     */
    void setEndpoints(List<Endpoint> endpoints);

    /**
     * Set the loadbalance endpoint
     * 
     * @param endpoint the endpoint which uses this algorithm
     */
    void setLoadBalanceEndpoint(Endpoint endpoint);

    /**
     * This method returns the next node according to the algorithm implementation.
     *
     * @param synapseMessageContext SynapseMessageContext of the current message
     * @param algorithmContext      The context in which holds run time states related to the algorithm
     * @return Next node for directing the message
     */
    Endpoint getNextEndpoint(MessageContext synapseMessageContext,
                             AlgorithmContext algorithmContext);


    /**
     * This method returns the next member to which the request has been sent to,
     * according to the algorithm implementation.
     *
     * @param algorithmContext The context in which holds run time states related to the algorithm
     * @return Next application member to which the request has to be sent to
     */
    Member getNextApplicationMember(AlgorithmContext algorithmContext);

    /**
     * Resets the algorithm to its initial position. Initial position depends on the implementation.
     *
     * @param algorithmContext The context in which holds run time states related to the algorithm
     */
    void reset(AlgorithmContext algorithmContext);

    /**
     * Return the name of the load balancing algorithm
     * @return the name of the algorithm implemented
     */
    public String getName();

    public LoadbalanceAlgorithm clone();
}
