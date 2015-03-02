/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SimpleClientSessionDispatcher;

import java.util.Properties;

/**
 * Creates {@link org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint} using an XML configuration.
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

public class DynamicLoadbalanceEndpointSerializer extends EndpointSerializer {
    
    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof DynamicLoadbalanceEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);

        DynamicLoadbalanceEndpoint dynamicLoadbalanceEndpoint = (DynamicLoadbalanceEndpoint) endpoint;

        // serialize the parameters
        serializeProperties(dynamicLoadbalanceEndpoint, endpointElement);

        serializeCommonAttributes(endpoint,endpointElement);
        

        Dispatcher dispatcher = dynamicLoadbalanceEndpoint.getDispatcher();
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

            long sessionTimeout = dynamicLoadbalanceEndpoint.getSessionTimeout();
            if (sessionTimeout != -1) {
                OMElement sessionTimeoutElement = fac.createOMElement("sessionTimeout",
                        SynapseConstants.SYNAPSE_OMNAMESPACE);
                sessionTimeoutElement.setText(String.valueOf(sessionTimeout));
                sessionElement.addChild(sessionTimeoutElement);
            }
            endpointElement.addChild(sessionElement);
        }

        OMElement dynamicLoadbalanceElement
                = fac.createOMElement("dynamicLoadbalance", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(dynamicLoadbalanceElement);

        // set if failover is turned off in the endpoint
        if (!dynamicLoadbalanceEndpoint.isFailover()) {
            dynamicLoadbalanceElement.addAttribute("failover", "false", null);
        }

        LoadBalanceMembershipHandler loadBalanceMembershipHandler = dynamicLoadbalanceEndpoint.getLbMembershipHandler();

        dynamicLoadbalanceElement.addAttribute(XMLConfigConstants.LOADBALANCE_ALGORITHM,
                loadBalanceMembershipHandler.getLoadbalanceAlgorithm().getClass().getName(),
                null);

        OMElement membershipHandlerElement =
                fac.createOMElement("membershipHandler", SynapseConstants.SYNAPSE_OMNAMESPACE);
        dynamicLoadbalanceElement.addChild(membershipHandlerElement);

        membershipHandlerElement.addAttribute("class",loadBalanceMembershipHandler.getClass().getName(),null);

        Properties membershipHandlerProperties = loadBalanceMembershipHandler.getProperties();
        OMElement propertyElement;
        for (Object property : membershipHandlerProperties.keySet()){
            propertyElement = fac.createOMElement("property", SynapseConstants.SYNAPSE_OMNAMESPACE);
            membershipHandlerElement.addChild(propertyElement);
            propertyElement.addAttribute("name", property.toString(), null);
            propertyElement.addAttribute("value", membershipHandlerProperties.getProperty((String)property), null);
        }

        return endpointElement;
    }
}
