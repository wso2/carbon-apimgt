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

import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.SynapseException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMElement;

import java.util.Properties;

/**
 * This is a generic XMLToObjectMapper implementation for all endpoint types. Use this if the
 * endpoint type is not known at the time mapper is created. If the endpoint type is known use
 * the EndpointFactory implementation for that specific endpoint.
 *
 * @see XMLToObjectMapper
 */
public class XMLToEndpointMapper implements XMLToObjectMapper {

    private static XMLToEndpointMapper instance = new XMLToEndpointMapper();

    private XMLToEndpointMapper() {}

    public static XMLToEndpointMapper getInstance() {
        return instance;
    }

    /**
     * Constructs the Endpoint implementation for the given OMNode.
     *
     * @param om OMNode containing endpoint configuration. This should be an OMElement.
     * @return Endpoint implementation for the given OMNode.
     */
    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            OMElement epElement = (OMElement) om;
            return EndpointFactory.getEndpointFromElement(epElement, false, properties);
        } else {
            throw new SynapseException("Configuration is not in proper format.");
        }
    }
}
