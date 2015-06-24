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
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.FaultMediator;

/**
 * Serializer for {@link FaultMediator} instances.
 * 
 * @see FaultMediatorFactory
 */
public class FaultMediatorSerializer extends AbstractMediatorSerializer {

    private static final String SOAP11 = "soap11";

    private static final String SOAP12 = "soap12";

    private static final String POX = "pox";

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof FaultMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        FaultMediator mediator = (FaultMediator) m;
        OMElement fault = fac.createOMElement("makefault", synNS);
        saveTracingState(fault,mediator);

        if(mediator.getSoapVersion()==FaultMediator.SOAP11) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, SOAP11));
        } else if(mediator.getSoapVersion()==FaultMediator.SOAP12) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, SOAP12));
        } else if(mediator.getSoapVersion()==FaultMediator.POX) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, POX));
        }

        if (mediator.isSerializeResponse()) {
            if (mediator.isMarkAsResponse()) {
                fault.addAttribute(fac.createOMAttribute("response", nullNS, "true"));
            } else {
                fault.addAttribute(fac.createOMAttribute("response", nullNS, "false"));
            }
        }

        OMElement code = mediator.getSoapVersion()!=FaultMediator.POX?fac.createOMElement("code", synNS, fault): null;
        if (mediator.getFaultCodeValue() != null && code != null) {
            OMNamespace ns = code.declareNamespace(mediator.getFaultCodeValue().getNamespaceURI(),
                    mediator.getFaultCodeValue().getPrefix());
            code.addAttribute(fac.createOMAttribute(
                    "value", nullNS, ns.getPrefix() + ":"
                    + mediator.getFaultCodeValue().getLocalPart()));

        } else if (mediator.getFaultCodeExpr() != null && code != null) {
            SynapseXPathSerializer.serializeXPath(mediator.getFaultCodeExpr(), code, "expression");

        } else if (mediator.getSoapVersion() != FaultMediator.POX) {
            handleException("Fault code is required for a fault " +
                    "mediator unless it is a pox fault");
        }

        OMElement reason = fac.createOMElement("reason", synNS, fault);
        if (mediator.getFaultReasonValue() != null) {
            reason.addAttribute(fac.createOMAttribute(
                "value", nullNS, mediator.getFaultReasonValue()));

        } else if (mediator.getFaultReasonExpr() != null) {

            SynapseXPathSerializer.serializeXPath(
                mediator.getFaultReasonExpr(), reason, "expression");

        } else if (mediator.getSoapVersion() != FaultMediator.POX) {
            handleException("Fault reason is required for a fault " +
                    "mediator unless it is a pox fault");
        }


        if (mediator.getFaultNode() != null) {
            OMElement node = fac.createOMElement("node", synNS, fault);
            node.setText(mediator.getFaultNode().toString());
        }

        if (mediator.getFaultRole() != null) {
            OMElement role = fac.createOMElement("role", synNS, fault);
            role.setText(mediator.getFaultRole().toString());
        }

        if (mediator.getFaultDetailExpr() != null) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            SynapseXPathSerializer.serializeXPath(
                    mediator.getFaultDetailExpr(), detail, "expression");            
        } else if (mediator.getFaultDetail() != null) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            detail.setText(mediator.getFaultDetail());
        } else if (!mediator.getFaultDetailElements().isEmpty()) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            for (OMElement element : mediator.getFaultDetailElements()) {
                if (element != null) {
                    detail.addChild(element.cloneOMElement());
                }
            }
        }

        return fault;
    }

    public String getMediatorClassName() {
        return FaultMediator.class.getName();
    }
}
