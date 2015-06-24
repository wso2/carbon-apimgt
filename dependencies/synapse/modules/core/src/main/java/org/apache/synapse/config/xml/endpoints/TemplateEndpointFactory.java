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
import org.apache.synapse.endpoints.TemplateEndpoint;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class TemplateEndpointFactory extends EndpointFactory {
    public Endpoint createEndpoint(OMElement endpointElement, boolean a, Properties properties) {
        TemplateEndpoint templateEndpoint = new TemplateEndpoint();

        OMAttribute endpointNameAttribute = endpointElement.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        if (endpointNameAttribute != null) {
            templateEndpoint.addParameter("name", endpointNameAttribute.getAttributeValue());
            templateEndpoint.setName(endpointNameAttribute.getAttributeValue());
        } /*else {
            handleException("Error loading the configuration from Template " +
                    "Endpoint, name attribute is missing");
        }*/

        OMAttribute endpointURIAttribute = endpointElement.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "uri"));
        if (endpointURIAttribute != null) {
            templateEndpoint.addParameter("uri", endpointURIAttribute.getAttributeValue());
        } /* else {
            handleException("Error loading the configuration from Template Endpoint, " +
                    templateEndpoint.getName() + " uri attribute is missing");
        }*/

        OMAttribute endpointTemplateAttribute = endpointElement.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "template"));
        if (endpointTemplateAttribute != null) {
            templateEndpoint.setTemplate(endpointTemplateAttribute.getAttributeValue());
        } else {
            handleException("Error loading the configuration from endpoint group, " +
                    templateEndpoint.getName() +
                    " template attribute is missing");
        }

        Iterator paramItr = endpointElement.getChildrenWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "parameter"));
        while (paramItr.hasNext()) {
            OMElement paramElement = (OMElement) paramItr.next();

            OMAttribute paramName = paramElement.getAttribute(new QName("name"));
            OMAttribute paramValue = paramElement.getAttribute(new QName("value"));

            if (paramName == null) {
                handleException("parameter name should be present");
            }

            if (paramValue == null) {
                handleException("parameter value should be present");
            }


            assert paramName != null;
            assert paramValue != null;

            templateEndpoint.addParameter(paramName.getAttributeValue(),
                    paramValue.getAttributeValue());
        }

        return templateEndpoint;
    }
}
