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
import org.apache.synapse.mediators.filters.FilterMediator;

/**
 * <pre>
 * &lt;filter (source="xpath" regex="string") | xpath="xpath"&gt;
 *   mediator+
 * &lt;/filter&gt;
 * </pre>
 *
 * <p>or if the filter medaitor needs to support the else behavior as well (i.e. a set of mediators
 * to be executed when the filter evaluates to false).</p>
 *
 * <pre>
 * &lt;filter (source="xpath" regex="string") | xpath="xpath"&gt;
 *   &lt;then [sequence="string"]&gt;
 *      mediator+
 *   &lt;/then&gt;
 *   &lt;else [sequence="string"]&gt;
 *      mediator+
 *   &lt;/else&gt;
 * &lt;/filter&gt;
 * </pre>
 */
public class FilterMediatorSerializer extends AbstractListMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof FilterMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        FilterMediator mediator = (FilterMediator) m;
        OMElement filter = fac.createOMElement("filter", synNS);

        if (mediator.getSource() != null && mediator.getRegex() != null) {

            SynapsePathSerializer.serializePath(mediator.getSource(), filter, "source");

            filter.addAttribute(fac.createOMAttribute(
                "regex", nullNS, mediator.getRegex().pattern()));

        } else if (mediator.getXpath() != null) {

            SynapsePathSerializer.serializePath(mediator.getXpath(), filter, "xpath");

        } else {
            handleException("Invalid filter mediator. " +
                "Should have either a 'source' and a 'regex' OR an 'xpath' ");
        }

        saveTracingState(filter, mediator);

        if (mediator.isThenElementPresent()) {

            OMElement thenElem = fac.createOMElement("then", synNS);
            filter.addChild(thenElem);

            if (mediator.getThenKey() != null) {
                thenElem.addAttribute(
                    fac.createOMAttribute("sequence", nullNS, mediator.getThenKey()));
            } else {
                serializeChildren(thenElem, mediator.getList());
            }

            if (mediator.getElseMediator() != null || mediator.getElseKey() != null) {

                OMElement elseElem = fac.createOMElement("else", synNS);
                filter.addChild(elseElem);

                if (mediator.getElseKey() != null) {
                    elseElem.addAttribute(
                        fac.createOMAttribute("sequence", nullNS, mediator.getElseKey()));
                } else {
                    serializeChildren(elseElem, mediator.getElseMediator().getList());
                }
            }
            
        } else {
            serializeChildren(filter, mediator.getList());
        }

        return filter;
    }

    public String getMediatorClassName() {
        return FilterMediator.class.getName();
    }
}
