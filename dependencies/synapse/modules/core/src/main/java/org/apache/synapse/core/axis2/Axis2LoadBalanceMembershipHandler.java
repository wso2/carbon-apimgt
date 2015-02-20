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
package org.apache.synapse.core.axis2;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;

import java.util.Properties;

/**
 * Bridge between Axis2 membership notification and Synapse load balancing
 */
public class Axis2LoadBalanceMembershipHandler implements LoadBalanceMembershipHandler {
    private static final Log log = LogFactory.getLog(Axis2LoadBalanceMembershipHandler.class);

    private String lbDomain;
    private GroupManagementAgent groupMgtAgent;
    private ConfigurationContext configCtx;
    private LoadbalanceAlgorithm algorithm;
    private Properties properties;

    public void init(Properties props, LoadbalanceAlgorithm algorithm) {
        this.properties = props;
        this.lbDomain = props.getProperty("applicationDomain");
        if(lbDomain == null){
            String msg = "The applicationDomain property has not been specified in the " +
                         "dynamicLoadbalance configuration in the synapse.xml file. This has " +
                         "to be the same as the applicationDomain entry in the loadBalancer" +
                         " entry in the axis2.xml file.";
            log.error(msg);
            throw new SynapseException(msg);
        }
        this.algorithm = algorithm;
    }

    public void setConfigurationContext(ConfigurationContext configCtx) {
        this.configCtx = configCtx;

        // The following code does the bridging between Axis2 and Synapse load balancing
        ClusteringAgent clusteringAgent = configCtx.getAxisConfiguration().getClusteringAgent();
        if(clusteringAgent == null){
            String msg = "In order to enable load balancing across an Axis2 cluster, " +
                         "the cluster entry should be enabled in the axis2.xml file";
            log.error(msg);
            throw new SynapseException(msg);
        }
        groupMgtAgent = clusteringAgent.getGroupManagementAgent(lbDomain);
        if(groupMgtAgent == null){
            String msg =
                    "A LoadBalanceEventHandler has not been specified in the axis2.xml " +
                    "file for the domain " + lbDomain;
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    public ConfigurationContext getConfigurationContext(){
        return configCtx;
    }

    /**
     * Getting the next member to which the request has to be sent in a round-robin fashion
     *
     * @param context The AlgorithmContext
     * @return The current member
     */
    public Member getNextApplicationMember(AlgorithmContext context) {
        algorithm.setApplicationMembers(groupMgtAgent.getMembers());
        return algorithm.getNextApplicationMember(context);
    }

    public LoadbalanceAlgorithm getLoadbalanceAlgorithm() {
        return this.algorithm;
    }

    public Properties getProperties() {
        return this.properties;
    }
}
