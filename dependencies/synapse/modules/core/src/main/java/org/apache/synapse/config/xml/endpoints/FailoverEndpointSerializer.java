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
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.FailoverEndpoint;

/**
 * Serializes {@link FailoverEndpoint} to XML configuration.
 *
 * @see FailoverEndpointFactory
 */
public class FailoverEndpointSerializer extends EndpointSerializer {

    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof FailoverEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        FailoverEndpoint failoverEndpoint = (FailoverEndpoint) endpoint;

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);

        OMElement failoverElement
                = fac.createOMElement("failover", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(failoverElement);

        serializeCommonAttributes(endpoint,endpointElement);

        for (Endpoint childEndpoint : failoverEndpoint.getChildren()) {
            failoverElement.addChild(EndpointSerializer.getElementFromEndpoint(childEndpoint));
        }

        if (!failoverEndpoint.isDynamic()) {
            failoverElement.addAttribute("dynamic", "false", null);
        }

        // serialize the parameters
        serializeProperties(failoverEndpoint, endpointElement);

        return endpointElement;
    }
}
