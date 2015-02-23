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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.MediatorPropertyFactory;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.PropertyInclude;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.endpoints.AbstractEndpoint;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * All endpoint factories should extend from this abstract class. Use EndpointFactory to obtain the
 * correct endpoint for particular endpoint configuration. As endpoints can be nested inside
 * each other, EndpointFactory implementations may call other EndpointFactory implementations
 * recursively to obtain the required endpoint hierarchy.
 * <p/>
 * This also serves as the {@link XMLToObjectMapper} implementation for specific endpoint
 * implementations. If the endpoint type is not known use {@link XMLToEndpointMapper} as the
 * generic {@link XMLToObjectMapper} for all endpoints.
 */
public abstract class EndpointFactory implements XMLToObjectMapper {

    static Log log;
    private DefinitionFactory customDefnFactory = null;

    protected EndpointFactory() {
        log = LogFactory.getLog(this.getClass());
    }

    private static final String ENDPOINT_NAME_PREFIX = "endpoint_";

    public static final QName ON_FAULT_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "onError");

    private static final QName DESCRIPTION_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description");

    /**
     * Core method which is exposed for the external use, and this will find the proper
     * {@link EndpointFactory} and create the endpoint which is of the format {@link Endpoint}.
     *
     * @param elem        XML from which the endpoint will be built
     * @param isAnonymous whether this is an anonymous endpoint or not
     * @param properties bag of properties to pass in any information to the factory
     * @return created endpoint
     */
    public static Endpoint getEndpointFromElement(OMElement elem, boolean isAnonymous,
                                                  Properties properties) {
        return getEndpointFactory(elem).createEndpointWithName(elem, isAnonymous, properties);
    }

    /**
     * Core method which is exposed for the external use, and this will find the proper
     * {@link EndpointFactory} and create the endpoint which is of the format {@link Endpoint}.However
     * definition for this endpoint will be built using a custom Endpoint Defn factory.
     *
     * @param elem        XML from which the endpoint will be built
     * @param factory    custom definition factory which this endpoint will be used to build
     * @param isAnonymous whether this is an anonymous endpoint or not
     * @param properties bag of properties to pass in any information to the factory
     * @return created endpoint
     */
    public static Endpoint getEndpointFromElement(OMElement elem,DefinitionFactory factory,
                                                  boolean isAnonymous,
                                                  Properties properties) {
        EndpointFactory fac = getEndpointFactory(elem);
        fac.setEndpointDefinitionFactory(factory);
        return fac.createEndpointWithName(elem, isAnonymous, properties);
    }

    /**
     * Creates the {@link Endpoint} object from the provided {@link OMNode}
     *
     * @param om XML node from which the endpoint will be built
     * @param properties bag of properties to pass in any information to the factory
     * @return created endpoint as an {@link Object}
     */
    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            return createEndpointWithName((OMElement) om, false, properties);
        } else {
            handleException("Invalid XML configuration for an Endpoint. OMElement expected");
        }
        return null;
    }

    /**
     * Creates the Endpoint implementation for the given XML endpoint configuration. If the endpoint
     * configuration is an inline one, it should be an anonymous endpoint. If it is defined as an
     * immediate child element of the definitions tag it should have a name, which is used as the
     * key in local registry.
     *
     * @param epConfig          OMElement containing the endpoint configuration.
     * @param anonymousEndpoint false if the endpoint has a name. true otherwise.
     * @param properties bag of properties to pass in any information to the factory
     * @return Endpoint implementation for the given configuration.
     */
    protected abstract Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                               Properties properties);

    /**
     *  Make sure that the endpoints created by the factory has a name
     * 
     * @param epConfig          OMElement containing the endpoint configuration.
     * @param anonymousEndpoint false if the endpoint has a name. true otherwise.
     * @param properties bag of properties to pass in any information to the factory
     * @return Endpoint implementation for the given configuration.
     */
    private Endpoint createEndpointWithName(OMElement epConfig, boolean anonymousEndpoint,
                                            Properties properties) {
        
        Endpoint ep = createEndpoint(epConfig, anonymousEndpoint, properties);
        OMElement descriptionElem = epConfig.getFirstChildWithName(DESCRIPTION_Q);
        if (descriptionElem != null) {
            ep.setDescription(descriptionElem.getText());
        }

        // if the endpoint doesn't have a name we will generate a unique name.
        if (anonymousEndpoint && ep.getName() == null) {
//            String uuid = UIDGenerator.generateUID();
//            uuid = uuid.replace(':', '_');
//            ep.setName(ENDPOINT_NAME_PREFIX + uuid);
            if (ep instanceof AbstractEndpoint) {
                ((AbstractEndpoint) ep).setAnonymous(true);
            }
        }

        OMAttribute onFaultAtt = epConfig.getAttribute(ON_FAULT_Q);
        if (onFaultAtt != null) {
            ep.setErrorHandler(onFaultAtt.getAttributeValue());
        }
        return ep;
    }

    protected void extractSpecificEndpointProperties(EndpointDefinition definition,
        OMElement elem) {

        // overridden by the Factories which has specific building
    }

    /**
     * Returns the EndpointFactory implementation for given endpoint configuration. Throws a
     * SynapseException, if there is no EndpointFactory for given configuration.
     *
     * @param configElement Endpoint configuration.
     * @return EndpointFactory implementation.
     */
    private static EndpointFactory getEndpointFactory(OMElement configElement) {

        if (configElement.getAttribute(new QName("key")) != null) {
            return IndirectEndpointFactory.getInstance();
        }

        if (configElement.getAttribute(new QName("key-expression")) != null) {
            return ResolvingEndpointFactory.getInstance();
        }

        if (configElement.getAttribute(new QName("template")) != null) {
            return new TemplateEndpointFactory();
        }

        OMElement addressElement = configElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "address"));
        if (addressElement != null) {
            return AddressEndpointFactory.getInstance();
        }

        OMElement wsdlElement = configElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "wsdl"));
        if (wsdlElement != null) {
            return WSDLEndpointFactory.getInstance();
        }

        OMElement defaultElement = configElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "default"));
        if (defaultElement != null) {
            return DefaultEndpointFactory.getInstance();
        }

        OMElement lbElement = configElement.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "loadbalance"));
        if (lbElement != null) {
            OMElement sessionElement = configElement.
                    getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "session"));
            if (sessionElement != null) {
                return SALoadbalanceEndpointFactory.getInstance();
            } else {
                return LoadbalanceEndpointFactory.getInstance();
            }
        }

        OMElement dlbElement = configElement.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "dynamicLoadbalance"));
        if (dlbElement != null) {
            return DynamicLoadbalanceEndpointFactory.getInstance();
        }

        OMElement sdlbElement = configElement.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serviceDynamicLoadbalance"));
        if (sdlbElement != null) {
            return ServiceDynamicLoadbalanceEndpointFactory.getInstance();
        }

        OMElement foElement = configElement.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "failover"));
        if (foElement != null) {
            return FailoverEndpointFactory.getInstance();
        }
        
        OMElement rcplElement = configElement.getFirstChildWithName
        		(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "recipientlist"));
        if(rcplElement != null){
        	return RecipientListEndpointFactory.getInstance();
        }

        OMElement httpElement = configElement.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "http"));
        if (httpElement != null) {
            return HTTPEndpointFactory.getInstance();
        }

		OMElement classElement = configElement.getFirstChildWithName
				(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "class"));
		if (classElement != null) {
			return ClassEndpointFactory.getInstance();
		}
        
        
        handleException("Invalid endpoint configuration.");
        // just to make the compiler happy : never executes
        return null;
    }

    /**
     * Helper method to construct children endpoints
     *
     * @param listEndpointElement OMElement representing  the children endpoints
     * @param parent Parent endpoint
     * @param properties bag of properties to pass in any information to the factory
     * @return List of children endpoints
     */
    protected ArrayList<Endpoint> getEndpoints(OMElement listEndpointElement, Endpoint parent,
                                               Properties properties) {

        ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();
        ArrayList<String> keys = new ArrayList<String>();
        Iterator iter = listEndpointElement.getChildrenWithName(XMLConfigConstants.ENDPOINT_ELT);
        while (iter.hasNext()) {
            OMElement endptElem = (OMElement) iter.next();
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(endptElem, true, properties);
            if (endpoint instanceof IndirectEndpoint) {
                String key = ((IndirectEndpoint) endpoint).getKey();
                if (!keys.contains(key)) {
                    keys.add(key);
                } else {
                    handleException("Same endpoint definition cannot be used with in the siblings");
                }
            }
            endpoint.setParentEndpoint(parent);
            endpoints.add(endpoint);
        }

        return endpoints;
    }

    /**
     * provide a custom Endpoint definition factory
     * @param factory
     */
    public void setEndpointDefinitionFactory(DefinitionFactory factory){
        customDefnFactory = factory;
    }

    /**
     * return current factory for building this endpoint definition
     * @return
     */
    public DefinitionFactory getEndpointDefinitionFactory(){
        return customDefnFactory;
    }

    /**
     * Helper method to extract endpoint properties.
     *
     * @param endpoint actual endpoint to set the properties
     * @param endpointElement actual endpoint element
     */
    protected void processProperties(PropertyInclude endpoint, OMElement endpointElement) {
        List<MediatorProperty> properties =
                MediatorPropertyFactory.getMediatorProperties(endpointElement);

        if (properties != null && properties.size() > 0) {
            endpoint.addProperties(properties);
        }
    }

    protected static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    protected static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
