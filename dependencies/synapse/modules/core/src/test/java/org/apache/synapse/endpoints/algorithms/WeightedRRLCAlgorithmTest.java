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

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

public class WeightedRRLCAlgorithmTest extends TestCase {
    private String[] hosts = {"localhost:9000", "localhost:9001",
                              "localhost:9002", "localhost:9003",
                              "localhost:9004"};

    private String[] weights = {"6", "5", "3", "2", "1"};

    private AtomicInteger[] connections = {
            new AtomicInteger(18), new AtomicInteger(5),
            new AtomicInteger(4), new AtomicInteger(2),
            new AtomicInteger(1)};

    public void testInitialization () {
        LoadbalanceEndpoint endpoint = createLoadBalanceEndpoint();

        LoadbalanceAlgorithm algo = endpoint.getAlgorithm();

        assert algo instanceof WeightedRRLCAlgorithm;                    
        WeightedRRLCAlgorithm algorithm = (WeightedRRLCAlgorithm) algo;

        assertEquals(6 + 5 + 3 + 2 + 1, algorithm.getTotalWeight());
        assertEquals(0, algorithm.getTotalConnections());
        assertEquals(0, algorithm.getEndpointCursor());
        assertEquals(2, algorithm.getRoundsPerRecalculation());
    }

    public void testNextEndpoint() {
        MessageContext messageContext = createMessageContext();
        LoadbalanceEndpoint endpoint = createLoadBalanceEndpoint();

        String []firstTwoRoundsExpected = {
                "6", "6", "6", "6", "6", "6", "5", "5", "5", "5", "5", "3", "3", "3", "2", "2", "1",
                "6", "6", "6", "6", "6", "6", "5", "5", "5", "5", "5", "3", "3", "3", "2", "2", "1"};

        LoadbalanceAlgorithm algo = endpoint.getAlgorithm();

        String []firstTwoRoundsResults = new String[34];
        for (int i = 0; i < 34; i++) {
            Endpoint epr = algo.getNextEndpoint(messageContext, null);
            if (epr instanceof AddressEndpoint) {
                firstTwoRoundsResults[i] =
                        ((AddressEndpoint)epr).getProperty(
                                WeightedRRLCAlgorithm.LB_WEIGHTED_RRLC_WEIGHT).getValue();
            }
        }

        for (int i = 0; i < 34; i++) {
            assertEquals(firstTwoRoundsExpected[i], firstTwoRoundsResults[i]);
        }

        String []secondTwoRoundsExpected = {
                "6", "6", "6", "6", "5", "5", "5", "5", "5", "5", "5", "3", "3", "3", "3", "2", "2",
                "2", "2", "1", "1", "6", "6", "6", "6", "5", "5", "5", "5", "5", "5", "5", "3", "3"};
        String []secondTwoRoundsResults = new String[34];
        for (int i = 0; i < 34; i++) {
            Endpoint epr = algo.getNextEndpoint(messageContext, null);
            if (epr instanceof AddressEndpoint) {
                secondTwoRoundsResults[i] =
                        ((AddressEndpoint)epr).getProperty(
                                WeightedRRLCAlgorithm.LB_WEIGHTED_RRLC_WEIGHT).getValue();
            }
        }

        for (int i = 0; i < 34; i++) {
            assertEquals(secondTwoRoundsExpected[i], secondTwoRoundsResults[i]);
        }
    }

    private LoadbalanceEndpoint createLoadBalanceEndpoint() {
        LoadbalanceEndpoint loadbalanceEndpoint = new LoadbalanceEndpoint();
        List<Endpoint> endpoints = createEndpoints();
        WeightedRRLCAlgorithm algorithm = new WeightedRRLCAlgorithm();

        MediatorProperty property = new MediatorProperty();
        property.setName(WeightedRRLCAlgorithm.LB_WEIGHTED_RRLC_ROUNDS_PER_RECAL);
        property.setValue("2");
        loadbalanceEndpoint.addProperty(property);        

        algorithm.setEndpoints(endpoints);
        algorithm.setLoadBalanceEndpoint(loadbalanceEndpoint);

        loadbalanceEndpoint.setChildren(endpoints);
        loadbalanceEndpoint.setAlgorithm(algorithm);

        SynapseEnvironment env = new Axis2SynapseEnvironment(
                new ConfigurationContext(new AxisConfiguration()), new SynapseConfiguration());
        loadbalanceEndpoint.init(env);
        return loadbalanceEndpoint;
    }

    private MessageContext createMessageContext() {
        org.apache.axis2.context.MessageContext axisMessageContext =
                new org.apache.axis2.context.MessageContext();

        MessageContext synapseMessageContext =
                new Axis2MessageContext(axisMessageContext, null, null);

        axisMessageContext.setProperty("OPEN_CONNNECTIONS_MAP", createMap());

        return synapseMessageContext;
    }

    private Map<String, AtomicInteger> createMap() {
        Map<String, AtomicInteger> connectionsMap = new HashMap<String, AtomicInteger>();
        for (int i = 0; i < hosts.length; i++) {
            connectionsMap.put(hosts[i], connections[i]);
        }
        return connectionsMap;
    }


    private List<Endpoint> createEndpoints() {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        for (int i = 0; i < hosts.length; i++) {
            AddressEndpoint addressEndpoint = new AddressEndpoint();

            EndpointDefinition definition = new EndpointDefinition();
            definition.setAddress("http://" + hosts[i] + "/");
            addressEndpoint.setDefinition(definition);

            MediatorProperty property = new MediatorProperty();
            property.setName(WeightedRRLCAlgorithm.LB_WEIGHTED_RRLC_WEIGHT);
            property.setValue(weights[i]);
            addressEndpoint.addProperty(property);

            endpoints.add(addressEndpoint);
        }

        return endpoints;
    }
}
