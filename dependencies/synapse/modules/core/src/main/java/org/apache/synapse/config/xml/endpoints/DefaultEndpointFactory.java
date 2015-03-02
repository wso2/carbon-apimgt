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
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.DefaultEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Creates {@link DefaultEndpoint} using a XML configuration.
 * <p/>
 * Configuration syntax:
 * <pre>
 * &lt;endpoint [name="<em>name</em>"]&gt;
 *   &lt;default [format="soap11|soap12|pox|get"] [optimize="mtom|swa"]
 *      [encoding="<em>charset encoding</em>"]
 *          [statistics="enable|disable"] [trace="enable|disable"]&gt;
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
public class DefaultEndpointFactory extends EndpointFactory {

    private static DefaultEndpointFactory instance = new DefaultEndpointFactory();

    protected DefaultEndpointFactory() {
    }

    public static DefaultEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        DefaultEndpoint defaultEndpoint = new DefaultEndpoint();
        OMAttribute name = epConfig.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            defaultEndpoint.setName(name.getAttributeValue());
        }

        OMElement defaultElement = epConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "default"));
        if (defaultElement != null) {
            EndpointDefinition endpoint = createEndpointDefinition(defaultElement);
            defaultEndpoint.setDefinition(endpoint);
            processAuditStatus(endpoint, defaultEndpoint.getName(),defaultElement);
        }

        processProperties(defaultEndpoint, epConfig);
        
        return defaultEndpoint;
    }

    @Override
    protected void extractSpecificEndpointProperties(EndpointDefinition definition,
        OMElement elem) {

        OMAttribute format
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "format"));
        if (format != null) {
            String forceValue = format.getAttributeValue().trim().toLowerCase();
            if (SynapseConstants.FORMAT_POX.equals(forceValue)) {
                definition.setForcePOX(true);
                definition.setFormat(SynapseConstants.FORMAT_POX);

            } else if (SynapseConstants.FORMAT_GET.equals(forceValue)) {
                definition.setForceGET(true);
                definition.setFormat(SynapseConstants.FORMAT_GET);

            } else if (SynapseConstants.FORMAT_SOAP11.equals(forceValue)) {
                definition.setForceSOAP11(true);
                definition.setFormat(SynapseConstants.FORMAT_SOAP11);

            } else if (SynapseConstants.FORMAT_SOAP12.equals(forceValue)) {
                definition.setForceSOAP12(true);
                definition.setFormat(SynapseConstants.FORMAT_SOAP12);

            } else if (SynapseConstants.FORMAT_REST.equals(forceValue)) {
                definition.setForceREST(true);
                definition.setFormat(SynapseConstants.FORMAT_REST);

            } /*else if(!TemplateMappingsPopulator.populateMapping(definition, EndpointDefinition.EndpointDefKey.format, forceValue)) {
                handleException("force value -\"" + forceValue + "\" not yet implemented");
            }*/
        }

    }

    /**
     * Creates an EndpointDefinition instance using the XML fragment specification. Configuration
     * for EndpointDefinition always resides inside a configuration of an AddressEndpoint. This
     * factory extracts the details related to the EPR provided for address endpoint.
     *
     * @param elem XML configuration element
     * @return EndpointDefinition object containing the endpoint details.
     */
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
    
    protected void processAuditStatus(EndpointDefinition definition ,String name , OMElement epOmElement){

        if (name == null || "".equals(name)) {
            name = SynapseConstants.ANONYMOUS_ENDPOINT;
        }
        AspectConfiguration aspectConfiguration = new AspectConfiguration(name);
        definition.configure(aspectConfiguration);
        OMAttribute statistics = epOmElement.getAttribute(
                new QName(XMLConfigConstants.STATISTICS_ATTRIB_NAME));
        if (statistics != null) {
            String statisticsValue = statistics.getAttributeValue();
            if (statisticsValue != null) {
                if (XMLConfigConstants.STATISTICS_ENABLE.equals(statisticsValue)) {
                    aspectConfiguration.enableStatistics();
                }
            }
        }
    }   
}
