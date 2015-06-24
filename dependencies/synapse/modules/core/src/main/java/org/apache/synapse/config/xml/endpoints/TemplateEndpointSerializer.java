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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.TemplateEndpoint;

import javax.xml.namespace.QName;
import java.util.Map;

public class TemplateEndpointSerializer extends EndpointSerializer {

    protected static OMNamespace nullNS;

    public TemplateEndpointSerializer() {
        fac = OMAbstractFactory.getOMFactory();
        nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    }

    public OMElement serializeEndpoint(Endpoint epr) {
        TemplateEndpoint endpoints = (TemplateEndpoint) epr;

        OMElement endpointElement = fac.createOMElement("endpoint",
                SynapseConstants.SYNAPSE_OMNAMESPACE);

        if (endpoints.getName() != null) {
            endpointElement.addAttribute(
                    fac.createOMAttribute("name", nullNS, endpoints.getName()));
        }

        endpointElement.addAttribute(fac.createOMAttribute("template", nullNS, endpoints.getTemplate()));

        Map<String, String> parameters = endpoints.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().equals("name")) {
                endpointElement.addAttribute(fac.createOMAttribute("name", nullNS, entry.getValue()));
            } else if (entry.getKey().equals("uri")) {
                endpointElement.addAttribute(fac.createOMAttribute("uri", nullNS, entry.getValue()));
            } else {
                OMElement paramElement = fac.createOMElement(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "parameter"));

                endpointElement.addChild(paramElement);
                paramElement.addAttribute(fac.createOMAttribute("name", nullNS, entry.getKey()));
                paramElement.addAttribute(fac.createOMAttribute("value", nullNS, entry.getValue()));
            }
        }

        return endpointElement;
    }
}
