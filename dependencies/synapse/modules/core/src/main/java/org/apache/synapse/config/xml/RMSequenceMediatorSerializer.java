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
import org.apache.synapse.mediators.builtin.RMSequenceMediator;

/**
 * Serializer for {@link RMSequenceMediator} instances.
 * 
 * @see RMSequenceMediatorFactory
 */
public class RMSequenceMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof RMSequenceMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        RMSequenceMediator mediator = (RMSequenceMediator) m;
        OMElement sequence = fac.createOMElement("RMSequence", synNS);
        saveTracingState(sequence, mediator);
        
        if(mediator.isSingle() && mediator.getCorrelation() != null) {
            handleException("Invalid RMSequence mediator. A RMSequence can't have both a " 
                    + "single attribute value of true and a correlation attribute specified.");
        }
        if(mediator.isSingle() && mediator.getLastMessage() != null) {
            handleException("Invalid RMSequence mediator. A RMSequence can't have both a " 
                    + "single attribute value of true and a last-message attribute specified.");
        }
        
        if (mediator.isSingle()) {
            sequence.addAttribute(fac.createOMAttribute("single", nullNS, String.valueOf(mediator.isSingle())));
        } else if (mediator.getCorrelation() != null) {
            SynapseXPathSerializer.serializeXPath(
                mediator.getCorrelation(), sequence, "correlation");
        } else {
            handleException("Invalid RMSequence mediator. Specify a single message sequence " 
                    + "or a correlation attribute.");
        }
        
        if (mediator.getLastMessage() != null) {
            SynapseXPathSerializer.serializeXPath(
                mediator.getLastMessage(), sequence, "last-message");
        }
        
        if (mediator.getVersion() != null) {
            sequence.addAttribute(fac.createOMAttribute("version", nullNS, mediator.getVersion()));
        }

        return sequence;
    }

    public String getMediatorClassName() {
        return RMSequenceMediator.class.getName();
    }
}
