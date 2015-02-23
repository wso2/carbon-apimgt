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
import org.apache.synapse.mediators.transform.HeaderMediator;

import javax.xml.namespace.QName;

/**
 * Serializer for {@link HeaderMediator} instances.
 * 
 * @see HeaderMediatorFactory
 */
public class HeaderMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof HeaderMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        HeaderMediator mediator = (HeaderMediator) m;
        OMElement header = fac.createOMElement("header", synNS);
        saveTracingState(header,mediator);

        QName qName = mediator.getQName();
        if (qName != null) {
            if (qName.getNamespaceURI() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS,
                    (qName.getPrefix() != null && !"".equals(qName.getPrefix())
                        ? qName.getPrefix() + ":" : "") + 
                    qName.getLocalPart()));
                header.declareNamespace(qName.getNamespaceURI(), qName.getPrefix());
            } else {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS, qName.getLocalPart()));
            }
        }
        
        if (mediator.getScope() != null) {
            // if we have already built a mediator with scope, scope should be valid, now save it
            header.addAttribute(fac.createOMAttribute("scope", nullNS, mediator.getScope()));
        }

        if (mediator.getAction() == HeaderMediator.ACTION_REMOVE) {
            header.addAttribute(fac.createOMAttribute(
                "action", nullNS, "remove"));
        } else {
            if (mediator.getValue() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "value", nullNS, mediator.getValue()));

            } else if (mediator.getExpression() != null) {

                SynapseXPathSerializer.serializeXPath(
                    mediator.getExpression(), header, "expression");

            } else if (!mediator.isImplicit()) {
                handleException("Value or expression required for a set header mediator");
            }
        }
        if (mediator.hasEmbeddedXml()) {
            for (OMElement e : mediator.getEmbeddedXml()) {
                header.addChild(e);
            }
        }
        return header;
    }

    public String getMediatorClassName() {
        return HeaderMediator.class.getName();
    }
}
