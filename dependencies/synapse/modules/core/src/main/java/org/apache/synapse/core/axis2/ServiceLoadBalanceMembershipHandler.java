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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Bridge between Axis2 membership notification and Synapse load balancing
 */
public class ServiceLoadBalanceMembershipHandler implements LoadBalanceMembershipHandler {
    private static final Log log = LogFactory.getLog(ServiceLoadBalanceMembershipHandler.class);

    private ConfigurationContext configCtx;

    /**
     * Key - Host, Value - DomainAlgorithmContext
     */
    private Map<String, DomainAlgorithmContext> hostDomainAlgorithmContextMap =
                                    new HashMap<String, DomainAlgorithmContext>();
    private ClusteringAgent clusteringAgent;

    public ServiceLoadBalanceMembershipHandler(Map<String, String> hostDomainMap,
                                               LoadbalanceAlgorithm algorithm,
                                               ConfigurationContext configCtx,
                                               boolean isClusteringEnabled,
                                               String endpointName) {
        for (Map.Entry<String, String> entry : hostDomainMap.entrySet()) {
            AlgorithmContext algorithmContext =
                new AlgorithmContext(isClusteringEnabled, configCtx, endpointName + "." + entry.getKey());
            this.hostDomainAlgorithmContextMap.put(entry.getKey(),
                                   new DomainAlgorithmContext(entry.getValue(), algorithm.clone(), algorithmContext));
        }
    }

    public void init(Properties props, LoadbalanceAlgorithm algorithm) {
        // Nothing to do
    }

    public void setConfigurationContext(ConfigurationContext configCtx) {
        this.configCtx = configCtx;

        // The following code does the bridging between Axis2 and Synapse load balancing
        clusteringAgent = configCtx.getAxisConfiguration().getClusteringAgent();
        if(clusteringAgent == null){
            String msg = "In order to enable load balancing across an Axis2 cluster, " +
                         "the cluster entry should be enabled in the axis2.xml file";
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
     * @deprecated Use {@link #getNextApplicationMember(String)}
     */
    public Member getNextApplicationMember(AlgorithmContext context) {
        throw new UnsupportedOperationException("This operation is invalid. " +
                                                "Call getNextApplicationMember(String host)");
    }

    public Member getNextApplicationMember(String host) {
        DomainAlgorithmContext domainAlgorithmContext = getDomainAlgorithmContext(host);
        String lbDomain = domainAlgorithmContext.getDomain();
        LoadbalanceAlgorithm algorithm = domainAlgorithmContext.getAlgorithm();
        GroupManagementAgent groupMgtAgent = clusteringAgent.getGroupManagementAgent(lbDomain);
        if(groupMgtAgent == null){
            String msg =
                    "A LoadBalanceEventHandler has not been specified in the axis2.xml " +
                    "file for the domain " + lbDomain + " for host " + host;
            log.error(msg);
            throw new SynapseException(msg);
        }
        algorithm.setApplicationMembers(groupMgtAgent.getMembers());
        AlgorithmContext context = domainAlgorithmContext.getAlgorithmContext();
        return algorithm.getNextApplicationMember(context);
    }

    private DomainAlgorithmContext getDomainAlgorithmContext(String host) {
        DomainAlgorithmContext domainAlgorithmContext = hostDomainAlgorithmContextMap.get(host);
        if(domainAlgorithmContext == null) {
            int indexOfDot;
            if ((indexOfDot = host.indexOf(".")) != -1) {
                domainAlgorithmContext = getDomainAlgorithmContext(host.substring(indexOfDot + 1));
            } else {
                throw new SynapseException("Domain not found for host" + host);
            }
        }
        return domainAlgorithmContext;
    }

    public LoadbalanceAlgorithm getLoadbalanceAlgorithm() {
        return null;
    }

    public Properties getProperties() {
        return null;
    }

    /**
     * POJO for maintaining the domain & AlgorithmContext for a particular host
     */
    private static class DomainAlgorithmContext {
        // The clustering domain
        private String domain;
        private AlgorithmContext algorithmContext;
        private LoadbalanceAlgorithm algorithm;

        private DomainAlgorithmContext(String domain, LoadbalanceAlgorithm algorithm,
                                       AlgorithmContext algorithmContext) {
            this.domain = domain;
            this.algorithm = algorithm;
            this.algorithmContext = algorithmContext;
        }

        public LoadbalanceAlgorithm getAlgorithm() {
            return algorithm;
        }

        public String getDomain() {
            return domain;
        }

        public AlgorithmContext getAlgorithmContext() {
            return algorithmContext;
        }
    }
}
