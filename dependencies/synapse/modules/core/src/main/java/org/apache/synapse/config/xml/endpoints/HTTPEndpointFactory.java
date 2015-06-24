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
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.HTTPEndpoint;
import com.damnhandy.uri.template.UriTemplate;

import javax.xml.namespace.QName;
import java.util.Properties;

public class HTTPEndpointFactory extends DefaultEndpointFactory {

    private static HTTPEndpointFactory instance = new HTTPEndpointFactory();

    public static HTTPEndpointFactory getInstance() {
        return instance;
    }

    @Override
    public EndpointDefinition createEndpointDefinition(OMElement elem) {
            DefinitionFactory fac = getEndpointDefinitionFactory();
        EndpointDefinition endpointDefinition;
        if (fac == null) {
            fac = new EndpointDefinitionFactory();
            endpointDefinition = fac.createDefinition(elem);
        } else{
            endpointDefinition = fac.createDefinition(elem);
        }
        extractSpecificEndpointProperties(endpointDefinition, elem);
        return endpointDefinition;
    }

    @Override
    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint, Properties properties) {
        HTTPEndpoint httpEndpoint = new HTTPEndpoint();
        OMAttribute name = epConfig.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            httpEndpoint.setName(name.getAttributeValue());
        }

        OMElement httpElement = epConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "http"));

        if (httpElement != null) {
            EndpointDefinition definition = createEndpointDefinition(httpElement);


            OMAttribute uriTemplateAttr = httpElement.getAttribute(new QName("uri-template"));
            if (uriTemplateAttr != null) {


                    httpEndpoint.setUriTemplate(UriTemplate.fromTemplate(uriTemplateAttr.getAttributeValue()));
                    // Set the address to the initial template value.
                    definition.setAddress(uriTemplateAttr.getAttributeValue());


            }


            httpEndpoint.setDefinition(definition);
            processAuditStatus(definition, httpEndpoint.getName(), httpElement);

            OMAttribute methodAttr = httpElement.getAttribute(new QName("method"));
            if (methodAttr != null) {
                httpEndpoint.setHttpMethod(methodAttr.getAttributeValue());
            }

        }

        processProperties(httpEndpoint, epConfig);

        return httpEndpoint;
    }
}
