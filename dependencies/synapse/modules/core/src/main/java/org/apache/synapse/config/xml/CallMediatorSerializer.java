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
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.builtin.CallMediator;

/**
 * Serializer for {@link org.apache.synapse.mediators.builtin.CallMediator} instances.
 *
 * @see org.apache.synapse.config.xml.CallMediatorFactory
 */
public class CallMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof CallMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        CallMediator mediator = (CallMediator) m;
        OMElement call = fac.createOMElement("call", synNS);
        saveTracingState(call, mediator);

        Endpoint activeEndpoint = mediator.getEndpoint();
        if (activeEndpoint != null) {
            call.addChild(EndpointSerializer.getElementFromEndpoint(activeEndpoint));
        }

        return call;
    }

    public String getMediatorClassName() {
        return CallMediator.class.getName();
    }
}
