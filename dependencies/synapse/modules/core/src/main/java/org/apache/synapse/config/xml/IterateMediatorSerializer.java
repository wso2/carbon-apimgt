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
import org.apache.synapse.mediators.eip.splitter.IterateMediator;

/**
 * This class will be acting as the serializer for the IterateMediator which will convert the
 * IterateMediator instance to the following xml configuration
 *
 * <pre>
 * &lt;iterate [continueParent=(true | false)] [preservePayload=(true | false)]
 *          (attachPath="xpath")? expression="xpath"&gt;
 *   &lt;target [to="uri"] [soapAction="qname"] [sequence="sequence_ref"]
 *          [endpoint="endpoint_ref"]&gt;
 *     &lt;sequence&gt;
 *       (mediator)+
 *     &lt;/sequence&gt;?
 *     &lt;endpoint&gt;
 *       endpoint
 *     &lt;/endpoint&gt;?
 *   &lt;/target&gt;+
 * &lt;/iterate&gt;
 * </pre>
 */
public class IterateMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * This method will implement the serialization logic of the IterateMediator class to the
     * relevant xml configuration
     *
     * @param m
     *          IterateMediator to be serialized
     *
     * @return OMElement describing the serialized configuration of the IterateMediator
     */
    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof IterateMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        
        OMElement itrElem = fac.createOMElement("iterate", synNS);
        saveTracingState(itrElem, m);

        IterateMediator itrMed = (IterateMediator) m;
        if (itrMed.isContinueParent()) {
            itrElem.addAttribute("continueParent", Boolean.toString(true), nullNS);
        }

        if (itrMed.getId() != null) {
            itrElem.addAttribute("id", itrMed.getId(), nullNS);
        }

        if (itrMed.isPreservePayload()) {
            itrElem.addAttribute("preservePayload", Boolean.toString(true), nullNS);
        }

        if (itrMed.getAttachPath() != null && !".".equals(itrMed.getAttachPath().toString())) {
            SynapseXPathSerializer.serializeXPath(itrMed.getAttachPath(), itrElem, "attachPath");
        }
        
        if (itrMed.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(itrMed.getExpression(), itrElem, "expression");
        } else {
            handleException("Missing expression of the IterateMediator which is required.");
        }

        if (itrMed.getTarget() != null && !itrMed.getTarget().isAsynchronous()) {
            itrElem.addAttribute("sequential", "true", nullNS);
        }

        itrElem.addChild(TargetSerializer.serializeTarget(itrMed.getTarget()));

        return itrElem;
    }

    /**
     * This method implements the getMediatorClassName of the interface MediatorSerializer and
     * will be used in getting the mediator class name which will be serialized by this serializer
     *
     * @return String representing the full class name of the mediator
     */
    public String getMediatorClassName() {
        return IterateMediator.class.getName();
    }
}
