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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.ext.AnnotatedCommandMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * Creates an instance of a AnnotatedCommand mediator using XML configuration specified
 * <p/>
 * <pre>
 * &lt;annotatedCommand name=&quot;class-name&quot;&gt;
 *   &lt;property name=&quot;string&quot; value=&quot;literal&quot;&gt;
 *      either literal or XML child
 *   &lt;/property&gt;
 *   &lt;property name=&quot;string&quot; expression=&quot;XPATH expression&quot;/&gt;
 * &lt;/annoatedCommand&gt;
 * </pre>
 */
public class AnnotatedCommandMediatorFactory extends AbstractMediatorFactory {

    private static final QName ANNOTATED_COMMAND_Q =
        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "annotatedCommand");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        AnnotatedCommandMediator pojoMediator = new AnnotatedCommandMediator();

        // Class name of the Command object should be present
        OMAttribute name = elem.getAttribute(ATT_NAME);
        if (name == null) {
            String msg = "The name of the actual POJO command implementation class" +
                    " is a required attribute";
            log.error(msg);
            throw new SynapseException(msg);
        }

        // load the class for the command object
        try {
            pojoMediator.setCommand(
                    getClass().getClassLoader().loadClass(name.getAttributeValue()));
        } catch (ClassNotFoundException e) {
            handleException("Unable to load the class specified as the command "
                    + name.getAttributeValue(), e);
        }

        // setting the properties to the command. these properties will be instantiated
        // at the mediation time
        for (Iterator it = elem.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if("property".equals(child.getLocalName())) {

                String propName = child.getAttribute(ATT_NAME).getAttributeValue();
                if (propName == null) {
                    handleException(
                        "A POJO command mediator property must specify the name attribute");
                } else {
                    if (child.getAttribute(ATT_EXPRN) != null) {
                        SynapseXPath xpath;
                        try {
                            xpath = SynapseXPathFactory.getSynapseXPath(child, ATT_EXPRN);
                            pojoMediator.addMessageSetterProperty(propName, xpath);
                        } catch (JaxenException e) {
                            handleException("Error instantiating XPath expression : " +
                                child.getAttribute(ATT_EXPRN), e);
                        }
                    } else {
                        if (child.getAttribute(ATT_VALUE) != null) {
                            pojoMediator.addStaticSetterProperty(propName,
                                child.getAttribute(ATT_VALUE).getAttributeValue());
                        } else {
                            handleException("A POJO mediator property must specify either " +
                                "name and expression attributes, or name and value attributes");
                        }
                    }
                }
            }
        }

        return pojoMediator;
    }

    public QName getTagQName() {
        return ANNOTATED_COMMAND_Q;
    }

}

