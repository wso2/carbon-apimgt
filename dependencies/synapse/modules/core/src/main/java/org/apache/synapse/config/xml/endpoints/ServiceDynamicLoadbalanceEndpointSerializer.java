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
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.ServiceDynamicLoadbalanceEndpoint;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SimpleClientSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;

import java.util.Map;

/**
 * Creates {@link org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint} using an XML configuration.
 * <p/>
 * <pre>
 * <endpoint name="sdLB">
 * <serviceDynamicLoadbalance algorithm="org.apache.synapse.endpoints.algorithms.RoundRobin">
 *          <services>
 *              <service>
 *                  <host>as.cloud.wso2.com</host>
 *                  <domain>wso2as.domain</domain>
 *              </service>
 *              <service>
 *                  <host>esb.cloud.wso2.com</host>
 *                  <domain>wso2esb.domain</domain>
 *              </service>
 *              <service>
 *                  <host>governance.cloud.wso2.com</host>
 *                  <domain>wso2governance.domain</domain>
 *              </service>
 *                  <service>
 *                      <host>gs.cloud.wso2.com</host>
 *                      <domain>wso2gs.domain</domain>
 *              </service>
 *          </services>
 * </serviceDynamicLoadbalance>
 * </endpoint>
 * </pre>
 */

public class ServiceDynamicLoadbalanceEndpointSerializer extends EndpointSerializer {

    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof ServiceDynamicLoadbalanceEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
          = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);

        ServiceDynamicLoadbalanceEndpoint dynamicLoadbalanceEndpoint =
          (ServiceDynamicLoadbalanceEndpoint) endpoint;

        // serialize the parameters
        serializeProperties(dynamicLoadbalanceEndpoint, endpointElement);

        serializeCommonAttributes(endpoint, endpointElement);

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
          = fac.createOMElement("serviceDynamicLoadbalance", SynapseConstants.SYNAPSE_OMNAMESPACE);
        endpointElement.addChild(dynamicLoadbalanceElement);

        // Load balance algorithm
        dynamicLoadbalanceElement.addAttribute(XMLConfigConstants.LOADBALANCE_ALGORITHM,
                                               dynamicLoadbalanceEndpoint.getAlgorithm().getClass().getName(),
                                               null);

        // Host-domain map
        OMElement servicesEle
          = fac.createOMElement("services", SynapseConstants.SYNAPSE_OMNAMESPACE);
        dynamicLoadbalanceElement.addChild(servicesEle);

        Map<String, String> hostDomainMap = dynamicLoadbalanceEndpoint.getHostDomainMap();
        for (Map.Entry<String, String> entry : hostDomainMap.entrySet()) {
            OMElement serviceEle
              = fac.createOMElement("service", SynapseConstants.SYNAPSE_OMNAMESPACE);
            servicesEle.addChild(serviceEle);
            OMElement hostEle
              = fac.createOMElement("host", SynapseConstants.SYNAPSE_OMNAMESPACE);
            hostEle.setText(entry.getKey());
            serviceEle.addChild(hostEle);
            OMElement domainEle
              = fac.createOMElement("domain", SynapseConstants.SYNAPSE_OMNAMESPACE);
            domainEle.setText(entry.getValue());
            serviceEle.addChild(domainEle);
        }

        return endpointElement;
    }
}
