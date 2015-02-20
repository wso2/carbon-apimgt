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
import org.apache.synapse.mediators.builtin.EnqueueMediator;


public class EnqueueMediatorSerializer extends AbstractMediatorSerializer{

    public OMElement serializeSpecificMediator(Mediator m) {
        assert m instanceof EnqueueMediator :
                "Unsupported mediator passed in for serialization : " + m.getType();

        EnqueueMediator mediator = (EnqueueMediator) m;
        OMElement enqueue = fac.createOMElement("enqueue", synNS);
        saveTracingState(enqueue, mediator);

        if (mediator.getExecutorName() != null) {
            enqueue.addAttribute(fac.createOMAttribute(
                    "executor", nullNS, mediator.getExecutorName()));
        } else {
            handleException("Invalid enqueue mediator. queue is required");
        }

        if (mediator.getSequenceName() != null) {
            enqueue.addAttribute(fac.createOMAttribute(
                    "sequence", nullNS, mediator.getSequenceName()));
        }  else {
            handleException("Invalid enqueue mediator. sequence is required");
        }

        enqueue.addAttribute(fac.createOMAttribute(
                "priority", nullNS, mediator.getPriority() + ""));

        return enqueue;
    }

    public String getMediatorClassName() {
        return EnqueueMediator.class.getName();
    }
}
