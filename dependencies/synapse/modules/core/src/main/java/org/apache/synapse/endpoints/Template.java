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

package org.apache.synapse.endpoints;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A template with the endpoint information.
 */
public class Template implements SynapseArtifact {

    private static Log log = LogFactory.getLog(Template.class);

    private OMElement element = null;

    private String name = null;

    private List<String> parameters = new ArrayList<String>();

    private String fileName = null;

    private String description = null;

    public Endpoint create(TemplateEndpoint templateEndpoint, Properties properties) {
        // first go through all the elements and replace with the parameters
        OMElement clonedElement = element.cloneOMElement();
        replaceElement(templateEndpoint, clonedElement);

        return EndpointFactory.getEndpointFromElement(clonedElement, false, properties);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void addParameter(String name) {
        parameters.add(name);
    }

    public void setElement(OMElement element) {
        this.element = element;
    }

    public OMElement getElement() {
        return element;
    }

    private void replaceElement(TemplateEndpoint templateEndpoint, OMElement element) {
        Iterator attributesItr = element.getAllAttributes();
        while (attributesItr.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributesItr.next();

            String replace = replace(attribute.getAttributeValue(), templateEndpoint);
            if (replace != null && replace.contains("{system.prop")) {
                String newReplace = new String(replace);
                Pattern pattern = Pattern.compile("\\{(.*?)\\}");
                Matcher matcher = pattern.matcher(replace);
                while (matcher.find()){
                    String propStr = matcher.group(0);
                    String propName = propStr.substring(propStr.indexOf("{system.prop") + 13,propStr.length()-1);
                    String propVal = System.getProperty(propName);
                    if (propVal != null) {
                        newReplace = newReplace.replace(propStr, propVal);
                    } else {
                        log.warn("System property " + propName + " not defined in JVM");
                    }
                }
                log.info("Calculated replacement with system property "+ replace);
                replace = newReplace;
            }

            if (replace != null) {
                attribute.setAttributeValue(replace);
            }
        }

        if (element.getText() != null && !"".equals(element.getText())) {
            String replace = replace(element.getText(), templateEndpoint);

            if (replace != null) {
                element.setText(replace);
            }
        }

        Iterator elemItr = element.getChildElements();
        while (elemItr.hasNext()) {
            OMElement childElement = (OMElement) elemItr.next();

            replaceElement(templateEndpoint, childElement);
        }
    }

    private String replace(String value, TemplateEndpoint templateEndpoint) {
        if (value.startsWith("$")) {
            String param = value.substring(1);

            if (templateEndpoint.getParameters().containsKey(param) &&
                    parameters.contains(param)) {
                return templateEndpoint.getParameterValue(param);
            }
        }

        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
