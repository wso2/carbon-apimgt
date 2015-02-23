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
import org.apache.synapse.mediators.eip.splitter.CloneMediator;
import org.apache.synapse.mediators.eip.Target;

import java.util.Iterator;

/**
 * This will serialize the CloneMediator to the xml configuration as specified bellow
 *
 * <pre>
 *  &lt;clone [continueParent=(true | false)]&gt;
 *   &lt;target [to="uri"] [soapAction="qname"] [sequence="sequence_ref"]
 *          [endpoint="endpoint_ref"]&gt;
 *     &lt;sequence&gt;
 *       (mediator)+
 *     &lt;/sequence&gt;?
 *     &lt;endpoint&gt;
 *       endpoint
 *     &lt;/endpoint&gt;?
 *   &lt;/target&gt;+
 * &lt;/clone&gt;
 * </pre>
 */
public class CloneMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * This method will implement the serializeMediator method of the MediatorSerializer interface
     * and implements the serialization of CloneMediator to its configuration
     *
     * @param m Mediator of the type CloneMediator which is subjected to the serialization
     * @return OMElement serialized in to xml from the given parameters
     */
    public OMElement serializeSpecificMediator(Mediator m) {

        OMElement cloneElem = fac.createOMElement("clone", synNS);
        saveTracingState(cloneElem, m);

        CloneMediator clone = (CloneMediator) m;
        if (clone.isContinueParent()) {
            cloneElem.addAttribute("continueParent", Boolean.toString(true), nullNS);
        }

        if (clone.getId() != null) {
            cloneElem.addAttribute("id", clone.getId(), nullNS);
        }

        if (clone.isSequential()) {
            cloneElem.addAttribute("sequential", "true", nullNS);
        }

        for (Object o : clone.getTargets()) {
            if (o instanceof Target) {
                cloneElem.addChild(TargetSerializer.serializeTarget((Target) o));
            }
        }
        return cloneElem;
    }

    /**
     * This method will implement the getMediatorClassName method of the
     * MediatorSerializer interface
     * 
     * @return full class name of the Mediator which is serialized by this Serializer
     */
    public String getMediatorClassName() {
        return CloneMediator.class.getName();
    }
}
