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
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.ext.ClassMediator;

import java.util.Iterator;

/**
 * <pre>
 * &lt;class name="class-name"&gt;
 *   &lt;property name="string" (value="literal" | expression="xpath")/&gt;*
 * &lt;/class&gt;
 * </pre>
 */
public class ClassMediatorSerializer extends AbstractMediatorSerializer  {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof ClassMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        ClassMediator mediator = (ClassMediator) m;
        OMElement clazz = fac.createOMElement("class", synNS);
        saveTracingState(clazz, mediator);

        if (mediator.getMediator() != null && mediator.getMediator().getClass().getName() != null) {
            clazz.addAttribute(fac.createOMAttribute(
                "name", nullNS, mediator.getMediator().getClass().getName()));
        } else {
            handleException("Invalid class mediator. The class name is required");
        }

        Iterator itr = mediator.getProperties().keySet().iterator();
        while(itr.hasNext()) {
            String propName = (String) itr.next();
            Object o = mediator.getProperties().get(propName);
            OMElement prop = fac.createOMElement(PROP_Q, clazz);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));

            if (o instanceof String) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, (String) o));
            } else {
                prop.addChild((OMNode) o);
            }
            clazz.addChild(prop);
        }

        return clazz;
    }

    public String getMediatorClassName() {
        return ClassMediator.class.getName();
    }
}
