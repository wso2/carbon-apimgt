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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.PropertyInclude;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.aspects.statistics.StatisticsConfigurable;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.MediatorPropertySerializer;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.endpoints.EndpointDefinition;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * All endpoint serializers should implement this interface. Use EndpointSerializer to
 * obtain the correct EndpointSerializer implementation for a particular endpoint.
 * EndpointSerializer implementation may call other EndpointSerializer implementations to serialize
 * nested endpoints.
 *
 * @see EndpointFactory
 */
public abstract class EndpointSerializer {

    private Log log;

    protected OMFactory fac;

    protected EndpointSerializer() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Core method which is exposed to the external use, and serializes the {@link Endpoint} to the
     * XML format
     *
     * @param endpoint to be serialized
     * @return XML format of the serialized endpoint
     */
    public static OMElement getElementFromEndpoint(Endpoint endpoint) {

        EndpointSerializer endpointSerializer = getEndpointSerializer(endpoint);
        OMElement elem = endpointSerializer.serializeEndpoint(endpoint);

        OMElement descriptionElem = endpointSerializer.getSerializedDescription(endpoint);
        if (descriptionElem != null) {
            elem.addChild(descriptionElem);
        }
        return elem;
    }

    /**
     * Serializes the given endpoint implementation to an XML object.
     *
     * @param endpoint Endpoint implementation to be serialized.
     * @return OMElement containing XML configuration.
     */
    protected abstract OMElement serializeEndpoint(Endpoint endpoint);

    private OMElement getSerializedDescription(Endpoint endpoint) {
        OMElement descriptionElem = fac.createOMElement("description", SynapseConstants.SYNAPSE_OMNAMESPACE);

        if (endpoint.getDescription() != null) {
            descriptionElem.setText(endpoint.getDescription());
            return descriptionElem;
        } else {
            return null;
        }
    }

    protected void serializeSpecificEndpointProperties(EndpointDefinition endpointDefinition,
        OMElement element) {

        // overridden by the Serializers which has specific serialization
    }


    protected void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }

    /**
     * Returns the EndpointSerializer implementation for the given endpoint. Throws a SynapseException,
     * if there is no serializer for the given endpoint type.
     *
     * @param endpoint Endpoint implementaion.
     * @return EndpointSerializer implementation.
     */
    public static EndpointSerializer getEndpointSerializer(Endpoint endpoint) {

        if (endpoint instanceof AddressEndpoint) {
            return new AddressEndpointSerializer();
        } else if (endpoint instanceof DefaultEndpoint) {
            return new DefaultEndpointSerializer();
        } else if (endpoint instanceof WSDLEndpoint) {
            return new WSDLEndpointSerializer();
        } else if (endpoint instanceof IndirectEndpoint) {
            return new IndirectEndpointSerializer();
        } else if (endpoint instanceof ResolvingEndpoint) {
            return new ResolvingEndpointSerializer();
        } else if (endpoint instanceof SALoadbalanceEndpoint) {
            return new SALoadbalanceEndpointSerializer();
        } else if (endpoint instanceof ServiceDynamicLoadbalanceEndpoint){
            return new ServiceDynamicLoadbalanceEndpointSerializer();
        } else if (endpoint instanceof DynamicLoadbalanceEndpoint){
            return new DynamicLoadbalanceEndpointSerializer();
        } else if (endpoint instanceof LoadbalanceEndpoint) {
            return new LoadbalanceEndpointSerializer();
        } else if (endpoint instanceof FailoverEndpoint) {
            return new FailoverEndpointSerializer();
        } else if (endpoint instanceof TemplateEndpoint) {
            return new TemplateEndpointSerializer();
        } else if(endpoint instanceof RecipientListEndpoint){
        	return new RecipientListEndpointSerializer();
        } else if (endpoint instanceof HTTPEndpoint) {
            return new HTTPEndpointSerializer();
        } else if (endpoint instanceof ClassEndpoint) {
            return new ClassEndpointSerializer();
        }

        throw new SynapseException("Serializer for endpoint " +
                endpoint.getClass().toString() + " is not defined.");
    }

    protected void serializeCommonAttributes(Endpoint endpoint, OMElement element) {

        String name = endpoint.getName();
        boolean anon = ((AbstractEndpoint) endpoint).isAnonymous();
        if (name != null && !anon) {
            element.addAttribute("name", name, null);
        }

        //serialize the message stores
        String messageStore = endpoint.getErrorHandler();
        if (messageStore != null) {
            element.addAttribute(EndpointFactory.ON_FAULT_Q.getLocalPart(),
                    messageStore, null);
        }
    }


    protected void serializeProperties(PropertyInclude endpoint, OMElement element) {
        Collection<MediatorProperty> properties = endpoint.getProperties();
        if (properties != null && properties.size() > 0) {
            MediatorPropertySerializer.serializeMediatorProperties(element, properties);
        }
    }
}
