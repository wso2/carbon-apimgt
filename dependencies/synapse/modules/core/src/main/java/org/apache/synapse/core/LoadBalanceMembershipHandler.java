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
package org.apache.synapse.core;

import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;

import java.util.Properties;

/**
 * This interface is responsible for providing the next member to which a message has to be sent to.
 * Generally, this interface will work with a GCF or other membership discovery mechanism
 */
public interface LoadBalanceMembershipHandler {

    /**
     * Initialize this
     *
     * @param properties The properties specific to this LoadBalanceMembershipHandler
     * @param algorithm  The load balancing algorithm
     */
    void init(Properties properties, LoadbalanceAlgorithm algorithm);

    /**
     * Set the Axis2 ConfigurationContext
     *
     * @param configCtx Axis2 ConfigurationContext
     */
    void setConfigurationContext(ConfigurationContext configCtx);

    /**
     * Get the Axis2 ConfigurationContext
     *
     * @return Axis2 ConfigurationContext
     */
    ConfigurationContext getConfigurationContext();

    /**
     * Get the next application member to whom the message has to be sent to
     *
     * @param context The AlgorithmContext which holds information needed for the algorithm
     * @return Next application member to whom the message has to be sent to
     */
    Member getNextApplicationMember(AlgorithmContext context);

    /**
     * Get the algorithum uses in this membership handler
     *
     * @return  Load balance algorithm use for this Membership handler
     */
    LoadbalanceAlgorithm getLoadbalanceAlgorithm();

    /**
     * get the properties used to init this membership handler
     *
     * @return get the initial properties
     */
    Properties getProperties();


}
