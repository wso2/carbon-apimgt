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
import org.apache.synapse.mediators.template.TemplateMediator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *  Serializer class for Template to serialize into a  configuration as follows
 * <template name="simple_func">
	    <parameter name="p1"/>
        <parameter name="p2"/>*
        <mediator/>+
    </template>
 */
public class TemplateMediatorSerializer extends AbstractListMediatorSerializer {
    @Override
    protected OMElement serializeSpecificMediator(Mediator m) {
        if (!(m instanceof TemplateMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        TemplateMediator mediator = (TemplateMediator) m;
        OMElement templateElem = fac.createOMElement("template", synNS);

        if (mediator.getName() != null) {
            templateElem.addAttribute(fac.createOMAttribute(
                    "name", nullNS, mediator.getName()));
            serializeParams(templateElem,mediator);
            serializeBody(templateElem, mediator.getList());
            saveTracingState(templateElem, mediator);
        }

        return templateElem;
    }

    private void serializeParams(OMElement templateElem, TemplateMediator mediator) {
        Collection<String> params = mediator.getParameters();
        for (String param : params) {
            if (param != null && !"".equals(param)) {
                OMElement paramEl = fac.createOMElement("parameter", synNS);
                paramEl.addAttribute(fac.createOMAttribute("name", nullNS, param));
                templateElem.addChild(paramEl);
            }
        }
    }

    private void serializeBody(OMElement templateElem, List<Mediator> childMediatorList){
        OMElement seqEl = fac.createOMElement("sequence", synNS);
        templateElem.addChild(seqEl);
        serializeChildren(seqEl, childMediatorList);
    }

    public String getMediatorClassName() {
        return TemplateMediator.class.getName();
    }
}
