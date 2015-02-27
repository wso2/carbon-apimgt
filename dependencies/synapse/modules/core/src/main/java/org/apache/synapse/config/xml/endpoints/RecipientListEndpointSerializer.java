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
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.RecipientListEndpoint;

/**
 * @author nuwan
 * 
 * erializes {@link RecipientListEndpoint} to an XML configuration.
 *
 * @see RecipientListEndpointFactory
 *
 */
public class RecipientListEndpointSerializer extends EndpointSerializer {

	@Override
	protected OMElement serializeEndpoint(Endpoint endpoint) {
		if (!(endpoint instanceof RecipientListEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);
        
        RecipientListEndpoint recipientListEndpoint = (RecipientListEndpoint) endpoint;

        // serialize the parameters
        serializeProperties(recipientListEndpoint, endpointElement);

        serializeCommonAttributes(endpoint,endpointElement);

        OMElement recipientListElement
                = fac.createOMElement("recipientlist", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(recipientListElement);
        
		// Serialize endpoint elements which are children of the recipientlist
		// element
		if (recipientListEndpoint.getChildren() != null) {
			for (Endpoint childEndpoint : recipientListEndpoint.getChildren()) {
				recipientListElement.addChild(EndpointSerializer
						.getElementFromEndpoint(childEndpoint));
			}
		} else if (recipientListEndpoint.getMembers() != null) {
            for (Member member : recipientListEndpoint.getMembers()) {
                OMElement memberEle = fac.createOMElement(
                        "member", SynapseConstants.SYNAPSE_OMNAMESPACE, recipientListElement);
                memberEle.addAttribute(fac.createOMAttribute(
                        "hostName", null, member.getHostName()));
                memberEle.addAttribute(fac.createOMAttribute(
                        "httpPort", null, String.valueOf(member.getHttpPort())));
                memberEle.addAttribute(fac.createOMAttribute(
                        "httpsPort", null, String.valueOf(member.getHttpsPort())));
                recipientListElement.addChild(memberEle);
            }
        }else{
            OMElement dynamicEpEle = fac.createOMElement(
                    "endpoints", SynapseConstants.SYNAPSE_OMNAMESPACE, recipientListElement);
            new ValueSerializer().serializeValue(recipientListEndpoint.getDynamicEnpointSet(), "value", dynamicEpEle);
            dynamicEpEle.addAttribute(fac.createOMAttribute("max-cache", null,
                                                            String.valueOf(recipientListEndpoint.getCurrentPoolSize())));
            recipientListElement.addChild(dynamicEpEle);
        }

        return endpointElement;
    }

}
