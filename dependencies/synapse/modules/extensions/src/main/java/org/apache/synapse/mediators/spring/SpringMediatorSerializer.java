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

package org.apache.synapse.mediators.spring;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;

/**
 * <spring bean="exampleBean1" (config="spring1" | src="spring.xml)"/>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SpringMediatorSerializer extends AbstractMediatorSerializer {

    private static final OMNamespace sprNS =
            fac.createOMNamespace(XMLConfigConstants.SYNAPSE_NAMESPACE, "spring");

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof SpringMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        SpringMediator mediator = (SpringMediator) m;
        OMElement spring = fac.createOMElement("spring", sprNS);

        if (mediator.getBeanName() != null) {
            spring.addAttribute(fac.createOMAttribute(
                "bean", nullNS, mediator.getBeanName()));
        } else {
            handleException("Invalid mediator. Bean name required.");
        }
        saveTracingState(spring,mediator);

        if (mediator.getConfigKey() != null) {
            spring.addAttribute(fac.createOMAttribute(
                "key", nullNS, mediator.getConfigKey()));
        }

        // TODO add support for src attribute - or replace with a reg key!

        return spring;
    }

    public String getMediatorClassName() {
        return SpringMediator.class.getName();
    }
}
