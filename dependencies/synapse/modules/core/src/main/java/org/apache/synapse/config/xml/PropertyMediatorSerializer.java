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
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * <pre>
 * &lt;property name="string" [action=set/remove] (value="literal" | expression="xpath") [type="literal"]&gt;
 *     [Random XML]
 * &lt;/property&gt;
 * </pre>
 */
public class PropertyMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof PropertyMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        PropertyMediator mediator = (PropertyMediator) m;
        OMElement property = fac.createOMElement("property", synNS);
        saveTracingState(property, mediator);

        if (mediator.getName() != null) {
            property.addAttribute(fac.createOMAttribute(
                    "name", nullNS, mediator.getName()));
        } else {
            handleException("Invalid property mediator. Name is required");
        }

        if (mediator.getValue() != null) {
            property.addAttribute(fac.createOMAttribute(
                    "value", nullNS, mediator.getValue().toString()));
        } else if (mediator.getValueElement() != null) {
            property.addChild(mediator.getValueElement());
        } else if (mediator.getExpression() != null) {
            SynapsePathSerializer.serializePath((SynapsePath) mediator.getExpression(),
                    property, "expression");
        } else if (mediator.getAction() == PropertyMediator.ACTION_SET) {
            handleException("Invalid property mediator. Value or expression is required if " +
                    "action is SET");
        }

        if (mediator.getScope() != null) {
            // if we have already built a mediator with scope, scope should be valid, now save it
            property.addAttribute(fac.createOMAttribute("scope", nullNS, mediator.getScope()));
        }

        if (mediator.getAction() == PropertyMediator.ACTION_REMOVE) {
            property.addAttribute(fac.createOMAttribute(
                    "action", nullNS, "remove"));
        } else if (mediator.getType() != null) {
            property.addAttribute(fac.createOMAttribute(
                    "type" , nullNS, mediator.getType()));
        }

        if (mediator.getPattern() != null) {
            property.addAttribute(fac.createOMAttribute(
                    "pattern", nullNS, mediator.getPattern().toString()));
            if (mediator.getGroup() >= 0) {
                property.addAttribute(fac.createOMAttribute(
                        "group", nullNS, Integer.toString(mediator.getGroup())));
            }
        }

        return property;
    }

    public String getMediatorClassName() {
        return PropertyMediator.class.getName();
    }
}
