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
import org.apache.axis2.clustering.Member;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;

/**
 * Serializes {@link LoadbalanceEndpoint} to an XML configuration.
 *
 * @see LoadbalanceEndpointFactory
 */
public class LoadbalanceEndpointSerializer extends EndpointSerializer {

    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof LoadbalanceEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);
        
        LoadbalanceEndpoint loadbalanceEndpoint = (LoadbalanceEndpoint) endpoint;

        serializeCommonAttributes(endpoint,endpointElement);

        OMElement loadbalanceElement
                = fac.createOMElement("loadbalance", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(loadbalanceElement);

        loadbalanceElement.addAttribute(XMLConfigConstants.LOADBALANCE_ALGORITHM,
                                        loadbalanceEndpoint.getAlgorithm().getClass().getName(),
                                        null);

        // set if failover is turned off in the endpoint
        if (!loadbalanceEndpoint.isFailover()) {
            loadbalanceElement.addAttribute("failover", "false", null);
        }

        // Serialize endpoint elements which are children of the loadbalance element
        if (loadbalanceEndpoint.getChildren() != null) {
            for (Endpoint childEndpoint : loadbalanceEndpoint.getChildren()) {
                loadbalanceElement.addChild(EndpointSerializer.getElementFromEndpoint(childEndpoint));
            }

        } else {
            for (Member member : loadbalanceEndpoint.getMembers()) {
                OMElement memberEle = fac.createOMElement(
                        "member", SynapseConstants.SYNAPSE_OMNAMESPACE, loadbalanceElement);
                memberEle.addAttribute(fac.createOMAttribute(
                        "hostName", null, member.getHostName()));
                memberEle.addAttribute(fac.createOMAttribute(
                        "httpPort", null, String.valueOf(member.getHttpPort())));
                memberEle.addAttribute(fac.createOMAttribute(
                        "httpsPort", null, String.valueOf(member.getHttpsPort())));
                loadbalanceElement.addChild(memberEle);
            }
        }

        // serialize the parameters
        serializeProperties(loadbalanceEndpoint, endpointElement);

        return endpointElement;
    }
}
