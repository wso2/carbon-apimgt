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
import org.apache.synapse.endpoints.Template;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public class TemplateSerializer {
    protected static OMFactory fac = OMAbstractFactory.getOMFactory();

    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    public OMElement serializeEndpointTemplate(Template template, OMElement parent) {
        OMElement templateElement =
                fac.createOMElement("template", SynapseConstants.SYNAPSE_OMNAMESPACE);

        templateElement.addAttribute(fac.createOMAttribute("name", nullNS, template.getName()));

        List<String> parameters = template.getParameters();
        for (String entry : parameters) {
            OMElement paramElement = fac.createOMElement(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "parameter"));

            paramElement.addAttribute(fac.createOMAttribute("name", nullNS, entry));
            templateElement.addChild(paramElement);
        }

        templateElement.addChild(template.getElement().cloneOMElement());

        if (parent != null) {
            parent.addChild(templateElement);
        }

        return templateElement;
    }
}
