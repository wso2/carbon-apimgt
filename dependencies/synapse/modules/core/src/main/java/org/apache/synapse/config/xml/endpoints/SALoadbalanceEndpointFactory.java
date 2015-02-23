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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.endpoints.utils.LoadbalanceAlgorithmFactory;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.SALoadbalanceEndpoint;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SimpleClientSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Properties;

/**
 * Creates {@link SALoadbalanceEndpoint} from an XML configuration.
 *
 * &lt;endpoint [name="name"]&gt;
 *    &lt;session type="soap | ..other session types.." /&gt;
 *    &lt;loadbalance policy="policy"&gt;
 *       &lt;endpoint&gt;+
 *    &lt;/loadbalance&gt;
 * &lt;/endpoint&gt;
 */
public class SALoadbalanceEndpointFactory extends EndpointFactory {

    private static SALoadbalanceEndpointFactory instance = new SALoadbalanceEndpointFactory();

    private SALoadbalanceEndpointFactory() {}

    public static SALoadbalanceEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        // create the endpoint, manager and the algorithms
        SALoadbalanceEndpoint loadbalanceEndpoint = new SALoadbalanceEndpoint();

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

            } else if (type.equalsIgnoreCase("simpleClientSession")) {
                Dispatcher csDispatcher = new SimpleClientSessionDispatcher();
                loadbalanceEndpoint.setDispatcher(csDispatcher);
            }
        } else {
            handleException("Session affinity endpoints should " +
                    "have a session element in the configuration.");
        }

        // set endpoint name
        OMAttribute name = epConfig.getAttribute(new QName(
                org.apache.synapse.config.xml.XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            loadbalanceEndpoint.setName(name.getAttributeValue());
        }

        OMElement loadbalanceElement;
        loadbalanceElement = epConfig.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "loadbalance"));

        if(loadbalanceElement != null) {

            // set endpoints
            List<Endpoint> endpoints = getEndpoints(loadbalanceElement,
                    loadbalanceEndpoint, properties);
            loadbalanceEndpoint.setChildren(endpoints);

            // set load balance algorithm
            LoadbalanceAlgorithm algorithm = LoadbalanceAlgorithmFactory.
                    createLoadbalanceAlgorithm(loadbalanceElement, endpoints);
            loadbalanceEndpoint.setAlgorithm(algorithm);

            // process the parameters
            processProperties(loadbalanceEndpoint, epConfig);

            return loadbalanceEndpoint;
        }

        return null;
    }
}
