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
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Creates {@link AddressEndpoint} using a XML configuration.
 * <p/>
 * Configuration syntax:
 * <pre>
 * &lt;endpoint [name="<em>name</em>"]&gt;
 *   &lt;address uri="<em>endpoint address</em>" [format="soap11|soap12|pox|get"]
 *            [optimize="mtom|swa"]
 *            [encoding="<em>charset encoding</em>"]
 *            [statistics="enable|disable"] [trace="enable|disable"]&gt;
 *     .. extensibility ..
 *
 *     &lt;enableRM [policy="<em>key</em>"]/&gt;?
 *     &lt;enableSec [policy="<em>key</em>"]/&gt;?
 *     &lt;enableAddressing [version="final|submission"] [separateListener="true|false"]/&gt;?
 *
 *     &lt;timeout&gt;
 *       &lt;duration&gt;<em>timeout duration in seconds</em>&lt;/duration&gt;
 *       &lt;responseAction&gt;discard|fault&lt;/responseAction&gt;
 *     &lt;/timeout&gt;?
 *
 *     &lt;suspendDurationOnFailure&gt;
 *       <em>suspend duration in seconds</em>
 *     &lt;/suspendDurationOnFailure&gt;?
 *   &lt;/address&gt;
 * &lt;/endpoint&gt;
 * </pre>
 */
public class AddressEndpointFactory extends DefaultEndpointFactory {

    private static AddressEndpointFactory instance = new AddressEndpointFactory();

    private AddressEndpointFactory() {
    }

    public static AddressEndpointFactory getInstance() {
        return instance;
    }

    @Override
    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        AddressEndpoint addressEndpoint = new AddressEndpoint();
        OMAttribute name = epConfig.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            addressEndpoint.setName(name.getAttributeValue());
        }

        OMElement addressElement = epConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "address"));
        if (addressElement != null) {
            EndpointDefinition definition = createEndpointDefinition(addressElement);
            addressEndpoint.setDefinition(definition);
            processAuditStatus(definition, addressEndpoint.getName(), addressElement);
        }

        processProperties(addressEndpoint, epConfig);

        return addressEndpoint;
    }

    /**
     * Creates an EndpointDefinition instance using the XML fragment specification. Configuration
     * for EndpointDefinition always resides inside a configuration of an AddressEndpoint. This
     * factory extracts the details related to the EPR provided for address endpoint.
     *
     * @param elem XML configuration element
     * @return EndpointDefinition object containing the endpoint details.
     */
    @Override
    public EndpointDefinition createEndpointDefinition(OMElement elem) {

        OMAttribute address = elem.getAttribute(new QName("uri"));
        DefinitionFactory fac = getEndpointDefinitionFactory();
        EndpointDefinition endpointDefinition;
        if (fac == null) {
            fac = new EndpointDefinitionFactory();
            endpointDefinition = fac.createDefinition(elem);
        } else{
            endpointDefinition = fac.createDefinition(elem);
        }

        if (address != null) {
            endpointDefinition.setAddress(address.getAttributeValue().trim());
        }

        extractSpecificEndpointProperties(endpointDefinition, elem);
        return endpointDefinition;
    }
}
