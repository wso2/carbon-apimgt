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

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.Member;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.endpoints.utils.LoadbalanceAlgorithmFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

/**
 * Creates {@link LoadbalanceEndpoint} using an XML configuration.
 * <p/>
 * &lt;endpoint [name="name"]&gt;
 * &lt;loadbalance policy="load balance algorithm"&gt;
 * &lt;endpoint&gt;+
 * &lt;member hostName="host" httpPort="port" httpsPort="port"&gt;+
 * &lt;/loadbalance&gt;
 * &lt;/endpoint&gt;
 */
public final class LoadbalanceEndpointFactory extends EndpointFactory {

    private static LoadbalanceEndpointFactory instance = new LoadbalanceEndpointFactory();
    private static final QName MEMBER = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "member");

    private LoadbalanceEndpointFactory() {
    }

    public static LoadbalanceEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        // create the endpoint, manager and the algorithms

        OMElement loadbalanceElement = epConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "loadbalance"));

        if (loadbalanceElement != null) {

            LoadbalanceEndpoint loadbalanceEndpoint = new LoadbalanceEndpoint();

            // set endpoint name
            OMAttribute name = epConfig.getAttribute(new QName(
                    org.apache.synapse.config.xml.XMLConfigConstants.NULL_NAMESPACE, "name"));

            if (name != null) {
                loadbalanceEndpoint.setName(name.getAttributeValue());
            }

            LoadbalanceAlgorithm algorithm = null;

            // set endpoints or members
            if (loadbalanceElement.getFirstChildWithName(XMLConfigConstants.ENDPOINT_ELT) != null) {
                if(loadbalanceElement.
                        getChildrenWithName((MEMBER)).hasNext()){
                    String msg =
                            "Invalid Synapse configuration. " +
                            "child elements";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
                List<Endpoint> endpoints
                        = getEndpoints(loadbalanceElement, loadbalanceEndpoint, properties);
                loadbalanceEndpoint.setChildren(endpoints);
                algorithm =
                        LoadbalanceAlgorithmFactory.
                                createLoadbalanceAlgorithm(loadbalanceElement, endpoints);
                algorithm.setLoadBalanceEndpoint(loadbalanceEndpoint);
            } else if (loadbalanceElement.getFirstChildWithName(MEMBER) != null) {
                if(loadbalanceElement.
                        getChildrenWithName((XMLConfigConstants.ENDPOINT_ELT)).hasNext()){
                    String msg =
                            "Invalid Synapse configuration. " +
                            "loadbalanceEndpoint element cannot have both member & endpoint " +
                            "child elements";
                    log.error(msg);
                    throw new SynapseException(msg);
                }

                List<Member> members = getMembers(loadbalanceElement);
                loadbalanceEndpoint.setMembers(members);
                algorithm =
                        LoadbalanceAlgorithmFactory.
                                createLoadbalanceAlgorithm2(loadbalanceElement, members);
                loadbalanceEndpoint.startApplicationMembershipTimer();
            }

            if (loadbalanceEndpoint.getChildren() == null &&
                    loadbalanceEndpoint.getMembers() == null) {
                String msg = "Invalid Synapse configuration.\n"
                    + "A LoadbalanceEndpoint must have child elements, but the LoadbalanceEndpoint "
                    + "'" + loadbalanceEndpoint.getName() + "' does not have any child elements.";
                log.error(msg);
                throw new SynapseException(msg);
            }
            
            // set load balance algorithm
            loadbalanceEndpoint.setAlgorithm(algorithm);

            // set if failover is turned off
            String failover = loadbalanceElement.getAttributeValue(new QName("failover"));
            if (failover != null && failover.equalsIgnoreCase("false")) {
                loadbalanceEndpoint.setFailover(false);
            }

            // process the parameters
            processProperties(loadbalanceEndpoint, epConfig);

            return loadbalanceEndpoint;
        }

        return null;  //ToDo
    }

    private List<Member> getMembers(OMElement loadbalanceElement) {
        List<Member> members = new ArrayList<Member>();
        for(Iterator memberIter = loadbalanceElement.getChildrenWithName(MEMBER);
            memberIter.hasNext();){
            OMElement memberEle = (OMElement) memberIter.next();
            Member member = new Member(memberEle.getAttributeValue(new QName("hostName")), -1);
            String http = memberEle.getAttributeValue(new QName("httpPort"));
            if (http != null) {
                member.setHttpPort(Integer.parseInt(http));
            }
            String https = memberEle.getAttributeValue(new QName("httpsPort"));
            if (https != null && https.trim().length() != 0) {
                member.setHttpsPort(Integer.parseInt(https.trim()));
            }
            members.add(member);
        }
        return members;
    }
}
