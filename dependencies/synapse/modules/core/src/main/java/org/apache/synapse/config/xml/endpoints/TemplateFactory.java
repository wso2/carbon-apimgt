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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Template;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class TemplateFactory {
    public static final Log log = LogFactory.getLog(TemplateFactory.class);

    public Template createEndpointTemplate(OMElement element, Properties properties) {
        Template template = new Template();

        OMAttribute nameAttribute = element.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));


        if (nameAttribute != null) {
            template.setName(nameAttribute.getAttributeValue());
        } else {
            handleException("Error loading the configuration from endpointTemplate, '" +
                    "name' attribute missing");
        }

        Iterator paramItr = element.getChildrenWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "parameter"));
        while (paramItr.hasNext()) {
            OMElement paramElement = (OMElement) paramItr.next();

            OMAttribute paramName = paramElement.getAttribute(new QName("name"));

            if (paramName == null) {
                handleException("parameter name should be present");
            }

            assert paramName != null;

            template.addParameter(paramName.getAttributeValue().trim());
        }

        if (!template.getParameters().contains("name")) {
            template.addParameter("name");
        }
        if (!template.getParameters().contains("uri")) {
            template.addParameter("uri");
        }

        OMElement endpointElement = element.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
        if (endpointElement == null) {
            handleException("endpoint element is required in an endpoint template");
        }
        template.setElement(endpointElement);

        return template;
    }

    protected void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }
}
