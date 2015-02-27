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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.utils.LoadbalanceAlgorithmFactory;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.ServiceDynamicLoadbalanceEndpoint;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.*;

/**
 * Creates {@link org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint} using an XML configuration.
 * <p/>
 * <pre>
 * <endpoint name="sdLB">
 *       <serviceDynamicLoadbalance algorithm="org.apache.synapse.endpoints.algorithms.RoundRobin"
 *                                  configuration="file:repository/conf/lbservices.xml"/>
 * </endpoint>
 * </pre>
 * <p/>
 * The configuration file has the following format. This can be even specified inline
 * <loadBalancerConfig>
 * <services>
 * <service>
 * <hosts>
 * <host>as.cloud.wso2.com</host>
 * <host>as.stratoslive.com</host>
 * </hosts>
 * <domain>wso2as.domain</domain>
 * </service>
 * <service>
 * <hosts>
 * <host>esb.cloud.wso2.com</host>
 * <host>esb.stratoslive.com</host>
 * </hosts>
 * <domain>wso2esb.domain</domain>
 * </service>
 * <service>
 * <hosts>
 * <host>governance.cloud.wso2.com</host>
 * <host>governance.stratoslive.com</host>
 * </hosts>
 * <domain>wso2governance.domain</domain>
 * </service>
 * <service>
 * <hosts>
 * <host>gs.cloud.wso2.com</host>
 * <host>gs.stratoslive.com</host>
 * </hosts>
 * <domain>wso2gs.domain</domain>
 * </service>
 * </services>
 * </loadBalancerConfig>
 */
public class ServiceDynamicLoadbalanceEndpointFactory extends EndpointFactory {

    private static ServiceDynamicLoadbalanceEndpointFactory instance =
            new ServiceDynamicLoadbalanceEndpointFactory();
    public static final QName SERVICES_QNAME = new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                         "services");
    public static final QName LB_CONFIG_QNAME = new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                          "loadBalancerConfig");

    private ServiceDynamicLoadbalanceEndpointFactory() {
    }

    public static ServiceDynamicLoadbalanceEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        OMElement loadbalanceElement =
                epConfig.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                         "serviceDynamicLoadbalance"));
        if (loadbalanceElement == null) {
            return null;
        }

        String configuration =
                loadbalanceElement.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE,
                                                               "configuration"));
        OMElement servicesEle;
        if (configuration != null) {
            if (configuration.startsWith("$system:")) {
                configuration = System.getProperty(configuration.substring("$system:".length()));
            }
            // Load the file
            StAXOMBuilder builder = null;
            try {
                builder = new StAXOMBuilder(new URL(configuration).openStream());
            } catch (Exception e) {
                handleException("Could not load ServiceDynamicLoadbalanceEndpoint configuration file " +
                                configuration);
            }
            servicesEle = builder.getDocumentElement().getFirstChildWithName(SERVICES_QNAME);
        } else {
            OMElement lbConfigEle = loadbalanceElement.getFirstChildWithName(LB_CONFIG_QNAME);
            if (lbConfigEle == null) {
                throw new RuntimeException("loadBalancerConfig element not found as a child of " +
                                           "serviceDynamicLoadbalance element");
            }
            servicesEle = lbConfigEle.getFirstChildWithName(SERVICES_QNAME);
        }

        if (servicesEle == null) {
            throw new RuntimeException("services element not found in serviceDynamicLoadbalance configuration");
        }
        Map<String, String> hostDomainMap = new HashMap<String, String>();
        for (Iterator<OMElement> iter = servicesEle.getChildrenWithLocalName("service"); iter.hasNext();) {
            OMElement serviceEle = iter.next();
            OMElement hostsEle =
                    serviceEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "hosts"));
            if (hostsEle == null) {
                throw new RuntimeException("hosts element not found as a child of service element");
            }
            List<String> hosts = new ArrayList<String>();
            for (Iterator<OMElement> hostIter = hostsEle.getChildrenWithLocalName("host");
                 hostIter.hasNext();) {
                OMElement hostEle = hostIter.next();
                String host = hostEle.getText();
                if (host.trim().length() == 0) {
                    throw new RuntimeException("host cannot be null");
                }
                hosts.add(host);
            }
            OMElement domainEle =
                    serviceEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                               "domain"));
            if (domainEle == null) {
                throw new RuntimeException("domain element not found in as a child of services");
            }
            String domain = domainEle.getText();
            if (domain.trim().length() == 0) {
                throw new RuntimeException("domain cannot be null");
            }
            for (String host : hosts) {
                if (hostDomainMap.containsKey(host)) {
                    throw new RuntimeException("host " + host + " has been already defined for " +
                                               "clustering domain " + hostDomainMap.get(host));
                }
                hostDomainMap.put(host, domain);
            }
        }
        if (hostDomainMap.isEmpty()) {
            throw new RuntimeException("No service elements defined under services");
        }

        LoadbalanceAlgorithm algorithm =
                LoadbalanceAlgorithmFactory.
                        createLoadbalanceAlgorithm(loadbalanceElement, null);

        ServiceDynamicLoadbalanceEndpoint loadbalanceEndpoint =
                new ServiceDynamicLoadbalanceEndpoint(hostDomainMap, algorithm);

        // set endpoint name
        OMAttribute name =
                epConfig.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        if (name != null) {
            loadbalanceEndpoint.setName(name.getAttributeValue());
        }

        // get the session for this endpoint
        OMElement sessionElement =
                epConfig.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "session"));
        if (sessionElement != null) {

            OMElement sessionTimeout = sessionElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sessionTimeout"));

            if (sessionTimeout != null) {
                try {
                    loadbalanceEndpoint.setSessionTimeout(Long.parseLong(
                            sessionTimeout.getText().trim()));
                } catch (NumberFormatException nfe) {
                    handleException("Invalid session timeout value : " + sessionTimeout.getText());
                }
            }

            String type = sessionElement.getAttributeValue(new QName("type"));

            if (type.equalsIgnoreCase("soap")) {
                Dispatcher soapDispatcher = new SoapSessionDispatcher();
                loadbalanceEndpoint.setDispatcher(soapDispatcher);

            } else if (type.equalsIgnoreCase("http")) {
                Dispatcher httpDispatcher = new HttpSessionDispatcher();
                loadbalanceEndpoint.setDispatcher(httpDispatcher);

            }

            loadbalanceEndpoint.setSessionAffinity(true);
        }
        loadbalanceEndpoint.setFailover(false);

        return loadbalanceEndpoint;
    }
}
