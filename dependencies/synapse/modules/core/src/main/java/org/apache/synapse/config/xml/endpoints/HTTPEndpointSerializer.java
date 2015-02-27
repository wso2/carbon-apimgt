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
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.HTTPEndpoint;

public class HTTPEndpointSerializer extends DefaultEndpointSerializer {
    @Override
    protected OMElement serializeEndpoint(Endpoint endpoint) {
        if (!(endpoint instanceof HTTPEndpoint)) {
            handleException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement
                = fac.createOMElement("endpoint", SynapseConstants.SYNAPSE_OMNAMESPACE);
        HTTPEndpoint httpEndpoint = (HTTPEndpoint) endpoint;
        EndpointDefinition epDefinitionHttp = httpEndpoint.getDefinition();
        OMElement httpEPElement = serializeEndpointDefinition(epDefinitionHttp);

        if (httpEndpoint.getHttpMethod() != null) {
            httpEPElement.addAttribute(
                    fac.createOMAttribute("method", null, httpEndpoint.getHttpMethod()));
        }

        if (httpEndpoint.getUriTemplate() != null) {
        	httpEPElement.addAttribute(
                    fac.createOMAttribute("uri-template", null, httpEndpoint.getUriTemplate().getTemplate()));
        }

        endpointElement.addChild(httpEPElement);

        // serialize the properties
        serializeProperties(httpEndpoint, endpointElement);

        serializeCommonAttributes(endpoint, endpointElement);

        return endpointElement;
    }

    @Override
    public OMElement serializeEndpointDefinition(EndpointDefinition endpointDefinition) {

        OMElement element = fac.createOMElement("http", SynapseConstants.SYNAPSE_OMNAMESPACE);
        EndpointDefinitionSerializer serializer = new EndpointDefinitionSerializer();
        serializer.serializeEndpointDefinition(endpointDefinition, element);

        serializeSpecificEndpointProperties(endpointDefinition, element);
        return element;
    }
}
