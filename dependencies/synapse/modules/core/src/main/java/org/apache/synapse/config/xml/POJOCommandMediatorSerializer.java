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
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.ext.POJOCommandMediator;

/**
 * Serializer for {@link POJOCommandMediator} instances.
 * 
 * @see POJOCommandMediatorFactory
 */
public class POJOCommandMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {
        
        if (!(m instanceof POJOCommandMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        
        POJOCommandMediator mediator = (POJOCommandMediator) m;
        
        OMElement pojoCommand = fac.createOMElement("pojoCommand", synNS);
        saveTracingState(pojoCommand, mediator);

        if (mediator.getCommand() != null && mediator.getCommand().getClass().getName() != null) {
            pojoCommand.addAttribute(fac.createOMAttribute(
                "name", nullNS, mediator.getCommand().getName()));
        } else {
            handleException("Invalid POJO Command mediator. The command class name is required");
        }

        for (String propName : mediator.getStaticSetterProperties().keySet()) {
            Object value = mediator.getStaticSetterProperties().get(propName);
            OMElement prop = fac.createOMElement(PROP_Q);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));

            if (value instanceof String) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, (String) value));
            } else if (value instanceof OMElement) {
                prop.addChild((OMElement) value);
            } else {
                handleException("Unable to serialize the command " +
                    "mediator property with the naem " + propName + " : Unknown type");
            }

            if (mediator.getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                    mediator.getContextGetterProperties().get(propName)));
            } else if (mediator.getMessageGetterProperties().containsKey(propName)) {
                SynapseXPathSerializer.serializeXPath(
                    mediator.getMessageGetterProperties().get(propName), prop, "expression");
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : mediator.getMessageSetterProperties().keySet()) {
            OMElement prop = fac.createOMElement(PROP_Q);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
            SynapseXPathSerializer.serializeXPath(
                mediator.getMessageSetterProperties().get(propName), prop, "expression");

            if (mediator.getMessageGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadAndUpdateMessage"));
            } else if (mediator.getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                    mediator.getContextGetterProperties().get(propName)));
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadMessage"));                
            } else {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadMessage"));                                
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : mediator.getContextSetterProperties().keySet()) {
            OMElement prop = fac.createOMElement(PROP_Q);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
            prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                mediator.getContextSetterProperties().get(propName)));

            if (mediator.getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadAndUpdateContext"));
            } else if (mediator.getMessageGetterProperties().containsKey(propName)) {
                SynapseXPathSerializer.serializeXPath(
                    mediator.getMessageGetterProperties().get(propName), prop, "expression");
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadContext"));
            } else {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadContext"));                
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : mediator.getContextGetterProperties().keySet()) {
            if (!isSerialized(propName, mediator)) {
                String value = mediator.getContextGetterProperties().get(propName);
                OMElement prop = fac.createOMElement(PROP_Q);
                prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS, value));
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "UpdateContext"));
                pojoCommand.addChild(prop);
            }
        }

        for (String propName : mediator.getMessageGetterProperties().keySet()) {
            if (!isSerialized(propName, mediator)) {
                OMElement prop = fac.createOMElement(PROP_Q);
                prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
                SynapseXPathSerializer.serializeXPath(
                    mediator.getMessageGetterProperties().get(propName), prop, "expression");
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "UpdateMessage"));
                pojoCommand.addChild(prop);
            }
        }

        return pojoCommand;
    }

    private boolean isSerialized(String propName, POJOCommandMediator m) {
        return m.getContextSetterProperties().containsKey(propName) ||
            m.getStaticSetterProperties().containsKey(propName) ||
            m.getMessageSetterProperties().containsKey(propName);
    }

    public String getMediatorClassName() {
        return POJOCommandMediator.class.getName();
    }
}
