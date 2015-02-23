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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.utils.LoadbalanceAlgorithmFactory;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * Creates {@link DynamicLoadbalanceEndpoint} using an XML configuration.
 *
 * <pre>
 * &lt;endpoint>
 *       &lt;dynamicLoadbalance [failover="true|false"] [policy="load balance algorithm"]&gt;
 *           &lt;membershipHandler
 *                   class="HandlerClass"&gt;
 *              &lt;property name="some name" value="some domain"/&gt;+
 *           &lt;/membershipHandler&gt;
 *       &lt;/dynamicLoadbalance&gt;
 * &lt;/endpoint&gt;
 * </pre>
 */
public class DynamicLoadbalanceEndpointFactory extends EndpointFactory {

    private static final Log log = LogFactory.getLog(DynamicLoadbalanceEndpointFactory.class);
    private static DynamicLoadbalanceEndpointFactory instance =
            new DynamicLoadbalanceEndpointFactory();

    private DynamicLoadbalanceEndpointFactory() {
    }

    public static DynamicLoadbalanceEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        OMElement loadbalanceElement =
                epConfig.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                         "dynamicLoadbalance"));

        if (loadbalanceElement != null) {

            DynamicLoadbalanceEndpoint loadbalanceEndpoint = new DynamicLoadbalanceEndpoint();

            // set endpoint name
            OMAttribute name =
                    epConfig.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));

            if (name != null) {
                loadbalanceEndpoint.setName(name.getAttributeValue());
            }

            // get the session for this endpoint
            OMElement sessionElement = epConfig.
                    getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "session"));
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

            // set if failover is turned off
            String failover = loadbalanceElement.getAttributeValue(new QName("failover"));
            if (failover != null && failover.equalsIgnoreCase("false")) {
                loadbalanceEndpoint.setFailover(false);
            } else {
                loadbalanceEndpoint.setFailover(true);
            }

            OMElement eventHandler =
                    loadbalanceElement.
                            getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                            "membershipHandler"));
            if (eventHandler != null) {
                String clazz =
                        eventHandler.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE,
                                                                 "class")).trim();
                try {
                    LoadBalanceMembershipHandler lbMembershipHandler =
                            (LoadBalanceMembershipHandler) Class.forName(clazz).newInstance();
                    Properties lbProperties = new Properties();
                    for (Iterator props = eventHandler.getChildrenWithName(new QName(
                            SynapseConstants.SYNAPSE_NAMESPACE, "property")); props.hasNext();) {
                        OMElement prop = (OMElement) props.next();
                        String propName =
                                prop.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE,
                                                                 "name")).trim();
                        String propValue =
                                prop.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE,
                                                                 "value")).trim();
                        lbProperties.put(propName, propValue);
                    }

                    // Set load balance algorithm
                    LoadbalanceAlgorithm algorithm =
                            LoadbalanceAlgorithmFactory.
                                    createLoadbalanceAlgorithm(loadbalanceElement, null);
                    lbMembershipHandler.init(lbProperties, algorithm);
                    loadbalanceEndpoint.setLoadBalanceMembershipHandler(lbMembershipHandler);
                } catch (Exception e) {
                    String msg = "Could not instantiate " +
                            "LoadBalanceMembershipHandler implementation " + clazz;
                    log.error(msg, e);
                    throw new SynapseException(msg, e);
                }
            }

            processProperties(loadbalanceEndpoint, epConfig);

            return loadbalanceEndpoint;
        }
        return null;
    }
}
