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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;

import javax.xml.namespace.QName;
import java.util.Properties;

public class XMLToTemplateMapper implements XMLToObjectMapper {
    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (!(om instanceof OMElement)) {
            throw new SynapseException("Configuration is not in proper format.");
        }

        OMElement elem = (OMElement) om;
        OMElement element = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
        if (element != null) {
            return MediatorFactoryFinder.getInstance().getMediator(elem, properties);
        }

        element = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
        if (element != null) {
            TemplateFactory templateFactory = new TemplateFactory();
            return templateFactory.createEndpointTemplate(elem, properties);
        }
        return null;
    }
}
