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
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;

/**
 * Serializes an {@link WSDLEndpoint} to an XML configuration.
 * 
 * @see WSDLEndpointFactory
 */
public class WSDLEndpointSerializer extends EndpointSerializer {

    protected OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof WSDLEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);
        
        WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) endpoint;

        OMElement wsdlElement = fac.createOMElement("wsdl", SynapseConstants.SYNAPSE_OMNAMESPACE);
        String serviceName = wsdlEndpoint.getServiceName();
        if (serviceName != null) {
            wsdlElement.addAttribute("service", serviceName, null);
        }

        String portName = wsdlEndpoint.getPortName();
        if (portName != null) {
            wsdlElement.addAttribute("port", portName, null);
        }

        String uri = wsdlEndpoint.getWsdlURI();
        if (uri != null) {
            wsdlElement.addAttribute("uri", uri, null);
        }

        OMElement wsdlDoc = wsdlEndpoint.getWsdlDoc();
        if (wsdlDoc != null) {
            wsdlElement.addChild(wsdlDoc);
        }

        // currently, we have to get QOS information from the endpoint definition and set them as
        // special elements under the wsdl element. in future, these information should be
        // extracted from the wsdl.
        EndpointDefinition epDefinition = wsdlEndpoint.getDefinition();
        EndpointDefinitionSerializer serializer = new EndpointDefinitionSerializer();
        serializer.serializeEndpointDefinition(epDefinition, wsdlElement);
        serializeSpecificEndpointProperties(epDefinition, wsdlElement);
        endpointElement.addChild(wsdlElement);

         // serialize the parameters
        serializeProperties(wsdlEndpoint, endpointElement);

        serializeCommonAttributes(endpoint,endpointElement);

        return endpointElement;
    }

}
