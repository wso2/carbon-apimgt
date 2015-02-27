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

import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.ResolvingEndpoint;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.axiom.om.OMElement;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * 
 */
public class ResolvingEndpointFactory extends EndpointFactory {

    private static ResolvingEndpointFactory instance = new ResolvingEndpointFactory();

    private static final QName ATTR_KEY_EXPRESSION = new QName("key-expression");

    private ResolvingEndpointFactory() {}

    public static ResolvingEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        ResolvingEndpoint resolvingEndpoint = new ResolvingEndpoint();       
        String name = epConfig.getAttributeValue(new QName("name"));
        if (name != null) {
            resolvingEndpoint.setName(name);
        }
        try {
            resolvingEndpoint.setKeyExpression(
                    SynapseXPathFactory.getSynapseXPath(epConfig, ATTR_KEY_EXPRESSION));
        } catch (JaxenException e) {
            handleException("Couldn't build the ResolvingEndpoint, unable to set " +
                    "the key-expression XPath", e);
        }

        // process the parameters
        processProperties(resolvingEndpoint, epConfig);

        return resolvingEndpoint;
    }
}
