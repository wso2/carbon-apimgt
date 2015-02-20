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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.SALoadbalanceEndpoint;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SimpleClientSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;

/**
 * Serializes {@link SALoadbalanceEndpoint} to an XML configuration.
 *
 * @see SALoadbalanceEndpointFactory
 */
public class SALoadbalanceEndpointSerializer extends EndpointSerializer {

    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof SALoadbalanceEndpoint)) {
            handleException("Invalid endpoint type for serializing. " +
                    "Expected: SALoadbalanceEndpoint Found: " + endpoint.getClass().getName());
        }

        SALoadbalanceEndpoint loadbalanceEndpoint = (SALoadbalanceEndpoint) endpoint;

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);
        // serialize the parameters
        serializeProperties(loadbalanceEndpoint, endpointElement);
        
        serializeCommonAttributes(endpoint,endpointElement);

        OMElement loadbalanceElement
                = fac.createOMElement("loadbalance", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(loadbalanceElement);

        Dispatcher dispatcher = loadbalanceEndpoint.getDispatcher();
        if (dispatcher != null) {

            OMElement sessionElement = fac.createOMElement("session", SynapseConstants.SYNAPSE_OMNAMESPACE);
            if (dispatcher instanceof SoapSessionDispatcher) {
                sessionElement.addAttribute("type", "soap", null);
            } else if (dispatcher instanceof HttpSessionDispatcher) {
                sessionElement.addAttribute("type", "http", null);
            } else if (dispatcher instanceof SimpleClientSessionDispatcher) {
                sessionElement.addAttribute("type", "simpleClientSession", null);
            } else {
                handleException("invalid session dispatcher : " + dispatcher.getClass().getName());
            }

            long sessionTimeout = loadbalanceEndpoint.getSessionTimeout();
            if (sessionTimeout != -1) {
                OMElement sessionTimeoutElement = fac.createOMElement("sessionTimeout",
                        SynapseConstants.SYNAPSE_OMNAMESPACE);
                sessionTimeoutElement.setText(String.valueOf(sessionTimeout));
                sessionElement.addChild(sessionTimeoutElement);
            }
            endpointElement.addChild(sessionElement);
        }

        loadbalanceElement.addAttribute(XMLConfigConstants.LOADBALANCE_ALGORITHM,
                loadbalanceEndpoint.getAlgorithm().getClass().getName(),
                null);

        for (Endpoint childEndpoint : loadbalanceEndpoint.getChildren()) {
            loadbalanceElement.addChild(EndpointSerializer.getElementFromEndpoint(childEndpoint));
        }

        return endpointElement;
    }
}
